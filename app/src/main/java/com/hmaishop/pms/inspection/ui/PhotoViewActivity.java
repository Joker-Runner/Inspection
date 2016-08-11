package com.hmaishop.pms.inspection.ui;

import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.bean.Photo;
import com.hmaishop.pms.inspection.adapter.PhotoViewAdapter;
import com.hmaishop.pms.inspection.bean.Task;

import java.io.File;
import java.util.List;

/**
 * 照片墙Activity
 */
public class PhotoViewActivity extends AppCompatActivity {

    GridView photoWallView;
    Task task;
    List<Photo> photoList;  //照片列表

    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoWallView = (GridView) findViewById(R.id.photo_wall);
        databaseManager = new DatabaseManager(this);
        registerForContextMenu(photoWallView);

        task = (Task) getIntent().getSerializableExtra("Task");
        setTitle("查看照片");
        initPhotoWall(task);
    }

    /**
     * 初始化照片墙
     * @param task 初始化 task 的照片
     */
    public void initPhotoWall(Task task) {
        photoList = databaseManager.queryPhoto(task);
        String[] imagePaths = new String[photoList.size()];
        for (int i = 0; i < photoList.size(); i++) {
            imagePaths[i] = photoList.get(i).getPhotoId();
        }
        PhotoViewAdapter photoViewAdapter = new PhotoViewAdapter(this, 0, imagePaths, photoWallView);
        photoWallView.setAdapter(photoViewAdapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderIcon(R.mipmap.ic_launcher);
        menu.setHeaderTitle("选择操作");
        menu.add(1, 1, 1, "删除");
//        menu.add(1,2,1,"预览");

//        MenuInflater menuInflater =new MenuInflater(this);
//        menuInflater.inflate(R.menu.menu_context,menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.
                AdapterContextMenuInfo) item.getMenuInfo();
        final Photo photo = photoList.get(menuInfo.position);
        final File tempFile = new File(Environment.getExternalStorageDirectory() + "/Inspection/Cache/temp");
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        switch (item.getItemId()) {
            case 1:
                //点击删除后，并不直接将其删除，而是将其移入到一个temp文件夹中以备撤回删除操作
                databaseManager.deletePhoto(photo);
                moveFile(photo.getPhotoId(), tempFile.getAbsolutePath());
                Snackbar.make(photoWallView, "data deleted", Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                databaseManager.insertPhoto(photo);
                                File tempImage = new File(tempFile, photo.getPhotoId().
                                        substring(photo.getPhotoId().lastIndexOf("/")));
                                File file = new File(Environment.getExternalStorageDirectory() +
                                        "/Inspection/" + task.getId() + "/" + task.getSubTaskId());
                                moveFile(tempImage.getAbsolutePath(), file.getAbsolutePath());
                                initPhotoWall(task);    //撤回时将照片放回原来的位置，并且刷新列表
                            }
                        }).show();
                initPhotoWall(task);
                break;
            case 2:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 移动文件
     *
     * @param srcFileName 源文件完整路径
     * @param destDirName 目的目录完整路径
     * @return 文件移动成功返回true，否则返回false
     */
    public boolean moveFile(String srcFileName, String destDirName) {

        File srcFile = new File(srcFileName);
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        File destDir = new File(destDirName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return srcFile.renameTo(new File(destDirName + File.separator + srcFile.getName()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.closeDatabase();
    }
}
