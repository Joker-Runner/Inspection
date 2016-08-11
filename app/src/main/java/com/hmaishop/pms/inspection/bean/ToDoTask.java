package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;
import com.hmaishop.pms.inspection.util.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 巡查任务 Bean
 *
 * Created by Joker_Runner on 7/18 0018.
 */


public class ToDoTask {

    //[{"mission_id":3,"checker_num":1,"create_time":"2016-07-10T16:00:00.000Z","time":"上午",
    // "temporary":1,"reservoir_name":"asda","department_name":"溢洪道","detail":"111"},

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
        title = beginTime.getMonth() + "月" +beginTime.getDay() +"日" + amPm + "任务";
        return title;
    }


    public String getDetails() {
        return details;
    }


}
