package com.hmaishop.pms.inspection.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.util.HttpUtil;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.adapter.SubTaskAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 显示SubTask列表信息，选择要上报的水库部位
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    MainActivity mainActivity;
    private LinearLayout showMain;
    private ListView listView;
    private View view;

    private SubTaskAdapter subTaskAdapter;
    private List<SubTask> subTaskList;
    private int todoTaskId;
    private GestureDetector gestureDetector;
    private HttpUtil httpUtil;
    private DatabaseManager databaseManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public void setArgument(MainActivity mainActivity, int todoTaskId) {
        this.mainActivity = mainActivity;
        this.todoTaskId = todoTaskId;
    }

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_list, container, false);
        showMain = (LinearLayout) view.findViewById(R.id.show_main);
        listView = (ListView) view.findViewById(R.id.task_list);

        showMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.refreshActivity(Constants.SHOW_MAIN);
            }
        });
        gestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();
        initSubTaskList(todoTaskId);
        return view;
    }

    /**
     * 获取任务列表并初始化ListView
     *
     * @param todoTaskId TodoTask 的任务Id
     */
    public void initSubTaskList(final int todoTaskId) {
        databaseManager = new DatabaseManager(getContext());

        if (sharedPreferences.getInt("init" +todoTaskId,0) == 1) {
            Log.d("TAG","List...aCache...");
            subTaskList = databaseManager.querySubTasks(todoTaskId);
            setSubTaskAdapter(subTaskList);
        } else {
            Log.d("TAG","List...http...");
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        httpUtil = new HttpUtil(Constants.IP, 3000);
                        Log.d("TAG", httpUtil.getSubkey(todoTaskId));
                        Type listType = new TypeToken<ArrayList<SubTask>>() {
                        }.getType();
                        ArrayList<SubTask> subTasks = new Gson().fromJson(httpUtil.getSubkey(todoTaskId), listType);
                        Message message = new Message();
                        message.obj = subTasks;
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
            if (getActivity() != null) {
                subTaskList = (List<SubTask>) msg.obj;
                Log.d("TAG","insert..." + subTaskList.size());
                for (SubTask subTask : subTaskList) {
                    databaseManager.insertSubTask(subTask, "");
                }
                editor.putInt("init" +todoTaskId,1).commit();
                setSubTaskAdapter(subTaskList);
            }

        }
    };

    /**
     * 设置Task的适配器
     *
     * @param subTaskList list内容，Task链表
     */
    public void setSubTaskAdapter(List<SubTask> subTaskList) {
        subTaskAdapter = new SubTaskAdapter
                (getActivity(), subTaskList, R.layout.sub_task_item);
        listView.setAdapter(subTaskAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseManager.closeDatabase();
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e2.getY() - e1.getY() > 5) {
                mainActivity.refreshActivity(Constants.SHOW_MAIN);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }


}
