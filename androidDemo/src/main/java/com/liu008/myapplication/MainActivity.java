package com.liu008.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.liu008.myapplication.adapter.ViewPagerAdapter;
import com.liu008.myapplication.service.PushService;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.view.BaseActivity;
import com.liu008.myapplication.view.MeFragment;
import com.liu008.myapplication.view.PingDaoActivity;

import java.lang.reflect.Field;

public class MainActivity extends BaseActivity {
    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;
    private View rl_PingDaoSetting;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("info", "activity_onCreate-执行了: ");
        initView();
        //如果是从频道通知栏点击激发启动的，应该继续跳转
        checkIsNotification();
        setListener();
        //给ViewPager设置Fragment
        setupViewPager(viewPager);
    }

    private void setListener() {
        //底部菜单点击监听，指定对应的VIEWPAGER页面
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.item_pintao:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.item_friends:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.item_me:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });

        //viewPager滑动翻页监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            //把和当前page页对应的底部菜单item设为选中
            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //禁止ViewPager滑动
//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
    }

    //给ViewPager设置Fragment
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter=new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(BaseFragment.newInstance("这是频道页"));
        adapter.addFragment(BaseFragment.newInstance("这是好友页"));
        adapter.addFragment(new MeFragment());
        viewPager.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initView() {
       viewPager=(ViewPager) findViewById(R.id.viewPager);
       bottomNavigationView=(BottomNavigationView)findViewById(R.id.bottom_navigation);


        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
    }

    @Override
    public void finish() {
        System.out.println("man_finish被执行了！");
        super.finish();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 判断一下是不是从通知栏点击进入的
     */
    public void checkIsNotification(){
        Bundle bundle = getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE);
        //有bundle数据代表是从通知栏进入的，并且之前APP已经死掉了
        if (bundle!=null){
            Intent intent = new Intent(this, PingDaoActivity.class);
            intent.putExtra(MyConstant.EXTRA_BUNDLE,getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE));
            startActivity(intent);
        }
    }
}
