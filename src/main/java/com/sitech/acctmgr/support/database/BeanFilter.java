package com.sitech.acctmgr.support.database;

/**
 * bean过滤器
 *
 * @author zhangjp
 * @version 1.0
 */
@FunctionalInterface
interface BeanFilter<E, P> {

    /**
     * 根据传入的bean以及过滤规则过滤
     * 如果匹配，则返回true，否则返回false
     *
     * @param bean         待过滤的bean
     * @param filterPolicy 过滤规则
     *
     * @return true or false
     */
    boolean filter(final E bean, final P filterPolicy);


}
