package com.hmaishop.pms.inspection.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.adapter.TaskAdapter;
import com.hmaishop.pms.inspection.bean.Photo;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.bean.Task;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.util.CompressPicture;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;
import com.hmaishop.pms.inspection.util.InsertLatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 详细问题汇报界面
 */
public class ReportActivity extends AppCompatActivity
        implements LocationSource, AMapLocationListener {

    private MapView mapView;
    private AMap aMap;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;

    private ListView taskListView;
    private EditText editText;
    private Button taskCommit;

    private List<Task> taskList;
    private TaskAdapter taskAdapter;

    private SubTask subTask;

    private DatabaseManager databaseManager;
    private HttpUtil httpUtil;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        taskListView = (ListView) findViewById(R.id.task_list);
        editText = (EditText) findViewById(R.id.task_remark);
        taskCommit = (Button) findViewById(R.id.task_commit);
        init();

        if (subTask.getRemark() != null) {
            editText.setText(subTask.getRemark());
        }
        taskCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subTask.setHaveDone(true);
                databaseManager.updateSubTask(subTask, editText.getText().toString());
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        initListView();
    }


    /**
     * 初始化设置
     */
    public void init() {
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();
        databaseManager = new DatabaseManager(this);
        Bundle bundle = getIntent().getExtras();
        subTask = (SubTask) bundle.getSerializable("subTask");
        if (!subTask.getSubTaskTitle().equals("")) {
            setTitle(subTask.getSubTaskTitle());
        }
        initMap();
    }


    /**
     * 初始化地图设置，并绘制轨迹
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
            List<Integer> colorList = new ArrayList<Integer>();
            for (int i = 0; i < Constants.latLngList.size(); i++) {
                colorList.add(Color.argb(255, 0, 255, 0));
            }
            aMap.addPolyline(new PolylineOptions().colorValues(colorList)
                    .addAll(Constants.latLngList).useGradient(true).width(20));
        }
    }

    /**
     * 获取任务列表并初始化ListView
     */
    private void initListView() {
        if (sharedPreferences.getInt(subTask.getId() + "_" + subTask.getSubTaskId(), 0) == 1) {
            Log.d("TAG", "aCache...init");
            taskList = databaseManager.queryTasks(subTask);
            setTaskAdapter(taskList);
        } else {
            Log.d("TAG", "http...init");
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        httpUtil = new HttpUtil(Constants.IP, 3000);
                        Log.d("TAG Task...", httpUtil.getDetail(subTask.getId(), subTask.getSubTaskId()));
                        Type listType = new TypeToken<ArrayList<Task>>() {
                        }.getType();
                        ArrayList<Task> tasks = new Gson().fromJson(httpUtil.
                                getDetail(subTask.getId(), subTask.getSubTaskId()), listType);
                        Message message = new Message();
                        message.obj = tasks;
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /**
     * 处理子线程获取到的数据的Handler，并初始化listView
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            taskList = (List<Task>) msg.obj;
            if (taskList != null) {

                Log.d("TAG", "插入..." + taskList.size());
                for (Task task : taskList) {
                    databaseManager.insertTask(task);
                }
                editor.putInt(subTask.getId() + "_" + subTask.getSubTaskId(), 1).commit();
                setTaskAdapter((List<Task>) msg.obj);
            } else {
                Toast.makeText(ReportActivity.this, "Task 为空", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 设置Task的适配器
     *
     * @param taskList list内容，Task链表
     */
    public void setTaskAdapter(List<Task> taskList) {
        taskAdapter = new TaskAdapter(ReportActivity.this, taskList,
                R.layout.task_item, databaseManager);
        taskListView.setAdapter(taskAdapter);
        setListViewHeightBasedOnChildren(taskListView);
    }

    /**
     * 设置一些aMap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setScaleControlsEnabled(true);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
    }


    /**
     * 拍照成功的回调函数
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //对于哪个Task进行操作
        if (resultCode == RESULT_OK) {
            Task task = taskList.get(requestCode);

            File tempFile = new File(Environment.getExternalStorageDirectory() + "/Inspection/Cache/temp");
            File tempImage = new File(tempFile, "temp.jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(tempImage));


            File file = new File(Environment.getExternalStorageDirectory() + "/Inspection/" +
                    task.getId() + "/" + task.getSubTaskId());
            if (!file.exists()) {
                file.mkdirs();
            }
            File imageFile = new File(file, task.getTaskId() + "_" +
                    System.currentTimeMillis() + ".jpg");

            Photo photo = new Photo(task.getId(), task.getSubTaskId(), task.getTaskId(),
                    CompressPicture.saveMyBitmap(bitmap, imageFile));   //工具类中设置压缩比
            databaseManager.insertPhoto(photo);
            task.setPictureNum(databaseManager.queryPhoto(task).size() + 1);
            databaseManager.updateTask(task);
            taskList.set(requestCode, task);
            taskAdapter.setTaskList(taskList);
            taskAdapter.notifyDataSetChanged();
            taskListView.setAdapter(taskAdapter);
            setListViewHeightBasedOnChildren(taskListView);
        }
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
                finish();
        }
        return true;
    }

    /**
     * 定位成功后回调的函数
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0
                    && aMapLocation.getLocationType() != 6) {//舍弃基站定位结果
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                if (InsertLatLng.insert(latLng)) {
                    Constants.latLngList.add(latLng);
                    databaseManager.insertLatLng(subTask.getId(), latLng);
                    editor.putString(Constants.A_LATLNG,new Gson().toJson(latLng)).commit();
                }
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("AMapErr", errText);
            }
        }
    }

    /**
     * 激活定位
     *
     * @param onLocationChangedListener
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
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

    /**
     * 关闭定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    /**
     * 保持 ListView 在 ScrollView 中正常显示
     *
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height =totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }
}
