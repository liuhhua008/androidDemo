package com.liu008.myapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.alibaba.fastjson.JSONObject;
import com.liu008.myapplication.http.HttpResponseCallBack;
import com.liu008.myapplication.model.UserManage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageUtils {
    /**
     * Save image to the SD card
     *
     * @param photoBitmap
     * @param photoName
     * @param path
     */
    public static String savePhoto(Bitmap photoBitmap, String path,
                                   String photoName) {
        String localPath = null;
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //File photoFile = new File(path, photoName + ".png");
            File photoFile = new File(path, photoName + ".jpg");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                   // if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                    if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                            fileOutputStream)) { // 转换完成
                        localPath = photoFile.getPath();
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                        fileOutputStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localPath;
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
        final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }



    /**
     *
     * @param url
     * @param file
     * @param jsonObject
     * @param httpResponseCallBack
     */
    public static void upLoadImage (Context context,String url, File file, JSONObject jsonObject, final HttpResponseCallBack httpResponseCallBack){
        //定义一个JSON的MediaType（互联网媒体类型）
       // final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //1.定义一个OkhttpClient
        OkHttpClient client= ApiHttpClient.getInstance(context);
        //2.建立body，然后设置这个body里面放的数据类型是什么。
        MultipartBody.Builder builder=new MultipartBody.Builder().setType(MultipartBody.FORM);
        String userId=UserManage.getInstance().getUserId(context);
        //表单中加入file
        builder.addFormDataPart("file",userId+".jpg",RequestBody.create(MediaType.parse("image/jpg"), file));
        //从本地获取userid放入表单
        builder.addFormDataPart("userId", userId);
        RequestBody body =builder.build();

        //3.创建一个请求，利用构建器方式添加url和请求体。
        Request request = new Request.Builder().post(body).url(url).build();
        //4.定义一个call，利用okhttpclient的newcall方法来创建对象。因为Call是一个接口不能利用构造器实例化。
        Call call = client.newCall(request);
        //5.这是异步调度方法，上传和接受的工作都在子线程里面运作，如果要使用同步的方法就用call.excute(),此方法返回的就是Response
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpResponseCallBack.error(e);//错误发生时的处理
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                httpResponseCallBack.response(response.body().string());//把服务器发回来的数据response解析成string传入方法
            }
        });
    }
}
