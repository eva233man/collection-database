package com.sitech.acctmgr.support.database.lang;

import com.sitech.acctmgr.support.database.function.FunctionInvoker;

/**
 * 重命名字段
 *
 * @author zhangjp
 * @version 1.0
 */

public class AliasField {

    //原字段
    private String originalFieldName;
    //重命名字段
    private String aliasFiedldName;
    //字段元素所在的集合
    private ElementLocation elementLocation = ElementLocation.LEFT;
    //原字段操作类
    private FunctionInvoker invoker;
    //原字段操作需要的参数
    private Object[] objects;
    private boolean isInvoker = false;

    public String getOriginalFieldName() {
        return originalFieldName;
    }

    public void setOriginalFieldName(String originalFieldName) {
        this.originalFieldName = originalFieldName;
    }

    public String getAliasFiedldName() {
        return aliasFiedldName;
    }

    public void setAliasFiedldName(String aliasFiedldName) {
        this.aliasFiedldName = aliasFiedldName;
    }

    public ElementLocation getElementLocation() {
        return elementLocation;
    }

    public void setElementLocation(ElementLocation elementLocation) {
        this.elementLocation = elementLocation;
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
