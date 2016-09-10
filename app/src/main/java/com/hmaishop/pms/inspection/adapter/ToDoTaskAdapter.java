package com.hmaishop.pms.inspection.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.service.LocationService;
import com.hmaishop.pms.inspection.ui.MainActivity;
import com.hmaishop.pms.inspection.ui.ToDoTaskActivity;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;
import com.hmaishop.pms.inspection.util.ZipUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ToDoTask适配器
 * <p/>
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

    String[] items = new String[]{"步行","自行车","驾车"};

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
                    editor.putInt(Constants.locationInterval,6*1000).commit();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("确定要开始吗？");
                    alertDialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0:
                                    editor.putInt(Constants.locationInterval,6*1000).commit();
                                    Log.d("TAG","put..."+6);
                                    break;
                                case 1:
                                    editor.putInt(Constants.locationInterval,4*1000).commit();
                                    Log.d("TAG","put..."+4);
                                    break;
                                case 2:
                                    editor.putInt(Constants.locationInterval,2*1000).commit();
                                    Log.d("TAG","put..."+2);
                                    break;
                                default:
                                    editor.putInt(Constants.locationInterval,3*1000).commit();
                                    Log.d("TAG","put..."+3);
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

//        /**
//         * 任务提交处理
//         */
//        todoTaskSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int k = 0;
//                // 是否已经开始巡查
//                if (sharedPreferences.getBoolean(toDoTask.getId() + "", false)) {
//                    for (SubTask subTask : databaseManager.querySubTasks(toDoTask.getId())) {
//                        if (!subTask.isHaveDone()) {
//                            k++;
//                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//                            alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
//                            alertDialog.setTitle(" 警告！");
//                            alertDialog.setMessage("请全部检查完成后提交");
//                            alertDialog.setCancelable(false);
//                            alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    // Do something
//                                }
//                            });
//                            alertDialog.setPositiveButton("开始巡查", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    Intent intent = new Intent(context, MainActivity.class);
//                                    intent.putExtra(Constants.TODO_TASK_ID, toDoTask.getId());
//                                    context.startActivity(intent);
//                                }
//                            });
//                            alertDialog.show();
//                            break;
//                        }
//                    }
//                    if (k == 0) {
//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//                        alertDialog.setTitle("确定要提交吗？");
//                        alertDialog.setCancelable(false);
//                        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                uploadTasks(toDoTask);
//                            }
//                        });
//                        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        });
//                        alertDialog.show();
//                    }
//                } else {
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//                    alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
//                    alertDialog.setTitle(" 警告！");
//                    alertDialog.setMessage("您还没有开始巡查");
//                    alertDialog.setCancelable(true);
//                    alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    });
//                    alertDialog.show();
//                }
//            }
//        });
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

