package com.chunshu.jdsjwt;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chunshu.jdsjwt.model.JsonResult;
import com.chunshu.jdsjwt.utils.DownloadUtils;
import com.chunshu.jdsjwt.utils.HttpUtils;
import com.chunshu.jdsjwt.utils.JsInterface;
import com.chunshu.jdsjwt.utils.WidgetUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
//    public static final String URL_INDEX = "http://39.130.160.197:28080/app/index.html";
//    public static final String URL_API = "http://39.130.160.197:28080/api";

    private static final String TAG = "MainActivity";

    // 更新弹窗
    private AlertDialog updateAlert;
    private ProgressBar updatePb;
    private TextView updateTvCurrSize;
    private TextView updateTvTotalSize;
    private boolean isCheckedUpdate = false;

    // 等待弹窗
    private AlertDialog waitAlert;

    // 加载弹窗
    public AlertDialog loadingAlert;

    // 主线程handler
    private MainHandler mainHandler;
    private static final int MSG_APP_UPDATE_SUCCESS = 10;
    private static final int MSG_APP_UPDATE_FAILED = 11;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    private boolean isOnCreate = false;

    public static final int REQUEST_CODE_SCAN = 171;
    public static final int RESULT_CODE_SCAN = 172;

    private WebView webView;
    private WebSettings webSetting;

    private EditText etTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        etTest = findViewById(R.id.et_test);

        findViewById(R.id.btn_test2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                x5WebView.reload();
//                x5WebView.loadUrl("javascript:history.back();location.reload()");
                webView.evaluateJavascript("history.back();location.reload()", null);
            }
        });

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_wait, null);
//        loadingAlert = new AlertDialog.Builder(MainActivity.this)
////                .setIcon(R.mipmap.ic_launcher)
////                .setTitle("正在加载")
//                .setView(view)
//                .setCancelable(false).create();
        loadingAlert = WidgetUtils.createLoading(this, "正在加载...");

//        loadingAlert.show();

        // loadingAlert.show();

        mainHandler = new MainHandler();

        webView = findViewById(R.id.x5WebView);
        webSetting = webView.getSettings();
//        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // 设置与Js交互的权限
        webSetting.setJavaScriptEnabled(true);

        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSetting.setUseWideViewPort(true);//关键点

        webSetting.setAllowFileAccess(true);
        webSetting.setAllowFileAccessFromFileURLs(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setDomStorageEnabled(true);  // 开启 DOM storage 功能
//        webSetting.setAppCacheMaxSize(1024*1024*8);
//        String appCachePath = this.getApplicationContext().getCacheDir().getAbsolutePath();
//        webSetting.setAppCachePath(appCachePath);
//        webSetting.setAllowFileAccess(true);    // 可以读取文件缓存
//        webSetting.setAppCacheEnabled(true);    //开启H5(APPCache)缓存功能

        // 开启Cookie
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);

        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        x5WebView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

//        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);

