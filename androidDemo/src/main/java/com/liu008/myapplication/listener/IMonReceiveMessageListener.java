package com.liu008.myapplication.listener;

import android.os.IBinder;
import android.os.RemoteException;

import com.liu008.myapplication.manager.DataManager;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * IM消息监听类
 */
public class IMonReceiveMessageListener implements RongIMClient.OnReceiveMessageListener {
    /**
     * 收到消息的处理。
     *
     * @param message 收到的消息实体。
     * @param i    剩余未拉取消息数目。
     * @return 收到消息是否处理完成，true 表示自己处理铃声和后台通知，false 走融云默认处理方式。
     */
    @Override
    public boolean onReceived(Message message, int i) {
        //判断是不是加好友的系统消息
        if (message.getContent() instanceof ContactNotificationMessage){
            ContactNotificationMessage messgeContent=(ContactNotificationMessage)message.getContent();
           //把系统消息的用户信息写入缓存，系统会自动补全用户信息，并且会更新好友通知列表
            DataManager.instance().putContactNotificationData(messgeContent);
            //如果是验证通过的消息
            if ("AcceptResponse".equals(messgeContent.getOperation())){
                //通知写入好友列表缓存
                DataManager.instance().updateFriendsList(messgeContent.getSourceUserId(),1);
            }
        }

        return false;
    }
}
