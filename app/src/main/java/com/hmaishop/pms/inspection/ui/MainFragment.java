package com.hmaishop.pms.inspection.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.util.CompressPicture;
import com.hmaishop.pms.inspection.util.Constants;

/**
 * 主Fragment，显示头像，巡查中...
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private GestureDetector gestureDetector;

    private View view;
    private ImageView icon;
    private MainActivity mainActivity;
    private LinearLayout showList;

    private SharedPreferences sharedPreferences;

    public MainFragment() {
        // Required empty public constructor
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);
        icon = (ImageView) view.findViewById(R.id.icon);
        showList = (LinearLayout) view.findViewById(R.id.show_list);

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
//                Toast.makeText(mainActivity, InsertLatLng.aLatLng.toString(), Toast.LENGTH_LONG).show();
//                Toast.makeText(mainActivity, new Gson().toJson(InsertLatLng.aLatLng), Toast.LENGTH_LONG).show();

            }
        });
        showList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.refreshActivity(Constants.SHOW_LIST);
            }
        });

        gestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        return view;
    }


    /**
     * 手势识别
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getY() - e2.getY() > 5) {
                mainActivity.refreshActivity(Constants.SHOW_LIST);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

}
