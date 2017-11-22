package com.sitech.acctmgr.support.database;

import java.util.concurrent.ConcurrentHashMap;

/**
 * CQL语句的预解析缓存
 * 可以考虑选用redis、Guava内存缓存器、本地ConcurrentHashMap等
 * 单例模式
 *
 * @author zhangjp
 * @version 1.0
 */

public class CQLCacher {
    private final ConcurrentHashMap<String,CollectionProcessTemplate> cqlMap = new ConcurrentHashMap<>(16);

    private static final CQLCacher cache = new CQLCacher();


    public static CQLCacher getInstance(){
        return cache;
    }

    public CollectionProcessTemplate get(String cql){
        return this.cqlMap.get(cql);
    }

    public void set(String cql, CollectionProcessTemplate template){
        this.cqlMap.put(cql, template);
    }

    public boolean isExist(String cql){
        return this.cqlMap.containsKey(cql);
    }

}
