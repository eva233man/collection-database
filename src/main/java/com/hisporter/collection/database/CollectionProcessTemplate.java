package com.hisporter.collection.database;

import com.hisporter.collection.database.lang.*;
import com.hisporter.collection.database.exception.ParameterException;
import com.hisporter.collection.database.function.FunctionInvoker;
import com.sitech.acctmgr.support.database.lang.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 集合处理模板
 * 描述集合的处理规则
 * 通过解析CPQL来获得
 *
 * @author zhangjp
 * @version 1.0
 */

public class CollectionProcessTemplate {
    //过滤字段及规则
    private final List<FilterField> leftFilterFields = new LinkedList<>();
    private final List<FilterField> rightFilterFields = new LinkedList<>();

    //关联字段
    private JoinField joinField = new JoinField();
    //关联方式 默认外关联
    private JoinType joinType = JoinType.OUTER;
    //处理重命名字段
    private final List<AliasField> aliasFields = new LinkedList<>();
    //处理字段及规则
    private final List<OperationField> operationFields = new LinkedList<>();
    //分组字段集合
    private final List<String> groupbyFields = new LinkedList<>();
    //排序字段集合
    private final List<OrderbyField> orderbyFields = new LinkedList<>();

    private boolean isJoin = false;
    private boolean isFilter = false;
    private boolean isGroupby = false;
    private boolean isOrderby = false;
    private boolean isAlias = false;

    public List<FilterField> getLeftFilterFields() {
        return leftFilterFields;
    }

    public List<FilterField> getRightFilterFields() {
        return rightFilterFields;
    }

    public List<OperationField> getOperationFields() {
        return operationFields;
    }

    public List<String> getGroupbyFields() {
        return groupbyFields;
    }

    public List<OrderbyField> getOrderbyFields() {
        return orderbyFields;
    }

    public JoinField getJoinField() {
        return joinField;
    }

    public List<AliasField> getAliasFields() {
        return aliasFields;
    }

    /**
     * 添加重命名字段，适合于关联查询
     *
     * @param originalFieldName 原字段名称
     * @param aliasFiedldName   重命名后的字段名，必须在返回的集合中存在
     * @param elementLocation   原字段所处的位置 枚举值 ElementLocation.LEFT表示左表；ElementLocation.RIGHT表示右表
     *
     * @return this
     */
    public CollectionProcessTemplate addAliasField(String originalFieldName, String aliasFiedldName, ElementLocation elementLocation) {
        AliasField aliasField = new AliasField();
        aliasField.setOriginalFieldName(originalFieldName);
        aliasField.setAliasFiedldName(aliasFiedldName);
        aliasField.setElementLocation(elementLocation);
        aliasFields.add(aliasField);
        if (!isAlias) {
            isAlias = true;
        }
        return this;
    }

    /**
     * 添加重命名字段，适合于单表查询
     * 默认设置的是左表
     *
     * @param originalFieldName 原字段名称
     * @param aliasFiedldName   重命名后的字段名，必须在返回的集合中存在
     *
     * @return this
     */
    public CollectionProcessTemplate addAliasField(String originalFieldName, String aliasFiedldName) {
        return addAliasField(originalFieldName, aliasFiedldName, ElementLocation.LEFT);
    }

    /**
     * 添加重命名字段，适合于单表查询
     * 默认设置的是左表，并且设置该字段的操作函数
     *
     * @param originalFieldName 原字段名称
     * @param aliasFiedldName   重命名后的字段名，必须在返回的集合中存在
     * @param invoker           操作调用器，必须实现 FunctionInvoker
     * @param args              调用器的传参
     *
     * @return this
     */
    public CollectionProcessTemplate addAliasField(String originalFieldName, String aliasFiedldName, FunctionInvoker invoker, Object... args) {
        AliasField aliasField = new AliasField();
        aliasField.setOriginalFieldName(originalFieldName);
        aliasField.setAliasFiedldName(aliasFiedldName);
        aliasField.setInvoker(invoker);
        aliasField.setObjects(args);
        aliasFields.add(aliasField);
        if (!isAlias) {
            isAlias = true;
        }
        return this;
    }

    /**
     * 添加重命名字段，适合于关联查询
     * 设置该字段的操作函数
     *
     * @param originalFieldName 原字段名称
     * @param aliasFiedldName   重命名后的字段名，必须在返回的集合中存在
     * @param elementLocation   原字段所处的位置 枚举值 ElementLocation.LEFT表示左表；ElementLocation.RIGHT表示右表
     * @param invoker           操作调用器，必须实现 FunctionInvoker
     * @param args              调用器的传参
     *
     * @return
     */
    public CollectionProcessTemplate addAliasField(String originalFieldName, String aliasFiedldName, ElementLocation elementLocation, FunctionInvoker invoker, Object... args) {
        AliasField aliasField = new AliasField();
        aliasField.setOriginalFieldName(originalFieldName);
        aliasField.setAliasFiedldName(aliasFiedldName);
        aliasField.setElementLocation(elementLocation);
        aliasField.setInvoker(invoker);
        aliasField.setObjects(args);
        aliasFields.add(aliasField);
        if (!isAlias) {
            isAlias = true;
        }
        return this;
    }

