package test;

import com.alibaba.fastjson.JSON;
import com.sitech.acctmgr.support.SystemClock;
import com.sitech.acctmgr.support.database.CollectionProcessEngine;
import com.sitech.acctmgr.support.database.CollectionProcessTemplate;
import com.sitech.acctmgr.support.database.CollectionProcessor;
import com.sitech.acctmgr.support.database.function.SubstrInvoker;
import com.sitech.acctmgr.support.database.lang.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangjinpeng on 2017/4/24.
 */

public class TestCollectionProcessor {
    List<PointGeneralDetInfo> generalDetInfos = new ArrayList<>();
    List<PointSourceInfo> sourceInfos = new ArrayList<>();

    @Before
    public void init(){

        for (int i = 0; i < 10; i++) {
            PointGeneralDetInfo pointGeneralDetInfo = new PointGeneralDetInfo();
            pointGeneralDetInfo.setPointCode("01");
            pointGeneralDetInfo.setCurPoint(100);
            pointGeneralDetInfo.setPointCodeName("消费积分");
            pointGeneralDetInfo.setPointType("1234abcd");
            pointGeneralDetInfo.setBeginTime("20170815000000");
            generalDetInfos.add(pointGeneralDetInfo);
            PointGeneralDetInfo pointGeneralDetInfo1 = new PointGeneralDetInfo();
            pointGeneralDetInfo1.setPointCode("01");
            pointGeneralDetInfo1.setCurPoint(200);
            pointGeneralDetInfo1.setPointType("1234abcd");
            pointGeneralDetInfo1.setPointCodeName("消费积分");
            pointGeneralDetInfo.setBeginTime("20170815000002");
            generalDetInfos.add(pointGeneralDetInfo1);
            PointGeneralDetInfo pointGeneralDetInfo2 = new PointGeneralDetInfo();
            pointGeneralDetInfo2.setPointCode("01");
            pointGeneralDetInfo2.setCurPoint(300);
            pointGeneralDetInfo2.setPointType("1234efsd");
            pointGeneralDetInfo2.setPointCodeName("消费积分");
            pointGeneralDetInfo.setBeginTime("20170814000002");
            generalDetInfos.add(pointGeneralDetInfo2);
            PointGeneralDetInfo pointGeneralDetInfo3 = new PointGeneralDetInfo();
            pointGeneralDetInfo3.setPointCode("02");
            pointGeneralDetInfo3.setCurPoint(300);
            pointGeneralDetInfo3.setPointType("1234efsd");
            generalDetInfos.add(pointGeneralDetInfo3);
            PointGeneralDetInfo pointGeneralDetInfo4 = new PointGeneralDetInfo();
            pointGeneralDetInfo4.setPointCode("02");
            pointGeneralDetInfo4.setCurPoint(400);
            pointGeneralDetInfo4.setPointType("1234efsd");
            generalDetInfos.add(pointGeneralDetInfo4);

            PointGeneralDetInfo pointGeneralDetInfo5 = new PointGeneralDetInfo();
            pointGeneralDetInfo5.setPointCode("03");
            pointGeneralDetInfo5.setCurPoint(400);
            pointGeneralDetInfo5.setPointType("5678efsd");
            generalDetInfos.add(pointGeneralDetInfo5);
            PointGeneralDetInfo pointGeneralDetInfo6 = new PointGeneralDetInfo();
            pointGeneralDetInfo6.setPointCode("03");
            pointGeneralDetInfo6.setCurPoint(400);
            pointGeneralDetInfo6.setPointType("5678efsd");
            generalDetInfos.add(pointGeneralDetInfo6);
            PointGeneralDetInfo pointGeneralDetInfo7 = new PointGeneralDetInfo();
            pointGeneralDetInfo7.setPointCode("04");
            pointGeneralDetInfo7.setPointType("5678efsd");
            pointGeneralDetInfo7.setPointCodeName("促销积分");
            pointGeneralDetInfo7.setCurPoint(500);
            generalDetInfos.add(pointGeneralDetInfo7);
        }
        for(int i=0; i<10; i++) {
            PointSourceInfo pointSourceInfo = new PointSourceInfo();
            pointSourceInfo.setPointCode("01");
            pointSourceInfo.setPoints(100);
            pointSourceInfo.setPointCodeName("消费积分");
            sourceInfos.add(pointSourceInfo);
            PointSourceInfo pointSourceInfo1 = new PointSourceInfo();
            pointSourceInfo1.setPointCode("01");
            pointSourceInfo1.setPoints(200);
            pointSourceInfo1.setPointCodeName("消费积分");
            sourceInfos.add(pointSourceInfo1);
            PointSourceInfo pointSourceInfo2 = new PointSourceInfo();
            pointSourceInfo2.setPointCode("01");
            pointSourceInfo2.setPoints(300);
            sourceInfos.add(pointSourceInfo2);
            PointSourceInfo pointSourceInfo3 = new PointSourceInfo();
            pointSourceInfo3.setPointCode("02");
            pointSourceInfo3.setPoints(300);
            sourceInfos.add(pointSourceInfo3);
            PointSourceInfo pointSourceInfo4 = new PointSourceInfo();
            pointSourceInfo4.setPointCode("02");
            pointSourceInfo4.setPoints(400);
            sourceInfos.add(pointSourceInfo4);

            PointSourceInfo pointSourceInfo5 = new PointSourceInfo();
            pointSourceInfo5.setPointCode("03");
            pointSourceInfo5.setPoints(400);
            sourceInfos.add(pointSourceInfo5);
            PointSourceInfo pointSourceInfo6 = new PointSourceInfo();
            pointSourceInfo6.setPointCode("03");
            pointSourceInfo6.setPoints(400);
            sourceInfos.add(pointSourceInfo6);
            PointSourceInfo pointSourceInfo7 = new PointSourceInfo();
            pointSourceInfo7.setPointCode("05");
            pointSourceInfo7.setPointCodeName("促销积分");
            pointSourceInfo7.setPoints(500);
            sourceInfos.add(pointSourceInfo7);
        }
    }

