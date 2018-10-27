package com.tencent.qcloud.timchat.ui.volumeservice;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.timchat.R;

public class MainActivity extends AppCompatActivity {
    public final String MOD_PACKAGENAME = "com.tencent.qcloud.timchat.ui.volumeservice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button next_button =(Button)findViewById(R.id.next);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"请使用音量键进入",Toast.LENGTH_LONG).show();
                finish();

            }
        });
        Button btn_clear = (Button) findViewById(R.id.button_clear);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text_code = (EditText) findViewById(R.id.edit_keys);
                text_code.setText("");
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("code", "");
                editor.apply();
            }
        });

        Button btn_toggle = (Button) findViewById(R.id.button2_toggle);
        btn_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView text_status = (TextView) findViewById(R.id.text_status);
                text_status.setText("Status: ");
                if (!isServiceRunning(BackgroundService.class)) {
                    startService(new Intent(getApplicationContext(), BackgroundService.class));
                    text_status.setText(text_status.getText() + "开启");
                }
                else {
                    stopService(new Intent(getApplicationContext(), BackgroundService.class));
                    text_status.setText(text_status.getText() + "关闭");
                }
            }
        });
        final Switch toggle = (Switch) findViewById(R.id.switch1);
        PackageManager pm = getPackageManager();
        ComponentName cn = new ComponentName(getApplicationContext(), MOD_PACKAGENAME + ".MainActivityLauncher");
        if (pm.getComponentEnabledSetting(cn) == 1) { toggle.setChecked(true);}
        //toggle.setChecked(true);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PackageManager packageManager = getApplicationContext().getPackageManager();
                ComponentName aliasName = new ComponentName(getApplicationContext(), MOD_PACKAGENAME + ".MainActivityLauncher");
                if (toggle.isChecked()) {
                    packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    Log.d("tg", "en");
                }
                else {
                    packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Log.d("tg", "dis");
                }
            }
        });

        pref = getApplicationContext().getSharedPreferences("Code", MODE_PRIVATE);
        EditText text_code = (EditText) findViewById(R.id.edit_keys);
        text_code.setText(pref.getString("code", ""));

    }

    @Override
    public void onResume(){
        super.onResume();
        TextView text_status = (TextView) findViewById(R.id.text_status);
        if (!isServiceRunning(BackgroundService.class)) { text_status.setText("状态: 关闭"); }
        else { text_status.setText("状态: 开启"); }
    }

    private SharedPreferences pref;

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isServiceRunning(BackgroundService.class)) { return super.onKeyDown(keyCode, event); }
        EditText text_code = (EditText) findViewById(R.id.edit_keys);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            text_code.setText(text_code.getText() + "-");
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("code", text_code.getText().toString());
            editor.apply();
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            text_code.setText(text_code.getText() + "+");
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("code", text_code.getText().toString());
            editor.apply();
        }
        return super.onKeyDown(keyCode, event);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
