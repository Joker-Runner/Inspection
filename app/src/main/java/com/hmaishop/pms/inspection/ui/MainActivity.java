package com.hmaishop.pms.inspection.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.util.BaseActivity;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.util.InsertLatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 */
public class MainActivity extends BaseActivity implements LocationSource, AMapLocationListener {


    private LinearLayout mapContainer;
    private ImageButton location;
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private LatLng latLng;

    private int toDoTaskId;

    private int showWhat;

    private DatabaseManager databaseManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapContainer = (LinearLayout) findViewById(R.id.map_container);
        location = (ImageButton) findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latLng != null) {
                    if (aMap.getCameraPosition().zoom <= 12) {
                        aMap.moveCamera(CameraUpdateFactory.newCameraPosition
                                (new CameraPosition(latLng, 12, 0, 0)));
                    } else {
                        aMap.moveCamera(CameraUpdateFactory.newCameraPosition
                                (new CameraPosition(latLng, aMap.getCameraPosition().zoom, 0, 0)));
                    }
                } else {
                    Toast.makeText(MainActivity.this,"定位失败",Toast.LENGTH_SHORT).show();
                }
            }
        });

        toDoTaskId = getIntent().getIntExtra(Constants.TODO_TASK_ID, -1);

        init();
        refreshActivity(Constants.SHOW_MAIN);
    }

    /**
     * 初始化
     */
    private void init() {
        databaseManager = new DatabaseManager(this);
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
            Constants.latLngList = new ArrayList<>();
        }
    }

    /**
     * 设置一些aMap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setScaleControlsEnabled(true);//设置比例尺是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
    }

    /**
     * 刷新Activity 显示的Fragment
     *
     * @param actionTag 标签
     */
    public void refreshActivity(int actionTag) {
        switch (actionTag) {
            case Constants.SHOW_MAIN:
                setTitle("巡查中...");
                showWhat = Constants.SHOW_MAIN;
                LinearLayout.LayoutParams layoutParamsMain = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                layoutParamsMain.weight = 2;
                mapContainer.setLayoutParams(layoutParamsMain);
                MainFragment mainFragment = new MainFragment();
                mainFragment.setMainActivity(this);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.container, mainFragment).commit();
                break;
            case Constants.SHOW_LIST:
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


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        refreshActivity(Constants.SHOW_MAIN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
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
        databaseManager.closeDatabase();
        editor.remove(Constants.A_LATLNG).commit();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
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
     * 定位成功后回调的函数
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        try {
            if (mListener != null && aMapLocation != null) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0
                        && aMapLocation.getLocationType() != 6) {//舍弃基站定位结果
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                    latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    if (InsertLatLng.insert(latLng)) {
                        Constants.latLngList.add(latLng);
                        databaseManager.insertLatLng(toDoTaskId, latLng);
                        editor.putString(Constants.A_LATLNG,new Gson().toJson(latLng)).commit();
                        Log.d("TAG","put"+latLng);
                        List<Integer> colorList = new ArrayList<Integer>();
                        for (int i = 0; i < Constants.latLngList.size(); i++) {
                            colorList.add(Color.argb(255, 0, 255, 0));
                        }
                        if (!Constants.latLngList.isEmpty()) {
                            aMap.addPolyline(new PolylineOptions().colorValues(colorList)
                                    .addAll(Constants.latLngList).useGradient(true).width(20));
                        } else {
                            Log.d("TAG", "绘图数据为空");
                        }
                    }
                } else {
                    String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                    Log.e("AMapErr", errText);
                }

            }
        } catch (Exception e) {
            Log.e("TAG", "定位回调绘图有异常...");
        }
    }


    /**
     * 激活定位
     *
     * @param onLocationChangedListener
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔
            mLocationOption.setInterval(Constants.locationInterval);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

}
