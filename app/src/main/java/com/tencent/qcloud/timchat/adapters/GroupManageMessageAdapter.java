package com.tencent.qcloud.timchat.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.ext.group.TIMGroupPendencyGetType;
import com.tencent.imsdk.ext.group.TIMGroupPendencyHandledStatus;
import com.tencent.imsdk.ext.group.TIMGroupPendencyItem;
import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.timchat.model.GroupFuture;
import com.tencent.qcloud.timchat.model.UserInfo;
import com.tencent.qcloud.ui.CircleImageView;

import java.util.List;

/**
 * 群管理消息adapter
 */
public class GroupManageMessageAdapter extends ArrayAdapter<GroupFuture> {


    private int resourceId;
    private View view;
    private ViewHolder viewHolder;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public GroupManageMessageAdapter(Context context, int resource, List<GroupFuture> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView != null){
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }else{
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (CircleImageView) view.findViewById(R.id.avatar);
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.des = (TextView) view.findViewById(R.id.description);
            viewHolder.remark = (TextView) view.findViewById(R.id.remark);
            viewHolder.status = (TextView) view.findViewById(R.id.status);
            view.setTag(viewHolder);
        }
        Resources res = getContext().getResources();
        final TIMGroupPendencyItem data = getItem(position).getFutureItem();
        String from = data.getFromUser(), to = data.getToUser();
        if (data.getPendencyType() == TIMGroupPendencyGetType.APPLY_BY_SELF){
            if (from.equals(UserInfo.getInstance().getId())){
                //自己申请加入群
                viewHolder.avatar.setImageResource(R.drawable.head_group);
                viewHolder.name.setText(data.getGroupId());
                viewHolder.des.setText(String.format("%s%s",res.getString(R.string.summary_me),res.getString(R.string.summary_group_apply)));
            }else{
                //别人申请加入我的群
                viewHolder.avatar.setImageResource(R.drawable.head_other);
                viewHolder.name.setText(from);
                viewHolder.des.setText(String.format("%s%s",res.getString(R.string.summary_group_apply),data.getGroupId()));
            }
            viewHolder.remark.setText(data.getRequestMsg());
        }else{
            if (to.equals(UserInfo.getInstance().getId())){
                //别人邀请我入群
                viewHolder.avatar.setImageResource(R.drawable.head_group);
                viewHolder.name.setText(data.getGroupId());
                viewHolder.des.setText(String.format("%s%s%s",res.getString(R.string.summary_group_invite),res.getString(R.string.summary_me),res.getString(R.string.summary_group_add)));
            }else{
                //邀请别人入群
                viewHolder.avatar.setImageResource(R.drawable.head_other);
                viewHolder.name.setText(to);
                viewHolder.des.setText(String.format("%sTA%s%s",res.getString(R.string.summary_group_invite),res.getString(R.string.summary_group_add),data.getGroupId()));
            }
            viewHolder.remark.setText(String.format("%s %s",res.getString(R.string.summary_invite_person),from));
        }

        switch (getItem(position).getType()){
            case HANDLED_BY_OTHER:
            case HANDLED_BY_SELF:
                viewHolder.status.setText(res.getString(R.string.agreed));
                viewHolder.status.setTextColor(res.getColor(R.color.text_gray1));
                viewHolder.status.setOnClickListener(null);
                break;
            case NOT_HANDLED:
                viewHolder.status.setText(res.getString(R.string.agree));
                viewHolder.status.setTextColor(res.getColor(R.color.text_blue1));
                viewHolder.status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        data.accept(null, new TIMCallBack() {
                            @Override
                            public void onError(int i, String s) {
                                if (i == 10013){
                                    //已经是群成员
                                    Toast.makeText(getContext(), getContext().getString(R.string.group_member_already), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onSuccess() {
                                getItem(position).setType(TIMGroupPendencyHandledStatus.HANDLED_BY_SELF);
                                notifyDataSetChanged();
                            }
                        });
                    }
                });
                break;
        }

        return view;
    }


    public class ViewHolder{
        ImageView avatar;
        TextView name;
        TextView des;
        TextView remark;
        TextView status;
    }
}
