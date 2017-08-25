package com.sitech.acctmgr.support.database.lang;

/**
 * 比较器使用的定义类
 *
 * @author zhangjp
 * @version 1.0
 */
public class OrderbyField {

    private String feildName;
    //升序还是降序
    private OrderType orderType = OrderType.ASC;

    public String getFeildName() {
        return feildName;
    }

    public OrderbyField setFeildName(String feildName) {
        this.feildName = feildName;
        return this;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderbyField setOrderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }
}
