package com.hisporter.collection.database.exception;

/**
 * 参数异常
 *
 * @author zhangjp
 * @version 1.0
 */

public class ParameterException extends DataBaseException {

    public ParameterException(String errCode, String errMsg) {
        super(errCode, errMsg);
    }

}
