package com.sitech.acctmgr.support.database.function;

/**
 * 方法调用工厂类
 *
 * @author zhangjp
 * @version 1.0
 */

public class MethodInvokerFactory {

    public static MethodInvoker getMethodInvoker(String methodName){
        if(methodName.equalsIgnoreCase("substr")){
            return new SubstrInvoker();
        }
        return null;
    }

}
