package com.sitech.acctmgr.support.database;


import com.sitech.acctmgr.support.beanutils.BeanCopierUtils;
import com.sitech.acctmgr.support.beanutils.JavaBeanUtils;
import com.sitech.acctmgr.support.database.exception.GroupbyException;
import com.sitech.acctmgr.support.database.exception.ParameterException;
import com.sitech.acctmgr.support.database.lang.*;
import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.reflect.FastClass;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;


/**
 * 集合处理引擎，基于bean的集合的查询、过滤、分组实现
 *
 * @author zhangjp
 * @version 1.2 2017/4/24 增加关联查询功能，类及类名重构，改成CollectionProcessEngine
 * @version 1.3 2017/7/3 修改报错信息，增加异常明细信息s
 * @version 1.4 2017/7/28 增加groupby中count功能
 * @version 1.5 2017/8/1 增加一种新的单列表分组方法，返回值的list类型可以指定
 * @version 1.6 2017/8/31 删除普通单列表方法，其他方法都增加入参list的元素类型，因为可能存在子类的情况
 * @version 1.7 2017/9/4 修改关联表的时候如果右表为空的情况
 * @version 1.8 2017/11/22 合并了单表和两表关联的代码
 */
class CollectionProcessEngine {

    /**
     * 根据多字段合并，合并后仍然返回list
     * 排序只会根据设置的合并字段groupby，操作字段按设置的规则进行操作
     * 其余字段会以分组第一条的内容保留
     *
     * @param collection   待groupBy的list，元素必须是javaBean
     * @param template 规则模板
     * @param <E>      bean的类型
     * @param tClazz   返回的list类型
     *
     * @return 分组后的list
     */
    static <T, E> List<T> process(final List<E> collection, final Class<E> eClazz, final CollectionProcessTemplate template, final Class<T> tClazz) {
        return process(collection, eClazz, null, null, template, tClazz);
    }

