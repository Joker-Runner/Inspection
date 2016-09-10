package com.hmaishop.pms.inspection.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
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
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
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
import com.hmaishop.pms.inspection.service.LocationService;
import com.hmaishop.pms.inspection.util.BaseActivity;
import com.hmaishop.pms.inspection.util.CompressPicture;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 详细问题汇报界面
 */
public class ReportActivity extends BaseActivity implements LocationSource {

    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocation aMapLocation = null;

    private ListView taskListView;
    private EditText editText;
    private Button taskCommit;

    private boolean firstLocation = true;

    private SubTask subTask;
    private List<LatLng> latLngList;
    private LatLng latLng;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private MyReceiver myReceiver;
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

        /**
         * 坐标空
         */
        taskCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subTask.setHaveDone(true);
                databaseManager.updateSubTask(subTask, latLng, editText.getText().toString());
                finish();
            }
        });


        init();
        initListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        IntentFilter intentFilter = new IntentFilter("Location");
        registerReceiver(myReceiver, intentFilter);
    }

    /**
     * 初始化设置
     */
    public void init() {
        myReceiver = new MyReceiver();
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();
        databaseManager = new DatabaseManager(this);

        Bundle bundle = getIntent().getExtras();
        subTask = (SubTask) bundle.getSerializable("subTask");
//        if (subTask.getSubTaskTitle()!=null&&!subTask.getSubTaskTitle().equals("")) {
            setTitle(subTask.getSubTaskTitle());
//        }
        if (subTask.getRemark() != null) {
            editText.setText(subTask.getRemark());
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

            // 初始化绘制轨迹
            List<Integer> colorList = new ArrayList<>();
            latLngList = databaseManager.queryLatLng(subTask.getId());
            for (int i = 0; i < latLngList.size(); i++) {
                colorList.add(Color.argb(255, 0, 255, 0));
            }
            aMap.addPolyline(new PolylineOptions().colorValues(colorList)
                    .addAll(latLngList).useGradient(true).width(20));

            Message message = new Message();
            message.arg1 = 2;
            handler.sendMessage(message);
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
        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
    }

    /**
     * 获取任务列表并初始化ListView
     */
    private void initListView() {
        if (sharedPreferences.getInt(subTask.getId() + "_" + subTask.getSubTaskId(), 0) == 1) {
            // 第一次初始化从网络获取
            Log.d("TAG", "aCache...init");
            taskList = databaseManager.queryTasks(subTask);
            setTaskAdapter(taskList);
        } else {
            // 后面直接从本地数据库中读取
            Log.d("TAG", "http...init");
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    httpUtil = new HttpUtil(Constants.IP, 3000);
                    Log.d("TAG Task...", httpUtil.getDetail(subTask.getId(), subTask.getSubTaskId()));
                    Type listType = new TypeToken<ArrayList<Task>>() {
                    }.getType();
                    String taskJson = httpUtil.getDetail(subTask.getId(), subTask.getSubTaskId());
                    if (taskJson == null || taskJson == "fail") {
                        Message message = new Message();
                        message.arg1 = 3;
                        handler.sendMessage(message);
                    } else {
                        ArrayList<Task> tasks = new Gson().fromJson(taskJson, listType);
                        Message message = new Message();
                        message.arg1 = 1;
                        message.obj = tasks;
                        handler.sendMessage(message);
                    }
                }
            }.start();
        }
    }

    /**
     * 1.处理子线程获取到的数据的Handler，并初始化listView
     * 2.定位成功后回调
     * 3.获取任务失败
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 1: // 初始化 Task 列表
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
                    break;
                case 2: // 定位成功后回调
                    if (firstLocation){
                        aMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                        firstLocation = false;
                    }

                    aMapLocation = LocationService.aMapLocation;
                    mListener.onLocationChanged(aMapLocation);
                    latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

//                    if (aMapLocation.getLocationType() != 1 && Constants.noGPS){
//                        Snackbar.make(taskCommit,"搜索不到GPS信号,",Snackbar.LENGTH_LONG)
//                                .setAction("不再显示", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        Constants.noGPS = false;
//                                    }
//                                }).show();
//                    }
                    break;
                case 3: // 获取任务失败
                    Toast.makeText(ReportActivity.this, "获取任务失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
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
     * 拍照成功的回调函数
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (latLng != null) {
            databaseManager.updateSubTask(subTask, latLng, editText.getText().toString());
        }

        // 对于哪个Task进行操作
        if (resultCode == RESULT_OK) {
            Task task = taskList.get(requestCode);

            File tempFile = new File(Environment.getExternalStorageDirectory() + "/Inspection/Cache/temp");
            File tempImage = new File(tempFile, "temp.jpg");
//            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(tempImage));


            File file = new File(Environment.getExternalStorageDirectory() + "/Inspection/" +
                    task.getId() + "/" + task.getSubTaskId());
            if (!file.exists()) {
                file.mkdirs();
            }
            File imageFile = new File(file, task.getTaskId() + "_" +
                    System.currentTimeMillis() + ".jpg");

            Photo photo = new Photo(task.getId(), task.getSubTaskId(), task.getTaskId(),
                    CompressPicture.saveMyBitmap(tempImage.getAbsolutePath(), imageFile));   // 工具类中设置压缩比
            databaseManager.insertPhoto(photo);
            task.setPictureNum(databaseManager.queryPhoto(task).size() + 1);
            databaseManager.updateTask(task);
            Log.d("TAG", "Refresh TaskList...");
            taskList.set(requestCode, task);
            taskAdapter.setTaskList(taskList);
            taskAdapter.notifyDataSetChanged();
            taskListView.setAdapter(taskAdapter);
            setListViewHeightBasedOnChildren(taskListView);
        }
    }

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
     * 保持 ListView 在 ScrollView 中正常显示
     *
     * @param listView ListView
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     * 定位服务的 BroadcastReceiver
     */
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = new Message();
            message.arg1 = 2;
            handler.sendMessage(message);
        }
    }
}
