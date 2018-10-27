package com.tencent.qcloud.timchat.model;


import com.tencent.imsdk.ext.sns.TIMFriendFutureItem;
import com.tencent.imsdk.ext.sns.TIMFutureFriendType;

/**
 * 好友关系链消息的界面绑定数据
 * 可用于本地操作后界面修改
 */
public class FriendFuture {

    TIMFriendFutureItem futureItem;

    private TIMFutureFriendType type;

    public FriendFuture(TIMFriendFutureItem item){
        futureItem = item;
        type = futureItem.getType();
    }


    public TIMFutureFriendType getType() {
        return type;
    }

    public void setType(TIMFutureFriendType type) {
        this.type = type;
    }

    public String getName(){
        return futureItem.getProfile().getNickName().equals("") ? futureItem.getIdentifier() : futureItem.getProfile().getNickName();
    }

    public String getMessage(){
        return futureItem.getAddWording();
    }


    public String getIdentify(){
        return futureItem.getIdentifier();
    }



}
