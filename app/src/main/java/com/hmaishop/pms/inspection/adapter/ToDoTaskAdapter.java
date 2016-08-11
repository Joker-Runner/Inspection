package com.hmaishop.pms.inspection.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.MyLatLng;
import com.hmaishop.pms.inspection.bean.Photo;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.bean.Task;
import com.hmaishop.pms.inspection.bean.ToDoTask;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.util.HttpUtil;
import com.hmaishop.pms.inspection.util.ZipUtil;
import com.hmaishop.pms.inspection.ui.MainActivity;
import com.hmaishop.pms.inspection.ui.ToDoTaskActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ToDoTask适配器
 *
 * Created by Joker_Runner on 7/18 0018.
 */
public class ToDoTaskAdapter extends BaseAdapter {

    private Context context;
    private List<ToDoTask> todoTaskList;
    private int resource;
    private LayoutInflater inflater;

    private TextView todoTaskTitle;
    private TextView todoTaskDetails;
    private Button todoTaskBegin;
    private Button todoTaskSubmit;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DatabaseManager databaseManager;
    private HttpUtil httpUtil;

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
        todoTaskSubmit = (Button) view.findViewById(R.id.todo_task_submit);

        todoTaskTitle.setText(toDoTask.getTitle());
        todoTaskDetails.setText(toDoTask.getDetails());

        todoTaskBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //如果是第一次点击开始巡查则弹出“确定要开始？”，第二次或者以后则不弹出
                if (sharedPreferences.getBoolean(toDoTask.getId() + "", true)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("确定要开始吗？");
                    alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor.putBoolean(toDoTask.getId() + "", false).commit();
                            new Thread(){
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

        /**
         * 任务提交处理
         */
        todoTaskSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int k = 0;
                for (SubTask subTask : databaseManager.querySubTasks(toDoTask.getId())) {
                    if (!subTask.isHaveDone()) {
                        k++;
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
                        alertDialog.setTitle(" 警告！");
                        alertDialog.setMessage("请全部检查完成后提交");
                        alertDialog.setCancelable(false);
                        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra(Constants.TODO_TASK_ID, toDoTask.getId());
                                context.startActivity(intent);
//                                ((Activity)context).finish();
                            }
                        });
                        alertDialog.show();
                        break;
                    }
                }
                if (k == 0) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//                    alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
                    alertDialog.setTitle("确定要提交吗？");
                    alertDialog.setCancelable(false);
                    alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            uploadTasks(toDoTask);
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

    /**
     * 上传巡查任务报告
     *
     * @param toDoTask 上传的任务
     */
    public void uploadTasks(final ToDoTask toDoTask) {
        Log.d("TAG", "全部检查完成，开始上传...");
        List<MyLatLng> myLatLngList = databaseManager.queryLatLng(toDoTask.getId());

        List<SubTask> subTaskList = databaseManager.querySubTasks(toDoTask.getId());

        List<List<Task>> taskLists = new ArrayList<List<Task>>();
        for (SubTask subTask : databaseManager.querySubTasks(toDoTask.getId())) {
            taskLists.add(databaseManager.queryTasks(subTask));
        }

        List<Photo> photoList = new ArrayList<Photo>();
        for (SubTask subtask : databaseManager.querySubTasks(toDoTask.getId())) {
            List<List<Photo>> photoLists = new ArrayList<List<Photo>>();
            for (Task task : databaseManager.queryTasks(subtask)) {
                for (Photo photo : databaseManager.queryPhoto(task)) {
                    photo.setPhotoId(photo.getPhotoId().substring(photo.getPhotoId().
                            indexOf("Inspection/")));
                    photoList.add(photo);
                }
            }
        }

        String latLngString = new Gson().toJson(myLatLngList);//LatLng
        String subTaskString = new Gson().toJson(subTaskList);//SubTask
        final String taskListString = new Gson().toJson(taskLists);//Task
        String photoString = new Gson().toJson(photoList);//Photo

        File file = new File(Environment.getExternalStorageDirectory() + "/Inspection/" +
                toDoTask.getId());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/Inspection/", toDoTask.getId() + ".zip");
        final String photoZip = file1.getAbsolutePath();
        try {
            ZipUtil.ZipFolder(file.getAbsolutePath(), photoZip);//PhotoZip
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String sumString = "{\"lat\":" + latLngString + ",\"photo\":" + photoString +
                ",\"task\":" + taskListString + ",\"sub\":" + subTaskString + "}";

        final ArrayList<String> photoZips = new ArrayList<String>();
        photoZips.add(photoZip);

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (httpUtil.upSumTasks(sumString)=="success"&&
                            httpUtil.upFiles(photoZips)=="success"){
                        Toast.makeText(context, "提交成功", Toast.LENGTH_LONG).show();
                        editor.remove(toDoTask.getId() + "").commit();//移除
                        ((ToDoTaskActivity) context).initListView();
                    } else {
                        Toast.makeText(context, "提交失败，请重新提交", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Log.d("TAG", "总 " + sumString);
        Log.d("TAG", "照片Zip " + photoZip);
    }
}