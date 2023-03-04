package com.xlab13.playhacker;

import android.graphics.Bitmap;

import com.example.GetGiftsQuery;
import com.example.GetHealthQuery;
import com.example.GetJobsQuery;
import com.example.GetLongtermJobsQuery;
import com.xlab13.playhacker.utils.ChatItem;
import com.xlab13.playhacker.utils.DataController;
import com.xlab13.playhacker.utils.GameItem;
import com.xlab13.playhacker.utils.Profile;

import java.util.Date;
import java.util.List;

public class Config {
    public static DataController mDataController;

    public static Profile user;
    public static List<ChatItem> globalChat;

    public static int btc_rub = 0;
    public static boolean isGiftReceived;
    public static List<GetGiftsQuery.Gift> gifts;

    public static boolean isCooldown = false;
    public static int cooldownTime = 1000;

    public static Bitmap PLAYER_AVATAR;
    public static final String MUSIC_INTENT = "com.xlab13.playhacker.music";
}
