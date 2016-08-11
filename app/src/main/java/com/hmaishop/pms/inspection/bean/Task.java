package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 详情任务 Bean
 *
 * Created by Joker_Runner on 7/13 0013.
 */

//[{"stand":1,"mission_id":2,"department_ID":1,"check_standard":"有无裂缝"},
// {"stand":2,"mission_id":2,"department_ID":1,"check_standard":"有无裂缝，异常变形"}]
//    {"mission_id":40,"department_ID":1,"stand_ID":1,"reservoir_Name":"asda",
// "picture_num":0,"have_problem":0,"department_name":"坝顶","stand_content":"有无裂缝，异常变形"}
public class Task implements Serializable {
    @SerializedName("mission_id")
    private int id;
    @SerializedName("department_ID")
    private int subTaskId;
    @SerializedName("stand_ID")
    private int taskId;
    @SerializedName("stand_content")
    private String taskTitle;
//    @SerializedName("task_detail")
//    private String taskDetails= "Detail";
    @SerializedName("have_problem")
    private int haveProblem = 0;
    @SerializedName("picture_num")
    private int pictureNum = 0;



//    public String getTaskDetails() {
//        return taskDetails;
//    }
//
//    public void setTaskDetails(String taskDetails) {
//        this.taskDetails = taskDetails;
//    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSubTaskId(int subTaskId) {
        this.subTaskId = subTaskId;
    }

    public int getId() {
        return id;
    }

    public int getSubTaskId() {
        return subTaskId;
    }

    public int getPictureNum() {
        return pictureNum;
    }

    public void setPictureNum(int pictureNum) {
        this.pictureNum = pictureNum;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public boolean isHaveProblem() {
        return haveProblem==1;
    }

    public void setHaveProblem(boolean haveProblem) {
        if (haveProblem) {
            this.haveProblem = 1;
        } else {
            this.haveProblem = 0;
        }
    }

//    public Task(int taskId, String taskTitle, String taskDetails) {
//
//        this.taskId = taskId;
//        this.taskTitle = taskTitle;
//        this.taskDetails = taskDetails;
//    }
}
