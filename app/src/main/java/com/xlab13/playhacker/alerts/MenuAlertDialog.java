package com.xlab13.playhacker.alerts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.activities.clan.ClanActivity;
import com.xlab13.playhacker.activities.clan.ClansActivity;
import com.xlab13.playhacker.activities.FriendsActivity;
import com.xlab13.playhacker.activities.chat.ChatsActivity;
import com.xlab13.playhacker.R;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;
public class MenuAlertDialog implements View.OnClickListener {
    private Context context;

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;

    private RelativeLayout layout;
    private TextView btnFriends, btnDM, btnMyClan, btnClans;
    private ImageView btnClose;


    public MenuAlertDialog(Context context){
        this.context = context;

        layout = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.alertdialog_menu, null);

        btnFriends = layout.findViewById(R.id.btnMenuFriends);
        btnDM = layout.findViewById(R.id.btnMenuDM);
        btnMyClan = layout.findViewById(R.id.btnMenuMyClan);
        btnClans = layout.findViewById(R.id.btnMenuClans);

        btnFriends.setOnClickListener(this);
        btnDM.setOnClickListener(this);
        btnMyClan.setOnClickListener(this);
        btnClans.setOnClickListener(this);


        btnClose = layout.findViewById(R.id.btnMenuClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTools.playSound();
                mDialog.dismiss();
            }
        });

        mBuilder = new AlertDialog.Builder(context, R.style.CustomDialog);
        mBuilder.setView(layout);
    }

    public void showDialog(){
        if(layout.getParent()!=null)
            ((ViewGroup)layout.getParent()).removeView(layout);
        mBuilder.setView(layout);
        mDialog = mBuilder.create();
        mDialog.show();
    }

    @Override
    public void onClick(View v) {
        mTools.blockButton(v);
        mTools.playSound();
        switch (v.getId()){
            case R.id.btnMenuFriends:
                context.startActivity(new Intent(context, FriendsActivity.class));
                break;
            case R.id.btnMenuDM:
                context.startActivity(new Intent(context, ChatsActivity.class));
                break;
            case R.id.btnMenuMyClan:
                if (Config.user.clan != null){
                    context.startActivity(new Intent(context, ClanActivity.class));
                } else{
                    MyAlertDialog dialog = new MyAlertDialog(context);
                    dialog.setText("Вы не состоите в клане");
                    dialog.addButton("Закрыть", (v1) ->{
                        dialog.dismissDialog();
                    });
                    dialog.showDialog();
                }

                break;
            case R.id.btnMenuClans:
                context.startActivity(new Intent(context, ClansActivity.class));
                break;
        }
    }
}
