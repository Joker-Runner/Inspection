package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;
import com.hmaishop.pms.inspection.util.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 巡查任务 Bean
 * <p>
 * Created by Joker_Runner on 7/18 0018.
 */
public class ToDoTask {

    private DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);

    @SerializedName("mission_id")
    private int id;
    @SerializedName("create_time")
    private Date beginTime;
    @SerializedName("time")
    private String amPm;
    private String title;
    @SerializedName("detail")
    private String details;
    @SerializedName("reservoir_name")
    private String reservoirName;


    public int getId() {
        return id;
    }

    public String getTitle() {
        String title = new String();
        title = beginTime.getMonth() + "月" + beginTime.getDay() + "日" + amPm + "任务";
        return title;
    }

    public String getDetails() {
        return details;
    }
}