    /**
     * 根据模板规则关联查询并过滤
     *
     * @param leftCollection  关联查询左集合
     * @param rightCollection 关联查询右集合
     * @param template        模板规则
     * @param <T>             返回集合的元素类型
     * @param <LE>            左集合的元素类型
     * @param <RE>            右集合的元素类型
     *
     * @return 关联查询过滤后的新的集合
     */
    static <T, LE, RE> List<T> process(final List<LE> leftCollection, final Class<LE> lClazz, final List<RE> rightCollection, final Class<RE> rClazz,
                                       final CollectionProcessTemplate template, final Class<T> tClazz) {
        if (leftCollection == null || leftCollection.size() == 0) {
            return null;
        }
        boolean mapFlag = false;
        if (leftCollection.getClass().isInstance(Map.class)) {
            if (rightCollection.getClass().isInstance(Map.class)) {
                mapFlag = true;
            } else {
                throw new ParameterException("990039", "如果集合传入的类型是map，那么必须都是map，包括返回值");
            }
        }

        boolean isFilter = template.isFilter();
        boolean isGroupby = template.isGroupby();
        boolean isJoin = template.isJoin();
        boolean isAlias = template.isAlias();
        List<String> groupbyFields = template.getGroupbyFields();
        List<OperationField> operationFields = template.getOperationFields();
        List<AliasField> aliasFields = template.getAliasFields();

        JoinType joinType = template.getJoinType();
        if (rightCollection == null || rightCollection.size() == 0) {
            if (joinType == JoinType.OUTER) {
                isJoin = false;
            } else {
                return null;
            }
        }

        //基于右集合构建索引
        HashMap<List<String>, Integer> joinRightFieldIndex = new HashMap<>();
        List<String> joinLeftFields = template.getJoinField().getLeftFieldNames();
        if (isJoin) {
            List<String> joinRightFields = template.getJoinField().getRightFieldNames();
            int index = 0;
            try {
                for (RE rline : rightCollection) {
                    final List<String> fieldValues = new LinkedList<>();
                    for (String rightJoinField : joinRightFields) {
                        fieldValues.add(getPropertyString(rline, rightJoinField));
                    }
                    joinRightFieldIndex.put(fieldValues, index);
                    index++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new GroupbyException("990031", "获取右集合元素的属性失败:" + e.getMessage());
            }
        }

        BeanCopier rCopier = null;
        BeanCopier lCopier = null;
        if (!mapFlag) {
            //创建bean拷贝器，从左集合元素拷贝到最终的返回集合元素中
            lCopier = BeanCopierUtils.getCopier(lClazz, tClazz);
            if (isJoin) {
                //创建bean拷贝器，从右集合元素拷贝到最终的返回集合元素中
                rCopier = BeanCopierUtils.getCopier(rClazz, tClazz);
            }
        }

        List<T> outList = new ArrayList<>(); //返回结果
        int outLastIndex = 0;//标记当前outList的数组末尾标识
        //用来groupby搜索的索引,value对应outList中的索引值
        HashMap<List<String>, int[]> indexMap = new HashMap<>();
        for (LE leftLine : leftCollection) {
            RE rightLine = null;
            if (isJoin) {
                final List<String> fieldValues = new LinkedList<>();
                try {
                    for (String leftJoinFieldName : joinLeftFields) {
                        fieldValues.add(getPropertyString(leftLine, leftJoinFieldName));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new GroupbyException("990032", "获取右集合元素的属性失败:" + e.getMessage());
                }
                //拿到匹配的
                Integer index = joinRightFieldIndex.get(fieldValues);
                if (index == null && joinType == JoinType.INNER) {
                    continue;//没关联上，内关联直接循环下一个
                } else if (index != null) {
                    rightLine = rightCollection.get(index);
                }
            }

            if (isFilter) {
                BeanFilter<Object, List<FilterField>> beanFilter = BeanFilterFunction::filter;
                if (!beanFilter.filter(leftLine, template.getLeftFilterFields())) {
                    continue;//如果返回false，校验不通过，则该记录剔除
                }
                if (isJoin) {
                    if (!beanFilter.filter(rightLine, template.getRightFilterFields())) {
                        continue;//如果返回false，校验不通过，则该记录剔除
                    }
                }
            }

            /**
             * 开始根据左集合元素和右集合元素，通过拷贝合并成T最终的集合类型
             * 然后再做groupby合并
             */
            try {
                T line;
                if (!mapFlag) {
                    line = (T) tClazz.newInstance();
                    if (isJoin && rightLine != null) {
                        rCopier.copy(rightLine, line, null);
                    }
                    lCopier.copy(leftLine, line, null);
                } else {
                    line = (T) leftCollection.getClass().newInstance();
                    if (isJoin && rightLine != null) {
                        ((Map) line).putAll((Map) rightLine);
                    }
                    ((Map) line).putAll((Map) leftLine);
                }

                if (isAlias) {
                    for (AliasField aliasField : aliasFields) {
                        if (aliasField.getElementLocation() == ElementLocation.LEFT) {
                            setAliasProperty(line, leftLine, aliasField);
                        } else if (isJoin && rightLine != null) {
                            setAliasProperty(line, rightLine, aliasField);
                        }
                    }
                }

                if (isGroupby) {
                    List<String> lineKey = new LinkedList<>();
                    for (String field : groupbyFields) {
                        lineKey.add(getPropertyString(line, field));
                    }
                    if (indexMap.containsKey(lineKey)) {
                        int[] index = indexMap.get(lineKey);
                        T oldline = outList.get(index[0]);
                        index[1]++;
                        for (OperationField operationField : operationFields) {
                            operBean(line, oldline, operationField, index);
                        }
                    } else {
                        int[] index = {outLastIndex, 1};
                        for (OperationField operationField : operationFields) {
                            if (operationField.getOperation() == OperType.COUNT) {
                                operBean(line, line, operationField, index);
                            }
                        }
                        indexMap.put(lineKey, index);
                        outList.add(line);
                        outLastIndex++;
                    }
                } else {
                    outList.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new GroupbyException("990033", "集合处理失败:" + e.getMessage());
            }
        }
        indexMap = null;//释放引用
        joinRightFieldIndex = null;
        joinLeftFields = null;

        return outList;
    }

    private static <E> String getPropertyString(E line, String Field) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (!line.getClass().isInstance(Map.class)) {
            return JavaBeanUtils.getProperty(Field, line);
        } else {
            return ((Map) line).get(Field).toString();
        }
    }


    /**
     * 重命名字段，将原bean中的字段拷贝到新的bean的另一个字段中
     *
     * @param line       要设置的新的bean
     * @param oline      原bean
     * @param aliasField 要重命名的规则说明
     * @param <T>        新的bean类型
     * @param <E>        原bean的类型
     *
     * @throws IntrospectionException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static <T, E> void setAliasProperty(T line, E oline, AliasField aliasField) throws IntrospectionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String aFieldName = aliasField.getAliasFiedldName();
        String originalValue = getPropertyString(oline, aliasField.getOriginalFieldName());

        if (aliasField.isInvoker()) {
            String value = originalValue;
            originalValue = aliasField.getInvoker().invoke(value, aliasField.getObjects());
        }
        if (!line.getClass().isInstance(Map.class)) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(aFieldName, line.getClass());
            Method set = propertyDescriptor.getWriteMethod();
            setProperty(set, line, originalValue, propertyDescriptor.getPropertyType());
        } else {
            ((Map) line).put(aFieldName, originalValue);
        }
    }

    /**
     * 判断需要合并的时候调用，合并新的bean中的值到旧的bean中
     * 根据反射调用get、set方法，需要做类型转换
     * 经测试原生的反射方式和Invoker的方式要比apache的BeanUtils快
     *
     * @param line           后来的bean
     * @param oldline        原先的bean
     * @param operationField 操作字段及操作符封装类
     *
     * @throws IntrospectionException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static <E> void operBean(final E line, final E oldline, final OperationField operationField, final int[] index)
            throws IntrospectionException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String field = operationField.getField();
        if (!line.getClass().isInstance(Map.class)) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field, line.getClass());
            /**
             * 使用原生
             */
            Method get = propertyDescriptor.getReadMethod();
            Method set = propertyDescriptor.getWriteMethod();
            Class propertyType = propertyDescriptor.getPropertyType();
            Object newValue;
            if (operationField.getOperation() == OperType.COUNT) {
                if (propertyType == int.class) {
                    newValue = index[1];
                } else {
                    throw new GroupbyException("990034", "COUNT元素的类型不是int类型，请检查！");
                }
            } else {
                newValue = oper(getProperty(get, oldline), getProperty(get, line), propertyType, operationField.getOperation());
            }
            setProperty(set, oldline, newValue, propertyType);

            /**
             * cglib fast反射
             */
//        Method get = propertyDescriptor.getReadMethod();
//        Method set = propertyDescriptor.getWriteMethod();
//        FastClass fastClass = FastClass.create(line.getClass());
//        Object newValue = oper(getPropertyString(fastClass, get, oldline), getPropertyString(fastClass, get, line), propertyDescriptor.getPropertyType(), operationField.getOperation());
//        setProperty(fastClass, set, oldline, newValue, propertyDescriptor.getPropertyType());


            /**
             * 使用Invoker
             * 和原生的差不太多，对Method做了缓存
             */
//        Method getMethod = propertyDescriptor.getReadMethod();
//        getMethod.setAccessible(true);
//        Method setMethod = propertyDescriptor.getWriteMethod();
//        setMethod.setAccessible(true);
//        Invokers.Invoker get = Invokers.newInvoker(getMethod);
//        Invokers.Invoker set = Invokers.newInvoker(setMethod);
//        Object newValue = oper(getPropertyString(get, oldline), getPropertyString(get, line), propertyDescriptor.getPropertyType(), operationField.getOperation());
//        setProperty(set, oldline, newValue, propertyDescriptor.getPropertyType());


            /**
             * 使用beanUtils包的方式来操作bean
             * 最慢，主要是里面校验、转换太多
             */
//        Object newValue = oper(BeanUtils.getPropertyString(oldline, field), BeanUtils.getPropertyString(line, field), propertyDescriptor.getPropertyType(), operationField.getOperation());
//        BeanUtils.setProperty(oldline, field, newValue);
        } else {
            Object newValue = oper(((Map) oldline).get(field), ((Map) line).get(field), ((Map) line).get(field).getClass(), operationField.getOperation());
            ((Map) oldline).put(field, newValue);
        }
    }

