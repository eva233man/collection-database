package com.hisporter.collection.database;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSON;
import com.hisporter.collection.database.lang.*;
import com.hisporter.collection.database.exception.ParserException;
import com.hisporter.collection.database.function.FunctionInvoker;
import com.hisporter.collection.database.function.FunctionInvokerFactory;
import com.sitech.acctmgr.support.database.lang.*;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 集合查询语言解析器
 * CQL=collection-query-language
 *
 * @author zhangjp
 * @version 1.0.0
 * @version 1.0.1 增加count解析
 */

class CQLParser {
    private static final Pattern METHOD_PATTERN = Pattern.compile("^([a-zA-Z]+)\\(([0-9a-zA-Z\\._\\s]+)(\\,([0-9a-zA-Z\\._\\s]+))+\\)$");
    private static final Pattern OPER_PATTERN = Pattern.compile("^([a-zA-Z]+)\\(([0-9a-zA-Z_\\s]+)\\)$");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^([0-9a-zA-Z_\\.]+)$");
    private static final Pattern GROUPBY_FIELDS_PATTERN = Pattern.compile("^([0-9a-zA-Z_]+)(\\s*\\,\\s*([0-9a-zA-Z_])+)*$");
    private static final Pattern JOIN_FROM_PATTERN = Pattern.compile("^([0-9a-zA-Z])+(\\s+(LEFT|left)){0,1}\\s*(JOIN|join)\\s+([0-9a-zA-Z]+){0,1}(\\s)?$");
    private static final Pattern NOJOIN_FROM_PATTERN = Pattern.compile("^([0-9a-zA-Z]+){0,1}(\\s)?$");

    private static final String LEFT_TABLE_ID = "L";
    private static final String RIGHT_TABLE_ID = "R";

    static {
        //初始化最好优先把程序里的静态cpql加载到内存

    }

    /**
     * 解析cpql，返回规则模板
     *
     * @param cql 待解析的cql语句
     * @return 规则模板
     */
    public static CollectionProcessTemplate parser(String cql){
        if(CQLCacher.getInstance().isExist(cql)){
            return CQLCacher.getInstance().get(cql);
        }

        //解析cql
        CollectionProcessTemplate template = parser2Template(cql);
        CQLCacher.getInstance().set(cql, template);
        return template;
    }


    private static CollectionProcessTemplate parser2Template(String cql){
        CollectionProcessTemplate template = new CollectionProcessTemplate();

        SQLSelectQueryBlock sqlSelectQuery = null;
        MySqlStatementParser sqlStatementParser = null;
        SQLSelectStatement sqlStatement = null;
        //换种方式测试
        try {
            String sql = StringUtils.replace(cql, "\\", "\\\\");
            sqlStatementParser = new MySqlStatementParser(SQLUtils.format(sql, JdbcConstants.MYSQL));
            sqlStatement = (SQLSelectStatement) sqlStatementParser.parseSelect();
            SQLSelect sqlSelect = sqlStatement.getSelect();
            sqlSelectQuery = (SQLSelectQueryBlock) sqlSelect.getQuery();
        }
        catch (ParserException e){
            e.printStackTrace();
            throw new ParserException("990040", "sql语句不符合mysql规范！解析失败！");
        }
        StringBuffer out = new StringBuffer() ;
        //创建sql解析的标准化输出
        SQLASTOutputVisitor sqlastOutputVisitor = SQLUtils.createFormatOutputVisitor(out , null , JdbcUtils.MYSQL) ;


        //解析from
        out.delete(0, out.length());
        sqlSelectQuery.getFrom().accept(sqlastOutputVisitor);
        String fromStr = out.toString().replaceAll("\\s", " ");
        String leftTableName = null;
        String rightTableName = null;
        if(JOIN_FROM_PATTERN.matcher(fromStr).matches()) {//关联查询
            if (StringUtils.contains(fromStr, " LEFT ")) {
                template.setJoinType(JoinType.OUTER);
                fromStr = StringUtils.replace(fromStr, "LEFT", "");
                fromStr = StringUtils.replace(fromStr, "JOIN", "");
            }
            else {
                template.setJoinType(JoinType.INNER);
                fromStr = StringUtils.replace(fromStr, "JOIN", "");
            }
            String[] names = StringUtils.split(fromStr, " ");
            leftTableName = names[0];
            rightTableName = names[1];
        }
        else if(NOJOIN_FROM_PATTERN.matcher(fromStr).matches()){//非关联查询
            leftTableName = fromStr.trim();
        }
        else {
            throw new ParserException("990040", "解析from子句失败，不符合from子句规范！");
        }



        MySqlSchemaStatVisitor schemaStatVisitor = new MySqlSchemaStatVisitor();
        sqlStatement.accept(schemaStatVisitor);
        //解析关联关系
        parseRelationships(schemaStatVisitor, template, leftTableName, rightTableName);

        //解析order by
        parseOrderBy(schemaStatVisitor, template);

        //解析select项
        parseSelect(sqlSelectQuery.getSelectList(), template, leftTableName, rightTableName);

        //解析where
        parseWhere(schemaStatVisitor, template, rightTableName);


        //解析group by
        out.delete(0, out.length()) ;
        sqlSelectQuery.getGroupBy().accept(sqlastOutputVisitor) ;
        String groupByItem = StringUtils.replace(out.toString(), "GROUP BY", "").trim();
        if(!GROUPBY_FIELDS_PATTERN.matcher(groupByItem).matches()){
            throw new ParserException("990043", "解析group by子句失败，" + groupByItem + " 不符合group by定义！字段不允许有函数或者表名！");
        }
        String[] fieldItems = StringUtils.split(groupByItem.replaceAll( "\\s", ""), ",");
        for(String fieldItem : fieldItems){
            template.addGroupbyField(fieldItem);
        }


//        System.out.println("template:" + JSON.toJSONString(template));

        return template;
    }

