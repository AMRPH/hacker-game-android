package com.xlab13.playhacker.utils;

import android.view.View;

public class GameItem {
    public String title;
    public int resourceID;
    public View.OnClickListener clickListener;

    public GameItem(String title, int resourceID, View.OnClickListener clickListener){
        this.title = title;
        this.resourceID = resourceID;
        this.clickListener = clickListener;
    }
}
