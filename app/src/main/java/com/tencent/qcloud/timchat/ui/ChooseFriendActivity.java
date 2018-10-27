package com.tencent.qcloud.timchat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.timchat.adapters.ExpandGroupListAdapter;
import com.tencent.qcloud.timchat.model.FriendProfile;
import com.tencent.qcloud.timchat.model.FriendshipInfo;
import com.tencent.qcloud.ui.TemplateTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChooseFriendActivity extends Activity {


    private ExpandGroupListAdapter mGroupListAdapter;
    private ExpandableListView mGroupListView;
    private List<FriendProfile> selectList = new ArrayList<>();
    private List<String> mAlreadySelect = new ArrayList<>();
    private List<FriendProfile> alreadySelectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_friend);
        TemplateTitle title = (TemplateTitle) findViewById(R.id.chooseTitle);
        List<String> selected = getIntent().getStringArrayListExtra("selected");
        if (selected != null){
            mAlreadySelect.addAll(selected);
        }
        title.setMoreTextAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectList.size() == 0){
                    Toast.makeText(ChooseFriendActivity.this, getString(R.string.choose_need_one), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra("select", getSelectIds());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        final Map<String, List<FriendProfile>> friends = FriendshipInfo.getInstance().getFriends();
        for (String id : mAlreadySelect){
            for (String key : friends.keySet()){
                for (FriendProfile profile : friends.get(key)){
                    if (id.equals(profile.getIdentify())){
                        profile.setIsSelected(true);
                        alreadySelectList.add(profile);
                    }
                }
            }
        }
        mGroupListView = (ExpandableListView) findViewById(R.id.groupList);
        mGroupListAdapter = new ExpandGroupListAdapter(this, FriendshipInfo.getInstance().getGroups(), friends, true);
        mGroupListView.setAdapter(mGroupListAdapter);
        mGroupListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                FriendProfile profile = friends.get(FriendshipInfo.getInstance().getGroups().get(groupPosition)).get(childPosition);
                if (alreadySelectList.contains(profile)) return false;
                onSelect(profile);
                mGroupListAdapter.notifyDataSetChanged();
                return false;
            }
        });
        mGroupListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        for (FriendProfile item : selectList){
            item.setIsSelected(false);
        }
        for (FriendProfile item : alreadySelectList){
            item.setIsSelected(false);
        }
    }

    private void onSelect(FriendProfile profile){
        if (!profile.isSelected()){
            selectList.add(profile);
        }else{
            selectList.remove(profile);
        }
        profile.setIsSelected(!profile.isSelected());
    }

    private ArrayList<String> getSelectIds(){
        ArrayList<String> result = new ArrayList<>();
        for (FriendProfile item : selectList){
            result.add(item.getIdentify());
        }
        return result;
    }
}
