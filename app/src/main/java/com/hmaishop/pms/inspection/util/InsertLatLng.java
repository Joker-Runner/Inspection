package com.hmaishop.pms.inspection.util;

import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

/**
 * 是否将本次的定位结果记录保存的工具类
 *
 * Created by Joker_Runner on 8/3 0003.
 */
public class InsertLatLng {
    public static LatLng aLatLng;

    public  static boolean insert(LatLng latLng) {
        if (aLatLng == null) {
            aLatLng = latLng;
            Log.d("TAG", "有效定位坐标..."+aLatLng.toString());
            return true;
        } else if (AMapUtils.calculateLineDistance(aLatLng, latLng) >= 5 &&
                AMapUtils.calculateLineDistance(aLatLng, latLng) < 100) {
            aLatLng = latLng;
            Log.d("TAG", "有效定位坐标...");
            return true;
        } else {
            Log.d("TAG", "定位位置无效...");
            return false;
        }
    }
}
