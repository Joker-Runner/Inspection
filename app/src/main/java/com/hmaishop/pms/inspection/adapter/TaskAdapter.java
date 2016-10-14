package com.hmaishop.pms.inspection.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.Task;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.ui.PhotoViewActivity;
import com.hmaishop.pms.inspection.ui.ReportActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Task适配器
 * <p>
 * Created by Joker_Runner on 7/13 0013.
 */
public class TaskAdapter extends BaseAdapter {

    private int resource;
    private Context context;
    private List<Task> taskList;
    private LayoutInflater inflater;
    private DatabaseManager databaseManager;

    String pathTakePhoto;
    Uri imageUri;

    /**
     * 刷新 ListView 时的列表传递
     *
     * @param taskList
     */
    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public TaskAdapter(Context context, List<Task> taskList, int resource, DatabaseManager databaseManager) {
        this.context = context;
        this.taskList = taskList;
        this.resource = resource;
        this.databaseManager = databaseManager;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int i) {
        return taskList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * 解决ToggleButton状态混乱的方法，饮鸩止渴
     *
     * @return
     */
    @Override
    public int getViewTypeCount() {
        if (getCount() < 1) {
            return 1;
        } else {
            return getCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(resource, null);
            viewHolder.taskTitle = (MyTextView) view.findViewById(R.id.task_title);
            viewHolder.haveProblem = (ToggleButton) view.findViewById(R.id.have_problem);
            viewHolder.imageSet = (ImageButton) view.findViewById(R.id.image_set);
            viewHolder.addImage = (ImageButton) view.findViewById(R.id.add_image);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final Task task = taskList.get(i);

        /**
         * 初始化view Item
         */
        viewHolder.taskTitle.setText(task.getTaskTitle());
        viewHolder.haveProblem.setChecked(task.isHaveProblem());
        task.setPictureNum(databaseManager.queryPhoto(task).size());
        databaseManager.updateTask(task);
        switch (databaseManager.queryPhoto(task).size()) {
            case 0:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_none_black_24dp);
                break;
            case 1:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_1_black_24dp);
                break;
            case 2:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_2_black_24dp);
                break;
            case 3:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_3_black_24dp);
                break;
            case 4:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_4_black_24dp);
                break;
            case 5:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_5_black_24dp);
                break;
            case 6:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_6_black_24dp);
                break;
            case 7:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_7_black_24dp);
                break;
            case 8:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_8_black_24dp);
                break;
            case 9:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_9_black_24dp);
                break;
            default:
                viewHolder.imageSet.setBackgroundResource(R.drawable.ic_filter_9_plus_black_24dp);
                break;
        }

        viewHolder.haveProblem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                viewHolder.haveProblem.setChecked(b);
                task.setHaveProblem(!task.isHaveProblem());
                databaseManager.updateTask(task);
            }
        });

        /**
         * 查看照片
         */
        viewHolder.imageSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PhotoViewActivity.class);
                intent.putExtra("Task", task);
                context.startActivity(intent);
            }
        });

        viewHolder.imageSet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context,"照片共 "+task.getPictureNum()+" 张",Toast.LENGTH_LONG).show();
                return true;
            }
        });

        /**
         * 拍照添加图片
         */
        viewHolder.addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //保存拍摄照片的temp文件目录
                File file = new File(Environment.getExternalStorageDirectory() + "/Inspection/Cache/temp");
                if (!file.exists()) {
                    file.mkdirs();
                }
                File outputImage = new File(file, "temp.jpg");
                pathTakePhoto = outputImage.toString();
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                ((ReportActivity) context).startActivityForResult(intent, i);

            }
        });
        return view;
    }

    class ViewHolder {
        MyTextView taskTitle;
        ToggleButton haveProblem;
        ImageButton imageSet;
        ImageButton addImage;
    }

}
