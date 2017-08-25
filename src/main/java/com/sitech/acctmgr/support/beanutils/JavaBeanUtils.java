package com.sitech.acctmgr.support.beanutils;

import com.sitech.common.utils.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * javabean的工具包
 *
 * @author zhangjp
 * @version 1.0
 */

public class JavaBeanUtils {

    /**
     * java转成HashMap
     *
     * @param bean
     *
     * @return
     */
    public static Map<String, Object> beanToMap(Object bean) {
        Map<String, Object> result = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor des : descriptors) {
                String fieldName = des.getName();

                Method getter = des.getReadMethod();
                Object fieldValue = getter.invoke(bean, new Object[]{});

                if (!fieldName.equalsIgnoreCase("class")) {

                    Pattern p = Pattern.compile("[A-Z]");
                    Matcher m = p.matcher(fieldName);

                    while (m.find()) {
                        fieldName = fieldName.replace(m.group(), "_" + m.group());

                    }
                    // System.err.println(fieldName + ">>>>>>>>>>" + fieldValue);
                    if (StringUtils.isNotEmptyOrNull(fieldValue)) {
                        result.put(fieldName.toUpperCase(), fieldValue);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 拼接某属性的 get方法
     *
     * @param fieldName
     *
     * @return String
     */
    public static String parGetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        int startIndex = 0;
        if (fieldName.charAt(0) == '_')
            startIndex = 1;
        return "get"
                + fieldName.substring(startIndex, startIndex + 1).toUpperCase()
                + fieldName.substring(startIndex + 1);
    }

    /**
     * 拼接在某属性的 set方法
     *
     * @param fieldName
     *
     * @return String
     */
    public static String parSetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        int startIndex = 0;
        if (fieldName.charAt(0) == '_')
            startIndex = 1;
        return "set"
                + fieldName.substring(startIndex, startIndex + 1).toUpperCase()
                + fieldName.substring(startIndex + 1);
    }

    /**
     * 根据类型进行类型转换
     *
     * @param value
     * @param clazz
     *
     * @return
     */
    public static Object convert(Object value, Class clazz) {
        if (clazz == Long.class || clazz == long.class) {
            if(value.getClass() == BigDecimal.class){
                return ((BigDecimal)value).longValue();
            }
            return Long.parseLong(String.valueOf(value));
        } else if (clazz == Double.class || clazz == double.class) {
            if(value.getClass() == BigDecimal.class){
                return ((BigDecimal)value).doubleValue();
            }
            return Double.parseDouble(String.valueOf(value));
        } else if (clazz == Float.class || clazz == float.class) {
            if(value.getClass() == BigDecimal.class){
                return ((BigDecimal)value).floatValue();
            }
            return Float.parseFloat(String.valueOf(value));
        } else if (clazz == Integer.class || clazz == int.class) {
            if(value.getClass() == BigDecimal.class){
                return ((BigDecimal)value).intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return Boolean.parseBoolean(String.valueOf(value));
        } else if (clazz == Byte.class || clazz == byte.class) {
            return Byte.parseByte(String.valueOf(value));
        } else if (clazz == String.class) {
            return String.valueOf(value);
        }
        return value;
    }

    /**
     * 根据类型进行比较两个值
     * 只对Date进行特殊处理，其他的格式按String处理
     * 后续特殊类型再补充
     *
     * @param value1
     * @param value2
     * @param clazz
     *
     * @return
     */
    public static int compare(Object value1, Object value2, Class clazz) {
        if (clazz == Date.class) {
            return ((Date) value1).compareTo((Date) value2);
        } else if (clazz == String.class) {
            return String.valueOf(value1).compareTo(String.valueOf(value2));
        } else {
            return getBigDecimal(value1).compareTo(getBigDecimal(value2));
        }
    }

    /**
     * 将Object转换成BigDecimal
     *
     * @param value
     *
     * @return
     */
    public static BigDecimal getBigDecimal(Object value) {
        BigDecimal ret = null;
        if (value != null) {
            if (value instanceof BigDecimal) {
                ret = (BigDecimal) value;
            } else if (value instanceof String) {
                ret = new BigDecimal((String) value);
            } else if (value instanceof BigInteger) {
                ret = new BigDecimal((BigInteger) value);
            } else if (value instanceof Number) {
                ret = new BigDecimal(((Number) value).doubleValue());
            } else {
                throw new ClassCastException("Not possible to coerce [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
            }
        }
        return ret;
    }


    /**
     * 通过反射获取bean的属性值
     *
     * @param fieldName bean中的属性名称
     * @param object    bean
     * @param <E>       bean的类型
     *
     * @return
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static <E> String getProperty(String fieldName, E object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getMethod = object.getClass().getMethod(parGetName(fieldName));
        getMethod.setAccessible(true);
        return String.valueOf(getMethod.invoke(object, new Object[]{}));
    }

}
