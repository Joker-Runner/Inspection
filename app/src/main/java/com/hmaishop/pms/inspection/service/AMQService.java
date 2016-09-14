package com.hmaishop.pms.inspection.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.google.gson.Gson;
import com.hmaishop.pms.inspection.R;
import com.hmaishop.pms.inspection.database.DatabaseManager;
import com.hmaishop.pms.inspection.ui.ToDoTaskActivity;
import com.hmaishop.pms.inspection.util.Constants;
import com.hmaishop.pms.inspection.util.HttpUtil;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * 通知推送服务
 */
public class AMQService extends Service {
    private static String TAG = "AMQService";
    //主机ip
    private static String HOST = Constants.IP;
    //端口号
    private static int PORT = 1883;
    //连接用户名
    private static String USER = "";
    //连接密码
    private static String PASSWORD = "";
    private static short KEEP_ALIVE_TIME = 60;
    private static String DISPATCH_QUEUE = "Inspection";
    private static String ACTION_RECEIVE = "AMQ";
    private static String ACTION_KEEP_ALIVE = "KEEP_ALIVE";
    //心跳频率
    private static int KEEP_ALIVE_SPEED = 1000 * 30;
    //mqtt 客户端
    private MQTT mqtt;
    //客户端ID
    private String ClientID;
    //回调方式的连接
    private CallbackConnection connection;
    private BroadcastReceiver receiver;
    private static IntentFilter filter;
    //连接 标识
    private boolean mStart = false;
    private Context context = this;

    private int checkerId;

    NotificationManager mNManager;
    Notification notification;

    SharedPreferences sharedPreferences;
    HttpUtil httpUtil;

    DatabaseManager databaseManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(Constants.SHARED, Context.MODE_APPEND);
        databaseManager = new DatabaseManager(this);
        ClientID = getClientID();
        mqtt = new MQTT();
        try {
            //设置主机
            mqtt.setHost(HOST, PORT);
            //设置客户端标识ID
            mqtt.setClientId(ClientID);
            //mqtt.setKeepAlive((short)60);
            //mqtt.setUserName(USER);
            //mqtt.setPassword(PASSWORD);
            //清除会话session
            mqtt.setCleanSession(false);
            //指定相同的队列
            mqtt.setDispatchQueue(Dispatch.createQueue(DISPATCH_QUEUE));
            //获得连接对象
            connection = mqtt.callbackConnection();
            //绑定监听事件
            connection.listener(new Listener() {
                @Override
                //连接成功
                public void onConnected() {
                    Log.i(TAG, "onConnected()");
                    mStart = true;
                }

                @Override
                //连接断开
                public void onDisconnected() {
                    Log.i(TAG, "onDisconnected()");
                    mStart = false;
                    //reConnect();
                }

                @Override
                //收到消息
                public void onPublish(UTF8Buffer utf8Buffer, Buffer buffer, Runnable runnable) {
                    Log.i(TAG, "onPublish()");
                    String msg = buffer.utf8().toString();

                    try {
                        JSONObject jsonObject = new JSONObject(msg);
                        switch (jsonObject.getString("tag")) {
                            case "1":   // 受到新任务
                                // 发通知
                                notification(jsonObject.getString("content"));
                                // 发广播
                                Intent intent = new Intent();
                                intent.putExtra("msg", "收到新任务");
                                intent.setAction("Broadcast_NewTask");
                                sendBroadcast(intent);
                                break;
                            case "2":   // 获取位置坐标

                                /**
                                 * latLng : 定位成功
                                 * null : 定位服务没有开启（不在巡查任务/服务出错）
                                 */

                                LatLng latLng = databaseManager.queryTopLatLng();
                                String upLatLng;
                                if (latLng == null) {
                                    upLatLng = "{\"user\":" + sharedPreferences.getInt
                                            (Constants.CHECKER_ID, -1) + ",\"tag\":\"null\"}";
                                } else {
                                    upLatLng = new Gson().toJson(latLng);
                                    upLatLng = "{\"user\":" + sharedPreferences.getInt
                                            (Constants.CHECKER_ID, -1) + ",\"tag\":\"lat_lng\"," + upLatLng.substring(1);
                                }

                                upLoadAMQ(jsonObject.getString("user"), upLatLng);
                                Log.d("TAG", "up " + upLatLng);
                                break;
                            default:
                                Log.d("TAG", "TAG不匹配");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        upLoadAMQ("Inspection", e.toString());
                    }

                    Log.i(TAG, msg);
                    //to do something

//                        Intent intent = new Intent(ACTION_RECEIVE);
//                        Bundle bundle = new Bundle();
//                        bundle.putString("msg", msg);
//                        intent.putExtras(bundle);
//                        if(Build.VERSION.SDK_INT>=12) intent.setFlags(32);
//                        sendBroadcast(intent);

                    runnable.run();
                }

                @Override
                //出错
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    mStart = false;
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        receiver = new Rec();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        mNManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        httpUtil = new HttpUtil(Constants.IP, 3000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if(intent.getAction().equals(ACTION_KEEP_ALIVE)){
//            KeepAlive();
//        }

        if (intent == null){
            checkerId = sharedPreferences.getInt(Constants.CHECKER_ID,-1);
        } else {
            checkerId = intent.getIntExtra("checkerId", -1);
        }

        start();

        registerReceiver(receiver, filter);
        flags = 1;
        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    private void start() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                connect();
                //KeepAlive();
                synchronized (AMQService.class) {
                    while (true)
                        try {
                            Log.i(TAG, "wait()");
                            AMQService.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            }
        }.start();
    }

    private void connect() {
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Topic[] topics = {new Topic("In", QoS.AT_LEAST_ONCE),
                        new Topic(checkerId + "", QoS.AT_LEAST_ONCE)};
                //.......
                connection.subscribe(topics, new Callback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.i(TAG, "subscribe success," + new String(bytes));
                        mStart = true;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.i(TAG, "subscribe onFailure," + throwable.toString());
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.i(TAG, "subscribe onFailure," + throwable.toString());
                throwable.printStackTrace();
            }
        });
    }

    private void reConnect() {
        if (isNetAvailable() && !mStart) {
            Log.i(TAG, "reConnect");
            connect();
        }
    }

    private boolean isNetAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return true;
        }
        return false;
    }

    private String getClientID() {
        return Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    /**
     * 接受推送通知时的回掉函数
     *
     * @param newTask 一个标签（新的临时任务/获取当前的位置信息/...）
     */
    private void notification(String newTask) {
        Intent intent = new Intent(context, ToDoTaskActivity.class);
        intent.putExtra("checkerId", sharedPreferences.getInt(Constants.CHECKER_ID, -1));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle("new message")
                    .setContentText("收到一个新的临时任务")
                    //.setSubText("msg1")
                    .setTicker("new msg")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            notification = builder.build();
            Log.i(TAG, "" + Build.VERSION_CODES.JELLY_BEAN);
        } else {
            notification = new Notification();
            notification.describeContents();
            notification.defaults = Notification.FLAG_AUTO_CANCEL;
            notification.when = System.currentTimeMillis();
            notification.tickerText = "收到一个新的临时任务";
            Log.i(TAG, "else");
        }
        mNManager.notify(1, notification);
    }

    public void upLoadAMQ(String user, String context) {
//        Log.d("TAG", context);
        connection.publish(user, context.getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    @Override
    public void onDestroy() {
        connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mStart = false;
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
        unregisterReceiver(receiver);
//        Intent intent = new Intent(this,AMQService.class);
//        startService(intent);
        super.onDestroy();
    }
}
