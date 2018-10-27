package com.tencent.qcloud.timchat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.ui.TemplateTitle;

/**
 * 修改文本页面
 */
public class EditActivity extends Activity implements TIMCallBack{


    private static EditInterface editAction;
    public final static String RETURN_EXTRA = "result";
    private static String defaultString;
    private EditText input;
    private static int lenLimit;

    /**
     * 启动修改文本界面
     *
     * @param context fragment context
     * @param title 界面标题
     * @param defaultStr 默认文案
     * @param reqCode 请求码，用于识别返回结果
     * @param action 操作回调
     */
    public static void navToEdit(Fragment context, String title, String defaultStr, int reqCode,EditInterface action){
        Intent intent = new Intent(context.getActivity(), EditActivity.class);
        intent.putExtra("title", title);
        context.startActivityForResult(intent, reqCode);
        defaultString = defaultStr;
        editAction = action;
    }


    /**
     * 启动修改文本界面
     *
     * @param context activity context
     * @param title 界面标题
     * @param defaultStr 默认文案
     * @param reqCode 请求码，用于识别返回结果
     * @param action 操作回调
     */
    public static void navToEdit(Activity context, String title, String defaultStr, int reqCode,EditInterface action){
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra("title", title);
        context.startActivityForResult(intent, reqCode);
        defaultString = defaultStr;
        editAction = action;
    }


    /**
     * 启动修改文本界面
     *
     * @param context fragment context
     * @param title 界面标题
     * @param defaultStr 默认文案
     * @param reqCode 请求码，用于识别返回结果
     * @param action 操作回调
     * @param limit 输入长度限制
     */
    public static void navToEdit(Fragment context, String title, String defaultStr, int reqCode,EditInterface action,int limit){
        Intent intent = new Intent(context.getActivity(), EditActivity.class);
        intent.putExtra("title", title);
        context.startActivityForResult(intent, reqCode);
        defaultString = defaultStr;
        editAction = action;
        lenLimit = limit;
    }


    /**
     * 启动修改文本界面
     *
     * @param context activity context
     * @param title 界面标题
     * @param defaultStr 默认文案
     * @param reqCode 请求码，用于识别返回结果
     * @param action 操作回调
     * @param limit 输入长度限制
     */
    public static void navToEdit(Activity context, String title, String defaultStr, int reqCode,EditInterface action,int limit){
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra("title", title);
        context.startActivityForResult(intent, reqCode);
        defaultString = defaultStr;
        editAction = action;
        lenLimit = limit;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getIntent().getStringExtra("title");
        input = (EditText) findViewById(R.id.editContent);
        if (defaultString != null){
            input.setText(defaultString);
            input.setSelection(defaultString.length());
        }
        if (lenLimit != 0){
            input.setFilters( new InputFilter[] { new InputFilter.LengthFilter(lenLimit) } );
        }
        TemplateTitle title = (TemplateTitle) findViewById(R.id.editTitle);
        title.setTitleText(getIntent().getStringExtra("title"));
        title.setMoreTextAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAction.onEdit(input.getText().toString(),EditActivity.this);
            }
        });


    }

    @Override
    protected void onStop(){
        super.onStop();
        defaultString = null;
        editAction = null;
        lenLimit = 0;
    }

    @Override
    public void onError(int i, String s) {
        Toast.makeText(this,getResources().getString(R.string.edit_error),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess() {
        Intent intent = new Intent();
        intent.putExtra(RETURN_EXTRA,input.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public interface EditInterface{
        void onEdit(String text, TIMCallBack callBack);
    }
}