//        Intent intent = new Intent("com.chunshu.noa.GuardReceiver");
//        intent.setComponent( new ComponentName( "com.chunshu.noa" ,
//                "com.chunshu.noa.GuardReceiver") );
//        sendBroadcast(intent, "com.chunshu.noa.GuardReceiver");
//        Intent intent = new Intent();
//        intent.setAction("com.chunshu.noa.GuardReceiver");
//        intent.setComponent( new ComponentName( "com.chunshu.noa" ,
//                "com.chunshu.noa.GuardReceiver") );
//        sendBroadcast(intent);

       /* webSetting.setDomStorageEnabled(true);  // 开启 DOM storage 功能
        webSetting.setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getBaseContext().getApplicationContext().getCacheDir().getAbsolutePath();
        webSetting.setAppCachePath(appCachePath);
        webSetting.setAllowFileAccess(true);    // 可以读取文件缓存
        webSetting.setAppCacheEnabled(true);*/

        // 通过addJavascriptInterface()将Java对象映射到JS对象
        //参数1：Javascript对象名
        //参数2：Java对象名
        //x5WebView.addJavascriptInterface(new ShareKit(this), "shareKit");//AndroidtoJS类对象映射到js的test对象


        webView.setWebViewClient(new WebViewClient() {
            //覆盖shouldOverrideUrlLoading 方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent sendIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(sendIntent);
                    return true;
                } else if (url.startsWith("sms:")) {
                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(sendIntent);
                    return true;
                } else {
//                    view.loadUrl(url);
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
                super.onPageFinished(view, url);

               /* String key = "token";
                String val = SpfsUtils.readString(MyApp.getContext(),"token");
                String key2 = "is_app";
                String val2 = "101";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    x5WebView.evaluateJavascript("window.localStorage.setItem('"+ key +"','"+ val +"');", null);
                    x5WebView.evaluateJavascript("window.localStorage.setItem('"+ key2 +"','"+ val2 +"');", null);
                } else {
                    x5WebView.loadUrl("javascript:localStorage.setItem('"+ key +"','"+ val +"');");
                    x5WebView.loadUrl("javascript:localStorage.setItem('"+ key2 +"','"+ val2 +"');");
                }*/
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // super.onReceivedSslError(view, handler, error);
                // handler.cancel();// super中默认的处理方式，WebView变成空白页
                if (handler != null) {
                    //忽略证书的错误继续加载页面内容
                    handler.proceed();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.d(TAG, "onReceivedError");
            }


        });
        webView.setWebChromeClient(new WebChromeClient() {
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, message);
                result.confirm();
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                Log.d(TAG, Arrays.toString(request.getResources()));
                request.grant(request.getResources());
            }
        });

        // 下载监听
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Map<String, String> params = parseUrl(url);
                String title = params.containsKey("title") ? params.get("title").toString() : null;
                try {
                    if (title != null) {
                        title = URLDecoder.decode(title, "UTF-8");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "文件" + (title != null ? (" " + title + " ") : "") + "开始下载...", Toast.LENGTH_SHORT).show();
                download(MainActivity.this, url, title);
            }
        });

        // 添加js接口，添加$App到window
        webView.addJavascriptInterface(new JsInterface(this, webView), "$App");
        webView.loadUrl("https://192.168.203.37:8089");
