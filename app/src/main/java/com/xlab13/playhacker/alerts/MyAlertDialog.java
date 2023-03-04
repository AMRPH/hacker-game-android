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

public class MyAlertDialog{
    private Context context;

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;

    private View layout;
    private TextView tvText;
    private Button btn1, btn2, btn3, btn4, btn5;
    private LinearLayout linearLayout;

    public MyAlertDialog(Context context){
        this.context = context;
        layout = ((Activity) context).getLayoutInflater().inflate(R.layout.alertdialog, null);
        tvText = layout.findViewById(R.id.tvExchangeTitle);

        btn1 = layout.findViewById(R.id.btnFirst);
        btn1.setVisibility(View.INVISIBLE);
        btn2 = layout.findViewById(R.id.btnSecond);
        btn2.setVisibility(View.INVISIBLE);
        btn3 = layout.findViewById(R.id.btnThird);
        btn3.setVisibility(View.INVISIBLE);
        linearLayout = layout.findViewById(R.id.ll1);

        mBuilder = new AlertDialog.Builder(context, R.style.CustomDialog);
    }

    public void setCancelable(Boolean cancelable) {
        mBuilder.setCancelable(cancelable);
    }

    public void setText(String text) {
        tvText.setText(text);
    }

    public void setText(String text, int size) {
        tvText.setText(text);
        tvText.setTextSize(size);
    }


    private int numButton = 1;
    public void addButton(String text, View.OnClickListener onClickListener){
        switch (numButton){
            case 1:
                btn1.setText(text);
                btn1.setOnClickListener(onClickListener);
                btn1.setVisibility(View.VISIBLE);
                numButton++;
                break;
            case 2:
                btn2.setText(text);
                btn2.setOnClickListener(onClickListener);
                btn2.setVisibility(View.VISIBLE);
                numButton++;
                break;
            case 3:
                btn3.setText(text);
                btn3.setOnClickListener(onClickListener);
                btn3.setVisibility(View.VISIBLE);
                numButton++;
                break;
            case 4:
                btn4 = new Button(context);
                TableLayout.LayoutParams layoutParams4 = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,1f);
                layoutParams4.setMargins(4,4,4,4);
                btn4.setText(text);
                btn4.setTextColor(Color.parseColor("#67C6BF"));
                btn4.setBackground(context.getDrawable(R.drawable.style_background));
                btn4.setLayoutParams(layoutParams4);
                btn4.setOnClickListener(onClickListener);
                linearLayout.addView(btn4);
                numButton++;
                break;
            case 5:
                btn5 = new Button(context);
                TableLayout.LayoutParams layoutParams5 = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,1f);
                layoutParams5.setMargins(4,4,4,4);
                btn5.setText(text);
                btn5.setTextColor(Color.parseColor("#67C6BF"));
                btn5.setBackground(context.getDrawable(R.drawable.style_background));
                btn5.setLayoutParams(layoutParams5);
                btn5.setOnClickListener(onClickListener);
                linearLayout.addView(btn5);
                numButton++;
                break;
            default:
                throw  new IndexOutOfBoundsException ("Buttons limit");
        }
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