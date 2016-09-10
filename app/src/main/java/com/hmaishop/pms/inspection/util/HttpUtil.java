package com.hmaishop.pms.inspection.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * http通信
 */
public class HttpUtil {
    //服务器地址
    private String mServerPath;
    //连接url对象
    private URL mUrl;
    //连接资源
    private HttpURLConnection mConn;
    //输出流对象
    private OutputStream mOut;
    //输入流对象
    private InputStream mIn;

    public HttpUtil(String host, int port) {
        mServerPath = "http://" + host + ":" + port;
    }

    /**
     * 登录
     *
     * @param meid //
     * @return 服务端返回值
     */
    public String login(String meid) {
        String jsonMsg = "{\"MEID\":\"" + meid + "\",\"device\":\"android\"}";
        try {
            mUrl = new URL(mServerPath + "/checkerinfo");
            mConn = (HttpURLConnection) mUrl.openConnection();
            //设置请求头信息
            setConnectConf();
            //发送数据
            int code = sendmsg(jsonMsg);
            //读取反馈数据
            if (code == 200) {
                return readResponse();
            } else {
                return Constants.HTTP_EXCEPTION;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        }
    }

    /**
     * 获取待办任务
     *
     * @param userId 用户ID
     * @return //
     */
    public String getMission(int userId) {
        String jsonMsg = "{\"num\":\"" + userId + "\"}";
        try {
            mUrl = new URL(mServerPath + "/mission");
            mConn = (HttpURLConnection) mUrl.openConnection();

            setConnectConf();
            int code = sendmsg(jsonMsg);
            if (code == 200) {
                return readResponse();
            } else {
                return Constants.HTTP_EXCEPTION;
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        }
    }

    /**
     * 获取任务子项
     *
     * @param missId
     * @return
     * @throws IOException
     */
    public String getSubkey(int missId) {
        String jsonMsg = "{\"mid\":\"" + missId + "\"}";
        try {
            mUrl = new URL(mServerPath + "/subkey");
            mConn = (HttpURLConnection) mUrl.openConnection();

            setConnectConf();
            int code = sendmsg(jsonMsg);
            if (code == 200) {
                return readResponse();
            } else {
                return Constants.HTTP_EXCEPTION;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        }
    }

    /**
     * 获取子项详情
     *
     * @param subId
     * @return
     * @throws IOException
     */
    public String getDetail(int todoTaskId, int subId) {
        String jsonMsg = "{\"did\":\"" + subId + "\",\"mid\":\"" + todoTaskId + "\"}";
        try {
            mUrl = new URL(mServerPath + "/detail");
            mConn = (HttpURLConnection) mUrl.openConnection();
            setConnectConf();
            int code = sendmsg(jsonMsg);
            if (code == 200) {
                return readResponse();
            }else {
                return Constants.HTTP_EXCEPTION;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        }
    }

    /**
     * 开始巡查后，给服务端发送通知
     *
     * @param todoTaskId 开始巡查的任务ID
     * @return
     */
    public String start(int todoTaskId) {
        String jsonMsg = todoTaskId + "";
        try {
            mUrl = new URL(mServerPath + "/start");
            mConn = (HttpURLConnection) mUrl.openConnection();

            setConnectConf();
            int code = sendmsg(jsonMsg);
            if (code == 200) {
                return readResponse();
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送任务报告
     *
     * @param string 发送的任务报告Json串
     * @return 网络请求返回码
     */
    public String upSumTasks(String string) {
        String jsonMsg = string;
        try {
            mUrl = new URL(mServerPath + "/probelmUpload");
            mConn = (HttpURLConnection) mUrl.openConnection();

            setConnectConf();
            int code = sendmsg(jsonMsg);
            if (code == 200) {
                return readResponse();
            } else {
                return Constants.HTTP_EXCEPTION;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        }
    }

    /**
     * 上传文件
     *
     * @param paths 文件路径
     * @return
     * @throws IOException
     */
    public String upFiles(ArrayList<String> paths) {
        try {
            //边界符
            String boundary = "---------------------------16487838927703";
            String prefix = "--";
            String end = "\r\n";
            //建立连接
            mUrl = new URL(mServerPath + "/file_upload");
            mConn = (HttpURLConnection) mUrl.openConnection();
            setConnectConf();
            //设置文件传输头信息
            mConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            mConn.setRequestProperty("Connection", "Keep-Alive");
            DataOutputStream fout = new DataOutputStream(mConn.getOutputStream());
            for (int i = 0; i < paths.size(); i++) {
                String filePath = paths.get(i);
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                //输出边界符
                fout.writeBytes(prefix + boundary + end);
                //输出文件描述
                fout.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + end + end);
                //获得文件输入流
                FileInputStream fileInputStream = new FileInputStream(filePath);
                //文件读取缓存大小
                byte[] buffer = new byte[1024 * 4];
                //文件每次读取长度
                int len;
                //循环读取文件并输出
                while ((len = fileInputStream.read(buffer)) != -1) {
                    fout.write(buffer, 0, len);
                }
                fileInputStream.close();
                //输出分隔边界符
                fout.writeBytes(end);
            }
            //输出结束边界符
            fout.writeBytes(prefix + boundary + prefix + end);
            fout.flush();
            if (fout != null) fout.close();
            //读取返回信息
            if (mConn.getResponseCode() == 200) {
                return readResponse();
            } else {
                return Constants.HTTP_EXCEPTION;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.HTTP_EXCEPTION;
        }
    }

    /**
     * 读取网络返回信息
     *
     * @return
     * @throws IOException
     */
    private String readResponse() throws IOException {
        mIn = mConn.getInputStream();
        //字节输出流
        ByteArrayOutputStream msgByteArray = new ByteArrayOutputStream();
        //读取长度
        int len = 0;
        //读取缓存大小
        byte buffer[] = new byte[1024];
        //循环读取
        while ((len = mIn.read(buffer)) != -1) {
            msgByteArray.write(buffer, 0, len);
        }
        //关闭资源
        mIn.close();
        msgByteArray.close();
        //返回信息
        return new String(msgByteArray.toByteArray());
    }

    /**
     * 设置连接头部信息
     *
     * @throws ProtocolException
     */
    private void setConnectConf() throws ProtocolException {
        //设置传输方式
        mConn.setRequestMethod("POST");
        //设置超时时间
        mConn.setConnectTimeout(5 * 1000);
        mConn.setReadTimeout(5 * 1000);
        //设置可读写
        mConn.setDoOutput(true);
        mConn.setDoInput(true);
        //关闭缓存
        mConn.setUseCaches(false);
        //设置编码方式
        mConn.setRequestProperty("Charset", "UTF-8");
    }

    /**
     * 发送文本信息
     *
     * @param data
     * @return
     * @throws IOException
     */
    private int sendmsg(String data) throws IOException {
        //组装数据
        String msg = "data=" + data;
        //获得输出流
        mOut = mConn.getOutputStream();
        //写出数据
        mOut.write(msg.getBytes());
        mOut.flush();
        if (mOut != null) mOut.close();
        //返回网络状态码
        return mConn.getResponseCode();
    }

    /**
     * 建立HTTP请求，并获取Bitmap对象。
     *
     * @param imageUrl 图片Url
     * @param savePath 保存路径
     * @return Bitmap的保存路径
     */
    public String downloadBitmap(String imageUrl, String savePath) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        File file = new File(savePath, fileName);
        Bitmap bitmap = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(mServerPath + "/"+imageUrl);
            Log.d("TAG",url.toString());
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(10 * 1000);
            bitmap = BitmapFactory.decodeStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        if (bitmap == null) {
            return "";
        } else {
            return saveImage(bitmap, file);
        }
    }

    /**
     * 保存 Bitmap 对象到本地
     *
     * @param bmp  要保存的 Bitmap 对象
     * @param file 要保存图片的路径
     * @return 保存图片的路径
     */
    public static String saveImage(Bitmap bmp, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}