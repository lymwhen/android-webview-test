package com.chunshu.jdsjwt.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import okhttp3.Request;
import okhttp3.Response;

public class PicUtils {
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // height和width图片长宽的像素
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // 计算inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 根据inSampleSize压缩图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // 计算inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 根据inSampleSize压缩图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static Bitmap decodeSampledBitmapFromUrl(String url, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 计算inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 根据inSampleSize压缩图片
        options.inJustDecodeBounds = false;

        Bitmap bitmap = null;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = HttpUtils.getInstance().newCall(request).execute()) {
            InputStream is = response.body().byteStream();
            bitmap = BitmapFactory.decodeStream(is, new Rect(), options);
            is.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return bitmap;
    }

    public static File copyFile(File src, String destPath, String destName) {
        if ((src == null) || (destPath == null)) {
            return null;
        }
        String destFullPath = (destPath.charAt(destPath.length() - 1) != '/' ? (destPath + "/") : destPath)
                + destName
                + (src.getPath().lastIndexOf('.') > 0 ? src.getPath().substring(src.getPath().lastIndexOf('.')) : "");
        File dest = new File(destFullPath);
        if (dest.exists()) {
            return null;
        }
        try {
            if (!dest.createNewFile())
                return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dest).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return dest;
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static int dp2px(int dp, Resources resources) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    /**
     * 得到amr的时长
     *
     * @param file
     * @return amr文件时间长度
     * @throws IOException
     */
    public static int getAmrDuration(File file) throws IOException {
        long duration = -1;
        int[] packedSize = {12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0,
                0, 0};
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            long length = file.length();// 文件的长度
            int pos = 6;// 设置初始位置
            int frameCount = 0;// 初始帧数
            int packedPos = -1;

            byte[] datas = new byte[1];// 初始数据值
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }

            duration += frameCount * 20;// 帧数*20
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return (int) ((duration / 1000) + 1);
    }

    public static int getAmrLength(File file) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();// 单位毫秒
        } catch (Exception ex) {
            return 0;
        }
    }

    public static String msToLengthStr(int lengthMs) {
        int length = (lengthMs + 500) / 1000;
        int minutes = length / 60;
        int seconds = length % 60;
        return (minutes > 0 ? (String.valueOf(minutes) + "'") : "")
                + (seconds > 0 ? (String.valueOf(seconds) + "\"") : "")
                + (minutes == 0 && seconds == 0 ? "0\"" : "");
    }

    public static void setDrawableColor(Drawable drawable, int r, int g, int b, int a) {
        float[] colorMatrix = new float[]{
                0, 0, 0, 0, r,
                0, 0, 0, 0, g,
                0, 0, 0, 0, b,
                0, 0, 0, (float) a / 255, 0
        };
        drawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }

    public static Drawable setDrawableColor(Drawable drawable, int color) {
        int r = (color & 0xff0000) >> 16;
        int g = (color & 0x00ff00) >> 8;
        int b = (color & 0x0000ff);
        setDrawableColor(drawable, r, g, b, 255);
        return drawable;
    }

    public static int changeAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return Color.argb(alpha, red, green, blue);
    }

}
