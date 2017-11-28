package com.hisporter.collection.database.function;

/**
 * select、groupby 字段的调用接口
 * 用于反射调用
 *
 * @author zhangjp
 * @version 1.0
 */
@FunctionalInterface
public interface FunctionInvoker {

    String invoke(final String field, final Object... args);
    
}
