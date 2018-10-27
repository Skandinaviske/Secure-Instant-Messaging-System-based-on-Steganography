package com.tencent.qcloud.timchat.model;

import android.content.Context;

import com.tencent.imsdk.TIMGroupMemberInfo;
import com.tencent.imsdk.TIMGroupMemberRoleType;
import com.tencent.qcloud.timchat.R;

import java.io.Serializable;

/**
 * 群成员数据
 */
public class GroupMemberProfile implements ProfileSummary,Serializable {

    private String name;
    private String id;
    private long quietTime;
    private TIMGroupMemberRoleType roleType;

    public GroupMemberProfile(TIMGroupMemberInfo info){
        name = info.getNameCard();
        id = info.getUser();
        quietTime = info.getSilenceSeconds();
        roleType = info.getRole();
    }

    /**
     * 获取头像资源
     */
    @Override
    public int getAvatarRes() {
        return R.drawable.head_other;
    }

    /**
     * 获取头像地址
     */
    @Override
    public String getAvatarUrl() {
        return null;
    }

    /**
     * 获取名字
     */
    @Override
    public String getName() {
        if (!name.equals("")){
            return name;
        }
        return id;
    }

    /**
     * 获取描述信息
     */
    @Override
    public String getDescription() {
        return null;
    }

    /**
     * 获取id
     */
    @Override
    public String getIdentify() {
        return id;
    }

    /**
     * 显示详情等点击事件
     *
     * @param context 上下文
     */
    @Override
    public void onClick(Context context) {

    }

    /**
     * 获取身份
     */
    public TIMGroupMemberRoleType getRole(){
        return roleType;
    }


    /**
     * 获取群名片
     */
    public String getNameCard(){
        if (name == null) return "";
        return name;
    }

    public long getQuietTime(){
        return quietTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setQuietTime(long quietTime) {
        this.quietTime = quietTime;
    }

    public void setRoleType(TIMGroupMemberRoleType roleType) {
        this.roleType = roleType;
    }
}
