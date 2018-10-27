package com.tencent.qcloud.timchat.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.sns.TIMFriendResult;
import com.tencent.qcloud.presentation.event.FriendshipEvent;
import com.tencent.qcloud.presentation.presenter.FriendshipManagerPresenter;
import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.timchat.adapters.GroupListAdapter;
import com.tencent.qcloud.timchat.model.FriendshipInfo;
import com.tencent.qcloud.ui.NotifyDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 好友分组管理
 */
public class ManageFriendGroupActivity extends FragmentActivity implements View.OnClickListener {

    private final String TAG = ManageFriendGroupActivity.class.getSimpleName();

    private ListView mMyGroupList;
    private GroupListAdapter mGroupListAdapter;
    private LinearLayout mAddGroup;
    private List<String> groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_group);
        mMyGroupList = (ListView) findViewById(R.id.group_list);
        mAddGroup = (LinearLayout) findViewById(R.id.add_group);
        mAddGroup.setOnClickListener(this);
        groups.addAll(FriendshipInfo.getInstance().getGroups());
        mGroupListAdapter = new GroupListAdapter(this, groups, this);
        mMyGroupList.setAdapter(mGroupListAdapter);


    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_group) {
            addDialog();
        }
    }


    private Dialog addGroupDialog;
    private void addDialog() {
        addGroupDialog = new Dialog(this, R.style.dialog);
        addGroupDialog.setContentView(R.layout.dialog_addgroup);
        TextView btnYes = (TextView) addGroupDialog.findViewById(R.id.confirm_btn);
        TextView btnNo = (TextView) addGroupDialog.findViewById(R.id.cancel_btn);
        final EditText inputView = (EditText) addGroupDialog.findViewById(R.id.input_group_name);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGroupDialog.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String groupname = inputView.getText().toString();
                if (groupname.equals("")) {
                    Toast.makeText(ManageFriendGroupActivity.this, getString(R.string.add_dialog_null), Toast.LENGTH_SHORT).show();
                } else {
                    FriendshipManagerPresenter.createFriendGroup(groupname, new TIMValueCallBack<List<TIMFriendResult>>() {
                        @Override
                        public void onError(int i, String s) {
                            Log.e(TAG, "onError code " + i + " msg " + s);
                            switch (i){
                                case 32218:
                                    //分组名称已存在
                                    Toast.makeText(ManageFriendGroupActivity.this, getString(R.string.add_group_error_existed), Toast.LENGTH_SHORT).show();
                                    break;
                                case 32214:
                                    //分组达到上限
                                    Toast.makeText(ManageFriendGroupActivity.this, getString(R.string.add_group_error_limit), Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(ManageFriendGroupActivity.this, getString(R.string.add_group_error), Toast.LENGTH_SHORT).show();
                                    break;

                            }
                        }

                        @Override
                        public void onSuccess(List<TIMFriendResult> timFriendResults) {
                            Toast.makeText(ManageFriendGroupActivity.this, getString(R.string.add_group_succ), Toast.LENGTH_SHORT).show();
//                            FriendshipEvent.getInstance().OnAddFriendGroups(null);
                            groups.add(groupname);
                            mGroupListAdapter.notifyDataSetChanged();
//                            FriendshipEvent.getInstance().OnAddFriendGroups(null);
                        }
                    });
                }
                addGroupDialog.dismiss();
            }
        });
        Window window = addGroupDialog.getWindow();
        window.setGravity(Gravity.TOP);
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setAttributes(lp);
        addGroupDialog.show();
    }

    public void deleteGroup(int position) {
        deleteDialog(position);
    }


    private void deleteDialog(final int position) {
        NotifyDialog dialog = new NotifyDialog();
        dialog.show(getString(R.string.delete_dialog_subtitle)+groups.get(position)+getString(R.string.delete_dialog_subtitle_sur), ManageFriendGroupActivity.this.getSupportFragmentManager(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FriendshipManagerPresenter.delFriendGroup(groups.get(position), new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Log.e(TAG, "onError code " + i + " msg " + s);
                        Toast.makeText(ManageFriendGroupActivity.this, getString(R.string.del_group_error), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(ManageFriendGroupActivity.this, getString(R.string.delete_group_succ), Toast.LENGTH_SHORT).show();
//                        FriendshipEvent.getInstance().OnDelFriendGroups(Collections.singletonList(groups.get(position)));
                        groups.remove(position);
                        mGroupListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }


}
