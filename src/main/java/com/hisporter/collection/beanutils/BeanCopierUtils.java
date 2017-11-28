package com.hisporter.collection.beanutils;


import net.sf.cglib.beans.BeanCopier;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象拷贝工具类
 * 用于对cglib的BeanCpoier进行优化
 * 对象的创建比较耗时，所以进行缓存
 *
 * @author zhangjp
 * @version 1.0
 */
public class BeanCopierUtils {
    public static ConcurrentHashMap<String,BeanCopier> beanCopierMap = new ConcurrentHashMap();

    public static void copyProperties(Object source, Object target){
        getCopier(source.getClass(), target.getClass()).copy(source, target, null);
    }

    public static BeanCopier getCopier(Class<?> class1,Class<?>class2){
        String beanKey =  generateKey(class1, class2);
        BeanCopier copier =  null;
        if(!beanCopierMap.containsKey(beanKey)){
            copier = BeanCopier.create(class1, class2, false);
            beanCopierMap.put(beanKey, copier);
        }else{
            copier = beanCopierMap.get(beanKey);
        }
        return copier;
    }

    private static String generateKey(Class<?> class1,Class<?>class2){
        return class1.toString() + class2.toString();
    }
}