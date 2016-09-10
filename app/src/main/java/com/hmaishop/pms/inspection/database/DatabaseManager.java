package com.hmaishop.pms.inspection.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.google.gson.Gson;
import com.hmaishop.pms.inspection.bean.MyLatLng;
import com.hmaishop.pms.inspection.bean.Photo;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.bean.Task;
import com.hmaishop.pms.inspection.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理类
 * <p>
 * Created by Joker_Runner on 7/22 0022.
 */
public class DatabaseManager {
    private SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase database;

    /**
     * 创建数据库，并得到SQLiteDatabase对象
     *
     * @param context 传入上下文对象
     */
    public DatabaseManager(Context context) {
        sqLiteOpenHelper = new MyDatabaseHelper(context, Constants.DATE_BASE, null, 1);
        database = sqLiteOpenHelper.getWritableDatabase();
    }

    /**
     * 插入定位坐标
     *
     * @param todo_task_id 归属的 ToDoTask ID
     * @param latLng       位置坐标
     */
    public void insertLatLng(int todo_task_id, String time, LatLng latLng) {
        ContentValues values = new ContentValues();
        values.put("todo_task_id", todo_task_id);
        values.put("lat_lng_time", time);
        values.put("latitude", latLng.latitude);
        values.put("longitude", latLng.longitude);
        database.insert(Constants.LAT_LNG_TABLE, null, values);
        Log.d("DATA", "InsertLatLng...");
    }

