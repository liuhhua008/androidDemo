package com.liu008.myapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by 008 on 2018/3/22.
 */

public class BaseFragment extends Fragment {
    public static BaseFragment newInstance(String info){
        Bundle args = new Bundle();
        BaseFragment fragment=new BaseFragment();
        args.putString("info",info);
        fragment.setArguments(args);
        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_base,null);
        TextView tvInfo=(TextView) view.findViewById(R.id.textView);
        tvInfo.setText(getArguments().getString("info"));
        tvInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从底部弹出一个提示条目
                //Snackbar.make(v, "Don't click me.please!.", Snackbar.LENGTH_SHORT).show();
                FrameLayout mContentContainer;
                //拿到除顶层窗口对象，对像ChildAt（0）为标题栏，1为下面全部内容
                ViewGroup mDecorView= (ViewGroup) getActivity().getWindow().getDecorView();
                //在子类Activity中去把标记为"folat_tag_frameLayout"的FrameLayout拿出来作为容器
                mContentContainer = (FrameLayout) ((ViewGroup)mDecorView.
                        getChildAt(0)).findViewWithTag("folat_tag_frameLayout");
                mContentContainer.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }
}
