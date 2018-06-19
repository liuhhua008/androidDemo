package com.liu008.myapplication.view;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.model.UserManage;

public class MeFragment extends Fragment {
    private Button btnLoginOut;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_me,null);
        btnLoginOut=(Button) view.findViewById(R.id.btnLoginOff);
        btnLoginOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //把token信息删除
                UserManage.getInstance().deleteUserInfo(getContext());
                Intent intent = new Intent(getContext(),LoginActivity.class);//跳转到登录页
                startActivity(intent);
                try {
                    MyApplication.instance.exit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}
