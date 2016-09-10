package com.hmaishop.pms.inspection.util;

/**
 * 一些常量
 * <p/>
 * Created by Joker_Runner on 7/18 0018.
 */
public class Constants {

    // 定位间隔
//    public static int locationInterval = 2 * 1000;
    public static String locationInterval = "locationInterval";

    // 后台唤醒间隔
    public static int alarmInterval = 2 * 1000;

    // 刷新界面
    final public static int SHOW_MAIN = 0;
    final public static int SHOW_LIST = 1;

    final public static String SHARED = "shared";

    // 是否提示没有GPS信号
    public static boolean noGPS = true;

    // 是否有任务正在检查
    public static boolean isChecking = false;

    // 巡查者信息 SharedPreferences
    final public static String CHECKER_ID = "checkerId";
    final public static String CHECKER_NAME = "checkerName";
    final public static String CHECKER_ICON = "checkerIcon";

    final public static String TODO_TASK_ID = "todoTaskId";
    final public static String SUB_TASK_ID = "subTaskId";
    final public static String TASK_ID = "taskId";

    // 服务器IP
    final public static String IP = "1546e5j729.imwork.net";
//    final public static String IP = "192.168.2.119";

    // 异常处理
    final public static String HTTP_EXCEPTION = "HttpException";

    // 日期格式
    final public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    // 数据库的表名及表的定义
    final public static String DATE_BASE = "Inspection.db";
    final public static String PHOTO_TABLE = "photo";
    final public static String TASK_TABLE = "task";
    final public static String SUB_TASK_TABLE = "sub_task";
    final public static String LAT_LNG_TABLE = "lat_lng";

    // 照片表
    final public static String CREATE_PHOTO_TABLE = "create table photo (" +
            "todo_task_id integer," +
            "sub_task_id integer," +
            "task_id integer," +
            "photo_id text)";

    // Task表
    final public static String CREATE_TASK_TABLE = "create table task (" +
            "todo_task_id integer," +
            "sub_task_id integer," +
            "task_id integer," +
            "task_title text," +
            "task_detail text," +
            "have_problem integer) ";

    // SubTask表
    final public static String CREATE_SUB_TASK_TABLE = "create table sub_task (" +
            "todo_task_id integer," +
            "sub_task_id integer," +
            "sub_task_title text," +
            "have_done text," +
            "latitude text," +
            "longitude text," +
            "remark text)";

    // 坐标表
    final public static String CREATE_LAT_LNG_TABLE = "create table lat_lng (" +
            "id integer primary key autoincrement," +
            "todo_task_id integer," +
            "lat_lng_time text," +
            "latitude text," +
            "longitude text)";
}
