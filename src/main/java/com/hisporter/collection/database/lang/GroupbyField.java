package com.hisporter.collection.database.lang;

import com.hisporter.collection.database.function.FunctionInvoker;

/**
 * group by 字段封装
 *
 * @author zhangjp
 * @version 1.0
 */

public class GroupbyField {

    //字段
    private String fieldName;
    //原字段操作类
    private FunctionInvoker invoker;
    //原字段操作需要的参数
    private Object[] objects;
    private boolean isInvoker = false;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public FunctionInvoker getInvoker() {
        return invoker;
    }

    public void setInvoker(FunctionInvoker invoker) {
        this.invoker = invoker;
        if(!isInvoker){
            isInvoker = true;
        }
    }

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    public boolean isInvoker() {
        return isInvoker;
    }
}
