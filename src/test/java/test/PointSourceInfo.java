package test;


/**
 * 用户积分来源信息类
 *
 * @author zhangjp
 * @version 1.0
 */

public class PointSourceInfo {

    private long idNo;
    private long userPointId;
    private long points;
    private String pointCode;
    private String pointCodeName;
    private String pointType;
    private String opTime;


    public long getUserPointId() {
        return userPointId;
    }

    public void setUserPointId(long userPointId) {
        this.userPointId = userPointId;
    }

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public String getOpTime() {
        return opTime;
    }

    public void setOpTime(String opTime) {
        this.opTime = opTime;
    }

    public String getPointCodeName() {
        return pointCodeName;
    }

    public void setPointCodeName(String pointCodeName) {
        this.pointCodeName = pointCodeName;
    }

    public long getIdNo() {
        return idNo;
    }

    public void setIdNo(long idNo) {
        this.idNo = idNo;
    }
}
