package com.chunshu.jdsjwt.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtils {

    private static final int MSG_SUCCESS = 1;
    private static final int MSG_PROCESS = 2;
    private static final int MSG_FAILED = 3;

    /**
     * @param url          下载连接
     * @param listener     下载监听
     */
    public static void download(Context context, final String url, final String destFilePath, final OnDownloadListener listener) {
        final Handler handler = new Handler(context.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if(message.what == MSG_SUCCESS){
                    if (listener != null) {
                        listener.onDownloadSuccess((File)message.obj);
                    }
                }else if(message.what == MSG_FAILED){
                    if (listener != null) {
                        listener.onDownloadFailed((Exception)message.obj);
                    }
                }else if(message.what == MSG_PROCESS){
                    if (listener != null) {
                        listener.onDownloading(message.arg1, message.arg2);
                    }
                }
                return false;
            }
        });

        OkHttpClient okHttpClient = null;
        try{
            okHttpClient = HttpUtils.getNoVerifyOkHttpClient();
        }catch(Exception e){
            if (listener != null) {
                listener.onDownloadFailed(new Exception("创建请求失败"));
            }
            return;
        }

        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                // 下载失败监听回调
                Message msg = new Message();
                msg.what = MSG_FAILED;
                msg.obj = e;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                File file = new File(destFilePath);
                File dir = file.getParentFile();
                // 储存下载文件的目录
                if (!dir.exists() && !dir.mkdirs()) {
                    Message msg = new Message();
                    msg.what = MSG_FAILED;
                    msg.obj = new Exception("创建文件目录失败");
                    handler.sendMessage(msg);
                }
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        Message msg = new Message();
                        msg.what = MSG_PROCESS;
                        msg.arg1 = (int) (sum / 1024);
                        msg.arg2 = (int) (total / 1024);
                        handler.sendMessage(msg);
                    }
                    fos.flush();
                    Message msg = new Message();
                    msg.what = MSG_SUCCESS;
                    msg.obj = file;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = MSG_FAILED;
                    msg.obj = e;
                    handler.sendMessage(msg);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    public interface OnDownloadListener {
        void onDownloadSuccess(File file);

        void onDownloading(int currSize, int totalSize);

        void onDownloadFailed(Exception e);
    }
}