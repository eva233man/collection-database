package com.sitech.acctmgr.support.database.lang;

import java.util.LinkedList;
import java.util.List;

/**
 * 关联字段
 *
 * @author zhangjp
 * @version 1.0
 */

public class JoinField {
    //关联左侧的字段名称
    private final List<String> leftFieldNames = new LinkedList<>();
    //关联右侧的字段名称
    private final List<String> rightFieldNames = new LinkedList<>();

    public List<String> getLeftFieldNames() {
        return leftFieldNames;
    }

    public List<String> getRightFieldNames() {
        return rightFieldNames;
    }

    public void addJoinField(String leftFieldName , String rightFieldName){
        this.leftFieldNames.add(leftFieldName);
        this.rightFieldNames.add(rightFieldName);
    }
}