    private static void parseWhere(MySqlSchemaStatVisitor schemaStatVisitor, CollectionProcessTemplate template, String rightTableName) {
        for(TableStat.Condition condition : schemaStatVisitor.getConditions()){
            String operator = condition.getOperator();
            List<Object> values = condition.getValues();
            if(operator.equals("=") && values.size() == 0){
                //关联关系
                continue;
            }
            else if(operator.equals("=")){
                String tableId = condition.getColumn().getTable();
                ElementLocation location = tableId.equals(rightTableName)? ElementLocation.RIGHT
                        : (tableId.equals(RIGHT_TABLE_ID)? ElementLocation.RIGHT : ElementLocation.LEFT);
                template.addFilterField(condition.getColumn().getName(), condition.getValues().get(0).toString(), location, FilterType.EQ);
            }
            else if(operator.equalsIgnoreCase("like")) {
                String tableId = condition.getColumn().getTable();
                ElementLocation location = tableId.equals(rightTableName)? ElementLocation.RIGHT
                        : (tableId.equals(RIGHT_TABLE_ID)? ElementLocation.RIGHT : ElementLocation.LEFT);
                template.addFilterField(condition.getColumn().getName(), condition.getValues().get(0).toString(), location, FilterType.LIKE);
            }
            else if(operator.equals(">")) {
                String tableId = condition.getColumn().getTable();
                ElementLocation location = tableId.equals(rightTableName)? ElementLocation.RIGHT
                        : (tableId.equals(RIGHT_TABLE_ID)? ElementLocation.RIGHT : ElementLocation.LEFT);
                template.addFilterField(condition.getColumn().getName(), condition.getValues().get(0), location, FilterType.GT);
            }
            else if(operator.equals(">=")) {
                String tableId = condition.getColumn().getTable();
                ElementLocation location = tableId.equals(rightTableName)? ElementLocation.RIGHT
                        : (tableId.equals(RIGHT_TABLE_ID)? ElementLocation.RIGHT : ElementLocation.LEFT);
                template.addFilterField(condition.getColumn().getName(), condition.getValues().get(0), location, FilterType.GE);
            }
            else if(operator.equals("<")) {
                String tableId = condition.getColumn().getTable();
                ElementLocation location = tableId.equals(rightTableName)? ElementLocation.RIGHT
                        : (tableId.equals(RIGHT_TABLE_ID)? ElementLocation.RIGHT : ElementLocation.LEFT);
                template.addFilterField(condition.getColumn().getName(), condition.getValues().get(0), location, FilterType.LT);
            }
            else if(operator.equals("<=")) {
                String tableId = condition.getColumn().getTable();
                ElementLocation location = tableId.equals(rightTableName)? ElementLocation.RIGHT
                        : (tableId.equals(RIGHT_TABLE_ID)? ElementLocation.RIGHT : ElementLocation.LEFT);
                template.addFilterField(condition.getColumn().getName(), condition.getValues().get(0), location, FilterType.LE);
            }
            else {
                throw new ParserException("990044", "解析where子句失败，暂时不支持关联关系和正则表达式之外的条件！");
            }
        }
    }

    /**
     * 解析两表关联关系
     *
     * @param schemaStatVisitor sql访问器
     * @param template 要设置的模板
     * @param leftTableName 左表名
     * @param rightTableName 右表名
     */
    private static void parseRelationships(MySqlSchemaStatVisitor schemaStatVisitor, CollectionProcessTemplate template,
                                           String leftTableName, String rightTableName) {
        for(TableStat.Relationship relationship : schemaStatVisitor.getRelationships()){
            Field leftField = getField(relationship.getLeft().getFullName(), leftTableName, rightTableName);
            Field rightField = getField(relationship.getRight().getFullName(), leftTableName, rightTableName);
            template.addJoinField(leftField.getFieldName(), rightField.getFieldName());
        }
    }