//    /**
//     * 上传巡查任务报告
//     * 并且关闭定位 Service
//     *
//     * @param toDoTask 上传的任务
////     */
//    public void uploadTasks(final ToDoTask toDoTask) {
//        Log.d("TAG", "全部检查完成，开始上传...");
//        Intent intent = new Intent(context, LocationService.class);
//        context.stopService(intent);
//        List<MyLatLng> myLatLngList = databaseManager.queryMyLatLng(toDoTask.getId());
//        Log.d("TAG", new Gson().toJson(myLatLngList));
//
//        List<SubTask> subTaskList = databaseManager.querySubTasks(toDoTask.getId());
//
//        List<List<Task>> taskLists = new ArrayList<List<Task>>();
//        for (SubTask subTask : subTaskList) {
//            taskLists.add(databaseManager.queryTasks(subTask));
//        }
//
//        List<Photo> photoList = new ArrayList<Photo>();
//
//        for (List<Task> taskList : taskLists) {
//            for (Task task : taskList) {
//                for (Photo photo : databaseManager.queryPhoto(task)) {
//                    photo.setPhotoId(photo.getPhotoId().substring(photo.getPhotoId().
//                            indexOf(photo.getId() + "/")));
//                    photoList.add(photo);
//                }
//            }
//        }
//
//        //
////        for (SubTask subtask : subTaskList) {
////            for (Task task : databaseManager.queryTasks(subtask)) {
////                for (Photo photo : databaseManager.queryPhoto(task)) {
////                    photo.setPhotoId(photo.getPhotoId().substring(photo.getPhotoId().
////                            indexOf(photo.getId() + "/")));
////                    photoList.add(photo);
////                }
////            }
////        }
//
//        String latLngString = new Gson().toJson(myLatLngList);//LatLng
//        String subTaskString = new Gson().toJson(subTaskList);//SubTask
//        final String taskListString = new Gson().toJson(taskLists);//Task
//        String photoString = new Gson().toJson(photoList);//Photo
//
//        final File file = new File(Environment.getExternalStorageDirectory() + "/Inspection/" +
//                toDoTask.getId());
//        if (!file.exists()) {
//            file.mkdir();
//        }
//        final File file1 = new File(Environment.getExternalStorageDirectory() + "/Inspection/", toDoTask.getId() + ".zip");
//        final String photoZip = file1.getAbsolutePath();
//        try {
//            ZipUtil.ZipFolder(file.getAbsolutePath(), photoZip);    // PhotoZip
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // 总的任务信息
//        final String sumString = "{\"lat\":" + latLngString + ",\"photo\":" + photoString +
//                ",\"task\":" + taskListString + ",\"sub\":" + subTaskString + "}";
//
//        final ArrayList<String> photoZips = new ArrayList<String>();
//        photoZips.add(photoZip);
//
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//
//                Message message0 = new Message();   // 开始上传任务
//                message0.arg1 = 0;
//                handler.sendMessage(message0);
//                /**
//                 * 上传任务
//                 */
//                String upSumTasks = httpUtil.upSumTasks(sumString);
//                String upFiles = httpUtil.upFiles(photoZips);
//
//                if (upSumTasks.equals("success") && upFiles.equals("success")) {
//
//                    file1.delete(); // 删除压缩包文件
//                    Message message1 = new Message();   // 任务提交成功，刷新任务列表
//                    message1.arg1 = 1;
//                    message1.arg2 = toDoTask.getId();
//                    message1.obj = file;
//                    handler.sendMessage(message1);
//
//                    Message message2 = new Message();    // 刷新列表
//                    message2.arg1 = 2;
//                    handler.sendMessage(message2);
//                } else {
//                    Message message3 = new Message();    // 任务提交失败
//                    message3.arg1 = 3;
//                    handler.sendMessage(message3);
//                }
//            }
//        }.start();
//
//        Log.d("TAG", "总 " + sumString);
//        Log.d("TAG", "照片Zip " + photoZip);
//    }
//
//
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.arg1) {
//                case 0: // 开始上传
//                    progressDialog = new ProgressDialog(context);
//                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                    progressDialog.setCancelable(false);
//                    progressDialog.setTitle("正在上传，请稍候...");
//                    progressDialog.show();
//
//                    break;
//                case 1: // 任务提交成功
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setTitle("提交成功");
//                    builder.setCancelable(true);
//                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                        }
//                    });
//                    builder.show();
//                    progressDialog.cancel();
//
//                    /**
//                     * 上传成功后删除本地缓存数据   处理异常
//                     */
//                    deleteRecursive((File) msg.obj);    // 删除文件夹
//                    databaseManager.deleteLatLng(msg.arg2);
//                    databaseManager.deletePhotos(msg.arg2);
//                    databaseManager.deleteTasks(msg.arg2);
//                    databaseManager.deleteSubTasks(msg.arg2);
//                    editor.remove(msg.arg2 + "").commit();  // 移除
//                    break;
//                case 2: // 刷新列表
//                    ((ToDoTaskActivity) context).initListView();
//                    break;
//                case 3: // 任务提交失败
//                    Toast.makeText(context.getApplicationContext(), "提交失败，请重新提交", Toast.LENGTH_LONG).show();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
//
//    /**
//     * 递归删除一个文件夹
//     *
//     * @param fileOrDirectory 要删除的文件夹
//     */
//    public void deleteRecursive(File fileOrDirectory) {
//        if (fileOrDirectory.isDirectory()) {
//            for (File child : fileOrDirectory.listFiles()) {
//                deleteRecursive(child);
//            }
//        } else {
//            fileOrDirectory.delete();
//        }
//    }
}