package com.tencent.qcloud.timchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.timchat.ui.ManageFriendGroupActivity;

import java.util.List;

/**
 *
 */
public class GroupListAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mGroupList;
    private ManageFriendGroupActivity mManagerGroupActivity;
    public GroupListAdapter(Context context, List<String> grouplist,ManageFriendGroupActivity activity) {
        mContext = context;
        mGroupList = grouplist;
        mManagerGroupActivity = activity;
    }


    @Override
    public int getCount() {
        return mGroupList.size();
    }

    @Override
    public Object getItem(int i) {
        return mGroupList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int groupPosition, View convertView, ViewGroup viewGroup) {
        ItemHolder Holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_mylist, null);
            Holder = new ItemHolder();
            Holder.groupname = (TextView) convertView.findViewById(R.id.groupName);
            Holder.delete = (TextView) convertView.findViewById(R.id.delete_group);
            convertView.setTag(Holder);
        } else {
            Holder = (ItemHolder) convertView.getTag();
        }

        if(mGroupList.get(groupPosition).equals("")){
            Holder.groupname.setText(mContext.getResources().getString(R.string.default_group_name));
            Holder.delete.setVisibility(View.INVISIBLE);
        }else{
            Holder.groupname.setText(mGroupList.get(groupPosition));
            Holder.delete.setVisibility(View.VISIBLE);
        }
        Holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManagerGroupActivity.deleteGroup(groupPosition);
            }
        });
        return convertView;
    }

    class ItemHolder {
        public TextView groupname;
        public TextView delete;
    }

}
