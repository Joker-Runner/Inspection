package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 子部位 Bean
 *
 * Created by Joker_Runner on 7/18 0018.
 */

//  [{"id":1,"mission_id":2,"department_name":"坝顶"},
//  {"id":2,"mission_id":2,"department_name":"坝堤"}]
//[{"id":14,"department_ID":1,"mission_id":40,"department_name":"坝顶"}
public class SubTask implements Serializable{
    @SerializedName("mission_id")
    private int id;
    @SerializedName("department_ID")
    private int subTaskId;
    @SerializedName("department_name")
    private String subTaskTitle;

    private boolean haveDone = false;

    private String remark = "";

    public boolean isHaveDone() {
        return haveDone;
    }

    public void setHaveDone(boolean haveDone) {
        this.haveDone = haveDone;
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
