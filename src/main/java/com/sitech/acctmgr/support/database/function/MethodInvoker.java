package com.sitech.acctmgr.support.database.function;

/**
 * select、groupby 字段的调用接口
 * 用于反射调用
 *
 * @author zhangjp
 * @version 1.0
 */
public interface MethodInvoker {

    String invoke(final String field, final Object... args);
}
