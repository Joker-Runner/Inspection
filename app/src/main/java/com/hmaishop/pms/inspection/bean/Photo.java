package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;

/**
 * 照片 Bean
 *
 * Created by Joker_Runner on 7/27 0027.
 */
public class Photo {
    @SerializedName("mission_id")
    private int id;
    @SerializedName("department_ID")
    private int subTaskId;
    @SerializedName("stand")
    private int taskId;
    @SerializedName("photo_id")
    private String photoId;

    public Photo() {
    }

    public Photo(int id, int subTaskId, int taskId, String photoId) {
        this.id = id;
        this.subTaskId = subTaskId;
        this.taskId = taskId;
        this.photoId = photoId;
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

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }
}
