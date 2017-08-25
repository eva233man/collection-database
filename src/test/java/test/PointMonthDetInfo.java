package test;

import java.io.Serializable;

/**
 * 积分月度明细信息
 * Created by zhangjinpeng on 2017/4/12.
 */

public class PointMonthDetInfo implements Serializable {
    private static final long serialVersionUID = 1322255512125186311L;

    private long idNo;
    private int cycleId;
    private String pointCode;
    private String pointCodeName;
    private long curPoint=0;
    private long beginPoint=0;
    private long addPoint=0;
    private String addTime;
    private long decreasePoint=0;
    private long endPoint=0;

    public int getCycleId() {
        return cycleId;
    }

    public void setCycleId(int cycleId) {
        this.cycleId = cycleId;
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

    public long getCurPoint() {
        return curPoint;
    }

    public void setCurPoint(long curPoint) {
        this.curPoint = curPoint;
    }

    public long getBeginPoint() {
        return beginPoint;
    }

    public void setBeginPoint(long beginPoint) {
        this.beginPoint = beginPoint;
    }

    public long getAddPoint() {
        return addPoint;
    }

    public void setAddPoint(long addPoint) {
        this.addPoint = addPoint;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public long getDecreasePoint() {
        return decreasePoint;
    }

    public void setDecreasePoint(long decreasePoint) {
        this.decreasePoint = decreasePoint;
    }

    public long getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(long endPoint) {
        this.endPoint = endPoint;
    }

    public long getIdNo() {
        return idNo;
    }

    public void setIdNo(long idNo) {
        this.idNo = idNo;
    }
}
