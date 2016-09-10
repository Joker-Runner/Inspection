package com.hmaishop.pms.inspection.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.service.LocationService;
import com.hmaishop.pms.inspection.util.BaseActivity;
import com.hmaishop.pms.inspection.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 */
public class MainActivity extends BaseActivity implements LocationSource {


    private LinearLayout mapContainer;
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    public static AMapLocation aMapLocation = null;

    private boolean firstLocation = true;
    private int showWhat;
    private int toDoTaskId;

    private MyReceiver myReceiver;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapContainer = (LinearLayout) findViewById(R.id.map_container);

        toDoTaskId = getIntent().getIntExtra(Constants.TODO_TASK_ID, -1);
        Log.d("TAG",toDoTaskId+"...");

        Log.d("TAG","onCreate...");

        init(); //初始化
        initLocation(); //开启定位服务
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        refreshActivity(Constants.SHOW_MAIN);
        Log.d("TAG","onResume...");

        // 注册广播接收者
        IntentFilter intentFilter = new IntentFilter("Location");
        registerReceiver(myReceiver, intentFilter);
    }

    /**
     * 初始化
     */
    private void init() {
        myReceiver = new MyReceiver();
        databaseManager = new DatabaseManager(this);
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * 设置一些aMap的属性
     */
    private void setUpMap() {
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        //设置比例尺是否显示
        aMap.getUiSettings().setScaleControlsEnabled(true);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
//        // 设置地图的缩放比例
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(13));
    }

    /**
     * 初始化启动定位服务
     */
    private void initLocation() {
        LocationService.toDoTaskId = toDoTaskId;
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);

        Log.d("TAG", "startLocationService...");
    }

    /**
     * 刷新Activity 显示的Fragment
     *
     * @param actionTag 标签
     */
    public void refreshActivity(int actionTag) {
        switch (actionTag) {
            case Constants.SHOW_MAIN:   //刷新MainActivity，显示巡查中...
                setTitle("巡查中...");
                showWhat = Constants.SHOW_MAIN;
                LinearLayout.LayoutParams layoutParamsMain = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                layoutParamsMain.weight = 2;
                mapContainer.setLayoutParams(layoutParamsMain);
                MainFragment mainFragment = new MainFragment();
                mainFragment.setArguments(this,toDoTaskId,databaseManager);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.container, mainFragment).commit();
                break;
            case Constants.SHOW_LIST:   //刷新MainActivity，显示部位列表
                setTitle("选择部位");
                showWhat = Constants.SHOW_LIST;
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                layoutParams.weight = 0;
                mapContainer.setLayoutParams(layoutParams);
                ListFragment listFragment = new ListFragment();
                listFragment.setArgument(this, toDoTaskId);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.container, listFragment).commit();
                break;
            default:
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 1: //处理定位信息
                    aMapLocation = LocationService.aMapLocation;
                    mListener.onLocationChanged(aMapLocation);

//                    if (aMapLocation.getLocationType() != 1 && Constants.noGPS) {
//                        Snackbar.make(mapContainer, "搜索不到GPS信号,", Snackbar.LENGTH_LONG)
//                                .setAction("不再显示", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        Constants.noGPS = false;
//                                    }
//                                }).show();
//                    }
                    if (firstLocation){
                        aMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                        firstLocation = false;
                    }

                    List<LatLng> latLngList = databaseManager.queryLatLng(toDoTaskId);
                    List<Integer> colorList = new ArrayList<>();
                    for (int i = 0; i < latLngList.size(); i++) {
                        colorList.add(Color.argb(255, 0, 255, 0));
                    }
                    if (!latLngList.isEmpty()) {
                        aMap.addPolyline(new PolylineOptions().colorValues(colorList)
                                .addAll(latLngList).useGradient(true).width(20));
                    } else {
                        Log.d("TAG", "绘图数据为空");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        unregisterReceiver(myReceiver);
        Log.d("TAG","onPause...");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        firstLocation = true;
        databaseManager.closeDatabase();
        Log.d("TAG","onDestroy...");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    /**
     * 按返回键时的操作
     */
    public void exit() {
        if (showWhat == Constants.SHOW_MAIN) {
            finish();
        } else if (showWhat == Constants.SHOW_LIST) {
            refreshActivity(Constants.SHOW_MAIN);
        }
    }

    /**
     * 定位服务的 BroadcastReceiver
     */
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = new Message();
            message.arg1 = 1;
            handler.sendMessage(message);
        }
    }
}