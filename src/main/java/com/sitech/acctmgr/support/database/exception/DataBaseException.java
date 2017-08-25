package com.sitech.acctmgr.support.database.exception;

/**
 * 内存集合处理器异常基类
 *
 * @author zhangjp
 * @version 1.0
 */

public abstract class DataBaseException extends RuntimeException{

    protected String errCode = "999999";

    public DataBaseException(){

    }

    public DataBaseException(String errCode){
        this.errCode = errCode;
    }

    public DataBaseException(String errCode, String errMsg) {
        super("errCode:" + errCode + ",errMsg:" + errMsg);
        this.errCode = errCode;
    }



}
