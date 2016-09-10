package com.hmaishop.pms.inspection.bean;

/**
 * 自定义带有 ID 的坐标 Bean
 * <p>
 * Created by Joker_Runner on 8/2 0002.
 */
public class MyLatLng {
    int id;
    double latitude;
    double longitude;

    String time;

    public MyLatLng(int id, String time, double latitude, double longitude) {
        this.id = id;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
