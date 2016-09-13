package com.hmaishop.pms.inspection.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.ui.MainActivity;
import com.hmaishop.pms.inspection.util.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 直接在 Service 中保存定位数据，Activity 中的定位仅用作显示位置
 */
public class LocationService extends Service implements AMapLocationListener {

    public static boolean isRunning;

    public static int toDoTaskId;  //设置定位的任务Id
    public static AMapLocation aMapLocation = null;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    private LatLng aLatLng = null;
    private LatLng latLng;
    private Intent alarmIntent = null;
    private PendingIntent alarmPi = null;
    private AlarmManager alarm = null;

    private DatabaseManager databaseManager;
    private SharedPreferences sharedPreferences;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG", "Service Started...");
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);

        // 前台服务，防止 kill
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.TODO_TASK_ID,toDoTaskId);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.logo);
        builder.setContentTitle("巡查中...");
        builder.setContentText("请勿关闭定位服务");
        startForeground(1, builder.build());

        sharedPreferences = getSharedPreferences(Constants.SHARED,MODE_APPEND);

        // 后台定时唤醒
        alarmIntent = new Intent();
        alarmIntent.setAction("LOCATION");
        alarmPi = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 动态注册一个广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("LOCATION");
        registerReceiver(alarmReceiver, filter);

        // 设置一个闹钟，2秒之后每隔一段时间执行启动一次定位程序
        if (null != alarm) {
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 2 * 1000,
                    sharedPreferences.getInt(Constants.locationInterval,3*3000), alarmPi);
        }


        // 开启定位
        startLocation();
        isRunning= true;
        Constants.isChecking = true;

        databaseManager = new DatabaseManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开启定位
     */
    public void startLocation() {
        initOption(sharedPreferences.getInt(Constants.locationInterval,3*1000));

        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 设置定位参数
     *
     * @param locationInterval 定位时间间隔
     */
    private void initOption(int locationInterval) {
        Log.d("TAG",locationInterval+"...");
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        locationOption.setInterval(locationInterval);
    }

    /**
     * 停止定位
     */
    public void stopLocation() {
        // 停止定位
        locationClient.stopLocation();

        //停止定位的时候取消闹钟
        if (null != alarm) {
            alarm.cancel(alarmPi);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
        Constants.isChecking = false;

        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }

        if (null != alarmReceiver) {
            unregisterReceiver(alarmReceiver);
            alarmReceiver = null;
        }
    }


    /**
     * 定位监听回调，并保存坐标到数据库和本地
     *
     * @param loc 定位结果 AMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation loc) {
        Log.d("TAG", loc.getLocationType() + "  " + loc.getLatitude() + "  " + loc.getLongitude());
        try {
            File dirFile = new File(Environment.getExternalStorageDirectory() + "/Inspection/Cache");
            File file = new File(dirFile, "LatLng.txt");

            FileOutputStream outStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream);
            writer.write(loc.getLocationType() + "  " + loc.getLatitude() + "  " + loc.getLongitude());
            writer.write("\n");
            writer.flush();
            writer.close();//记得关闭

            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Math.abs(loc.getLatitude()) > 0.01      // 舍弃(0,0)点
//                && (loc.getLocationType() == 1    // GPS 定位
//                || loc.getLocationType() == 5     // WiFi定位
//                || loc.getLocationType() == 6)    // 基站定位
                ) {
            aMapLocation = loc;

            // 定位成功后发送广播
            Intent intent = new Intent();
            intent.setAction("Location");
            sendBroadcast(intent);

            latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

            if (aLatLng == null) {  //如果aLatLng为空，则给aLatLng赋值为latLng
                aLatLng = latLng;
            } else if (AMapUtils.calculateLineDistance(latLng, aLatLng) < 500
                    && (loc.getLocationType() == 1
                    || loc.getLocationType() == 5   // WiFi定位
                    || loc.getLocationType() == 6   // 基站定位
            )) {        // 如果两次定位距离小于500,则保存此次坐标
                aLatLng = latLng;
                databaseManager.insertLatLng(toDoTaskId, System.currentTimeMillis() + "", latLng);
            } else {    // 如果两次定位距离大于500,则给aLatLng赋值为latLng，直至两次定位坐标小于500
                aLatLng = latLng;
            }
        }
    }


    private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("LOCATION")) {
                if (null != locationClient) {
                    locationClient.startLocation();
                }
            }
        }
    };
}
