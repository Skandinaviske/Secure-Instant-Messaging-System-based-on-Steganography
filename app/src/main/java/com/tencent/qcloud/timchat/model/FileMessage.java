package com.tencent.qcloud.timchat.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMFileElem;
import com.tencent.imsdk.TIMMessage;
import com.tencent.qcloud.timchat.MyApplication;
import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.timchat.adapters.ChatAdapter;
import com.tencent.qcloud.timchat.ui.ChatActivity;
import com.tencent.qcloud.timchat.utils.FileOpen;
import com.tencent.qcloud.timchat.utils.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * 文件消息
 */
public class FileMessage extends Message {


    public FileMessage(TIMMessage message){
        this.message = message;
    }

    public FileMessage(String filePath){
        message = new TIMMessage();
        TIMFileElem elem = new TIMFileElem();
        elem.setPath(filePath);
        elem.setFileName(filePath.substring(filePath.lastIndexOf("/")+1));
        message.addElement(elem);
    }



    /**
     * 显示消息
     *
     * @param viewHolder 界面样式
     * @param context    显示消息的上下文
     */

    @Override
    public void showMessage(ChatAdapter.ViewHolder viewHolder, Context context) {
        clearView(viewHolder);
        if (checkRevoke(viewHolder)) return;
        TIMFileElem e = (TIMFileElem) message.getElement(0);
        TextView tv = new TextView(MyApplication.getContext());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setTextColor(MyApplication.getContext().getResources().getColor(isSelf() ? R.color.white : R.color.black));
        tv.setText(e.getFileName());
        getBubbleView(viewHolder).addView(tv);

        getBubbleView(viewHolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                save();

            }
        });
        showStatus(viewHolder);
    }

    /**
     * 获取消息摘要
     */
    @Override
    public String getSummary() {
        String str = getRevokeSummary();
        if (str != null) return str;
        return MyApplication.getContext().getString(R.string.summary_file);
    }

    /**
     * 保存消息或消息文件
     */
    @Override
    public void save() {
        if (message == null) return;
        final TIMFileElem e = (TIMFileElem) message.getElement(0);
        String[] str = e.getFileName().split("/");
        String filename = str[str.length-1];
        if (FileUtil.isCacheFileExist(filename)) {
            //Toast.makeText(MyApplication.getContext(), MyApplication.getContext().getString(R.string.save_exist),Toast.LENGTH_SHORT).show();
            File myFile = new File(FileUtil.getCacheFilePath(e.getFileName()));
            try {
                FileOpen.openFile(MyApplication.getContext(), myFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        if (MyApplication.getChatContext() == null) { return; }
        final ProgressDialog mProgressDialog = new ProgressDialog(MyApplication.getChatContext());
        mProgressDialog.setTitle("请稍候");
        mProgressDialog.setMessage("附件正在下载……");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        e.getToFile(FileUtil.getCacheFilePath(filename), new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                mProgressDialog.dismiss();
                Log.e(TAG, "getFile failed. code: " + i + " errmsg: " + s);
            }

            @Override
            public void onSuccess() {
                mProgressDialog.dismiss();
                Log.e(TAG+"-path", FileUtil.getCacheFilePath(e.getFileName()));
                File myFile = new File(FileUtil.getCacheFilePath(e.getFileName()));
                try {
                    FileOpen.openFile(MyApplication.getContext(), myFile);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }
}
