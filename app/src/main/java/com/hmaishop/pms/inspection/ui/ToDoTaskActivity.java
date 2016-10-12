package com.hmaishop.pms.inspection.ui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.adapter.ToDoTaskAdapter;
import com.hmaishop.pms.inspection.bean.ToDoTask;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.util.ActivityCollector;
import com.hmaishop.pms.inspection.util.BaseActivity;
import com.hmaishop.pms.inspection.util.CompressPicture;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务列表 Activity
 */
public class ToDoTaskActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        NavigationView.OnNavigationItemSelectedListener {

    private ImageView nav_icon;
    private ImageView emptyTask;
    private MyReceiver myReceiver;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView todoTaskListView;
    private ToDoTaskAdapter toDoTaskAdapter;

    private SharedPreferences sharedPreferences;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.workbg);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);

        myReceiver = new MyReceiver();
        databaseManager = new DatabaseManager(this);
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);

        nav_icon = (ImageView) headerView.findViewById(R.id.nav_icon);
        emptyTask = (ImageView) findViewById(R.id.none_task);
        todoTaskListView = (ListView) findViewById(R.id.todo_task_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_red_light));

        /**
         * 设置头像，如果下载失败，则选一个默认头像
         */
        if (sharedPreferences.getString(Constants.CHECKER_ICON, "").equals("")) {
            nav_icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            nav_icon.setImageBitmap(CompressPicture.decodeSampledBitmapFromResource
                    (sharedPreferences.getString(Constants.CHECKER_ICON, ""), 100, 100));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("Broadcast_NewTask");
        registerReceiver(myReceiver, intentFilter);
        initListView();
    }

    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initListView();
            }
        }, 500);
    }

    /**
     * 获取并初始化任务列表
     */
    public void initListView() {
        swipeRefreshLayout.setRefreshing(true);
        /**
         * 取消所有通知
         */
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        new Thread() {
            @Override
            public void run() {
                super.run();

                int checkerId = sharedPreferences.getInt(Constants.CHECKER_ID, -1);
                HttpUtil httpUtil = new HttpUtil(Constants.IP, 3000);
                String missionJson = httpUtil.getMission(checkerId);
                if (missionJson == null) {
                    Message message = new Message();
                    message.arg1 = 2;
                    handler.sendMessage(message);
                } else if (missionJson.equals("fail")) {
                    Message message = new Message();
                    message.arg1 = 2;
                    handler.sendMessage(message);
                } else {
                    Log.d("TAG ToDoTask...", missionJson);
                    Type listType = new TypeToken<ArrayList<ToDoTask>>() {
                    }.getType();
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setDateFormat(Constants.DATE_FORMAT);
                    Gson gson = gsonBuilder.create();
                    ArrayList<ToDoTask> toDoTasks = gson.fromJson(missionJson, listType);
                    Message message = new Message();
                    message.arg1 = 1;
                    message.obj = toDoTasks;
                    handler.sendMessage(message);
                }
            }
        }.start();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 1.处理得到的ToDoTask 初始化列表
     * 2.获取任务失败
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 1: //获取任务成功
                    List<ToDoTask> toDoTaskList = (List<ToDoTask>) msg.obj;
                    if (toDoTaskList.size() == 0) {
                        todoTaskListView.setVisibility(View.INVISIBLE);
                        emptyTask.setVisibility(View.VISIBLE);
                    } else {
                        emptyTask.setVisibility(View.INVISIBLE);
                        todoTaskListView.setVisibility(View.VISIBLE);
                        toDoTaskAdapter = new ToDoTaskAdapter(ToDoTaskActivity.this, toDoTaskList,
                                R.layout.todo_task_item, databaseManager, sharedPreferences);
                        todoTaskListView.setAdapter(toDoTaskAdapter);
                        setListViewHeightBasedOnChildren(todoTaskListView);
                    }
                    break;
                case 2: //获取任务失败
                    Snackbar.make(emptyTask, "获取任务失败、请下拉重新获取", Snackbar.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.closeDatabase();
    }

    /**
     * 按返回键直接退出程序
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            ActivityCollector.finishAll();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.todo_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "收到广播...");
            initListView();
        }
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }
}
