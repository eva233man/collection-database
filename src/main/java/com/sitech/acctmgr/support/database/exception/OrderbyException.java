package com.sitech.acctmgr.support.database.exception;

/**
 * 排序异常
 *
 * @author zhangjp
 * @version 1.0
 */

public class OrderbyException extends DataBaseException {

    public OrderbyException(String errCode, String errMsg) {
        super(errCode, errMsg);
    }

}
