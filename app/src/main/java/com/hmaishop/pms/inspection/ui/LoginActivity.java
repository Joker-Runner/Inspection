package com.hmaishop.pms.inspection.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.bean.Checker;
import com.hmaishop.pms.inspection.service.AMQService;
import com.hmaishop.pms.inspection.util.ActivityCollector;
import com.hmaishop.pms.inspection.util.BaseActivity;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import java.io.File;
import java.io.Serializable;

/**
 * 登录Activity
 */
public class LoginActivity extends BaseActivity implements Serializable {

    String deviceId;
    HttpUtil httpUtil;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(R.style.AppTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawableResource(R.drawable.login);
        setContentView(R.layout.activity_login);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);

        deviceId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        Log.d("TAG", "" + deviceId);
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        editor = sharedPreferences.edit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 登录链接服务器，获取用户信息
         */
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1000);
                    httpUtil = new HttpUtil(Constants.IP, 3000);
                    String checkerString = httpUtil.login(deviceId);
                    Log.d("TAG", checkerString);
                    if (checkerString.equals(Constants.HTTP_EXCEPTION) || checkerString.equals("fail")) {
                        Log.d("TAG", "登陆失败...");
                        Message message = new Message();
                        message.arg1 = 1;
                        handler.sendMessage(message);
                    } else {
                        initLogin(checkerString);
                    }
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
        if (checkers.length >= 1) {
            final Checker checker = checkers[0];
            editor.putString(Constants.CHECKER_NAME, checker.getCheckerName()).commit();
            editor.putInt(Constants.CHECKER_ID, checker.getCheckerId()).commit();
            File icon = new File(Environment.getExternalStorageDirectory() + "/Inspection/Cache/icon/");
            if (!icon.exists()) {
                icon.mkdirs();
            }
            String iconString;
            // 下载头像到指定文件夹
            httpUtil = new HttpUtil(Constants.IP, 3000);
            iconString = httpUtil.downloadBitmap(checker.getCheckerIcon(), icon.getAbsolutePath());
            Log.d("TAG", "头像地址" + iconString);
            editor.putString(Constants.CHECKER_ICON, iconString).commit();

            /**
             * 开启通知服务
             */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LoginActivity.this, AMQService.class);
                    intent.putExtra("checkerId", checker.getCheckerId());
                    startService(intent);
                }
            }).start();
            Log.d("TAG", String.valueOf(sharedPreferences.getInt(Constants.CHECKER_ID, -1)));
            startToDoTaskActivity();
        } else {
            Message message = new Message();
            message.arg1 = 1;
            handler.sendMessage(message);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 1:
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setIcon(R.drawable.ic_warning_black_24dp);
                    builder.setTitle("登录失败");
                    builder.setMessage(" 请重新登陆或联系管理人员");
                    builder.setCancelable(false);
                    builder.setPositiveButton("重新登陆", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onResume();
                        }
                    });

                    builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.create();
                    builder.show();
                    break;
            }
        }
    };

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

