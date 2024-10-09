package com.chunshu.jdsjwt.utils;

import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class HttpUtils {

    public static final MediaType APPLICATION_JSON_UTF8_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client;

    public static synchronized OkHttpClient getInstance(){
        if(client == null){
            client = getNoVerifyOkHttpClientMaybe();
        }
        return client;
    }

    public static OkHttpClient.Builder getNoVerifyOkHttpClientBuilder() throws Exception{
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("SSL");
            // trustAllCerts信任所有的证书
            sslContext.init(null,new X509TrustManager[]{trustManager},null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new Exception(e);
        }

        // 不进行服务名校验
        HostnameVerifier noVerifier = (hostname, session) -> true;

        // 处理重定向，如 tomcat 配置 80 端口重定向到 443
        Interceptor redirectInterceptor = chain -> {
            Request request = chain.request();
            Response response = chain.proceed(request);
            int code = response.code();
            Log.d("redirect", String.valueOf(code));
            if (code == 307 || code == 302) {
                //获取重定向的地址
                String location = response.headers().get("Location");
//                Log.d(TAG, "redirect：" + "location = " + location);
                //重新构建请求
                Request newRequest = request.newBuilder().url(location).build();
                response = chain.proceed(newRequest);
            }
            return response;
        };

        return new OkHttpClient.Builder()
                .followRedirects(false)
                .addInterceptor(redirectInterceptor)
                .hostnameVerifier(noVerifier)
                .sslSocketFactory(sslSocketFactory, trustManager);
    }

    public static OkHttpClient getNoVerifyOkHttpClient() throws Exception {
        return getNoVerifyOkHttpClientBuilder().build();
    }

    public static OkHttpClient getNoVerifyOkHttpClientMaybe() {
        try{
            return getNoVerifyOkHttpClient();
        }catch(Exception e){
            Log.w(TAG, "error on getting no verify okhttp client, return new okhttp client");
            Log.w(TAG, e);
            return new OkHttpClient();
        }
    }
}
