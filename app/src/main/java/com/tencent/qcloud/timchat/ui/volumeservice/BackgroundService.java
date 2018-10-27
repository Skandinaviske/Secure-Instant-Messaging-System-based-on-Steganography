package com.tencent.qcloud.timchat.ui.volumeservice;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.tencent.qcloud.timchat.ui.SplashActivity;

public class BackgroundService extends Service {
    private MediaSessionCompat mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "start");
        mediaSession = new MediaSessionCompat(this, "BackgroundService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { mediaSession.setCallback(new MediaSessionCompat.Callback(){ }); }

        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        VolumeProviderCompat myVolumeProvider =
                new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, /*max volume*/100, /*initial volume level*/50) {
                    @Override
                    public void onAdjustVolume(int direction) {
                /*
                -1 -- volume down
                1 -- volume up
                0 -- volume button released
                 */
                        Log.d("ServiceKey", Integer.toString(direction));

                        long t = System.currentTimeMillis();
                        if (t - time > 1000) { input = ""; }
                        time = t;

                        if (direction == 1) { input += "+"; }
                        else if (direction == -1) { input += "-"; }

                        String c = pref.getString("code", "");
                        if (c.length() > 0 && input.equals(c)) {
                            time = 0;
                            input = "";
                            startActivity(new Intent(getApplicationContext(), SplashActivity.class));

                        }

                        Log.d("ServiceInput", input);
                    }
                };

mediaSession.setPlaybackToRemote(myVolumeProvider);
mediaSession.setActive(true);

pref = getApplicationContext().getSharedPreferences("Code", MODE_PRIVATE);
}

private long time = 0;
private String input = "";
private SharedPreferences pref;

@Override
public int onStartCommand(Intent intent, int flags, int startId) {
return START_STICKY;
}

@Override
public IBinder onBind(Intent intent) {
return null;
}

@Override
public void onDestroy() {
super.onDestroy();
Log.d("Service", "stop");
mediaSession.release();
}
}
