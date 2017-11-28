package com.hisporter.collection.database.exception;

/**
 * 参数异常
 *
 * @author zhangjp
 * @version 1.0
 */

public class FilterException extends DataBaseException {

    public FilterException(String errCode, String errMsg) {
        super(errCode, errMsg);
    }

}
