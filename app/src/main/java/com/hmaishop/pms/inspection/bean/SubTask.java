package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 子部位 Bean
 * <p>
 * Created by Joker_Runner on 7/18 0018.
 */
public class SubTask implements Serializable {
    @SerializedName("mission_id")
    private int id;
    @SerializedName("department_ID")
    private int subTaskId;
    @SerializedName("department_name")
    private String subTaskTitle;

    private boolean haveDone = false;

    private double latitude = 0;
    private double longitude = 0;

    private String remark = "";

    public boolean isHaveDone() {
        return haveDone;
    }

    public void setHaveDone(boolean haveDone) {
        this.haveDone = haveDone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubTaskId() {
        return subTaskId;
    }

    public void setSubTaskId(int subTaskId) {
        this.subTaskId = subTaskId;
    }

    public String getSubTaskTitle() {
        return subTaskTitle;
    }

    public void setSubTaskTitle(String subTaskTitle) {
        this.subTaskTitle = subTaskTitle;
    }
}
