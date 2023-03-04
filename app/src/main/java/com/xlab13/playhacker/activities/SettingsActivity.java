package com.xlab13.playhacker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Network;
import android.net.NetworkSpecifier;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class SettingsActivity extends AppCompatActivity {
    Context context;


    @BindView(R.id.ivSettingsSound)
    ImageView ivSound;

    @BindView(R.id.ivSettingsMusic)
    ImageView ivMusic;

    @BindView(R.id.ivSettingsLang)
    ImageView ivLang;


    public static final String SOUND = "sound";
    private boolean sound;
    public static final String MUSIC = "music";
    private boolean music;
    public static final String LANGUAGE = "lang";
    private String lang;

    SharedPreferences sPref;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        context = this;

        sPref = getSharedPreferences("game", Context.MODE_PRIVATE);

        sound = sPref.getBoolean(SOUND, true);
        music = sPref.getBoolean(MUSIC, true);
        lang = sPref.getString(LANGUAGE, "en");

        if (!sound){
            ivSound.setImageResource(R.drawable.icon_settings_sound_off);
        }

        if (!music){
            ivMusic.setImageResource(R.drawable.icon_settings_note_off);
        }
        
        if (!lang.equals("en")){
            ivLang.setImageResource(R.drawable.icon_settings_rus);
        }

        ivSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound = sPref.getBoolean(SOUND, true);
                if (sound) ivSound.setImageResource(R.drawable.icon_settings_sound_off);
                else ivSound.setImageResource(R.drawable.icon_settings_sound_on);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putBoolean(SOUND, !sound);
                ed.commit();
            }
        });

        ivMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                music = sPref.getBoolean(MUSIC, true);
                if (music) ivMusic.setImageResource(R.drawable.icon_settings_note_off);
                else ivMusic.setImageResource(R.drawable.icon_settings_note_on);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putBoolean(MUSIC, !music);
                ed.commit();
            }
        });

        ivLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lang = sPref.getString(LANGUAGE, "en");
                if (lang.equals("en")) {
                    lang = "ru";
                    ivLang.setImageResource(R.drawable.icon_settings_rus);
                } else {
                    lang = "en";
                    ivLang.setImageResource(R.drawable.icon_settings_eng);
                }
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString(LANGUAGE, lang);
                ed.commit();

                Locale locale = new Locale(lang);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.setLocale(locale);
                getBaseContext().getResources().updateConfiguration(config, null);
                recreate();
            }
        });
    }

    @OnClick(R.id.btnSettingsClose)
    void onCloseClick(View v) {
        mTools.playSound();
        finish();
    }


    @OnClick(R.id.ivSettingsSupport)
    void onSupportClick(View v) {
        mTools.playSound();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:maxrus13@tutanota.com"));
        startActivity(intent);
    }


    @OnClick(R.id.ivSettingsExit)
    void onExitClick(View v) {
        mTools.playSound();

        Config.user = null;
        Config.PLAYER_AVATAR = null;
        Config.btc_rub = 0;
        Config.globalChat = null;

        NetworkService.getInstance().clearData();

        SharedPreferences.Editor ed = sPref.edit();
        ed.remove("username");
        ed.remove("password");
        ed.commit();

        startActivity(new Intent(this, InitializationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
