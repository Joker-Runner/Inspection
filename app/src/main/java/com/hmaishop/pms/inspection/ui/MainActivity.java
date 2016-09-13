package com.hmaishop.pms.inspection.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
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
import com.google.gson.Gson;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.MyLatLng;
import com.hmaishop.pms.inspection.bean.Photo;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.bean.Task;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.service.LocationService;
import com.hmaishop.pms.inspection.util.BaseActivity;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;
import com.hmaishop.pms.inspection.util.ZipUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 */
public class MainActivity extends BaseActivity implements LocationSource {


    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    public static AMapLocation aMapLocation = null;

    private boolean firstLocation = true;
    private int showWhat;
    private int toDoTaskId;

    private MyReceiver myReceiver;
    private LinearLayout mapContainer;
    private ProgressDialog progressDialog;
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

        toDoTaskId = getIntent().getIntExtra(Constants.TODO_TASK_ID, -1);
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();

        Log.d("TAG", "onCreate...");

        init(); //初始化
        initLocation(); //开启定位服务
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        refreshActivity(Constants.SHOW_MAIN);
        Log.d("TAG", "onResume...");

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
                mainFragment.setArguments(this);
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
                case 0: // 开始上传
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("正在上传，请稍候...");
                    progressDialog.show();

                    break;
                case 1: // 任务提交成功
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提交成功");
                    builder.setCancelable(true);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.show();
                    progressDialog.cancel();

                    /**
                     * 上传成功后删除本地缓存数据   处理异常
                     */
                    deleteRecursive((File) msg.obj);    // 删除文件夹
                    File tempFile = new File(Environment.
                            getExternalStorageDirectory() + "/Inspection/Cache/temp");
                    deleteRecursive(tempFile);          // 删除临时文件夹
                    databaseManager.deleteLatLng(msg.arg2);
                    databaseManager.deletePhotos(msg.arg2);
                    databaseManager.deleteTasks(msg.arg2);
                    databaseManager.deleteSubTasks(msg.arg2);
                    editor.remove(msg.arg2 + "").commit();  // 移除
                    break;
                case 2: // 任务提交失败
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setTitle("提交失败");
                    builder1.setCancelable(true);
                    builder1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    builder1.setPositiveButton("重新提交", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            uploadTasks(toDoTaskId);
                        }
                    });
                    builder1.show();
                    progressDialog.cancel();
                    break;
                case 3: //处理定位信息
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
                    if (firstLocation) {
                        aMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                        firstLocation = false;
                    }

                    if (aMapLocation.getLocationType() != 2) {
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
        Log.d("TAG", "onPause...");
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
        Log.d("TAG", "onDestroy...");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                break;
            case R.id.upload_task:  // 上报任务
                uploadTasks();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 结束巡查,上报
     */
    public void uploadTasks() {
        int k = 0;
        for (SubTask subTask : databaseManager.querySubTasks(toDoTaskId)) {
            if (!subTask.isHaveDone()) {
                k++;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
                alertDialog.setTitle(" 警告！");
                alertDialog.setMessage("请全部检查完成后提交");
                alertDialog.setCancelable(false);
                alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do something
                    }
                });
                alertDialog.setPositiveButton("继续巡查", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.putExtra(Constants.TODO_TASK_ID, toDoTaskId);
                        startActivity(intent);
                    }
                });
                alertDialog.show();
                break;
            }
        }
        if (k == 0) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("确定要提交吗？");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    uploadTasks(toDoTaskId);
                }
            });
            alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alertDialog.show();
        }
    }

    /**
     * 上传巡查任务报告
     * 并且关闭定位 Service
     *
     * @param toDoTaskId 上传的任务
     */
    public void uploadTasks(final int toDoTaskId) {
        Log.d("TAG", "全部检查完成，开始上传...");
        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
        List<MyLatLng> myLatLngList = databaseManager.queryMyLatLng(toDoTaskId);
        Log.d("TAG", new Gson().toJson(myLatLngList));

        List<SubTask> subTaskList = databaseManager.querySubTasks(toDoTaskId);

        List<List<Task>> taskLists = new ArrayList<List<Task>>();
        for (SubTask subTask : subTaskList) {
            taskLists.add(databaseManager.queryTasks(subTask));
        }

        List<Photo> photoList = new ArrayList<Photo>();

        for (List<Task> taskList : taskLists) {
            for (Task task : taskList) {
                for (Photo photo : databaseManager.queryPhoto(task)) {
                    photo.setPhotoId(photo.getPhotoId().substring(photo.getPhotoId().
                            indexOf(photo.getId() + "/")));
                    photoList.add(photo);
                }
            }
        }


        String latLngString = new Gson().toJson(myLatLngList);//LatLng
        String subTaskString = new Gson().toJson(subTaskList);//SubTask
        final String taskListString = new Gson().toJson(taskLists);//Task
        String photoString = new Gson().toJson(photoList);//Photo

        final File file = new File(Environment.getExternalStorageDirectory()
                + "/Inspection/" + toDoTaskId);
        if (!file.exists()) {
            file.mkdir();
        }
        final File file1 = new File(Environment.getExternalStorageDirectory()
                + "/Inspection/", toDoTaskId + ".zip");
        final String photoZip = file1.getAbsolutePath();
        try {
            ZipUtil.ZipFolder(file.getAbsolutePath(), photoZip);    // PhotoZip
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 总的任务信息
        final String sumString = "{\"lat\":" + latLngString + ",\"photo\":" + photoString +
                ",\"task\":" + taskListString + ",\"sub\":" + subTaskString + "}";

        final ArrayList<String> photoZips = new ArrayList<String>();
        photoZips.add(photoZip);

        new Thread() {
            @Override
            public void run() {
                super.run();

                Message message0 = new Message();   // 开始上传任务
                message0.arg1 = 0;
                handler.sendMessage(message0);
                /**
                 * 上传任务
                 */
                HttpUtil httpUtil = new HttpUtil(Constants.IP, 3000);
                String upSumTasks = httpUtil.upSumTasks(sumString);
                String upFiles = httpUtil.upFiles(photoZips);

                if (upSumTasks.equals("success") && upFiles.equals("success")) {

                    file1.delete(); // 删除压缩包文件
                    Message message1 = new Message();   // 任务提交成功，刷新任务列表
                    message1.arg1 = 1;
                    message1.arg2 = toDoTaskId;
                    message1.obj = file;
                    handler.sendMessage(message1);

                } else {
                    Message message2 = new Message();    // 任务提交失败
                    message2.arg1 = 2;
                    handler.sendMessage(message2);
                }
            }
        }.start();

        Log.d("TAG", "总 " + sumString);
        Log.d("TAG", "照片Zip " + photoZip);
    }

    /**
     * 递归删除一个文件夹
     *
     * @param fileOrDirectory 要删除的文件夹
     */
    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
            fileOrDirectory.delete();
        } else {
            fileOrDirectory.delete();
        }
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
            message.arg1 = 3;
            handler.sendMessage(message);
        }
    }
}