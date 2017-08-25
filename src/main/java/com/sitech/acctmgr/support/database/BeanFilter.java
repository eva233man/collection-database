package com.sitech.acctmgr.support.database;

import com.sitech.acctmgr.support.beanutils.JavaBeanUtils;
import com.sitech.acctmgr.support.database.exception.FilterException;
import com.sitech.acctmgr.support.database.lang.ElementLocation;
import com.sitech.acctmgr.support.database.lang.FilterField;
import com.sitech.acctmgr.support.database.lang.FilterType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * bean过滤器
 *
 * @author zhangjp
 * @version 1.0
 */

public final class BeanFilter {

    /**
     * 根据传入的bean以及过滤规则过滤
     * 如果匹配，则返回true，否则返回false
     *
     * @param bean 待过滤的bean
     * @param filterFields 过滤规则
     * @param <E> bean元素类型
     * @return true or false
     */
    public static <E> boolean filter(E bean, final List<FilterField> filterFields){
        try {
            for (FilterField filterField : filterFields) {
                if(filterField.getElementLocation() == ElementLocation.RIGHT){
                    continue;
                }
                if(!match(getProperty(bean, filterField), filterField)){
                    return false;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new FilterException("990000","BeanFilter 取字段元素失败");
        }
        return true;
    }

    private static <E> Object getProperty(E bean, FilterField filterField) throws NoSuchMethodException, java.lang.reflect.InvocationTargetException, IllegalAccessException {
        if (!bean.getClass().isInstance(Map.class)) {
            Method getMethod = bean.getClass().getMethod(JavaBeanUtils.parGetName(filterField.getFieldName()));
            getMethod.setAccessible(true);
            return getMethod.invoke(bean, new Object[]{});
        }
        else {
            return ((Map)bean).get(filterField.getFieldName());
        }
    }

    /**
     * 校验field字段是否匹配当前的记录
     *
     * @param field 要校验的字段
     * @param filterField 校验规则
     * @return true:校验通过，false:校验不通过
     */
    private static boolean match(Object field, FilterField filterField){
        if(filterField.getFilterType() == FilterType.LIKE){
            if(Pattern.matches(filterField.getFilterValue().toString(), field.toString())){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            int compareTo = JavaBeanUtils.compare(field, filterField.getFilterValue(), field.getClass());
            if(compareTo ==0 && filterField.getFilterType() == FilterType.EQ){
                return true;
            }
            else if(compareTo >0 && filterField.getFilterType() == FilterType.GT){
                return true;
            }
            else if(compareTo >=0 && filterField.getFilterType() == FilterType.GE){
                return true;
            }
            else if(compareTo <0 && filterField.getFilterType() == FilterType.LT){
                return true;
            }
            else if(compareTo <=0 && filterField.getFilterType() == FilterType.LE){
                return true;
            }
            else {
                return false;
            }
        }
    }

    /**
     * 根据传入的bean以及过滤规则过滤
     * 如果匹配，则返回true，否则返回false
     *
     * @param lBean 待过滤的左集合元素
     * @param rBean 待过滤的右集合元素
     * @param filterFields 过滤规则
     * @param <LE> 左集合元素类型
     * @param <RE> 右集合元素类型
     * @return
     */
    public static <LE,RE> boolean filter(LE lBean, RE rBean, final List<FilterField> filterFields){
        try {
            for (FilterField filterField : filterFields) {
                if(filterField.getElementLocation() == ElementLocation.LEFT &&
                        !match(getProperty(lBean, filterField), filterField)){
                    return false;
                }
                else if(filterField.getElementLocation() == ElementLocation.RIGHT &&
                        !match(getProperty(rBean, filterField), filterField)) {
                    return false;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new FilterException("990000","BeanFilter 取字段元素失败");
        }
        return true;
    }
}
