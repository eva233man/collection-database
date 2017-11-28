package com.hisporter.collection.database.function;

import org.apache.commons.lang.StringUtils;

/**
 * Substr 字符串截取的调用实现
 *
 * @author zhangjp
 * @version 1.0
 */

public class SubstrFunctionInvoker implements FunctionInvoker {
    @Override
    public String invoke(String field, Object... args) {
        if(args.length == 1){
            return StringUtils.substring(field, Integer.parseInt(args[0].toString()) - 1);
        }
        if(args.length == 2){
            int start = Integer.valueOf(args[0].toString()) - 1;
            int add = Integer.valueOf(args[1].toString());
            return StringUtils.substring(field, start, start+add);
        }
        return null;
    }

    public static void main(String[] args){
        FunctionInvoker functionInvoker = new SubstrFunctionInvoker();
        System.out.println(functionInvoker.invoke("1234567", 2,2));
    }
}
