package com.sitech.acctmgr.support.database.lang;

/**
 * 字段
 *
 * @author zhangjp
 * @version 1.0
 */

public class Field {

    //原字段
    private String fieldName;
    //字段元素所在的集合
    private ElementLocation elementLocation;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public ElementLocation getElementLocation() {
        return elementLocation;
    }

    public void setElementLocation(ElementLocation elementLocation) {
        this.elementLocation = elementLocation;
    }
}
