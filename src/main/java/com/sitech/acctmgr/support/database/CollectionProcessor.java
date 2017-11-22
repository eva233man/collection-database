package com.sitech.acctmgr.support.database;

import com.sitech.acctmgr.support.database.lang.OrderbyField;

import java.util.Collections;
import java.util.List;

/**
 * 内存集合处理器
 * 用于模仿数据库的操作，提供select、where、groupby、orderby以及关联查询
 * 关联查询支持外关联、内关联
 * select 支持指定最后集合元素的处理：SUM、MAX、MIN
 * select 支持对元素的重命名
 * 支持sql解析器
 *
 * @author zhangjp
 * @version 1.0.0
 * @version 1.1.0 修改入参，增加入参list的元素类型
 */

public class CollectionProcessor {

    /**
     * 处理方法
     * 调用该方法，将对集合按cql进行处理后，返回相同类型的集合
     * 入参一个集合，不做关联，只做groupby、orderby、filter
     *
     * @param collections 要处理的集合
     * @param <E> 集合中的元素类型
     * @param cql 要处理的规则
     * @return 返回处理后的集合
     */
    public static <E> List<E> execute(List<E> collections, final Class<E> eClass, String cql) {
        if (collections == null || collections.size() == 0) {
            return null;
        }
        CollectionProcessTemplate template = CQLParser.parser(cql);

        return execute(collections, eClass, template);
    }

    /**
     * 处理方法
     * 调用该方法，将对集合按设置的模板规则进行处理后，返回相同类型的集合
     * 入参一个集合，不做关联，只做groupby、orderby、filter
     *
     * @param collections 要处理的集合
     * @param <E> 集合中的元素类型
     * @param template 要处理的规则，需要先new个CollectionProcessTemplate，并进行设置属性
     * @return 返回处理后的集合
     */
    public static <E> List<E> execute(List<E> collections, final Class<E> eClass, CollectionProcessTemplate template) {
        if (collections == null || collections.size() == 0) {
            return null;
        }

        List<E> outlist = CollectionProcessEngine.process(collections, eClass, template, eClass);
        boolean isOrderby = template.isOrderby();
        //orderby处理
        if(isOrderby) {
            orderby(outlist, template.getOrderbyFields());
        }
        template = null;
        return outlist;
    }

    /**
     * 处理方法
     * 调用该方法，将对集合按cql进行处理后，返回指定类型的集合
     * 入参一个集合，不做关联，只做groupby、orderby、filter
     *
     * @param collections 要处理的集合
     * @param <E> 集合中的元素类型
     * @param cql 要处理的规则
     * @param <T> 返回的集合元素类型
     * @return 返回处理后的集合
     */
    public static <T,E> List<T> execute(List<E> collections, final Class<E> eClass, String cql, Class<T> tClass) {
        if (collections == null || collections.size() == 0) {
            return null;
        }
        CollectionProcessTemplate template = CQLParser.parser(cql);

        return execute(collections, eClass, template, tClass);
    }

    /**
     * 处理方法
     * 调用该方法，将对集合按设置的模板规则进行处理后，返回指定类型的集合
     * 入参一个集合，不做关联，只做groupby、orderby、filter
     *
     * @param collections 要处理的集合
     * @param <E> 入口集合中的元素类型
     * @param template 要处理的规则，需要先new个CollectionProcessTemplate，并进行设置属性
     * @param <T> 返回的集合元素类型
     * @return 返回处理后的集合，类型为入参指定的集合类型
     */
    public static <T,E> List<T> execute(List<E> collections, final Class<E> eClass, CollectionProcessTemplate template, Class<T> tClazz) {
        if (collections == null || collections.size() == 0) {
            return null;
        }

        List<T> outlist = CollectionProcessEngine.process(collections, eClass, template, tClazz);
        boolean isOrderby = template.isOrderby();
        //orderby处理
        if(isOrderby) {
            orderby(outlist, template.getOrderbyFields());
        }
        template = null;
        return outlist;
    }

    /**
     * 处理方法
     * 调用该方法，将对集合按cql进行处理后，返回两种类型合并的集合
     * 入参两个集合，支持关联
     *
     * @param leftCollection 关联集合的左集合
     * @param <LE> 入参左集合的元素类型
     * @param rightCollection 关联集合的右集合
     * @param <RE> 入参右集合的元素类型
     * @param cql 要处理的规则
     * @param <T> 返回的集合元素类型
     * @return 返回处理的集合，集合的元素包括左集合、右集合的元素
     */
    public static <T,LE,RE> List<T> execute(List<LE> leftCollection,  final Class<LE> lClass,  List<RE> rightCollection, final Class<RE> rClass,
                                            String cql, Class<T> tClazz){

        if(leftCollection == null || leftCollection.size()==0){
            return null;
        }

        //解析cql，生成模板
        CollectionProcessTemplate template = CQLParser.parser(cql);

        return execute(leftCollection, lClass, rightCollection, rClass, template, tClazz);
    }

    /**
     * 处理方法
     * 调用该方法，将对集合按设置的规则模板进行关联查询处理后，返回设置的T类型的集合
     * T类型中包括了左集合、左集合的字段
     * 入参两个集合分别代表左集合、右集合，对应SQL关联查询中的左表、右表
     *
     * @param leftCollection 关联集合的左集合
     * @param <LE> 入参左集合的元素类型
     * @param rightCollection 关联集合的右集合
     * @param <RE> 入参右集合的元素类型
     * @param template 要处理的规则，需要先new个CollectionProcessTemplate，并进行设置属性
     * @param <T> 返回的集合元素类型
     * @return 返回处理的集合，集合的元素包括左集合、右集合的元素
     */
    public static <T,LE,RE> List<T> execute(List<LE> leftCollection, final Class<LE> lClass,  List<RE> rightCollection, final Class<RE> rClass,
                                            CollectionProcessTemplate template, Class<T> tClazz){
        if(leftCollection == null || leftCollection.size()==0){
            return null;
        }

        List<T> outlist = CollectionProcessEngine.process(leftCollection, lClass, rightCollection, rClass, template, tClazz);

        boolean isOrderby = template.isOrderby();
        //orderby处理
        if(isOrderby) {
            orderby(outlist, template.getOrderbyFields());
        }
        template = null;
        return outlist;
    }

    /**
     * 基于bean的排序方法
     * @param inList 要排序的集合，元素是bean
     * @param orderFields 排序字段的集合
     * @param <E> bean的类型泛型
     */
    private static <E> void orderby(List<E> inList, List<OrderbyField> orderFields){
        if(inList == null || inList.size() ==0){
            return;
        }
        if(orderFields == null || orderFields.size() ==0){
            return;
        }
        BeanOrderComparator<E> comparator = new BeanOrderComparator<E>();
        comparator.setOrders(orderFields);
        inList.sort(comparator);
    }

}
