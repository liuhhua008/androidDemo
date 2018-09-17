package com.liu008.myapplication.activity.me;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.liu008.myapplication.R;
import com.liu008.myapplication.activity.AddFriendActivity;
import com.liu008.myapplication.activity.MyPhoneContactActivity;
import com.liu008.myapplication.adapter.SortAdapter;
import com.liu008.myapplication.entity.CNitemInfo;
import com.liu008.myapplication.entity.IMuserInfo;
import com.liu008.myapplication.manager.DataManager;
import com.liu008.myapplication.sortlist.CharacterParser;
import com.liu008.myapplication.sortlist.SideBar;
import com.liu008.myapplication.sortlist.SortModel;
import com.liu008.myapplication.utils.ConstactUtil;
import com.liu008.myapplication.utils.PinyinComparator;
import com.liu008.myapplication.utils.ToolUtils;
import com.liu008.myapplication.view.ClearEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ContactsListActivity extends AppCompatActivity {
    private View mBaseView;
    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private ClearEditText mClearEditText;
    private List<CNitemInfo> contacts;

    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;

    private PinyinComparator pinyinComparator;
    private Toolbar toolbar;
    private Button btnRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        initView();
        initData();

    }

    private void initView() {
        sideBar = (SideBar) this.findViewById(R.id.sidrbar);
        dialog = (TextView) this.findViewById(R.id.dialog);
        sortListView = (ListView) this.findViewById(R.id.sortlist);
        //设置标题栏
        toolbar = findViewById(R.id.man_toolbar);
        btnRight = (Button) toolbar.findViewById(R.id.btnRight);
        btnRight.setVisibility(View.VISIBLE);
        toolbar.setTitle("我的好友");
        setSupportActionBar(toolbar);
        //设置返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //设置右侧加号
        setToolbarRight(null, R.drawable.de_ic_add, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactsListActivity.this,AddFriendActivity.class));
            }
        });
    }

    /**
     * 设置右边按键的显示方法
     *
     * @param text
     * @param icon
     * @param btnClick
     */
    protected void setToolbarRight(String text, @Nullable Integer icon, View.OnClickListener btnClick) {
        if (text != null) {
            btnRight.setText(text);
        }
        if (icon != null) {
            btnRight.setBackgroundResource(icon.intValue());
            ViewGroup.LayoutParams linearParams = btnRight.getLayoutParams();
            linearParams.height = ToolUtils.dip2px(this, 26);
            linearParams.width = ToolUtils.dip2px(this, 26);
            btnRight.setLayoutParams(linearParams);
        }
        btnRight.setOnClickListener(btnClick);
    }

    private void initData() {
        // 实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();

        sideBar.setTextView(dialog);
        //点击返回
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @SuppressLint("NewApi")
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sortListView.setSelection(position);
                }
            }
        });

        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                CNitemInfo itemInfo=adapter.getItem(position).getUserinfo();
                IMuserInfo info=new IMuserInfo();
                info.setUserId(itemInfo.getSourceId());
                info.setName(itemInfo.getName());
                info.setNickname(itemInfo.getNickname());
                info.setPortraitUri(itemInfo.getPortraitUri());
                info.setRelation(itemInfo.getRelation());
                info.setStatusCode(itemInfo.getStatusCode());
                startContactInfoActivity(info);
            }
        });

        new ContactsListActivity.ConstactAsyncTask().execute(0);

    }
    /**
     * 跳转到详细信息页。
     * 个人信息用intent发过去
     * @param userinfo
     */
    private void  startContactInfoActivity(IMuserInfo userinfo){
        Intent intent=new Intent(ContactsListActivity.this, ContactInfoActivity.class);
        intent.putExtra("uid",userinfo.getUserId());
        intent.putExtra("name",userinfo.getName());
        intent.putExtra("nickname",userinfo.getNickname());
        intent.putExtra("imageUri",userinfo.getPortraitUri());
        intent.putExtra("relation",userinfo.getRelation());
        intent.putExtra("statusCode",userinfo.getStatusCode());
        startActivity(intent);
    }
    /**
     * 后台取数据
     */
    private class ConstactAsyncTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... arg0) {
            int result = -1;
            //获取原始列表数据
            //callRecords = ConstactUtil.getAllCallRecords(ContactsListActivity.this);
            contacts= DataManager.instance().getContacts();
            result = 1;
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                List<CNitemInfo> constact = new ArrayList<CNitemInfo>();
//                //把map集合搞到list里去
//                for (Iterator<String> keys = contacts.keySet().iterator(); keys.hasNext(); ) {
//                    String key = keys.next();//userid
//                    constact.add(contacts.get(key).getNickname());//用户昵称
//                }
//                String[] names = new String[]{};
//                //集合转数组
//                names = constact.toArray(names);
                //把数据的首字母拿出来弄成List<SortModel>再拿回来作为源数据
                SourceDateList = filledData(contacts);

                // 根据a-z进行排序源数据
                Collections.sort(SourceDateList, pinyinComparator);
                adapter = new SortAdapter(ContactsListActivity.this, SourceDateList);
                sortListView.setAdapter(adapter);

                mClearEditText = (ClearEditText) ContactsListActivity.this
                        .findViewById(R.id.filter_edit);
                mClearEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View arg0, boolean arg1) {
                        mClearEditText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

                    }
                });
                // 根据输入框输入值的改变来过滤搜索
                mClearEditText.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                        filterData(s.toString());
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledData(List<CNitemInfo> date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < date.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(date.get(i).getNickname());//放入名字
            sortModel.setUserinfo(date.get(i));//放入userinfo
            // 汉字转换成拼音
            String pinyin = characterParser.getSelling(date.get(i).getNickname());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());//放入首字母
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            //没有输入值就直接给源数据
            filterDateList = SourceDateList;
        } else {
            //先清空一下数据
            filterDateList.clear();
            //此处是关键，把源数据中每一条都拿出来看其中是否包含关键字
            for (SortModel sortModel : SourceDateList) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1//关键字在本条数据中有出现
                        || characterParser.getSelling(name).startsWith(//或者是转成拼音看是否包含
                        filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        //通过更新listview的adapter内的方法来重新显示结果
        adapter.updateListView(filterDateList);
    }


    //设置EditText失去焦点应收起软键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            boolean hideInputResult = isShouldHideInput(v, ev);
            Log.v("hideInputResult", "zzz-->>" + hideInputResult);
            if (hideInputResult) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) ContactsListActivity.this
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (v != null) {
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            //之前一直不成功的原因是,getX获取的是相对父视图的坐标,getRawX获取的才是相对屏幕原点的坐标！！！
            Log.v("leftTop[]", "zz--left:" + left + "--top:" + top + "--bottom:" + bottom + "--right:" + right);
            Log.v("event", "zz--getX():" + event.getRawX() + "--getY():" + event.getRawY());
            if (event.getRawX() > left && event.getRawX() < right
                    && event.getRawY() > top && event.getRawY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