    @Test
    public void testJoin(){
        long start = SystemClock.now();
        CollectionProcessTemplate template = new CollectionProcessTemplate();
        template.setJoinType(JoinType.OUTER);
        template.addJoinField("pointCode", "pointCode");
        template.addAliasField("points", "addPoint", ElementLocation.RIGHT);
        template.addAliasField("beginTime", "addTime", ElementLocation.LEFT);
        template.addOperationField("curPoint", OperType.SUM);
        template.addOperationField("addPoint", OperType.SUM);
        template.addGroupbyField("pointCode");
        template.addGroupbyField("addTime");
        template.addOrderbyField("pointCode", 0, OrderType.ASC);
        template.addOrderbyField("addTime", 1, OrderType.ASC);
        template.addFilterField("pointCode", "^0[0-9]$", ElementLocation.LEFT, FilterType.LIKE);

        for(int i = 0; i < 1; i++) {
            List<PointMonthDetInfo> pointMonthDetInfos = CollectionProcessor.execute(generalDetInfos, PointGeneralDetInfo.class,
                    sourceInfos, PointSourceInfo.class, template, PointMonthDetInfo.class);
            for (PointMonthDetInfo info : pointMonthDetInfos) {
                System.out.println(JSON.toJSONString(info));
            }

        }
        long end = SystemClock.now();

        System.out.println("cost: " + (end - start));

    }

