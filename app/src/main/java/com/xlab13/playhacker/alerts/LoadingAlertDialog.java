package com.xlab13.playhacker.alerts;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.xlab13.playhacker.R;

public class LoadingAlertDialog {
    private Context context;

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;

    private View layout;

    public LoadingAlertDialog(Context context){
        this.context = context;
        layout = ((Activity) context).getLayoutInflater().inflate(R.layout.alertdialog_loading, null);

        mBuilder = new AlertDialog.Builder(context, R.style.CustomDialog);
    }

    public void showDialog(){
        if (!isShowing()){
            if(layout.getParent()!=null)
                ((ViewGroup)layout.getParent()).removeView(layout);
            mBuilder.setView(layout);
            mDialog = mBuilder.create();
            mDialog.show();
        }
    }

    public void dismissDialog(){
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    public boolean isShowing(){
        if (mDialog == null) return false;
        else return mDialog.isShowing();
    }
}
