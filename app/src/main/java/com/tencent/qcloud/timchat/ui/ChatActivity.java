package com.tencent.qcloud.timchat.ui;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMFaceElem;
import com.tencent.imsdk.TIMImage;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageStatus;
import com.tencent.imsdk.TIMSoundElem;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.ext.message.TIMMessageDraft;
import com.tencent.imsdk.ext.message.TIMMessageExt;
import com.tencent.imsdk.ext.message.TIMMessageLocator;
import com.tencent.qcloud.presentation.presenter.ChatPresenter;
import com.tencent.qcloud.presentation.viewfeatures.ChatView;
import com.tencent.qcloud.timchat.MyApplication;
import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.timchat.adapters.ChatAdapter;
import com.tencent.qcloud.timchat.model.CustomMessage;
import com.tencent.qcloud.timchat.model.FileMessage;
import com.tencent.qcloud.timchat.model.FriendProfile;
import com.tencent.qcloud.timchat.model.FriendshipInfo;
import com.tencent.qcloud.timchat.model.GroupInfo;
import com.tencent.qcloud.timchat.model.ImageMessage;
import com.tencent.qcloud.timchat.model.Message;
import com.tencent.qcloud.timchat.model.MessageFactory;
import com.tencent.qcloud.timchat.model.TextMessage;
import com.tencent.qcloud.timchat.model.UGCMessage;
import com.tencent.qcloud.timchat.model.VideoMessage;
import com.tencent.qcloud.timchat.model.VoiceMessage;
import com.tencent.qcloud.timchat.utils.FileUtil;
import com.tencent.qcloud.timchat.utils.MediaUtil;
import com.tencent.qcloud.timchat.utils.RecorderUtil;
import com.tencent.qcloud.ui.ChatInput;
import com.tencent.qcloud.ui.TemplateTitle;
import com.tencent.qcloud.ui.VoiceSendingView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.mobistego.MainActivity;
import it.mobistego.fragments.MainFragment;
import it.mobistego.fragments.midway;

public class ChatActivity extends FragmentActivity implements ChatView {

    private static final String TAG = "ChatActivity";

    private List<Message> messageList = new ArrayList<>();
    private ChatAdapter adapter;
    private ListView listView;
    private ChatPresenter presenter;
    private ChatInput input;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int IMAGE_STORE = 200;
    private static final int FILE_CODE = 300;
    private static final int IMAGE_PREVIEW = 400;
    private static final int VIDEO_RECORD = 500;
    public static String TABLE_NAME = "person";// 表名
    public static String FIELD_ID = "id";// 列名
    public static String  FIELD_NAME= "name";// 列名
    public static String FIELD_STATUS = "status";//列名
    public static String FIELD_PRIORITY = "priority";//列名
    public static String FIELD_TIME = "time";//列名
    public static String FIELD_CONENT = "content";//列名
    private Uri fileUri;
    private VoiceSendingView voiceSendingView;
    private String identify;
    private RecorderUtil recorder = new RecorderUtil();
    private TIMConversationType type;
    private String titleStr;
    private Handler handler = new Handler();


    public static void navToChat(Context context, String identify, TIMConversationType type){
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("identify", identify);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }


    /*public class BIGNUM
    {
        long longnum;
        int top;
        int dmax;
        int neg;
        int flags;
    }
    public int jni(){
        int i;
    BIGNUM struct;
    struct=  initpg();
    i=struct.dmax;
        return i;
    }

    static{
        System.loadLibrary("native-lib");
    }
    public native BIGNUM initpg();*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        identify = getIntent().getStringExtra("identify");
        type = (TIMConversationType) getIntent().getSerializableExtra("type");
        presenter = new ChatPresenter(this, identify, type);
        input = (ChatInput) findViewById(R.id.input_panel);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(ChatActivity.this, "DH密钥交互成功", Toast.LENGTH_LONG).show();
        input.setChatView(this);
        adapter = new ChatAdapter(this, R.layout.item_message, messageList);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        input.setInputMode(ChatInput.InputMode.NONE);
                        break;
                }
                return false;
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int firstItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && firstItem == 0) {
                    //如果拉到顶端读取更多消息
                    presenter.getMessage(messageList.size() > 0 ? messageList.get(0).getMessage() : null);

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItem = firstVisibleItem;
            }
        });
        registerForContextMenu(listView);
        TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);
        switch (type) {
            case C2C:
                title.setMoreImg(R.drawable.btn_person);
                if (FriendshipInfo.getInstance().isFriend(identify)){
                    title.setMoreImgAction(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                            intent.putExtra("identify", identify);
                            startActivity(intent);
                        }
                    });
                    FriendProfile profile = FriendshipInfo.getInstance().getProfile(identify);
                    title.setTitleText(titleStr = profile == null ? identify : profile.getName());
                }else{
                    title.setMoreImgAction(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent person = new Intent(ChatActivity.this,AddFriendActivity.class);
                            person.putExtra("id",identify);
                            person.putExtra("name",identify);
                            startActivity(person);
                        }
                    });
                    title.setTitleText(titleStr = identify);
                }
                break;
            case Group:
                title.setMoreImg(R.drawable.btn_group);
                title.setMoreImgAction(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, GroupProfileActivity.class);
                        intent.putExtra("identify", identify);
                        startActivity(intent);
                    }
                });
                title.setTitleText(GroupInfo.getInstance().getGroupName(identify));
                break;

        }
        voiceSendingView = (VoiceSendingView) findViewById(R.id.voice_sending);
        presenter.start();
        MyApplication.setChatContext(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        //退出聊天界面时输入框有内容，保存草稿
        if (input.getText().length() > 0){
            TextMessage message = new TextMessage(input.getText());
            presenter.saveDraft(message.getMessage());
        }else{
            presenter.saveDraft(null);
        }
//        RefreshEvent.getInstance().onRefresh();
        presenter.readMessages();
        MediaUtil.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stop();
    }


    /**
     * 显示消息
     *
     * @param message
     */
    @Override
    public void showMessage(TIMMessage message) {
        if (message == null) {
            adapter.notifyDataSetChanged();
        } else {
            Message mMessage = MessageFactory.getMessage(message);
            //Log.d("bigmom5",mMessage.getMessage().getElement(0).toString());
            if (mMessage != null) {
                if (mMessage instanceof CustomMessage){
                    CustomMessage.Type messageType = ((CustomMessage) mMessage).getType();
                    switch (messageType){
                        case TYPING:
                            TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);
                            title.setTitleText(getString(R.string.chat_typing));
                            handler.removeCallbacks(resetTitle);
                            handler.postDelayed(resetTitle,3000);
                            break;
                        default:
                            break;
                    }
                }else{
                    if (messageList.size()==0){
                        mMessage.setHasTime(null);
                    }else{
                        mMessage.setHasTime(messageList.get(messageList.size()-1).getMessage());
                    }
                    messageList.add(mMessage);
                    final String name = mMessage.getMessage().getSender().toString();
                    System.out.println(name+"???????");
                    final String status = mMessage.getMessage().status().toString();
                    System.out.println(status+"=======");
                    final String priority = mMessage.getMessage().getPriority().toString();
                    System.out.println(priority+"<<<<<<<");
                    final String time = getTime();
                    System.out.println(time+"]]]]]]");
                    TIMMessage item=mMessage.getMessage();
                    String result = "";
                    final String[] finalResult = {result};
                    for (int i = 0; i < item.getElementCount(); ++i) {

                    if ( item.getElement(i).getType() == TIMElemType.Text) {
                        TIMTextElem t = (TIMTextElem)item.getElement(i);
                        result += t.getText();
                        Log.d("bigmom4", result);
                        getTime();
                    }
                    else if ( item.getElement(i).getType() == TIMElemType.Face) {
                        TIMFaceElem faceElem = (TIMFaceElem) item.getElement(i);
                        byte[] data = faceElem.getData();
                        if (data != null){
                            result += new String(data, Charset.forName("UTF-8"));
                        }
                        Log.d("bigmom4", result);
                        getTime();
                    }
                    else if (item.getElement(i).getType() == TIMElemType.Image) {
                        if(mMessage.getMessage().status().toString()=="Sending") {
                            final TIMImageElem e = (TIMImageElem) item.getElement(i);
                            final String path = e.getPath();
                            //final String uuid = image.getUuid()

                            //final String[] finalResult = {result};
                            Thread thread = new Thread() {
                                @Override
                                public void run() {

                                    File xing = new File(path);
                                    InputStream in = null;
                                    try {
                                        in = new FileInputStream(xing);
                                    } catch (FileNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                    byte[] a = readStream(in);
                                    String ex = Base64.encodeToString(a, Base64.NO_WRAP);
                                    Log.d("bigmomx", ex);
                                   finalResult[0] =ex;
                                    getTime();
                                }
                            };
                            thread.start();
                        }else {final TIMImageElem e = (TIMImageElem) item.getElement(i);
                            for (final TIMImage image : e.getImageList()) {

                                //final String uuid = image.getUuid()
                                //final String[] finalResult = {result};
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        String x=image.getUrl();
                                        String b64 = getStringByGet(x);;
                                        Log.d("bigmomx",b64);
                                        finalResult[0]=b64;
                                        getTime();
                                    }
                                };
                                thread.start();

                            }
                        }


                    }
                    else if(item.getElement(i).getType() == TIMElemType.Sound) {
                        if (mMessage.getMessage().status().toString() == "Sending") {
                            final TIMSoundElem s = (TIMSoundElem) item.getElement(i);
                            final String path = s.getPath();
                            Log.d("bigmoms", path);

                            //final String uuid = image.getUuid()

                            //final String[] finalResult1 ={result};
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    File xing2 = new File(path);
                                    InputStream in = null;
                                    try {
                                        in = new FileInputStream(xing2);
                                    } catch (FileNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                    byte[] a = readStream(in);
                                    String ex = Base64.encodeToString(a, Base64.NO_WRAP);
                                    Log.d("bigmoms", ex);
                                    finalResult[0] = ex;
                                    getTime();
                                }
                            };
                            thread.start();

                        } else {
                            TIMSoundElem s = (TIMSoundElem) item.getElement(i);
                            s.getSoundToFile("/storage/emulated/0/Android/data/com.tencent.qcloud.timchat/cache/tempAudios", new TIMCallBack() {
                                @Override
                                public void onError(int i, String s) {
                                }

                                @Override
                                public void onSuccess() {
                                }
                            });
                            final String path = "/storage/emulated/0/Android/data/com.tencent.qcloud.timchat/cache/tempAudios";
                            //final String[] finalResult = {result};
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    File xing2 = new File(path);
                                    InputStream in = null;
                                    try {
                                        in = new FileInputStream(xing2);
                                    } catch (FileNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                    byte[] a = readStream(in);
                                    String ex = Base64.encodeToString(a, Base64.NO_WRAP);
                                    Log.d("bigmoms", ex);
                                    finalResult[0] = ex;
                                    getTime();
                                }
                            };
                            thread.start();

                        }
                    }
                    }
                    /*Thread thread = new Thread() {
                        @Override
                        public void run() {
                            SQLiteDatabase.loadLibs(getApplicationContext());
                            File databaseFile = getDatabasePath("test_cipher.db");
                            databaseFile.mkdirs();
                            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, "test123", null);
                            String sql = "INSERT INTO "+TABLE_NAME+"("+"'"+FIELD_NAME+"','"+FIELD_STATUS+"','"+FIELD_PRIORITY+"','"+FIELD_TIME+"','"+FIELD_CONENT+"')"+"VALUES ("+"'"+name+"','"+status+"','"+priority+"','"+time+"','"+finalResult[0]+"')";
                            try {
                                db.execSQL(sql);
                            } catch (SQLException e) {
                                Log.e(TAG, "onInsert " + TABLE_NAME + " Error " + e.toString());
                            }
                            db.close();

                        }
                    };thread.start();*/
                    Log.d("bigmomordinary", result);
                    Log.d("bigmom","蛤蟆神功");
                    adapter.notifyDataSetChanged();
                    listView.setSelection(adapter.getCount()-1);
                }

            }
        }

    }

    /**
     * 显示消息
     *
     * @param messages
     */
    @Override
    public void showMessage(List<TIMMessage> messages) {
        int newMsgNum = 0;
        for (int i = 0; i < messages.size(); ++i){
            Message mMessage = MessageFactory.getMessage(messages.get(i));
            if (mMessage == null || messages.get(i).status() == TIMMessageStatus.HasDeleted) continue;
            if (mMessage instanceof CustomMessage && (((CustomMessage) mMessage).getType() == CustomMessage.Type.TYPING ||
                    ((CustomMessage) mMessage).getType() == CustomMessage.Type.INVALID)) continue;
            ++newMsgNum;
            if (i != messages.size() - 1){
                mMessage.setHasTime(messages.get(i+1));
                messageList.add(0, mMessage);
            }else{
                mMessage.setHasTime(null);
                messageList.add(0, mMessage);
            }
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(newMsgNum);
    }

    @Override
    public void showRevokeMessage(TIMMessageLocator timMessageLocator) {
        for (Message msg : messageList) {
            TIMMessageExt ext = new TIMMessageExt(msg.getMessage());
            if (ext.checkEquals(timMessageLocator)) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 清除所有消息，等待刷新
     */
    @Override
    public void clearAllMessage() {
        messageList.clear();
    }

    /**
     * 发送消息成功
     *
     * @param message 返回的消息
     */
    @Override
    public void onSendMessageSuccess(TIMMessage message) {
        showMessage(message);
    }

    /**
     * 发送消息失败
     *
     * @param code 返回码
     * @param desc 返回描述
     */
    @Override
    public void onSendMessageFail(int code, String desc, TIMMessage message) {
        long id = message.getMsgUniqueId();
        for (Message msg : messageList){
            if (msg.getMessage().getMsgUniqueId() == id){
                switch (code){
                    case 80001:
                        //发送内容包含敏感词
                        msg.setDesc(getString(R.string.chat_content_bad));
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        }

        adapter.notifyDataSetChanged();

    }

    /**
     * 发送图片消息
     */
    @Override
    public void sendImage() {
        Intent intent_album = new Intent("android.intent.action.GET_CONTENT");
        intent_album.setType("image/*");
        startActivityForResult(intent_album, IMAGE_STORE);
}

    /**
     * 发送照片消息
     */
    @Override
    public void sendPhoto() {
        Intent intent_photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent_photo.resolveActivity(getPackageManager()) != null) {
            File tempFile = FileUtil.getTempFile(FileUtil.FileType.IMG);
            if (tempFile != null) {
                fileUri = Uri.fromFile(tempFile);
            }
            intent_photo.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent_photo, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    /**
     * 发送文本消息
     */
    @Override
    public void sendText() {
        Message message = new TextMessage(input.getText());
        presenter.sendMessage(message.getMessage());
        input.setText("");
    }

    /**
     * 发送文件
     */
    @Override
    public void sendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_CODE);
    }

    @Override
    public void sendSteg() {
        startActivityForResult(new Intent(ChatActivity.this, MainActivity.class), 1);
    }


    /**
     * 开始发送语音消息
     */
    @Override
    public void startSendVoice() {
        voiceSendingView.setVisibility(View.VISIBLE);
        voiceSendingView.showRecording();
        recorder.startRecording();

    }

    /**
     * 结束发送语音消息
     */
    @Override
    public void endSendVoice() {
        voiceSendingView.release();
        voiceSendingView.setVisibility(View.GONE);
        recorder.stopRecording();
        if (recorder.getTimeInterval() < 1) {
            Toast.makeText(this, getResources().getString(R.string.chat_audio_too_short), Toast.LENGTH_SHORT).show();
        } else if (recorder.getTimeInterval() > 60) {
            Toast.makeText(this, getResources().getString(R.string.chat_audio_too_long), Toast.LENGTH_SHORT).show();
        } else {
            Message message = new VoiceMessage(recorder.getTimeInterval(), recorder.getFilePath());
            presenter.sendMessage(message.getMessage());
        }
    }

    /**
     * 发送小视频消息
     *
     * @param fileName 文件名
     */
    @Override
    public void sendVideo(String fileName) {
        Message message = new VideoMessage(fileName);
        presenter.sendMessage(message.getMessage());
    }


    /**
     * 结束发送语音消息
     */
    @Override
    public void cancelSendVoice() {

    }

    /**
     * 正在发送
     */
    @Override
    public void sending() {
        if (type == TIMConversationType.C2C){
            Message message = new CustomMessage(CustomMessage.Type.TYPING);
            presenter.sendOnlineMessage(message.getMessage());
        }
    }

    /**
     * 显示草稿
     */
    @Override
    public void showDraft(TIMMessageDraft draft) {
        input.getText().append(TextMessage.getString(draft.getElems(), this));
    }

    @Override
    public void videoAction() {
        Intent intent = new Intent(this, TCVideoRecordActivity.class);
        startActivityForResult(intent, VIDEO_RECORD);
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                   ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Message message = messageList.get(info.position);
        menu.add(0, 1, Menu.NONE, getString(R.string.chat_del));
        if (message.isSendFail()){
            menu.add(0, 2, Menu.NONE, getString(R.string.chat_resend));
        }else if (message.getMessage().isSelf()){
            menu.add(0, 4, Menu.NONE, getString(R.string.chat_pullback));
        }
        if (message.getMessage().getElement(0).getType() == TIMElemType.Image) {
            menu.add(0, 3, Menu.NONE, "保存");
        }
        LinearLayout s = (LinearLayout) input.findViewById(R.id.btn_steg);
        if (s.getVisibility() == View.VISIBLE && message.getMessage().getElement(0).getType() == TIMElemType.Image) {
            menu.add(0, 5, Menu.NONE, "显隐");
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Message message = messageList.get(info.position);
        switch (item.getItemId()) {
            case 1:
                message.remove();
                messageList.remove(info.position);
                adapter.notifyDataSetChanged();
                break;
            case 2:
                messageList.remove(message);
                presenter.sendMessage(message.getMessage());
                break;
            case 3:
                message.save();
                break;
            case 4:
                presenter.revokeMessage(message.getMessage());
                break;
            case 5:
              final Intent appearence=new Intent(this, MainActivity.class);
                final String p;
                if ((message.getMessage()).isSelf()) {
                    p = ((TIMImageElem)message.getMessage().getElement(0)).getPath();
                    appearence.putExtra("path",p );
                    System.out.println("caaaaaaaaaaaaaaaaaaaaa");
                    startActivity(appearence);
                }
                else {
                    final String uuid = ((TIMImageElem)message.getMessage().getElement(0)).getImageList().get(0).getUuid();
                    p = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + uuid + ".jpg";

                    final File file = new File(p);
                    if (file.exists()) {
                        appearence.putExtra("path",p );
                        System.out.println("caaaaaaaaaaaaaaaaaaaaa");
                        startActivity(appearence);
                    }
                    else {
                        Toast.makeText(MyApplication.getContext(), "正在下载图片，请稍候", Toast.LENGTH_SHORT).show();
                        ((TIMImageElem)message.getMessage().getElement(0)).getImageList().get(0).getImage(p, new TIMCallBack() {
                            @Override
                            public void onError(int i, String s) {
                                Log.e(TAG, "getFile failed. code: " + i + " errmsg: " + s);
                                Toast.makeText(MyApplication.getContext(), "下载失败", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess() {
                                MediaScannerConnection.scanFile(MyApplication.getContext(),
                                        new String[] { p }, null,
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            public void onScanCompleted(String path, Uri uri) {
                                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                                Log.i("ExternalStorage", "-> uri=" + uri);
                                            }
                                        });
                                appearence.putExtra("path",p );
                                System.out.println("caaaaaaaaaaaaaaaaaaaaa");
                                startActivity(appearence);
                            }
                        });
                    }
                }

                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && fileUri != null) {
                showImagePreview(fileUri.getPath());
            }
        } else if (requestCode == 1 && data != null) {
            showImagePreview(data.getExtras().getString("result"));
        }
        else if (requestCode == IMAGE_STORE) {
            if (resultCode == RESULT_OK && data != null) {
                showImagePreview(FileUtil.getFilePath(this, data.getData()));
            }
        } else if (requestCode == FILE_CODE) {
            if (resultCode == RESULT_OK) {
                sendFile(FileUtil.getFilePath(this, data.getData()));
            }
        } else if (requestCode == IMAGE_PREVIEW){
            if (resultCode == RESULT_OK) {
                boolean isOri = data.getBooleanExtra("isOri",false);
                String path = data.getStringExtra("path");
                File file = new File(path);
                if (file.exists()){
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, options);
                    if (file.length() == 0 && options.outWidth == 0) {
                        Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
                    }else {
                        if (file.length() > 1024 * 1024 * 26){
                            Toast.makeText(this, getString(R.string.chat_file_too_large),Toast.LENGTH_SHORT).show();
                        }else{
                            Message message = new ImageMessage(path,isOri);
                            presenter.sendMessage(message.getMessage());
                        }
                    }
                }else{
                    Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == VIDEO_RECORD) {
            if (resultCode == RESULT_OK) {
                String videoPath = data.getStringExtra("videoPath");
                String coverPath = data.getStringExtra("coverPath");
                long duration = data.getLongExtra("duration", 0);
                Message message = new UGCMessage(videoPath, coverPath, duration);
                presenter.sendMessage(message.getMessage());
            }
        }

    }


    private void showImagePreview(String path){
        if (path == null) return;
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra("path", path);
        startActivityForResult(intent, IMAGE_PREVIEW);
    }

    private void sendFile(String path){
        if (path == null) return;
        File file = new File(path);
        if (file.exists()){
            if (file.length() > 1024 * 1024 * 10){
                Toast.makeText(this, getString(R.string.chat_file_too_large),Toast.LENGTH_SHORT).show();
            }else{
                Message message = new FileMessage(path);
                presenter.sendMessage(message.getMessage());
            }
        }else{
            Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 将标题设置为对象名称
     */
    private Runnable resetTitle = new Runnable() {
        @Override
        public void run() {
            TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);
            title.setTitleText(titleStr);
        }
    };

    public static byte[] readStream(InputStream inStream) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();

        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getStringByGet(String url){
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url)
                    .openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            byte[] data;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("秦崔果皇");
                InputStream in = conn.getInputStream();
                byte[] a = readStream(in);
                String ex = new String(Base64.encode(a, Base64.NO_WRAP));
                return ex;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getTime(){
        Calendar calendar = Calendar.getInstance();
        String created = calendar.get(Calendar.YEAR) + "年"
                + (calendar.get(Calendar.MONTH)+1) + "月"//从0计算
                + calendar.get(Calendar.DAY_OF_MONTH) + "日"
                + calendar.get(Calendar.HOUR_OF_DAY) + "时"
                + calendar.get(Calendar.MINUTE) + "分"+calendar.get(Calendar.SECOND)+"s";
        Log.e("bigmomtime", created);
        return created;
    }

}
