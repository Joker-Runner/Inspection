package com.hmaishop.pms.inspection.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.SubTask;
import com.hmaishop.pms.inspection.ui.ReportActivity;

import java.util.List;

/**
 * SubTask适配器
 * <p>
 * Created by Joker_Runner on 7/21 0021.
 */
public class SubTaskAdapter extends BaseAdapter {

    private int resource;
    private Context context;
    private List<SubTask> subTaskList;
    private LayoutInflater inflater;

    private TextView subTaskText;

    public SubTaskAdapter(Context context, List<SubTask> subTaskList, int resource) {
        this.context = context;
        this.subTaskList = subTaskList;
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return subTaskList.size();
    }

    @Override
    public Object getItem(int i) {
        return subTaskList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(resource, null);
        }
        final SubTask subTask = subTaskList.get(i);

        subTaskText = (TextView) view.findViewById(R.id.sub_task_text);
        subTaskText.setText(subTask.getSubTaskTitle());
        if (subTask.isHaveDone()) {
            subTaskText.setTextColor(context.getResources().getColor(R.color.colorLightGray));
        } else {
            subTaskText.setTextColor(context.getResources().getColor(R.color.colorGray));
        }

        subTaskText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReportActivity.class);
                Bundle bundle = new Bundle();
                Log.d("TAG", "id  " + subTask.getId());
                Log.d("TAG", "subId  " + subTask.getSubTaskId());
                bundle.putSerializable("subTask", subTask);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        return view;
    }
}
