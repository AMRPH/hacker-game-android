package com.xlab13.playhacker;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.xlab13.playhacker.activities.ErrorConnectionActivity;
import com.xlab13.playhacker.activities.SettingsActivity;
import com.xlab13.playhacker.alerts.MyAlertDialog;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


public class tools {
    private static tools mInstance;
    Context context;

    MediaPlayer mediaPlayer;

    public tools(Context context){
        this.context = context;
        mediaPlayer = MediaPlayer.create(context, R.raw.click);
    }

    public static tools getInstance(Context context) {
        if (mInstance == null){
            mInstance = new tools(context);
        }
        return mInstance;
    }


    public void debug(String message){
        Log.d("~~~", message);
    }


    public void startMusic(int musicId){
        Intent intent = new Intent(Config.MUSIC_INTENT);
        intent.putExtra("musicId", musicId);
        context.sendBroadcast(intent);
    }

    public void stopMusic(){
        Intent intent = new Intent(Config.MUSIC_INTENT);
        intent.putExtra("musicId", 0);
        context.sendBroadcast(intent);
    }

    public void playMusic(){
        Intent intent = new Intent(Config.MUSIC_INTENT);
        intent.putExtra("musicId", 1);
        context.sendBroadcast(intent);
    }

    public void playSound(){
        boolean soundOn = context.getSharedPreferences("game", Context.MODE_PRIVATE)
                .getBoolean(SettingsActivity.SOUND, true);

        if (soundOn){
            mediaPlayer.start();
        }
    }


    public void startErrorConnectionActivity(Context context){
        context.startActivity(new Intent(context, ErrorConnectionActivity.class));
        ((Activity) context).finish();
    }

    public void showErrorDialog(Context context, String error){
        MyAlertDialog alertDialog = new MyAlertDialog(context);
        alertDialog.setText(error);
        alertDialog.addButton("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTools.playSound();
                alertDialog.dismissDialog();
            }
        });
        alertDialog.showDialog();
    }

    public void closeKeyboard(Context context){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
    }


    public void blockButton(View view){
        view.setClickable(false);
        android.os.Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                view.setClickable(true);
            }
        }, 500);
    }


    public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = bitmap.getWidth()/8;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public Bitmap resourcesToBitmap (int id,  int width, int height) {

        Drawable drawable = context.getDrawable(id);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public String reduceNumber(double num){
        String numStr = String.valueOf((int)num);
        if (num >= 10000 && num < 999999){
            double dNum;
            dNum = num/1000.0;
            if (dNum-(int)dNum != 0.0 && ((dNum-(int)dNum) >= 0.1)){
                MathContext mathContext = new MathContext(15, RoundingMode.HALF_UP);
                BigDecimal bigDecimal = new BigDecimal(dNum, mathContext);
                bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_DOWN);
                numStr = bigDecimal.doubleValue()+ "K";
            } else numStr = (int)dNum + "K";
        } else if (num >= 1000000){
            double dNum;
            dNum = num/1000000.0;
            if (dNum-(int)dNum != 0.0 && ((dNum-(int)dNum) >= 0.1)){
                MathContext mathContext = new MathContext(15, RoundingMode.HALF_UP);
                BigDecimal bigDecimal = new BigDecimal(dNum, mathContext);
                bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_DOWN);
                numStr = bigDecimal.doubleValue()+ "M";
            } else numStr = (int)dNum + "M";
        }
        return  numStr;
    }
}