    /**
     * 解析orderby子句
     *
     * @param schemaStatVisitor sql访问器
     * @param template 要设置的模板
     */
    private static void parseOrderBy(MySqlSchemaStatVisitor schemaStatVisitor, CollectionProcessTemplate template) {
        int index =0;
        for(TableStat.Column orderbyColumn : schemaStatVisitor.getOrderByColumns()){
            Object orderType = orderbyColumn.getAttributes().get("orderBy.type");
            if(orderType == null) {
                template.addOrderbyField(orderbyColumn.getName(), index);
            }
            else {
                template.addOrderbyField(orderbyColumn.getName(), index, getOrderType(orderType.toString()));
            }
            index++;
        }
    }

    /**
     * 解析select子句
     *
     * @param selectItemList select子句
     * @param template 要设置的模板
     * @param leftTableName 左表表名
     * @param rightTableName 右表表名
     */
    private static void parseSelect(List<SQLSelectItem> selectItemList, CollectionProcessTemplate template,
                                    String leftTableName, String rightTableName){
        StringBuffer itemOut = new StringBuffer();
        SQLASTOutputVisitor sqlastOutputItemVisitor = SQLUtils.createFormatOutputVisitor(itemOut , null , JdbcUtils.MYSQL) ;

        for(SQLSelectItem sqlSelectItem : selectItemList){
            String aliasName = sqlSelectItem.getAlias();
            itemOut.delete(0, itemOut.length());
            sqlSelectItem.getExpr().accept(sqlastOutputItemVisitor);
            String id = itemOut.toString();

            if(OPER_PATTERN.matcher(id).matches()){//匹配Group操作符
                String[] method = StringUtils.split(id, "(", 2);
                Field field = getField(StringUtils.split(method[1], ")")[0], leftTableName, rightTableName);
                template.addOperationField(field.getFieldName(), getMethod(method[0]));
            }
            else {
                if (METHOD_PATTERN.matcher(id).matches()) {
                    String[] method = StringUtils.split(id, "(", 2);
                    FunctionInvoker invoker = FunctionInvokerFactory.getMethodInvoker(method[0]);
                    String[] parameters = StringUtils.split(StringUtils.replaceOnce(method[1], ")", ""), ", ");
                    Field field = getField(parameters[0], leftTableName, rightTableName);
                    template.addAliasField(field.getFieldName(), aliasName, field.getElementLocation(), invoker, Arrays.copyOfRange(parameters,1, parameters.length));
                }
                else {
                    Field field = getField(id, leftTableName, rightTableName);
                    template.addAliasField(field.getFieldName(), aliasName, field.getElementLocation());
                }
            }
        }


    }

    private static OrderType getOrderType(String orderType){
        if(orderType.equalsIgnoreCase("desc")){
            return OrderType.DESC;
        }
        else {
            return OrderType.ASC;
        }
    }

    private static OperType getMethod(String method) {
        if(method.equalsIgnoreCase("sum")){
            return OperType.SUM;
        }
        else if(method.equalsIgnoreCase("max")){
            return OperType.MAX;
        }
        else if(method.equalsIgnoreCase("min")){
            return OperType.MIN;
        }
        else if(method.equalsIgnoreCase("strcat")){
            return OperType.STRCAT;
        }
        else if(method.equalsIgnoreCase("count")){
            return OperType.COUNT;
        }
        return null;
    }

    private static Field getField(String item, String leftTableName, String rightTableName){
        Field field = new Field();
        if (!FIELD_PATTERN.matcher(item).matches()) {
            throw new ParserException("990041", "解析select子句失败，field:" + item + " 不符合字段定义！");
        }
        if(item.contains(".")){
            String[] split = StringUtils.split(item, ".");
            String tableId = split[0];
            field.setElementLocation(tableId.equals(rightTableName)? ElementLocation.RIGHT
                    : (tableId.equals(RIGHT_TABLE_ID)? ElementLocation.RIGHT : ElementLocation.LEFT));
            if(field.getElementLocation() == ElementLocation.LEFT && !tableId.equals(leftTableName) && !tableId.equals(LEFT_TABLE_ID)){
                throw new ParserException("990042", "解析select子句失败，field:" + tableId + " 不符合表名定义！");
            }
            field.setFieldName(split[1]);
        }
        else {
            field.setFieldName(item);
        }
        return field;
    }


    public static void main(String[] args) {
//        System.out.println(OPER_PATTERN.matcher("MAX(value)").matches());
//        System.out.println(METHOD_PATTERN.matcher("substr(value, 0, 1), id").matches());
//        System.out.println(ID_PATTERN.matcher("L.value").matches());
//        System.out.println(JOIN_FROM_PATTERN.matcher("L left join R").matches());
//        System.out.println(NOJOIN_FROM_PATTERN.matcher("Lefagre").matches());
//        System.out.println(GROUPBY_FIELDS_PATTERN.matcher("code, id , value").matches());

        System.out.println(JSON.toJSONString(CQLParser.parser2Template("select substr(id,3,2) , L1.code code, L1.name name, count(value) value " +
                "from L1 left join R " +
                "where L.code=R.code " +
                "and L.code like '\\s(01|02)' " +
                "group by code , id " +
                "order by code desc, id")));

    }
}
