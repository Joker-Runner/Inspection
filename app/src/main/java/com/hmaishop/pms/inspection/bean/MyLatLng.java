package com.hmaishop.pms.inspection.bean;

import com.amap.api.maps.model.LatLng;

/**
 * 自定义带有 ID 的坐标 Bean
 *
 * Created by Joker_Runner on 8/2 0002.
 */
public class MyLatLng {
    int id;
    double latitude;
    double longitude;

//    public MyLatLng(int id, LatLng latLng){
//        this.id = id;
//        latitude = latLng.latitude;
//        longitude = latLng.longitude;
//    }

    public MyLatLng(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