    @Test
    public void testSql(){
        long start = SystemClock.now();
        for(int i = 0; i < 100; i++) {
            String cql = "select points addPoint, sum(curPoint), sum(addPoint) from gen left join source " +
                    "where l.pointCode=r.pointCode and l.pointCode>='02' " +
                    "group by pointCode order by pointCode";
            List<PointMonthDetInfo> pointMonthDetInfos = CollectionProcessor.execute(generalDetInfos, PointGeneralDetInfo.class,
                    sourceInfos, PointSourceInfo.class, cql, PointMonthDetInfo.class);
            for (PointMonthDetInfo info : pointMonthDetInfos) {
                System.out.println(JSON.toJSONString(info));
            }

        }
        long end = SystemClock.now();
        System.out.println("cost: " + (end - start));

    }

    @Test
    public void testSqlForGroupby(){
        long start = SystemClock.now();
            String cql = "select points addPoint, sum(curPoint), sum(addPoint) from gen left join source " +
                    "where l.idNo=r.idNo and r.idNo like '(?!0)' group by idNo";
            List<PointMonthDetInfo> pointMonthDetInfos = CollectionProcessor.execute(generalDetInfos, PointGeneralDetInfo.class,
                    sourceInfos, PointSourceInfo.class, cql, PointMonthDetInfo.class);
            for (PointMonthDetInfo info : pointMonthDetInfos) {
                System.out.println(JSON.toJSONString(info));
            }
        long end = SystemClock.now();
        System.out.println("cost: " + (end - start));
    }

    @Test
    public void testSqlForGroupby2(){
        long start = SystemClock.now();
        String cql = "select points addPoint, sum(curPoint), sum(addPoint) from gen left join source " +
                "where l.idNo=r.idNo and r.idNo like '^0' group by idNo";
        List<PointMonthDetInfo> pointMonthDetInfos = CollectionProcessor.execute(generalDetInfos, PointGeneralDetInfo.class,
                sourceInfos, PointSourceInfo.class, cql, PointMonthDetInfo.class);
        for (PointMonthDetInfo info : pointMonthDetInfos) {
            System.out.println(JSON.toJSONString(info));
        }
        long end = SystemClock.now();
        System.out.println("cost: " + (end - start));
    }

    @Test
    public void testSingle(){

        long start = SystemClock.now();
        CollectionProcessTemplate template = new CollectionProcessTemplate();
        template.addAliasField("curPoint", "initPoint");
        template.addAliasField("pointType", "pointType", new SubstrInvoker(), 3,2);
        template.addOperationField("curPoint", OperType.SUM);
        template.addOperationField("count", OperType.COUNT);
        template.addGroupbyField("pointCode");
        template.addOrderbyField("pointCode", 0, OrderType.ASC);
//        template.addFilterField("pointCode", "^0", ElementLocation.LEFT, FilterType.LIKE);

        for(int i = 0; i < 100; i++) {
            List<PointGeneralDetInfo> results = CollectionProcessor.execute(generalDetInfos, PointGeneralDetInfo.class, template);
            for (PointGeneralDetInfo info : results) {
                System.out.println(JSON.toJSONString(info));
            }

        }
        long end = SystemClock.now();

        System.out.println("cost: " + (end - start));
    }

    @Test
    public void testCost(){

        long start = SystemClock.now();

        List<OperationField> operationFields = new ArrayList<>();
        OperationField operationField = new OperationField();
        operationField.setField("curPoint");
        operationField.setOperation(OperType.SUM);
        operationFields.add(operationField);
        List<String> groupbyFields = new ArrayList<>();
        groupbyFields.add("pointCode");
        CollectionProcessTemplate template = new CollectionProcessTemplate();
        template.addGroupbyField("pointCode")
                .addOperationField("curPoint", OperType.SUM)
                .addJoinField("pointCode", "pointCode");
        for (int i = 0; i < 100; i++) {
            List<PointGeneralDetInfo> pointGeneralDetInfos = CollectionProcessor.execute(generalDetInfos, PointGeneralDetInfo.class, template);
            for (PointGeneralDetInfo info : pointGeneralDetInfos) {
                System.out.println(JSON.toJSONString(info));
            }

        }
        long end = SystemClock.now();


        System.out.println("cost: " + (end - start));

    }
}
