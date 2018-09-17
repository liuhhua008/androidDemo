package com.liu008.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liu008.myapplication.activity.MyPhoneContactActivity;
import com.liu008.myapplication.activity.me.ContactsListActivity;
import com.liu008.myapplication.adapter.ViewPagerAdapter;
import com.liu008.myapplication.utils.DragPointView;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.NToast;
import com.liu008.myapplication.activity.BaseActivity;
import com.liu008.myapplication.fragment.MeFragment;
import com.liu008.myapplication.activity.PingDaoActivity;
import com.liu008.myapplication.utils.NetUtils;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class MainActivity extends BaseActivity implements IUnReadMessageObserver, DragPointView.OnDragListencer, View.OnClickListener {
    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private TextView tvNotice;
    private View rl_PingDaoSetting;
    /**
     * 会话列表的fragment
     */
    private ConversationListFragment mConversationListFragment = null;
    //消息红色小标
    private DragPointView mUnreadNumView;
    private Conversation.ConversationType[] mConversationsTypes = null;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("info", "activity_onCreate-执行了: ");
        //聊天会话列表的监听注册
        //RongIM.setConversationListBehaviorListener(new MyConversationListBehaviorListener());
        initView();
        //如果是从频道通知栏点击激发启动的，应该继续跳转
        checkIsNotification();
        setListener();
        //给ViewPager设置Fragment
        setupViewPager(viewPager);
    }

    /**
     * 刷新时检查一下网络连接情况
     */
    @Override
    protected void onResume() {
        //检查一下网络是否在线
        if(!NetUtils.checkOnlineState(this)){
            tvNotice.setVisibility(View.VISIBLE);
        }else {
            tvNotice.setVisibility(View.GONE);
        }
        super.onResume();
    }

    private void setListener() {
        //底部菜单点击监听，指定对应的VIEWPAGER页面
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                onResume();
                switch (item.getItemId()) {
                    case R.id.item_pintao:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.item_xiaoxi:
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
        mUnreadNumView.setOnClickListener(this);
        mUnreadNumView.setDragListencer(this);
    }

    //给ViewPager设置Fragment
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(BaseFragment.newInstance("这是频道页"));
        //设置消息页--------------------------------
        ConversationListFragment listFragment = (ConversationListFragment) ConversationListFragment.instantiate(this, ConversationListFragment.class.getName());
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                //设置私聊会话是否聚合显示
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false")
                //群组
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")
                //公共服务号
                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")
                //订阅号
                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")
                //系统
                //.appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")
                .build();
        mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.PUBLIC_SERVICE,
                Conversation.ConversationType.APP_PUBLIC_SERVICE,
                //Conversation.ConversationType.SYSTEM,
                Conversation.ConversationType.DISCUSSION
        };
        listFragment.setUri(uri);
        adapter.addFragment(listFragment);
        adapter.addFragment(new MeFragment());
        viewPager.setAdapter(adapter);
        initData();
    }

    private void initData() {
        final Conversation.ConversationType[] conversationTypes = {
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP, Conversation.ConversationType.SYSTEM,
                Conversation.ConversationType.PUBLIC_SERVICE, Conversation.ConversationType.APP_PUBLIC_SERVICE
        };
        //设置未读消息都有哪些类型
        RongIM.getInstance().addUnReadMessageCountChangedObserver(this, conversationTypes);

        //刷新用户缓存数据。
//      RongIM.getInstance().refreshUserInfoCache(new UserInfo(connectResultId, nickName, Uri.parse(portraitUri)));
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initView() {
        tvNotice=findViewById(R.id.tv_main_notice);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        // mUnreadNumView = (DragPointView) findViewById(R.id.seal_num);
        LinearLayout gallery= (LinearLayout) bottomNavigationView.getMenu().findItem(R.id.item_xiaoxi).getActionView();
        mUnreadNumView = gallery.findViewById(R.id.seal_num);
        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        toolbar=findViewById(R.id.man_toolbar);
        //生成选项菜单
        toolbar.inflateMenu(R.menu.menu_mantoolbar);
        setSupportActionBar(toolbar);
        ((MenuBuilder)toolbar.getMenu()).setOptionalIconsVisible(true);

    }

    /**
     * 显示toolbar那三个点-更多,以及右则图标
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mantoolbar, menu);
        return true;
    }

    /**
     * 标题栏菜单图标点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_friends://好友列表
                startActivity(new Intent(this, ContactsListActivity.class));
                break;
            case R.id.test_menu1:
                Toast.makeText(MainActivity.this, "菜单1", Toast.LENGTH_SHORT).show();
                break;
            case R.id.test_menu2:
                Toast.makeText(MainActivity.this, "菜单2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.test_menu3:
                Toast.makeText(MainActivity.this, "菜单3", Toast.LENGTH_SHORT).show();
                break;
            default:break;
        }
        return true;

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
    public void checkIsNotification() {
        Bundle bundle = getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE);
        //有bundle数据代表是从通知栏进入的，并且之前APP已经死掉了
        if (bundle != null) {
            Intent intent = new Intent(this, PingDaoActivity.class);
            intent.putExtra(MyConstant.EXTRA_BUNDLE, getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 设置未读消息的显示
     * IUnReadMessageObserver接口
     * @param count 未读消息数量
     */
    @Override
    public void onCountChanged(int count) {
        if (count == 0) {
            mUnreadNumView.setVisibility(View.GONE);
        } else if (count > 0 && count < 100) {
            mUnreadNumView.setVisibility(View.VISIBLE);
            mUnreadNumView.setText(String.valueOf(count));
        } else {
            mUnreadNumView.setVisibility(View.VISIBLE);
            mUnreadNumView.setText("...");
        }
    }


    /**
     * 清除未读消息
     */
    @Override
    public void onDragOut() {
        mUnreadNumView.setVisibility(View.GONE);
        NToast.shortToast(this, "清理成功");
        RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations != null && conversations.size() > 0) {
                    for (Conversation c : conversations) {
                        RongIM.getInstance().clearMessagesUnreadStatus(c.getConversationType(), c.getTargetId(), null);
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        }, mConversationsTypes);

    }

    @Override
    public void onClick(View view) {

    }
}
