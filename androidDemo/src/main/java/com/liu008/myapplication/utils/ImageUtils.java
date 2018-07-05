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
                            fileOutputStream)) { // ת�����
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
     * ת��ͼƬ��Բ��
     *
     * @param bitmap ����Bitmap����
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
        //����һ��JSON��MediaType��������ý�����ͣ�
       // final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //1.����һ��OkhttpClient
        OkHttpClient client= ApiHttpClient.getInstance(context);
        //2.����body��Ȼ���������body����ŵ�����������ʲô��
        MultipartBody.Builder builder=new MultipartBody.Builder().setType(MultipartBody.FORM);
        String userId=UserManage.getInstance().getUserId(context);
        //���м���file
        builder.addFormDataPart("file",userId+".jpg",RequestBody.create(MediaType.parse("image/jpg"), file));
        //�ӱ��ػ�ȡuserid�����
        builder.addFormDataPart("userId", userId);
        RequestBody body =builder.build();

        //3.����һ���������ù�������ʽ���url�������塣
        Request request = new Request.Builder().post(body).url(url).build();
        //4.����һ��call������okhttpclient��newcall����������������ΪCall��һ���ӿڲ������ù�����ʵ������
        Call call = client.newCall(request);
        //5.�����첽���ȷ������ϴ��ͽ��ܵĹ����������߳��������������Ҫʹ��ͬ���ķ�������call.excute(),�˷������صľ���Response
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpResponseCallBack.error(e);//������ʱ�Ĵ���
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                httpResponseCallBack.response(response.body().string());//�ѷ�����������������response������string���뷽��
            }
        });
    }
}
