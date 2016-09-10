package com.hmaishop.pms.inspection.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.MyLatLng;
import com.hmaishop.pms.inspection.bean.Photo;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.bean.Task;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.service.LocationService;
import com.hmaishop.pms.inspection.util.CompressPicture;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;
import com.hmaishop.pms.inspection.util.ZipUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 主Fragment，显示头像，巡查中...
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private int toDoTaskId;
    private DatabaseManager databaseManager;

    private View view;
    private ImageView icon;
    private ImageButton upLoadTask;
    private MainActivity mainActivity;

    private ProgressDialog progressDialog;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public MainFragment() {
        // Required empty public constructor
    }

    public void setArguments(MainActivity mainActivity, int toDoTaskId, DatabaseManager databaseManager) {
        this.mainActivity = mainActivity;
        this.toDoTaskId = toDoTaskId;
        this.databaseManager = databaseManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        icon = (ImageView) view.findViewById(R.id.icon);
        upLoadTask = (ImageButton) view.findViewById(R.id.up_load_task);

        sharedPreferences = mainActivity.getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();

        /**
         * 设置头像，如果下载失败，则选一个默认头像
         */
        if (sharedPreferences.getString(Constants.CHECKER_ICON, "").equals("")) {
            icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            icon.setImageBitmap(CompressPicture.decodeSampledBitmapFromResource
                    (sharedPreferences.getString(Constants.CHECKER_ICON, ""), 100, 100));
        }

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.refreshActivity(Constants.SHOW_LIST);
            }
        });

        upLoadTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int k = 0;
                for (SubTask subTask : databaseManager.querySubTasks(toDoTaskId)) {
                    if (!subTask.isHaveDone()) {
                        k++;
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
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
                        alertDialog.setPositiveButton("开始巡查", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.putExtra(Constants.TODO_TASK_ID, toDoTaskId);
                                getContext().startActivity(intent);
                            }
                        });
                        alertDialog.show();
                        break;
                    }
                }
                if (k == 0) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
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
        });
        return view;
    }

    /**
     * 上传巡查任务报告
     * 并且关闭定位 Service
     *
     * @param toDoTaskId 上传的任务
     */
    public void uploadTasks(final int toDoTaskId) {
        Log.d("TAG", "全部检查完成，开始上传...");
        Intent intent = new Intent(getContext(), LocationService.class);
        getContext().stopService(intent);
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

//                    Message message2 = new Message();    // 刷新列表
//                    message2.arg1 = 2;
//                    handler.sendMessage(message2);
                } else {
                    Message message3 = new Message();    // 任务提交失败
                    message3.arg1 = 3;
                    handler.sendMessage(message3);
                }
            }
        }.start();

        Log.d("TAG", "总 " + sumString);
        Log.d("TAG", "照片Zip " + photoZip);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 0: // 开始上传
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("正在上传，请稍候...");
                    progressDialog.show();

                    break;
                case 1: // 任务提交成功
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("提交成功");
                    builder.setCancelable(true);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getActivity().finish();
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
                case 2: // 返回任务列表
                    getActivity().finish();
                    break;
                case 3: // 任务提交失败
                    Toast.makeText(getContext().getApplicationContext(), "提交失败，请重新提交", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

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
}
