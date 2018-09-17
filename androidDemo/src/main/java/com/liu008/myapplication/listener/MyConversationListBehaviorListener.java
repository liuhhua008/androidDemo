package com.liu008.myapplication.listener;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.liu008.myapplication.activity.me.ContactInfoActivity;

import java.util.Map;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;

/**
 * 融云消息监听
 */
public class MyConversationListBehaviorListener implements RongIM.ConversationListBehaviorListener {
    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    /**
     * 点击他就会启动单聊的窗口，而单聊窗口中不可能进行添加好友的操作吧，因此就要拦截这个ContactNTF信息
     * @param context
     * @param view
     * @param uiConversation
     * @return
     */
    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        Log.i("conversationlist", "click");
        //1.判断消息类型是不是-联系人(好友)通知消息-
        if(uiConversation.getMessageContent() instanceof ContactNotificationMessage)
        {
            ContactNotificationMessage message = (ContactNotificationMessage) uiConversation.getMessageContent();
            Log.i("conversationlist", "contactmessage");
            //2.判断是不是好友请求消息
            if(message.getOperation().equals(ContactNotificationMessage.CONTACT_OPERATION_REQUEST))
            {

                //这里进行你自己的操作，我是启动了另一个Activity来处理这个消息
                Intent intent=new Intent(context, ContactInfoActivity.class);
                String json=message.getExtra();
                Map map= (Map) JSONObject.parse(json);
                UserInfo userinfo = RongUserInfoManager.getInstance().getUserInfo(message.getTargetUserId());
                intent.putExtra("uid",message.getTargetUserId());
                //intent.putExtra("name",message.get);//电话号码
                intent.putExtra("nickname",userinfo.getName());
                intent.putExtra("imageUri",userinfo.getPortraitUri());
//                intent.putExtra("relation",(int) map.get("relation"));
//                intent.putExtra("statusCode",(String) map.get("statusCode"));
                context.startActivity(intent);
            }
            else if(message.getOperation().equals(ContactNotificationMessage.CONTACT_OPERATION_ACCEPT_RESPONSE))
            {}
            else if(message.getOperation().equals(ContactNotificationMessage.CONTACT_OPERATION_REJECT_RESPONSE))
            {}


            return true;
        }
        else
        {
            return false;
        }
    }
}
