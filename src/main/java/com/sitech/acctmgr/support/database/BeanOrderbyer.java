package com.sitech.acctmgr.support.database;

import com.sitech.acctmgr.support.database.lang.OrderbyField;

import java.util.Collections;
import java.util.List;

/**
 * 基于javabean的公共排序类
 *
 * @author zhangjp
 * @version 1.0
 */

public final class BeanOrderbyer {

    /**
     * 基于bean的排序方法
     * @param inList 要排序的集合，元素是bean
     * @param orderFields 排序字段的集合
     * @param <E> bean的类型泛型
     */
    public static <E> void orderby(List<E> inList, List<OrderbyField> orderFields){
        if(inList == null || inList.size() ==0){
            return;
        }
        if(orderFields == null || orderFields.size() ==0){
            return;
        }
        BeanOrderComparator<E> comparator = new BeanOrderComparator<E>();
        comparator.setOrders(orderFields);
        Collections.sort(inList, comparator);
    }


}
