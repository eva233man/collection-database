
内存集合处理器
===

>内存集合处理器提供基于List集合的仿数据库的查询处理能力，支持select、where、group by、order by以及关联查询。\<br>
>用于对内存集合数据的轻量级加工，提供统一的API，减轻对物理库的依赖。\<br>
>为后续去O后的类sql处理提供统一的API.\<br>
>支持传入类sql语句来执行集合的查询处理，这个查询语言叫做：collection query language，简称cql.\<br>


#设计思路

    选用mysql的语法作为cql的语法基础
    将cql解析成抽象语法树，也就是将sql进行解析、语法校验、子句拆解，形成按select、from、where、group by、order by子句
    然后将抽象语法树映射到易于集合处理器程序执行的集合处理模板树

#版本升级说明

    升级到jdk1.8以后，性能提升了20%

#使用说明

    cql默认是以mysql的语法为基础来校验的
   
    1.	表名必须是字母和数字的组合
        不能是关键字 select、from、group 、by、order、left、join、where
        select中的表名默认支持L（左表）、R（右表）的缩写，所以写关联查询语句的时候，左表名不要起名叫”R”,右表名不要叫“L”
    2.	字段名必须是字母、数字、-、_ 这几种的组合，不允许含有“.”，小圆点是表名和字段的连接符，比如：table1.pointCode
    3.	from 子句，只支持单表查询、两表关联查询
    	两表关联查询，只支持join、left join两种，都是以左表为基准来查询
    	两表的关联查询写法必须是 A join B 或者 A left join B
    4.	select 子句，不需要明确指定要查询的字段，如果A、B表中存在的字段，在结果T中也存在字段，则会查询出来
    	select字段允许重命名，重命名后的字段必须在结果T中的字段中存在
    	select字段允许函数，现只支持substr，支持插件式扩展，可以提需求
    	select字段不允许分组函数后重命名，分组函数操作后的字段名还是之前的字段，比如sum(value)，返回的结果存放在返回集合名为value的属性上
    5.	group by子句，分组操作的字段是基于关联后的数据集做的操作，so，分组操作的字段不允许带表名
    	比如：select SUM(L.value) from A group by A.code，需要改成 select SUM(value) from A group by code
    	分组字段也是基于关联后的合并数据集合来做的操作，也不允许出现表名，直接写返回集合所拥有的字段名即可
    	如果需要重命名字段，则需要在select 条件中优先重命名字段，再做累加，比如：select R.value as num, sum(num)
    	group by不允许出现函数，比如：substr，如果需要做处理，则只需要在select中指定即可
    6.	order by 的字段是基于关联后的数据集（也就是结果数据集）做的操作，so，排序操作的子弹不允许带表名
    7.	where 子句，配置关联字段的关系以及过滤条件
    	如果是两表关联的关联条件，必须指定字段的表名，比如：L.code=R.code
    	非关联条件，只支持正则表达式的匹配方式，sql语句：code like ‘(01|02)’ ，关键字用like指定
                需要注意：正则表达式是java的正则表达式 （山西可能自己改成计费账务的自己写的那版）
                比如：code like ‘^(\\s(01|02))$’，匹配代码：Pattern.compile(”^(\\s(01|02))$”);
                除where子句中的like条件中允许出现“\\”，其他子句不允许出现
    	支持code=‘01’ or code>='02' 这样的条件，支持指定表名，如L.code<='02'，操作符暂时只支持like、>、>=、<、<=  

#执行：

    支持javabean和Map两种
    如果是Map方式，关联查询，左表、右表以及返回的类型都必须是Map
    如果是javabean的方式，返回集合的类型的元素定义了多少，才会返回多少字段



#例子：

```Java

    @Test
    public void testSql(){
        long start = SystemClock.now();
            String cql = "select points addPoint, sum(curPoint), sum(addPoint) from gen left join source where l.pointCode=r.pointCode group by pointCode order by pointCode";
            List<PointMonthDetInfo> pointMonthDetInfos = CollectionProcessor.execute(generalDetInfos, sourceInfos, cql, PointMonthDetInfo.class);
            for (PointMonthDetInfo info : pointMonthDetInfos) {
                System.out.println(JSON.toJSONString(info));
            }
    
        long end = SystemClock.now();
        System.out.println("cost: " + (end - start));
    
    }
```
