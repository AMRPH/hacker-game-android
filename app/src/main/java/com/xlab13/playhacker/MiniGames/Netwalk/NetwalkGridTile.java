package com.xlab13.playhacker.MiniGames.Netwalk;

import android.content.Context;

public class NetwalkGridTile extends androidx.appcompat.widget.AppCompatImageView {

    private NetwalkGridLocation gridLocation;

    public NetwalkGridTile(Context context) {
        super(context);
    }

    public NetwalkGridTile(Context context, NetwalkGridLocation gridLocation) {
        this(context);
        this.gridLocation = gridLocation;
    }

    public NetwalkGridLocation getGridLocation() {
        return gridLocation;
    }
}