    private static <E> Object getProperty(Method method, E object) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(object, new Object[]{});
    }

    private static <E> Object getProperty(FastClass fastClass, Method method, E object) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return fastClass.getMethod(method).invoke(object, new Object[]{});
    }

    private static <E> void setProperty(Method method, E object, Object value, Class clazz) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        method.invoke(object, new Object[]{JavaBeanUtils.convert(value, clazz)});
    }

    private static <E> void setProperty(FastClass fastClass, Method method, E object, Object value, Class clazz) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        fastClass.getMethod(method).invoke(object, new Object[]{JavaBeanUtils.convert(value, clazz)});
    }

    /**
     * 根据操作符操作两个对象，返回操作后的对象
     *
     * @param o1   bean的一个元素
     * @param o2   bean的一个元素
     * @param oper 操作符
     *
     * @return 操作后的对象
     */
    private static Object oper(Object o1, Object o2, Class clazz, OperType oper) {
        if (o1 == null) {
            return o2;
        }
        if (o2 == null) {
            return o1;
        }

        if (oper == OperType.SUM) {
            BigDecimal b1 = JavaBeanUtils.getBigDecimal(o1);
            BigDecimal b2 = JavaBeanUtils.getBigDecimal(o2);
            return b1.add(b2);
        } else if (oper == OperType.MAX) {
            if (JavaBeanUtils.compare(o1, o2, clazz) >= 0) {
                return o1;
            } else {
                return o2;
            }
        } else if (oper == OperType.MIN) {
            if (JavaBeanUtils.compare(o1, o2, clazz) <= 0) {
                return o1;
            } else {
                return o2;
            }
        } else if (oper == OperType.STRCAT) {
            String str1 = String.valueOf(o1);
            String str2 = String.valueOf(o2);

            int index = str1.indexOf(str2);
            if (index > -1) {
                return str1;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(str1).append(",").append(str2);
                return sb.toString();
            }
        }

        return o1;
    }

}

