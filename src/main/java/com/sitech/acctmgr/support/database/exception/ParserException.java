package com.sitech.acctmgr.support.database.exception;

/**
 * 解析异常
 *
 * @author zhangjp
 * @version 1.0
 */

public class ParserException extends DataBaseException {

    public ParserException(String errCode, String errMsg) {
        super(errCode, errMsg);
    }

}
