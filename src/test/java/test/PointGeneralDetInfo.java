package test;


/**
 * 用户积分的基本明细信息类
 *
 * @author zhangjp
 * @version 1.0
 */

public class PointGeneralDetInfo {

    private long idNo;
    private long userPointId;
    private long initPoint;
    private long curPoint;
    private String pointCode;
    private String pointCodeName;
    private String pointType;
    private String beginTime;
    private String endTime;

    private String openTime;
    private String foreignSn;
    private int count;


    public long getUserPointId() {
        return userPointId;
    }

    public void setUserPointId(long userPointId) {
        this.userPointId = userPointId;
    }

    public long getCurPoint() {
        return curPoint;
    }

    public void setCurPoint(long curPoint) {
        this.curPoint = curPoint;
    }

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getPointCodeName() {
        return pointCodeName;
    }

    public void setPointCodeName(String pointCodeName) {
        this.pointCodeName = pointCodeName;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(long initPoint) {
        this.initPoint = initPoint;
    }

    public String getForeignSn() {
        return foreignSn;
    }

    public void setForeignSn(String foreignSn) {
        this.foreignSn = foreignSn;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public long getIdNo() {
        return idNo;
    }

    public void setIdNo(long idNo) {
        this.idNo = idNo;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
