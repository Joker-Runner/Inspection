package com.hmaishop.pms.inspection.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.ToDoTask;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.ui.MainActivity;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import java.util.List;

/**
 * ToDoTask适配器
 * <p>
 * Created by Joker_Runner on 7/18 0018.
 */
public class ToDoTaskAdapter extends BaseAdapter {

    private int resource;
    private Context context;
    private List<ToDoTask> todoTaskList;
    private LayoutInflater inflater;

    private TextView todoTaskTitle;
    private TextView todoTaskDetails;
    private Button todoTaskBegin;
//    private Button todoTaskSubmit;
//
//    private ProgressDialog progressDialog;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DatabaseManager databaseManager;
    private HttpUtil httpUtil;

    String[] items = new String[]{"步行", "自行车", "驾车"};

    public ToDoTaskAdapter(Context context, List<ToDoTask> todoTaskList, int resource,
                           DatabaseManager databaseManager, SharedPreferences sharedPreferences) {
        this.context = context;
        this.todoTaskList = todoTaskList;
        this.resource = resource;
        this.databaseManager = databaseManager;
        this.sharedPreferences = sharedPreferences;
        httpUtil = new HttpUtil(Constants.IP, 3000);
        editor = sharedPreferences.edit();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return todoTaskList.size();
    }

    @Override
    public Object getItem(int i) {
        return todoTaskList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(resource, null);
        }

        final ToDoTask toDoTask = todoTaskList.get(i);

        todoTaskTitle = (TextView) view.findViewById(R.id.todo_task_title);
        todoTaskDetails = (TextView) view.findViewById(R.id.todo_task_details);
        todoTaskBegin = (Button) view.findViewById(R.id.todo_task_begin);
//        todoTaskSubmit = (Button) view.findViewById(R.id.todo_task_submit);

        todoTaskTitle.setText(toDoTask.getTitle());
        todoTaskDetails.setText(toDoTask.getDetails());

        todoTaskBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //是否已经开始巡查
                if (!sharedPreferences.getBoolean(toDoTask.getId() + "", false)) {
                    editor.putInt(Constants.locationInterval, 6 * 1000).commit();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("确定要开始吗？");
                    alertDialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    editor.putInt(Constants.locationInterval, 6 * 1000).commit();
                                    Log.d("TAG", "put..." + 6);
                                    break;
                                case 1:
                                    editor.putInt(Constants.locationInterval, 4 * 1000).commit();
                                    Log.d("TAG", "put..." + 4);
                                    break;
                                case 2:
                                    editor.putInt(Constants.locationInterval, 2 * 1000).commit();
                                    Log.d("TAG", "put..." + 2);
                                    break;
                                default:
                                    editor.putInt(Constants.locationInterval, 3 * 1000).commit();
                                    Log.d("TAG", "put..." + 3);
                                    break;
                            }
                        }
                    });
                    alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor.putBoolean(toDoTask.getId() + "", true).commit();
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    httpUtil.start(toDoTask.getId());
                                }
                            }.start();
                            startTodoTask(toDoTask);
                        }
                    });
                    alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alertDialog.show();
                } else {
                    startTodoTask(toDoTask);
                }
            }
        });
        return view;
    }

    /**
     * 开始巡查
     *
     * @param toDoTask 巡查的任务
     */
    public void startTodoTask(ToDoTask toDoTask) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constants.TODO_TASK_ID, toDoTask.getId());
        context.startActivity(intent);
    }
}