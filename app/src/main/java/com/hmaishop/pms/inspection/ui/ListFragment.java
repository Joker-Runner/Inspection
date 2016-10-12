package com.hmaishop.pms.inspection.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.adapter.SubTaskAdapter;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 显示SubTask列表信息，选择要上报的水库部位
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    private View view;
    private ListView listView;

    private int todoTaskId;
    private HttpUtil httpUtil;
    private SubTaskAdapter subTaskAdapter;
    private List<SubTask> subTaskList;
    private DatabaseManager databaseManager;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    MainActivity mainActivity;

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
        listView = (ListView) view.findViewById(R.id.task_list);

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

        if (sharedPreferences.getInt("init" + todoTaskId, 0) == 1) {
            Log.d("TAG", "List...aCache...");
            subTaskList = databaseManager.querySubTasks(todoTaskId);
            setSubTaskAdapter(subTaskList);
        } else {
            Log.d("TAG", "List...http...");
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    httpUtil = new HttpUtil(Constants.IP, 3000);
                    Log.d("TAG", httpUtil.getSubkey(todoTaskId));
                    Type listType = new TypeToken<ArrayList<SubTask>>() {
                    }.getType();
                    ArrayList<SubTask> subTasks = new Gson().fromJson(httpUtil.getSubkey(todoTaskId), listType);
                    Message message = new Message();
                    message.obj = subTasks;
                    handler.sendMessage(message);
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
                Log.d("TAG", "insert..." + subTaskList.size());
                for (SubTask subTask : subTaskList) {
                    databaseManager.insertSubTask(subTask);
                }
                editor.putInt("init" + todoTaskId, 1).commit();
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
}
