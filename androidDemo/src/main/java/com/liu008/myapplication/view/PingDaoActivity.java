package com.liu008.myapplication.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.liu008.myapplication.R;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.SystemUtil;

public class PingDaoActivity extends AppCompatActivity {
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_dao);
        textView= (TextView) findViewById(R.id.tv_PingDaoActivity);
        if (getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE)!=null){
            textView.setText(getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE).getString("testInfo"));
        }else if (getIntent().getStringExtra("testInfo")!=null){
            textView.setText(getIntent().getStringExtra("testInfo"));
        }else {
            System.out.println("EXTRA_BUNDLE没有数据");
        }

    }
}
