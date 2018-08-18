package com.liu008.myapplication.activity.me;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.liu008.myapplication.R;
import com.liu008.myapplication.http.HttpResponseCallBack;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.BitmapUtil;
import com.liu008.myapplication.utils.ImageUtils;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.PermissionUtil;
import com.liu008.myapplication.utils.UserUtils;
import com.liu008.myapplication.view.CustomProgress;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.io.File;
import java.util.List;

public class ImageHeadActivity extends AppCompatActivity {
    private static final String TAG = "ImageHeadActivity";
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    private static final int REQUEST_CODE_CAMERA = 100;
    protected static Uri tempUri;
    private CommonTitleBar titleBar;
    private ImageView imageView;

    private String mPhotoPath;//拍照产生照片文件路径
    private Bitmap mBitmap;
    private File mAvatarFile;//用来做系统剪裁的图片文件
    private File fileUri; //拍照产生照片文件
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 500:
                    //这里执行后台刷新个人信息了，否则个人信息还是老的
                    UserUtils.getSaveUserBasicInfo(ImageHeadActivity.this,mHandler);
                    CustomProgress.dimiss();
                    imageView.setImageBitmap(mBitmap);
                    Toast.makeText(ImageHeadActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_head);
        titleBar = (CommonTitleBar) findViewById(R.id.imagehead_titlebar);
        imageView=findViewById(R.id.iv_imagehead);
        titleBar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                switch (action) {
                    case CommonTitleBar.ACTION_LEFT_BUTTON://返回键
                        try {
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                        case CommonTitleBar.ACTION_RIGHT_TEXT:
                            showChoosePicDialog();
                            break;
                        default:break;
                }
            }
        });
        //从intent中的uri中加执
        Glide.with(this).load(getIntent().getStringExtra("uri"))
                .apply(new RequestOptions().error(R.mipmap.img_error))
                .into(imageView);
    }

    /**
     * 1.显示修改头像的对话框
     */
    public void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置头像");
        String[] items = {"选择本地照片", "拍照"};
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 选择本地照片
                        allPhoto();
                        break;
                    case TAKE_PICTURE: // 拍照
                        takePicture();
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 调用手机相册
     */
    private void allPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOOSE_PICTURE);
    }




    /**
     * 4.保存裁剪之后的图片数据
     *
     * @param
     */
    protected void setImageToView(Intent data) {
//        Bundle extras = data.getExtras();
//        if (extras != null) {
//            Bitmap photo = extras.getParcelable("data");
//            //Log.d(TAG, "setImageToView:" + photo);
//            // photo = ImageUtils.toRoundBitmap(photo); // 这个时候的图片已经被处理成圆形的了
//            //imageView.setImageBitmap(photo);
//            CustomProgress.show(ImageHeadActivity.this,"上传头像中...",false,null);
//            uploadPic(photo);
//        }
        if (fileUri!=null){
            Bitmap photo=BitmapUtil.getSmallBitmap(fileUri.getAbsolutePath(),300,300);
            imageView.setImageBitmap(photo);
            uploadPic(photo);
        }
    }

    /**
     * 3.裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
//        if (uri == null) {
//            Log.i("tag", "The uri is not exist.");
//        }
//        tempUri = uri;
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, "image/*");
//        // 设置裁剪
//        intent.putExtra("crop", "true");
//        // aspectX aspectY 是宽高的比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        // outputX outputY 是裁剪图片宽高
//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);
//        intent.putExtra("return-data", true);
//        startActivityForResult(intent, CROP_SMALL_PICTURE);

        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        //是否可裁剪
        intent.putExtra("corp", "true");
        //裁剪器高宽比
        intent.putExtra("aspectY", 1);
        intent.putExtra("aspectX", 1);
        //设置裁剪框高宽
        intent.putExtra("outputX", 700);
        intent.putExtra("outputY", 700);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //裁剪后的图片放在哪里
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileUri));
        //返回数据
        intent.putExtra("return-data", false);//return置为false，获取截图保存的uri
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * 5.保存和上传头像
     *
     * @param bitmap
     */
    private void uploadPic(final Bitmap bitmap) {
        // 上传至服务器
        // ... 可以在这里把Bitmap转换成file，然后得到file的url，做文件上传操作
        //path为SD卡根路径下加应用名称
        //getContext().getPackageManager().getApplicationLabel(getContext().getApplicationInfo())
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LaoSiJi/";
        //具体图片名从prefres文件中获取userid+时间戳作为图片名
        String imagePath = ImageUtils.savePhoto(bitmap, path, String
                .valueOf(UserManage.getInstance().getUserId(this)) + "-" + String.valueOf(System.currentTimeMillis()));
        Log.e("imagePath", imagePath + "");
        if (imagePath != null) {
            // 拿着imagePath上传了
            JSONObject jsonObject = new JSONObject();
//            try {
//                jsonObject.put("id","test");
//                jsonObject.put("phoneNum","111111");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            mBitmap=bitmap;
            File file = new File(imagePath);
            //上传头像至服务器
            ImageUtils.upLoadImage(this, MyConstant.APPSERVER_URL + "/portraitUpload", file, jsonObject, new HttpResponseCallBack() {
                @Override
                public void response(String response) {
//                    System.out.println(response);
//                    Looper.prepare();
//                    CustomProgress.dimiss();
//                    //这里执行后台刷新个人信息了，否则个人信息还是老的
//                    UserUtils.getSaveUserBasicInfo(ImageHeadActivity.ths,mHandler);
//                    imageView.setImageBitmap(bitmap);
//                    Toast.makeText(ImageHeadActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
//                    Looper.loop();
                    mHandler.sendEmptyMessage(500);//上传成功
                }

                @Override
                public void error(Exception e) {
                    e.printStackTrace();
                    Looper.prepare();
                    CustomProgress.dimiss();
                    Toast.makeText(ImageHeadActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }


            });
          //  Log.d(TAG, "imagePath:" + imagePath);
        }
    }

    /**
     * 调用手机相机拍照
     */
    private void takePicture() {
        //申请相机权限,已经包含了存储读写权限
        if (PermissionUtil.getCameraPermissions(this, REQUEST_CODE_CAMERA)) {
//            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            File file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
//            //判断是否是AndroidN以及更高的版本
//            if (Build.VERSION.SDK_INT >= 24) {
//                openCameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                tempUri = FileProvider.getUriForFile(this, "com.liu008.myapplication.FileProvider", file);
//            } else {
//                tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
//            }
//            // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
//            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
//            startActivityForResult(openCameraIntent, TAKE_PICTURE);
            File file = new File(this.getFilesDir(),"photos");
            if (!file.exists()) {
                file.mkdirs();
            }
            //mPhotoPath =file.getAbsolutePath() +"/"+ String.valueOf(System.currentTimeMillis()) + ".jpg";
            mPhotoPath =file.getAbsolutePath() +"/tempHead.jpg";
            fileUri = new File(mPhotoPath);
            Intent intentCamera = new Intent();
            Uri imageUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                imageUri = FileProvider.getUriForFile(ImageHeadActivity.this, "com.liu008.myapplication.FileProvider", fileUri);
            } else {
                imageUri = Uri.fromFile(fileUri);
            }
            intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intentCamera, TAKE_PICTURE);

        }
    }

    /**
     * 2.处理图片的回调方法
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回码是可以用的
            switch (requestCode) {
                case TAKE_PICTURE://相机返回结果
                    //相机返回结果，调用系统裁剪
                    mAvatarFile = new File(mPhotoPath);
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(this, "com.liu008.myapplication.FileProvider", mAvatarFile);
                    } else {
                        uri = Uri.fromFile(mAvatarFile);
                    }
                    startPhotoZoom(uri);

//                    startPhotoZoom(tempUri); // 开始对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE://3.相册返回结果
                    //调用相册
                    Cursor cursor = getContentResolver().query(data.getData(), new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                    //游标移到第一位，即从第一位开始读取
                    if (cursor != null) {
                        cursor.moveToFirst();
                        mPhotoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        cursor.close();
                        //调用系统裁剪
                        mAvatarFile = new File(mPhotoPath);
                        final Uri uri2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri2 = FileProvider.getUriForFile(this, "com.liu008.myapplication.FileProvider", mAvatarFile);
                        } else {
                            uri2 = Uri.fromFile(mAvatarFile);
                        }
                        startPhotoZoom(uri2);
                    }

                    //startPhotoZoom(data.getData());
                    break;
                case CROP_SMALL_PICTURE://4.让刚才选择裁剪得到的图片显示在界面上
                    if (data != null) {
                        setImageToView(data);
                    }
                    break;
            }
        }
    }

    /**
     * 申请权限的返回结果处理
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, new PermissionUtil.OnRequestPermissionsResultCallbacks() {
            @Override
            public void onPermissionsGranted(int requestCode, List<String> perms, boolean isAllGranted) {
                Log.e(TAG, "同意:" + perms.size() + "个权限,isAllGranted=" + isAllGranted);
                for (String perm : perms) {
                    Log.e(TAG, "同意:" + perm);
                }
            }

            @Override
            public void onPermissionsDenied(int requestCode, List<String> perms, boolean isAllDenied) {
                Log.e(TAG, "拒绝:" + perms.size() + "个权限,isAllDenied=" + isAllDenied);
                for (String perm : perms) {
                    Log.e(TAG, "拒绝:" + perm);
                }
            }
        });
        switch (requestCode) {
            case 100://相机
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();//重新去执行拍照
                } else {
                    Toast.makeText(ImageHeadActivity.this, "需要相机权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 200://存储权限
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // takePicture();
                } else {
                    // 没有获取 到权限，从新请求，或者关闭app
                    Toast.makeText(ImageHeadActivity.this, "需要存储权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
