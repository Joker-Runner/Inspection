package com.hmaishop.pms.inspection.util;

import com.amap.api.maps.model.LatLng;

import java.util.List;

/**
 * 一些常量
 *
 * Created by Joker_Runner on 7/18 0018.
 */
public class Constants {

    //定位轨迹数据
    public static List<LatLng> latLngList;

    public static int locationInterval = 5000;

    //TAG:各种操作的标签
    final public static int LOGIN = 0;
    final public static int BEGIN = 1;
    final public static int DONE = 2;


    //刷新界面
    final public static int SHOW_MAIN = 0;
    final public static int SHOW_LIST = 1;
    final public static int SHOW_REPORT = 2;

    final public static String SHARED = "shared";
//    final public static String SHARED_INIT = "sharedInit";

    final public static String A_LATLNG = "aLatLng";



    //巡查者信息
    final public static String CHECKER_ID = "checkerId";
    final public static String CHECKER_NAME = "checkerName";
    final public static String CHECKER_ICON = "checkerIcon";

    final public static String TODO_TASK_ID = "todoTaskId";
    final public static String SUB_TASK_ID = "subTaskId";
    final public static String TASK_ID = "taskId";
    final public static String SUB_TASK_TITLE = "subTaskTitle";

    //服务器IP
//    final public static String IP = "1546e5j729.imwork.net";
    final public static String IP = "192.168.2.145";

    //日期格式
    final public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    final public static  int TAKE_PHOTO = 1;


    //数据库的表名及表的定义
    final public static String DATE_BASE = "Inspection.db";
    final public static String PHOTO_TABLE = "photo";
    final public static String TASK_TABLE = "task";
    final public static String SUB_TASK_TABLE = "sub_task";
    final public static String LAT_LNG_TABLE = "lat_lng";

    //照片表
    final public static String CREATE_PHOTO_TABLE = "create table photo (" +
            "todo_task_id integer," +
            "sub_task_id integer," +
            "task_id integer," +
            "photo_id text)";

    //Task表
    final public static String CREATE_TASK_TABLE = "create table task (" +
            "todo_task_id integer," +
            "sub_task_id integer," +
            "task_id integer," +
            "task_title text," +
            "task_detail text," +
            "have_problem integer) ";

    //SubTask表
    final public static String CREATE_SUB_TASK_TABLE = "create table sub_task ("+
            "todo_task_id integer," +
            "sub_task_id integer," +
            "sub_task_title text," +
            "have_done text," +
            "remark text)";

    //ToDoTask表
    final public static String CREATE_LAT_LNG_TABLE = "create table lat_lng ("+
            "id integer primary key autoincrement,"+
            "todo_task_id integer," +
            "latitude text," +
            "longitude text)";
}
