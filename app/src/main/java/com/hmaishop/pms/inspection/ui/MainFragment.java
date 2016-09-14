package com.hmaishop.pms.inspection.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.util.CompressPicture;
import com.hmaishop.pms.inspection.util.Constants;

/**
 * 主Fragment，显示头像，巡查中...
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private View view;
    private ImageView icon;
    private Button upLoadTask;  // 检查部位
    private MainActivity mainActivity;

    private SharedPreferences sharedPreferences;

    public MainFragment() {
        // Required empty public constructor
    }

    public void setArguments(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        icon = (ImageView) view.findViewById(R.id.icon);
        upLoadTask = (Button) view.findViewById(R.id.up_load_task);

        sharedPreferences = mainActivity.getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);

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

            }
        });

        upLoadTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.refreshActivity(Constants.SHOW_LIST);
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.upload_task, menu);
    }
}
