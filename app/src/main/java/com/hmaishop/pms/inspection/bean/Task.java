package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 详情任务 Bean
 * <p>
 * Created by Joker_Runner on 7/13 0013.
 */
public class Task implements Serializable {
    @SerializedName("mission_id")
    private int id;
    @SerializedName("department_ID")
    private int subTaskId;
    @SerializedName("stand_ID")
    private int taskId;
    @SerializedName("stand_content")
    private String taskTitle;
    @SerializedName("have_problem")
    private int haveProblem = 0;
    @SerializedName("picture_num")
    private int pictureNum = 0;


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
        return haveProblem == 1;
    }

    public void setHaveProblem(boolean haveProblem) {
        if (haveProblem) {
            this.haveProblem = 1;
        } else {
            this.haveProblem = 0;
        }
    }
}