//        x5WebView.loadUrl("https://www.bing.com");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            checkUpdate();
        }
    }

    class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_APP_UPDATE_SUCCESS) {
                JsonResult json = (JsonResult) msg.obj;
                if (json.getData().get("isNeedUpdate").equals(1)) {
                    loadingAlert.dismiss();

                    String url = json.getData().getString("url");
                    String version = json.getData().getString("versionName");
                    String updateMsg = json.getData().getString("msg");
                    startUpdate(url, version, updateMsg);
                }
            } else if (msg.what == MSG_APP_UPDATE_FAILED) {
                loadingAlert.dismiss();
                Toast.makeText(MainActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        CookieSyncManager.getInstance().startSync();
        if (isOnCreate) {
            isOnCreate = false;
        } else {

            // 回调js方法
            webView.loadUrl("javascript:onActivityResume()");
        }
        Log.d("noa", "onActivityResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        CookieSyncManager.getInstance().stopSync();
        super.onPause();
    }

    public Map<String, String> parseUrl(String url) {
        Map<String, String> map = new HashMap<>();

        int index = url.indexOf("?");
        String param = url.substring(index + 1);
        if (param.length() > 0) {
            String[] params = param.split("&");
            for (String item : params) {
                String[] kv = item.split("=");
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    /**
     * 调用系统下载管理器下载
     *
     * @param context
     * @param url
     * @param title
     */
    public void download(Context context, String url, String title) {
        if (title == null || title.length() == 0) {
            title = url.substring(url.lastIndexOf("/") + 1, url.contains("?") ? url.indexOf("?") : url.length());
        }

        Uri uri = Uri.parse(url);        //下载连接
        DownloadManager manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);  //得到系统的下载管理
        DownloadManager.Request request = new DownloadManager.Request(uri);  //得到连接请求对象
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);   //指定在什么网络下进行下载，这里我指定了WIFI网络
        request.setDestinationInExternalPublicDir("/Download", title);  //制定下载文件的保存路径，我这里保存到根目录
        request.setVisibleInDownloadsUi(true);  //设置显示下载界面
        request.allowScanningByMediaScanner();  //表示允许MediaScanner扫描到这个文件，默认不允许。
        request.setTitle(title);      //设置下载中通知栏的提示消息
        request.setDescription("文档下载");//设置设置下载中通知栏提示的介绍
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        long downloadId = manager.enqueue(request);               //启动下载,该方法返回系统为当前下载请求分配的一个唯一的ID
    }

    /**
     * 返回键事件
     */
    private long lastDateMillis = 0L;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d("tag", String.valueOf(webView.canGoBack()));
            if (webView.canGoBack()) {
//                webSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                webView.goBack();
//                x5WebView.evaluateJavascript("history.back()", null);
                return false;
            } else {
                long nowMillis = System.currentTimeMillis();
                if ((nowMillis - lastDateMillis) > 1000) {
                    lastDateMillis = nowMillis;
                    Toast.makeText(MainActivity.this, "再次点击返回键退出", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return super.onKeyDown(keyCode, event);
                }
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        Log.d("checkupdate", "start");

        OkHttpClient client = null;
        try {
            client = HttpUtils.getNoVerifyOkHttpClient();
            FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
            formBody.add("version", String.valueOf(packageCode(this)));//传递键值对参数
            Request request = new Request.Builder()
                    .url(ServerConfig.URL_API + "/checkUpdateJwt")
                    .post(formBody.build())
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("noa", "checkUpdate onFailure");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i("noa", "checkUpdate onResponse");

                    if (response.isSuccessful()) {
                        final JsonResult json = JsonResult.parse(response.body().string());
                        Log.i("noa", "checkUpdate Failed");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (json.getData().get("isNeedUpdate").equals(1)) {
                                    loadingAlert.dismiss();

                                    String url = json.getData().getString("url");
                                    String version = json.getData().getString("versionName");
                                    String updateMsg = json.getData().getString("msg");
                                    startUpdate(url, version, updateMsg);
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "检查更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始更新
     */
    private void startUpdate(String url, String version, String msg) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update, null);
        TextView updateVersion = view.findViewById(R.id.tv_version);
        TextView updateMsg = view.findViewById(R.id.tv_msg);
        updateVersion.setText(version);
        updateMsg.setText(msg);

        updatePb = view.findViewById(R.id.pb_process);
        updateTvCurrSize = view.findViewById(R.id.tv_curr_size);
        updateTvTotalSize = view.findViewById(R.id.tv_total_size);
        updateAlert = new AlertDialog.Builder(this)
                .setTitle("应用更新")
                .setView(view)
                .setCancelable(false).show();

        DownloadUtils.download(this, url, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/update.apk", new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                updatePb.setProgress(updatePb.getMax());
                updateTvCurrSize.setText(updateTvTotalSize.getText());

                Uri apkUri =
                        FileProvider.getUriForFile(MainActivity.this, "com.chunshu.jdsjwt.fileprovider", file);

                updateAlert.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // 由于没有在Activity环境下启动Activity,设置下面的标签
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                startActivity(intent);
            }

            @Override
            public void onDownloading(int currSize, int totalSize) {
                updatePb.setMax(totalSize);
                updatePb.setProgress(currSize);
                updateTvCurrSize.setText(String.format(Locale.CHINA, "%.1fMB", (float) currSize / 1024));
                updateTvTotalSize.setText(String.format(Locale.CHINA, "%.1fMB", (float) totalSize / 1000));
            }

            @Override
            public void onDownloadFailed(Exception e) {
                updateAlert.dismiss();
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "更新失败，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        //i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }else if(requestCode == REQUEST_CODE_SCAN){
            if(resultCode == RESULT_CODE_SCAN){
                // 二维码扫描结果
                String qrResult = data.getStringExtra("qrResult");
                webView.loadUrl("javascript:onQrResult('" + qrResult + "')");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    public static int packageCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }
}