    /**
     * 查询指定 ToDoTask 的定位坐标列表  LatLng  绘轨迹
     *
     * @param todo_task_id ToDoTask ID
     * @return 位置坐标链表
     */
    public List<LatLng> queryLatLng(int todo_task_id) {
        List<LatLng> latLngList = new ArrayList<>();
        Cursor cursor = database.query(Constants.LAT_LNG_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            LatLng latLng;
            if (cursor.getInt(cursor.getColumnIndex("todo_task_id")) == todo_task_id) {
                latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex("latitude")),
                        cursor.getDouble(cursor.getColumnIndex("longitude")));
                latLngList.add(latLng);
            }
        }
        cursor.close();
        Log.d("DATA", "queryLatLng...");
        return latLngList;
    }

    /**
     * 查询指定 ToDoTask 的定位坐标列表  MyLatLng 上报
     *
     * @param todo_task_id ToDoTask ID
     * @return 位置坐标链表
     */
    public List<MyLatLng> queryMyLatLng(int todo_task_id) {
        List<MyLatLng> myLatLngList = new ArrayList<>();
        Cursor cursor = database.query(Constants.LAT_LNG_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            MyLatLng myLatLng;
            if (cursor.getInt(cursor.getColumnIndex("todo_task_id")) == todo_task_id) {
                myLatLng = new MyLatLng(todo_task_id, cursor.getString(cursor.getColumnIndex("lat_lng_time")),
                        cursor.getDouble(cursor.getColumnIndex("latitude")),
                        cursor.getDouble(cursor.getColumnIndex("longitude")));
                myLatLngList.add(myLatLng);
            }
        }
        cursor.close();
        Log.d("DATA", "queryMyLatLng...");
        return myLatLngList;
    }

    /**
     * 查询最近的一条定位坐标
     * @return
     */
    public LatLng queryTopLatLng(){
        LatLng latLng;
        Cursor cursor = database.rawQuery("select * from lat_lng order by id asc",null);
        Log.d("DATA", "queryTopLatLng...");
        if(cursor.getCount()>=1){
            cursor.moveToLast();
            latLng =  new LatLng(cursor.getDouble(cursor.getColumnIndex("latitude")),
                    cursor.getDouble(cursor.getColumnIndex("longitude")));
            cursor.close();
            return latLng;
        }else {
            cursor.close();
            return null;
        }
    }

    /**
     * 提交成功后删除 定位坐标数据
     *
     * @param todo_task_id 删除的任务 ID
     */
    public void deleteLatLng(int todo_task_id) {
        database.delete(Constants.LAT_LNG_TABLE, "todo_task_id = ?", new String[]{"" + todo_task_id});
    }

    /**
     * 插入照片
     *
     * @param photo 要插入的照片
     */
    public void insertPhoto(Photo photo) {
        ContentValues values = new ContentValues();
        values.put("todo_task_id", photo.getId());
        values.put("sub_task_id", photo.getSubTaskId());
        values.put("task_id", photo.getTaskId());
        values.put("photo_id", photo.getPhotoId());
        database.insert(Constants.PHOTO_TABLE, null, values);
        Log.d("DATA", "insertPhoto...");
    }

    /**
     * 查询照片
     *
     * @param task 要查询的照片的 Task
     * @return 要查询的照片链表
     */
    public List<Photo> queryPhoto(Task task) {
        List<Photo> photoList = new ArrayList<>();
        Cursor cursor = database.query(Constants.PHOTO_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Photo photo;
            if (cursor.getInt(cursor.getColumnIndex("todo_task_id")) == task.getId() &&
                    cursor.getInt(cursor.getColumnIndex("sub_task_id")) == task.getSubTaskId() &&
                    cursor.getInt(cursor.getColumnIndex("task_id")) == task.getTaskId()) {
                photo = new Photo();
                photo.setId(cursor.getInt(cursor.getColumnIndex("todo_task_id")));
                photo.setSubTaskId(cursor.getInt(cursor.getColumnIndex("sub_task_id")));
                photo.setTaskId(cursor.getInt(cursor.getColumnIndex("task_id")));
                photo.setPhotoId(cursor.getString(cursor.getColumnIndex("photo_id")));
                photoList.add(photo);
            }
        }
        cursor.close();
        Log.d("DATA", "queryPhoto...");
        return photoList;
    }

    /**
     * 删除 Photo
     *
     * @param photo 要删除的 Photo
     */
    public void deletePhoto(Photo photo) {
        database.delete(Constants.PHOTO_TABLE, "todo_task_id = ? and sub_task_id = ? and task_id = ?" +
                " and photo_id = ?", new String[]{"" + photo.getId(), "" + photo.getSubTaskId(),
                "" + photo.getTaskId(), "" + photo.getPhotoId()});
    }

    /**
     * 删除 Photos
     *
     * @param todo_task_id 删除的任务 ID
     */
    public void deletePhotos(int todo_task_id) {
        database.delete(Constants.PHOTO_TABLE, "todo_task_id = ?", new String[]{"" + todo_task_id});
    }

    /**
     * 插入 Task
     *
     * @param task 要插入的 Task
     */
    public void insertTask(Task task) {
        ContentValues values = new ContentValues();
        values.put("todo_task_id", task.getId());
        values.put("sub_task_id", task.getSubTaskId());
        values.put("task_id", task.getTaskId());
        values.put("task_title", task.getTaskTitle());
        values.put("have_problem", task.isHaveProblem() ? 1 : 0);
        database.insert(Constants.TASK_TABLE, null, values);
        Log.d("DATA", "insertTask..." + new Gson().toJson(task));
    }

    /**
     * 更新 Task
     *
     * @param task 要插入的 Task
     */
    public void updateTask(Task task) {
        ContentValues values = new ContentValues();
        values.put("todo_task_id", task.getId());
        values.put("sub_task_id", task.getSubTaskId());
        values.put("task_id", task.getTaskId());
        values.put("task_title", task.getTaskTitle());
        values.put("have_problem", task.isHaveProblem() ? 1 : 0);
        database.update(Constants.TASK_TABLE, values, "todo_task_id = ? and sub_task_id = ? and task_id = ?",
                new String[]{"" + task.getId(), "" + task.getSubTaskId(), "" + task.getTaskId()});
        Log.d("DATA", "updateTask ..." + task.getTaskId());
    }

    /**
     * 查询 Task
     *
     * @param subTask 要查询的 Task 的 SubTask
     * @return 查询得到的 Task 链表
     */
    public List<Task> queryTasks(SubTask subTask) {
        List<Task> taskList = new ArrayList<>();
        Cursor cursor = database.query(Constants.TASK_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Task task;
            if (cursor.getInt(cursor.getColumnIndex("todo_task_id")) == subTask.getId() &&
                    cursor.getInt(cursor.getColumnIndex("sub_task_id")) == subTask.getSubTaskId()) {
                task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex("todo_task_id")));
                task.setSubTaskId(cursor.getInt(cursor.getColumnIndex("sub_task_id")));
                task.setTaskId(cursor.getInt(cursor.getColumnIndex("task_id")));
                task.setTaskTitle(cursor.getString(cursor.getColumnIndex("task_title")));
                task.setHaveProblem(cursor.getInt(cursor.getColumnIndex("have_problem")) == 1);
                taskList.add(task);
            }
        }
        cursor.close();
        Log.d("DATA", "queryTasks...");
        Log.d("DATA", new Gson().toJson(taskList));
        return taskList;
    }

    /**
     * 删除 Tasks
     *
     * @param todo_task_id 删除的任务 ID
     */
    public void deleteTasks(int todo_task_id) {
        database.delete(Constants.TASK_TABLE, "todo_task_id = ?", new String[]{"" + todo_task_id});
    }

    /**
     * 插入 SubTask 及其问题描述
     *
     * @param subTask SubTask
     */
    public void insertSubTask(SubTask subTask) {
        ContentValues values = new ContentValues();
        values.put("todo_task_id", subTask.getId());
        values.put("sub_task_id", subTask.getSubTaskId());
        values.put("sub_task_title", subTask.getSubTaskTitle());
        values.put("have_done", subTask.isHaveDone() ? 1 : 0);
        values.put("latitude", subTask.getLatitude());
        values.put("longitude", subTask.getLongitude());
        values.put("remark", subTask.getRemark());
        database.insert(Constants.SUB_TASK_TABLE, null, values);
        Log.d("DATA", "insertSubTask...");
    }

    /**
     * 更新SubTask
     *
     * @param subTask SubTask
     * @param remark  问题描述
     */
    public void updateSubTask(SubTask subTask, LatLng latLng, String remark) {
        ContentValues values = new ContentValues();
        values.put("todo_task_id", subTask.getId());
        values.put("sub_task_id", subTask.getSubTaskId());
        values.put("sub_task_title", subTask.getSubTaskTitle());
        values.put("have_done", subTask.isHaveDone() ? 1 : 0);
        values.put("latitude", latLng.latitude);
        values.put("longitude", latLng.longitude);
        values.put("remark", remark);
        database.update(Constants.SUB_TASK_TABLE, values, "todo_task_id = ? and sub_task_id = ?",
                new String[]{"" + subTask.getId(), "" + subTask.getSubTaskId()});
        Log.d("DATA", "updateSubTask...");
    }

    /**
     * 查询 SubTask
     *
     * @param todoTaskId 要查询的 SubTask 归属的ToDoTask的ID
     * @return 查询的道德 SubTask 的链表
     */
    public List<SubTask> querySubTasks(int todoTaskId) {
        List<SubTask> subTaskList = new ArrayList<>();
        Cursor cursor = database.query(Constants.SUB_TASK_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            SubTask subTask;
            if (cursor.getInt(cursor.getColumnIndex("todo_task_id")) == todoTaskId) {
                subTask = new SubTask();
                subTask.setId(cursor.getInt(cursor.getColumnIndex("todo_task_id")));
                subTask.setSubTaskId(cursor.getInt(cursor.getColumnIndex("sub_task_id")));
                subTask.setSubTaskTitle(cursor.getString(cursor.getColumnIndex("sub_task_title")));
                subTask.setHaveDone(cursor.getInt(cursor.getColumnIndex("have_done")) == 1);
                subTask.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
                subTask.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
                subTask.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
                subTaskList.add(subTask);
            }
        }
        cursor.close();
        Log.d("DATA", "querySubTasks...");
        return subTaskList;
    }

    /**
     * 删除 SubTasks
     *
     * @param todo_task_id 删除的任务 ID
     */
    public void deleteSubTasks(int todo_task_id) {
        database.delete(Constants.SUB_TASK_TABLE, "todo_task_id = ?", new String[]{"" + todo_task_id});
    }

    /**
     * 关闭数据库
     */
    public void closeDatabase() {
        database.close();
    }
}
