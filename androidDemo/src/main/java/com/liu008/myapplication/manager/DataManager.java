package com.liu008.myapplication.manager;


import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.entity.CNitemInfo;
import com.liu008.myapplication.entity.IMuserInfo;
import com.liu008.myapplication.utils.ACache;
import com.liu008.myapplication.utils.NToast;
import com.liu008.myapplication.utils.UserUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.rong.message.ContactNotificationMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class DataManager {
    public static final String CNLISTCACHE = "CNlistCache";
    public static final String FRIENDSLISTCACHE = "friendslistCache";
    ACache cache = ACache.get(MyApplication.instance);
    private static DataManager s = new DataManager();
    private Handler handler = new Handler(Looper.getMainLooper());

    private DataManager() {
    }

    public static DataManager instance() {
        return s;
    }

    /**
     * 传入系统通知消息中所含的部分user信息，再去调网络把信息补全
     *
     * @param userInfo
     */
    public void saveCNuserinfo(final CNitemInfo userInfo) {
        UserUtils.getContactUserInfo(userInfo.getSourceId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String s = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map maps = (Map) JSON.parse(s);
                        //userInfo.setNickname((String)maps.get("nickName"));
                        userInfo.setName((String) maps.get("name"));
                        userInfo.setNickname((String) maps.get("nickName"));
                        userInfo.setPortraitUri((String) maps.get("headImageUrl"));
                        //1.先更新单条数据
                        putContactUserInfoCache(userInfo.getSourceId(), userInfo);
                        //2.更新好友通知LIST数据
                        updateCNlistCache(userInfo.getSourceId());
                    }
                });
            }
        });
    }


    /**
     * 把单条联系人信息写入缓存
     *
     * @param userId   联系人ID
     * @param userInfo 个人信息及关系数据
     */
    private void putContactUserInfoCache(String userId, CNitemInfo userInfo) {
        //单个条目写入缓存
        org.json.JSONObject jsonObject = null;
        try {
            jsonObject = new org.json.JSONObject(JSON.toJSONString(userInfo));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cache.put(userInfo.getSourceId(), jsonObject);
    }

    /**
     * 更新CNlist缓存,前提条件是该userId对应的条目已经更新了。
     *
     * @param userId
     */
    private void updateCNlistCache(String userId) {
        //整个LIST写入缓存
        JSONArray jsonArray = cache.getAsJSONArray(CNLISTCACHE);
        com.alibaba.fastjson.JSONArray fastjsonArray;
        boolean contant = false;
        if (jsonArray != null) {
            //原来有数据的话有必要对比一下
            fastjsonArray = com.alibaba.fastjson.JSONArray.parseArray(jsonArray.toString());
            for (int i = 0; i < fastjsonArray.size(); i++) {
                if (fastjsonArray.get(i).toString().equals(userId)) {
                    contant = true;
                }
            }
        } else {
            //没有的话直接插入
            fastjsonArray = new com.alibaba.fastjson.JSONArray();
        }
        if (contant == false) {
            fastjsonArray.add(userId);
            //save进缓存
            try {
                jsonArray = new JSONArray(fastjsonArray.toJSONString());
                cache.put(CNLISTCACHE, jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 更新好友列表
     * @param userId
     * @param flag   标志位 -1 删除  1 新增
     */
    public void updateFriendsList(String userId, int flag) {
        JSONArray jsonArray = cache.getAsJSONArray(FRIENDSLISTCACHE);
        com.alibaba.fastjson.JSONArray fastjsonArray;
        int index = -1;
        boolean contant = false;
        if (jsonArray != null) {
            //原来有数据的话有必要对比一下
            fastjsonArray = com.alibaba.fastjson.JSONArray.parseArray(jsonArray.toString());
            for (int i = 0; i < fastjsonArray.size(); i++) {
                if (fastjsonArray.get(i).toString().equals(userId)) {
                    contant = true;
                    index = i;
                    break;
                }
            }
        } else {
            fastjsonArray = new com.alibaba.fastjson.JSONArray();
        }
        switch (flag) {
            case -1://删除好友
                if (contant) {
                    fastjsonArray.remove(index);
                }
                break;
            case 1: //新增好友
                if (!contant) {
                    fastjsonArray.add(userId);
                }
                break;
        }
        //save进缓存
        try {
            jsonArray = new JSONArray(fastjsonArray.toJSONString());
            cache.put(FRIENDSLISTCACHE, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收到推送通知时写入好友通知数据
     */
    public void putContactNotificationData(ContactNotificationMessage message) {
        /**
         * 1.从message中拿取部分用户信息
         * 2.调用getContactUserInfo（userid）
         * 3.异步成功后存入
         */
        CNitemInfo userInfo = new CNitemInfo();
        userInfo.setSourceId(message.getSourceUserId());
        userInfo.setTargetId(message.getTargetUserId());
        userInfo.setMessage(message.getMessage());
        userInfo.setRelation(message.getExtra().charAt(0) - '0');
        userInfo.setStatusCode(message.getExtra().substring(1));
        //调异步执行方法去补全信息
        saveCNuserinfo(userInfo);
    }

    /**
     * 获取加好友通知的列表
     * //@param DataCallback callback
     *
     * @return
     */
    public List<CNitemInfo> getContactNotificationData() {
        //1.从缓存中取出JSONArray
        JSONArray jsonArray = cache.getAsJSONArray(CNLISTCACHE);
        if (jsonArray != null) {
            List<CNitemInfo> list = new ArrayList<>();
            //遍历数组重新装配
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String id = jsonArray.getString(i);//拿到userid
                    org.json.JSONObject jsonObject = cache.getAsJSONObject(id);//根据userid拿到JSONOBJECT
                    CNitemInfo item = JSONObject.parseObject(jsonObject.toString(), CNitemInfo.class);
                    list.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return list;
        }
        return null;
    }

    public void updateCNlistForCNitem(CNitemInfo userInfo) {
        if (userInfo != null && userInfo.getSourceId() != null) {
            //1.先更新单条数据
            putContactUserInfoCache(userInfo.getSourceId(), userInfo);
            //2.更新好友通知LIST数据
            updateCNlistCache(userInfo.getSourceId());
        }
    }

    public void clearCache() {
        cache.clear();
    }

    /**
     * 获取好友列表
     *
     * @return
     */
    public List<CNitemInfo> getContacts() {
        JSONArray jsonArray = cache.getAsJSONArray(FRIENDSLISTCACHE);
        if (jsonArray.length() == 0 || jsonArray == null) {
            //上服务器去下载
        } else {
            List<CNitemInfo> list = new ArrayList<>();
            //遍历数组重新装配
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String id = jsonArray.getString(i);//拿到userid
                    org.json.JSONObject jsonObject = cache.getAsJSONObject(id);//根据userid拿到JSONOBJECT
                    CNitemInfo item = JSONObject.parseObject(jsonObject.toString(), CNitemInfo.class);
                    list.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return list;
        }
        return null;
    }
}
