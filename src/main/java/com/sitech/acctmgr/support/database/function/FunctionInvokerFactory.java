package com.sitech.acctmgr.support.database.function;

import com.sitech.acctmgr.support.database.exception.ParameterException;
import org.apache.commons.lang.StringUtils;

/**
 * 方法调用工厂类
 *
 * @author zhangjp
 * @version 1.0
 * @version 1.1 写死的解析对应，改成根据类型转换
 */

public class FunctionInvokerFactory {

    public static FunctionInvoker getMethodInvoker(String methodName){
        String className = getMethodInvokerClassName(methodName);
        try {
            Class<FunctionInvoker> clazz = (Class<FunctionInvoker>) Class.forName(className);
            return clazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ParameterException("1001", "没有找到方法"+ methodName +"对应的类：" + className);
        }

    }

    /**
     * 根据传入的方法名转成首字母大写
     * 比如：subtr转成Substr
     */
    private static String getMethodInvokerClassName(String methodName){
        String lowerMethodName = StringUtils.lowerCase(methodName);
        char[] cs = lowerMethodName.toCharArray();
        cs[0]-=32;
        return "com.sitech.acctmgr.support.database.function." + String.valueOf(cs) + "FunctionInvoker";
    }

}
