package com.hisporter.collection.database.lang;

/**
 * 过滤字段定义
 *
 * @author zhangjp
 * @version 1.0
 */

public class FilterField {
    private String fieldName;
    private Object filterValue;
    private ElementLocation elementLocation = ElementLocation.LEFT;
    private FilterType filterType = FilterType.LIKE;

    public String getFieldName() {
        return fieldName;
    }

    public FilterField setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public Object getFilterValue() {
        return filterValue;
    }

    public FilterField setFilterValue(Object filterValue) {
        this.filterValue = filterValue;
        return this;
    }

    public ElementLocation getElementLocation() {
        return elementLocation;
    }

    public FilterField setElementLocation(ElementLocation elementLocation) {
        this.elementLocation = elementLocation;
        return this;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public FilterField setFilterType(FilterType filterType) {
        this.filterType = filterType;
        return this;
    }
}
