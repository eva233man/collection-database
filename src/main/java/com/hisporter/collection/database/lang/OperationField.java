package com.hisporter.collection.database.lang;

/**
 * 分组汇总字段及操作类
 *
 * @author zhangjp
 * @version 1.0
 */
public class OperationField {
    private String field; //字段名
    private OperType operation; //操作，必须是OperType中定义的操作

    public OperType getOperation() {
        return operation;
    }

    public OperationField setOperation(OperType operation) {
        this.operation = operation;
        return this;
    }

    public String getField() {
        return field;
    }

    public OperationField setField(String field) {
        this.field = field;
        return this;
    }
}
