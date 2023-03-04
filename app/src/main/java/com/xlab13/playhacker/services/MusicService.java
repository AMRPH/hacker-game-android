package com.xlab13.playhacker.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.xlab13.playhacker.activities.SettingsActivity;

import static com.xlab13.playhacker.Config.MUSIC_INTENT;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;


public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private BroadcastReceiver br;

    @Override
    public void onCreate() {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Thread t = new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                int musicId = intent.getIntExtra("musicId",0);
                                boolean musicOn = context.getSharedPreferences("game", Context.MODE_PRIVATE)
                                        .getBoolean(SettingsActivity.MUSIC, true);

                                switch (musicId){
                                    case 0:
                                        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
                                        break;
                                    case 1:
                                        if (mediaPlayer != null && !mediaPlayer.isPlaying() && musicOn) mediaPlayer.start();
                                        break;
                                    default:
                                        if (mediaPlayer != null) mediaPlayer.stop();
                                        mediaPlayer = MediaPlayer.create(context, musicId);
                                        mediaPlayer.setLooping(true);
                                        if (musicOn) mediaPlayer.start();
                                        break;
                                }

                            }
                        }
                );
                t.start();
            }
        };
        registerReceiver(br, new IntentFilter(MUSIC_INTENT));
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(br);
        super.onDestroy();
    }





}
