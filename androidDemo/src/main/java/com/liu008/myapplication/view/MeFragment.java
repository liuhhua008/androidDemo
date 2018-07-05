package com.liu008.myapplication.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.http.HttpResponseCallBack;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.ImageUtils;
import com.liu008.myapplication.utils.MyConstant;

import java.io.File;

import static android.app.Activity.RESULT_OK;

public class MeFragment extends Fragment {
    private static final String TAG = "MeFrgment";
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    protected static Uri tempUri;
    private ImageView ivHead;
    private Button btnLoginOut;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_me,null);
        initVews(view);
        setListener();
        return view;
    }

    private void initVews(View view){
        ivHead =(ImageView) view.findViewById(R.id.iv_head);
        btnLoginOut=(Button) view.findViewById(R.id.btnLoginOff);

    }

    private void setListener() {
        ivHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoosePicDialog();
            }
        });
        btnLoginOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //把token信息删除
                    UserManage.getInstance().deleteUserInfo(getContext());
                }catch (Exception ex){

                }finally {
                    Intent intent = new Intent(getContext(),LoginActivity.class);//跳转到登录页
                    startActivity(intent);
                    try {
                        MyApplication.instance.exit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回码是可以用的
            switch (requestCode) {
                case TAKE_PICTURE:
                    startPhotoZoom(tempUri); // 开始对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData()); // 开始对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        setImageToView(data); // 让刚才选择裁剪得到的图片显示在界面上
                    }
                    break;
            }
        }
    }

    /**
     * 显示修改头像的对话框
     */
    public void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("设置头像");
        String[] items = { "选择本地照片", "拍照" };
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 选择本地照片
                        Intent openAlbumIntent = new Intent(
                                Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
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
     * 保存裁剪之后的图片数据
     *
     * @param
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Log.d(TAG,"setImageToView:"+photo);
           // photo = ImageUtils.toRoundBitmap(photo); // 这个时候的图片已经被处理成圆形的了
            ivHead.setImageBitmap(photo);
            uploadPic(photo);
        }
    }
    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * 保存和上传头像
     * @param bitmap
     */
    private void uploadPic(Bitmap bitmap) {
        // 上传至服务器
        // ... 可以在这里把Bitmap转换成file，然后得到file的url，做文件上传操作

        // 注意这里得到的图片已经是圆形图片了
        // bitmap是没有做个圆形处理的，但已经被裁剪了
        //path为SD卡根路径下加应用名称
        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"
                +getContext().getPackageManager().getApplicationLabel(getContext().getApplicationInfo())+"/";
        //具体图片名从prefres文件中获取userid作为图片名
        String imagePath = ImageUtils.savePhoto(bitmap, path, String
                .valueOf(UserManage.getInstance().getUserId(getContext())));
        Log.e("imagePath", imagePath+"");
        if(imagePath != null){
            // 拿着imagePath上传了
            JSONObject jsonObject=new JSONObject();
//            try {
//                jsonObject.put("id","test");
//                jsonObject.put("phoneNum","111111");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            File file =new File(imagePath);
            ImageUtils.upLoadImage(getContext(),MyConstant.APPSERVER_URL+"/portraitUpload",file,jsonObject,new HttpResponseCallBack() {
                @Override
                public void response(String response) {
                    System.out.println(response);
                    Looper.prepare();
                    Toast.makeText(getActivity(),"上传成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                @Override
                public void error(Exception e) {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getActivity(),"上传失败",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }


            });
            Log.d(TAG,"imagePath:"+imagePath);
        }
    }

    private void takePicture() {
        if (Build.VERSION.SDK_INT>=23){
            //判断一下相机权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                // 进入这儿表示没有权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                    // 提示已经禁止
                    Toast.makeText(getActivity(), "已禁止使用相机", Toast.LENGTH_SHORT).show();
                } else {
                    //弹出权限请求对话
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 100);
                    return;
                }
            }
           //判断一下存储权限
            int check = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                return;
            }
        }
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= 24) {
            openCameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            tempUri = FileProvider.getUriForFile(getActivity(), "com.liu008.myapplication.FileProvider", file);
        } else {
            tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
        }
        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        startActivityForResult(openCameraIntent, TAKE_PICTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 100://相机
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePicture();//重新去执行拍照
                }else{
                    Toast.makeText(getActivity(), "需要相机权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 200://存储权限
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    takePicture();
                }else{
                    // 没有获取 到权限，从新请求，或者关闭app
                    Toast.makeText(getActivity(), "需要存储权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
