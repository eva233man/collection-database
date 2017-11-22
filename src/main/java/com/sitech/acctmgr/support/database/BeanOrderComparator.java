package com.sitech.acctmgr.support.database;


import com.sitech.acctmgr.support.beanutils.JavaBeanUtils;
import com.sitech.acctmgr.support.database.exception.OrderbyException;
import com.sitech.acctmgr.support.database.lang.OrderType;
import com.sitech.acctmgr.support.database.lang.OrderbyField;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

/**
 * JavaBean排序比较器
 *
 * @author zhangjp
 * @version 1.0
 */
class BeanOrderComparator<E> implements Comparator<E> {
    //排序字段及升降序等信息
    private List<OrderbyField> orders;

    /**
     * 实现比较
     *
     * @param bean1
     * @param bean2
     * @return
     */
    @Override
    public int compare(E bean1, E bean2) {
        for (OrderbyField order : orders) {
            int comp = 0;
            try {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(order.getFeildName(), bean1.getClass());
                Method get = propertyDescriptor.getReadMethod();

                Object value1 = getProperty(get, bean1);
                Object value2 = getProperty(get, bean2);
                comp = JavaBeanUtils.compare(value1, value2, propertyDescriptor.getPropertyType());
            }
            catch (Exception e) {
                throw new OrderbyException("990010", "传入的Bean属性" + order.getFeildName() + "反射失败！");
            }
            OrderType orderType = order.getOrderType();
            if (comp > 0) {
                if(orderType == OrderType.ASC) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
            else if (comp < 0) {
                if(orderType == OrderType.DESC) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
        }
        return 0;
    }

    private Object getProperty(Method method, E object) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(object, new Object[]{});
    }

    public void setOrders(List<OrderbyField> orders) {
        this.orders = orders;
    }
}
