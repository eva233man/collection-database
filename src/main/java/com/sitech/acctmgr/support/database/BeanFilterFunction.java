package com.sitech.acctmgr.support.database;

import com.sitech.acctmgr.support.beanutils.JavaBeanUtils;
import com.sitech.acctmgr.support.database.exception.FilterException;
import com.sitech.acctmgr.support.database.lang.FilterField;
import com.sitech.acctmgr.support.database.lang.FilterType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * bean过滤器函数库
 *
 * @author zhangjp
 * @version 1.0
 */

class BeanFilterFunction {

    /**
     * 根据传入的bean以及过滤规则过滤
     * 如果匹配，则返回true，否则返回false
     *
     * @param bean         待过滤的bean
     * @param filterFields 过滤规则
     *
     * @return true or false
     */
    static <E> boolean filter(final E bean, final List<FilterField> filterFields) {
        for (final FilterField filterField : filterFields) {
            if (!match(getProperty(bean, filterField), filterField)) {
                return false;
            }
        }
        return true;
    }

    private static <E> Object getProperty(E bean, FilterField filterField) {
        if (!bean.getClass().isInstance(Map.class)) {
            try {
                Method getMethod = bean.getClass().getMethod(JavaBeanUtils.parGetName(filterField.getFieldName()));
                getMethod.setAccessible(true);
                return getMethod.invoke(bean, new Object[]{});
            } catch (Exception e) {
                e.printStackTrace();
                throw new FilterException("990000", "BeanFilter 取字段元素失败");
            }
        } else {
            return ((Map) bean).get(filterField.getFieldName());
        }
    }


    /**
     * 校验field字段是否匹配当前的记录
     *
     * @param field       要校验的字段
     * @param filterField 校验规则
     *
     * @return true:校验通过，false:校验不通过
     */
    private static boolean match(final Object field, final FilterField filterField) {
        if (filterField.getFilterType() == FilterType.LIKE) {
            return Pattern.matches(filterField.getFilterValue().toString(), field.toString());
        } else {
            int compareTo = JavaBeanUtils.compare(field, filterField.getFilterValue(), field.getClass());
            if (compareTo == 0 && filterField.getFilterType() == FilterType.EQ) {
                return true;
            } else if (compareTo > 0 && filterField.getFilterType() == FilterType.GT) {
                return true;
            } else if (compareTo >= 0 && filterField.getFilterType() == FilterType.GE) {
                return true;
            } else if (compareTo < 0 && filterField.getFilterType() == FilterType.LT) {
                return true;
            } else if (compareTo <= 0 && filterField.getFilterType() == FilterType.LE) {
                return true;
            } else {
                return false;
            }
        }
    }
}
