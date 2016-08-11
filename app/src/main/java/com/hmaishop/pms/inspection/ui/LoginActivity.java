package com.hmaishop.pms.inspection.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.Checker;
import com.hmaishop.pms.inspection.service.AMQService;
import com.hmaishop.pms.inspection.util.ActivityCollector;
import com.hmaishop.pms.inspection.util.BaseActivity;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * 登录Activity
 */
public class LoginActivity extends BaseActivity implements Serializable {

    String deviceId;
    HttpUtil httpUtil;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        getWindow().setBackgroundDrawableResource(R.color.colorWhite);
        setContentView(R.layout.activity_login);

        deviceId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        Log.d("TAG", "" + deviceId);
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();

        /**
         * 登录链接服务器，获取用户信息
         */
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    httpUtil = new HttpUtil(Constants.IP, 3000);
                    String checkerString = httpUtil.login(deviceId);
                    Thread.sleep(1000);
                    initLogin(checkerString);
                    startToDoTaskActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 初始化用户信息
     *
     * @param checkerString 用户信息Json字符串
     */
    public void initLogin(String checkerString) {
        Checker[] checkers = new Gson().
                fromJson(checkerString, Checker[].class);
        if (checkers != null) {

            Checker checker = checkers[0];
            editor.putString(Constants.CHECKER_NAME, checker.getCheckerName()).commit();
            editor.putInt(Constants.CHECKER_ID, checker.getCheckerId()).commit();
            File icon = new File(Environment.getExternalStorageDirectory() + "/Inspection/Cache/icon/");
            if (!icon.exists()) {
                icon.mkdirs();
            }
            String iconString = new String();
            Log.d("TAG", checker.getCheckerIcon());
            //下载头像到指定文件夹
            iconString = HttpUtil.downloadBitmap(checker.getCheckerIcon(), icon.getAbsolutePath());
            Log.d("TAG", "头像地址" + iconString);
            editor.putString(Constants.CHECKER_ICON, iconString).commit();

            Intent intent = new Intent(LoginActivity.this, AMQService.class);
            intent.putExtra("checkerId", checker.getCheckerId());
            startService(intent);
            Log.d("TAG", String.valueOf(sharedPreferences.getInt(Constants.CHECKER_ID, -1)));
        }
    }

    private void startToDoTaskActivity() {
        Intent intent = new Intent(this, ToDoTaskActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCollector.finishAll();
    }
}