    /**
     * 添加关联字段
     *
     * @param leftFieldName  左表的字段名称
     * @param rightFieldName 右表的字段名称
     *
     * @return this
     */
    public CollectionProcessTemplate addJoinField(String leftFieldName, String rightFieldName) {
        if (leftFieldName == null || leftFieldName.equals("")) {
            throw new ParameterException("990020", "入参leftFieldName为空！");
        }
        if (rightFieldName == null || rightFieldName.equals("")) {
            throw new ParameterException("990021", "入参rightFieldName为空！");
        }
        this.joinField.addJoinField(leftFieldName, rightFieldName);
        if (this.joinField != null) {
            isJoin = true;
        }
        return this;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    /**
     * 设置关联类型
     * JoinType.INNER 表示内关联
     * JoinType.OUTER 表示外关联
     * 与物理库的关联不同，集合处理器的内关联是以左表为基准，如果右表中存在的则保留，右表不存在的过滤掉
     * 外关联是以左表为基准，如果右表中不存在，则左表记录保留，右表的记录元素为空
     * 注意：关联查询必须指定
     *
     * @param joinType 关联类型
     *
     * @return this 用于流式编程
     */
    public CollectionProcessTemplate setJoinType(JoinType joinType) {
        this.joinType = joinType;
        return this;
    }


    /**
     * 增加过滤字段，适用于关联查询指定
     *
     * @param fieldName       过滤字段元素名称
     * @param filterValue     过滤匹配的值
     * @param elementLocation 过滤字段所处的位置 枚举值 ElementLocation.LEFT表示左表；ElementLocation.RIGHT表示右表
     * @param filterType      过滤操作的类型，包括 like = > >= < <=
     *
     * @return this
     */
    public CollectionProcessTemplate addFilterField(String fieldName, Object filterValue, ElementLocation elementLocation, FilterType filterType) {
        if (fieldName == null || fieldName.equals("")) {
            throw new ParameterException("990022", "入参fieldName为空！");
        }
        if (filterValue == null || filterValue.equals("")) {
            throw new ParameterException("990023", "入参filter为空！");
        }
        if (elementLocation == ElementLocation.LEFT) {
            this.leftFilterFields.add(new FilterField().setFieldName(fieldName)
                    .setFilterValue(filterValue).setElementLocation(elementLocation).setFilterType(filterType));
        } else if (elementLocation == ElementLocation.RIGHT) {
            this.rightFilterFields.add(new FilterField().setFieldName(fieldName)
                    .setFilterValue(filterValue).setElementLocation(elementLocation).setFilterType(filterType));
        }
        if (!isFilter) {
            isFilter = true;
        }
        return this;
    }

    /**
     * 增加过滤字段，适用于关联查询指定
     *
     * @param fieldName   过滤字段元素名称
     * @param filterValue 过滤匹配的值
     * @param filterType  过滤操作的类型，包括 like = > >= < <=
     *
     * @return this
     */
    public CollectionProcessTemplate addFilterField(String fieldName, Object filterValue, FilterType filterType) {
        return addFilterField(fieldName, filterValue, ElementLocation.LEFT, filterType);
    }

    /**
     * 添加操作字段及操作
     * 操作暂时只支持SUM、MAX、MIN
     *
     * @param fieldName 操作的字段，注意：指定的是返回集合中的字段名称
     * @param operType  操作方法 枚举值 OperType.SUM OperType.MAX OperType.MIN
     *
     * @return this
     */
    public CollectionProcessTemplate addOperationField(String fieldName, OperType operType) {
        if (fieldName == null || fieldName.equals("")) {
            throw new ParameterException("990024", "入参fieldName为空！");
        }
        if (operType == null) {
            throw new ParameterException("990029", "入参operType为空！");
        }
        this.operationFields.add(new OperationField().setField(fieldName).setOperation(operType));
        return this;
    }

    /**
     * 添加分组字段
     *
     * @param fieldName 分组字段名称，注意：指定的是返回集合中的字段名称
     *
     * @return this
     */
    public CollectionProcessTemplate addGroupbyField(String fieldName) {
        if (fieldName == null || fieldName.equals("")) {
            throw new ParameterException("990025", "入参fieldName为空！");
        }
        this.groupbyFields.add(fieldName);
        if (!isGroupby) {
            isGroupby = true;
        }
        return this;
    }

    /**
     * 添加排序字段
     *
     * @param fieldName 要排序的字段，注意：指定的是返回集合中的字段名称
     * @param orderType 排序方式 枚举值 OrderType.ASC OrderType.DESC
     *
     * @return
     */
    public CollectionProcessTemplate addOrderbyField(String fieldName, int index, OrderType orderType) {
        if (fieldName == null || fieldName.equals("")) {
            throw new ParameterException("990027", "入参fieldName为空！");
        }
        this.orderbyFields.add(index, new OrderbyField().setFeildName(fieldName).setOrderType(orderType));
        if (!isOrderby) {
            isOrderby = true;
        }
        return this;
    }

    /**
     * 添加排序字段，默认从小到大 ASC
     *
     * @param fieldName 要排序的字段，注意：指定的是返回集合中的字段名称
     *
     * @return
     */
    public CollectionProcessTemplate addOrderbyField(String fieldName, int index) {
        if (fieldName == null || fieldName.equals("")) {
            throw new ParameterException("990028", "入参fieldName为空！");
        }
        this.orderbyFields.add(index, new OrderbyField().setFeildName(fieldName));
        if (!isOrderby) {
            isOrderby = true;
        }
        return this;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public boolean isFilter() {
        return isFilter;
    }

    public boolean isGroupby() {
        return isGroupby;
    }

    public boolean isOrderby() {
        return isOrderby;
    }

    public boolean isAlias() {
        return isAlias;
    }
}
