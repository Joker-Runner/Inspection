package com.hmaishop.pms.inspection.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务列表 Activity
 */
public class ToDoTaskActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView todoTaskListView;
    private ToDoTaskAdapter toDoTaskAdapter;

    private SharedPreferences sharedPreferences;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_task);

        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        todoTaskListView = (ListView) findViewById(R.id.todo_task_list);
        databaseManager = new DatabaseManager(this);


        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

    }

    @Override
    protected void onResume() {
        super.onResume();
        initListView();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    int checkerId = sharedPreferences.getInt(Constants.CHECKER_ID, -1);
                    HttpUtil httpUtil = new HttpUtil(Constants.IP, 3000);
                    Log.d("TAG ToDoTask...", httpUtil.getMission(checkerId));
                    Type listType = new TypeToken<ArrayList<ToDoTask>>() {
                    }.getType();
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setDateFormat(Constants.DATE_FORMAT);
                    Gson gson = gsonBuilder.create();
                    ArrayList<ToDoTask> toDoTasks = gson.fromJson(httpUtil.getMission
                            (checkerId), listType);
                    Message message = new Message();
                    message.obj = toDoTasks;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 处理得到的ToDoTask 初始化列表
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj == null) {
                ((TextView) findViewById(R.id.none_task)).setText("没有任务");
            } else {
                toDoTaskAdapter = new ToDoTaskAdapter(ToDoTaskActivity.this, (List<ToDoTask>) msg.obj,
                        R.layout.todo_task_item, databaseManager, sharedPreferences);
                todoTaskListView.setAdapter(toDoTaskAdapter);
                setListViewHeightBasedOnChildren(todoTaskListView);
            }
        }
    };

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
        super.onBackPressed();
        ActivityCollector.finishAll();
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
