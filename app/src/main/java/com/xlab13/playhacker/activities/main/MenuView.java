package com.xlab13.playhacker.activities.main;

import static com.xlab13.playhacker.Config.PLAYER_AVATAR;
import static com.xlab13.playhacker.Config.isCooldown;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.activities.MainActivity;
import com.xlab13.playhacker.activities.SettingsActivity;
import com.xlab13.playhacker.utils.Profile;

public class MenuView extends View {
    Context context;

    boolean clickable;

    PointF Center;
    PointF LeftDown;
    PointF LeftCenter;
    PointF LeftUp;
    PointF RightDown;
    PointF RightCenter;
    PointF RightUp;

    float width;
    float height;
    float density;

    float step;

    Paint pMainStroke, pAccent, pMain,  pMainLine, pMainDark, pBackMain, pBackDark, pBackLight, pFontText, pFontNumber, pPic, pStroke;

    Bitmap bmRouble, bmBitcoin, bmHealth, bmMood, bmAlco, bmJob, bmHack, bmBack, bmSettings;

    Path pathHealthBack, pathMoodBack, pathAlcoBack;

    private Profile user;

    public MenuView(Context context) {
        super(context);
        this.context = context;

        init();
    }

    public MenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init();
    }

    private void init(){
        user = Config.user;
        density = getResources().getDisplayMetrics().density;
        step = 3f * density;
        clickable = true;

        pMainStroke = new Paint();
        pMainStroke.setColor(ContextCompat.getColor(context, R.color.newMainLine));
        pMainStroke.setStrokeWidth(step/2);
        pMainStroke.setStyle(Paint.Style.STROKE);

        pStroke = new Paint();
        pStroke.setColor(ContextCompat.getColor(context, R.color.newAccent));
        pStroke.setStrokeWidth(step);
        pStroke.setStyle(Paint.Style.STROKE);

        pAccent = new Paint();
        pAccent.setColor(ContextCompat.getColor(context, R.color.newAccent));

        pMain = new Paint();
        pMain.setColor(ContextCompat.getColor(context, R.color.newMain));

        pMainLine = new Paint();
        pMainLine.setColor(ContextCompat.getColor(context, R.color.newMainLine));

        pMainDark = new Paint();
        pMainDark.setColor(ContextCompat.getColor(context, R.color.newMainDark));

        pBackMain = new Paint();
        pBackMain.setColor(ContextCompat.getColor(context, R.color.newBackMain));

        pBackDark = new Paint();
        pBackDark.setColor(ContextCompat.getColor(context, R.color.newBackDark));

        pBackLight = new Paint();
        pBackLight.setColor(ContextCompat.getColor(context, R.color.newBackLight));

        pFontText = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pFontText.setTypeface(getResources().getFont(R.font.hack));
        }
        pFontText.setColor(ContextCompat.getColor(context, R.color.newMain));

        pFontNumber = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pFontNumber.setTypeface(getResources().getFont(R.font.ciphra));
        }
        pFontNumber.setColor(ContextCompat.getColor(context, R.color.newMain));
        pFontNumber.setTextSize(step*7);

        pPic = new Paint(Paint.ANTI_ALIAS_FLAG);

        bmRouble = mTools.resourcesToBitmap(R.drawable.menu_rouble, (int) step*8, (int) step*8);
        bmBitcoin = mTools.resourcesToBitmap(R.drawable.menu_bitcoin, (int) step*8, (int) step*8);

        bmHealth = mTools.resourcesToBitmap(R.drawable.menu_health, (int) step*14, (int) step*14);
        bmMood = mTools.resourcesToBitmap(R.drawable.menu_mood, (int) step*14, (int) step*14);
        bmAlco = mTools.resourcesToBitmap(R.drawable.menu_alco, (int) step*14, (int) step*14);
        bmJob = mTools.resourcesToBitmap(R.drawable.menu_job, (int) step*14, (int) step*14);
        bmHack = mTools.resourcesToBitmap(R.drawable.menu_hack, (int) step*14, (int) step*14);

        bmBack = mTools.resourcesToBitmap(R.drawable.menu_back, (int) step*28, (int) step*28);
        bmSettings = mTools.resourcesToBitmap(R.drawable.menu_settings, (int) step*28, (int) step*28);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();

        setPoints();

        drawHex(canvas);
        drawBackground(canvas);

        drawTimeInfo(canvas);

        drawLevelInfo(canvas);

        drawHexInfo(canvas);
        drawArrow(canvas);
        drawCircle(canvas);
        drawHexIcons(canvas);

        drawButtons(canvas);
    }


    private void setPoints(){
        Center = new PointF(width/2,  height/2);

        double hudu = (Math.PI / 180) * 60;
        float radius = (float) (height / (2 * Math.cos(hudu / 2)));

        LeftUp = new PointF((float) (Center.x - radius * Math.cos(hudu * 5)), (float) (Center.y + radius * Math.sin(hudu * 5)));
        LeftCenter = new PointF((float) (Center.x - radius * Math.cos(hudu * 0)), (float) (Center.y + radius * Math.sin(hudu * 0)));
        LeftDown = new PointF((float) (Center.x - radius * Math.cos(hudu * 1)), (float) (Center.y + radius * Math.sin(hudu * 1)));
        RightDown = new PointF((float) (Center.x - radius * Math.cos(hudu * 2)), (float) (Center.y + radius * Math.sin(hudu * 2)));
        RightCenter = new PointF((float) (Center.x - radius * Math.cos(hudu * 3)), (float) (Center.y + radius * Math.sin(hudu * 3)));
        RightUp = new PointF((float) (Center.x - radius * Math.cos(hudu * 4)), (float) (Center.y + radius * Math.sin(hudu * 4)));
    }

    private void drawHex(Canvas canvas){
        Path path = new Path();
        path.moveTo(LeftUp.x, LeftUp.y);
        path.lineTo(LeftCenter.x, LeftCenter.y);
        path.lineTo(LeftDown.x, LeftDown.y);
        path.lineTo(RightDown.x, RightDown.y);
        path.lineTo(RightCenter.x, RightCenter.y);
        path.lineTo(RightUp.x, RightUp.y);
        path.lineTo(LeftUp.x, LeftUp.y);
        path.close();
        canvas.drawPath(path, pMainLine);

        path.reset();
        path.moveTo(LeftCenter.x + step * 2, LeftCenter.y + step / 2);
        path.lineTo(LeftDown.x + step, LeftDown.y - step);
        path.lineTo(RightDown.x - step, RightDown.y - step);
        path.lineTo(RightCenter.x - step * 2, RightCenter.y + step / 2);
        path.close();
        canvas.drawPath(path, pMainDark);


        pathHealthBack = new Path();
        pathHealthBack.moveTo(LeftCenter.x + step * 2, LeftCenter.y - step / 2);
        pathHealthBack.lineTo(LeftUp.x + step, LeftUp.y + step);
        pathHealthBack.lineTo(Center.x - step, Center.y - step / 2);
        pathHealthBack.close();
        canvas.drawPath(pathHealthBack, pMainDark);

        pathMoodBack = new Path();
        pathMoodBack.moveTo(Center.x, Center.y - step / 2);
        pathMoodBack.lineTo(LeftUp.x + step * 2, LeftUp.y + step);
        pathMoodBack.lineTo(RightUp.x - step * 2, RightUp.y + step);
        pathMoodBack.close();
        canvas.drawPath(pathMoodBack, pMainDark);

        pathAlcoBack = new Path();
        pathAlcoBack.moveTo(RightCenter.x - step * 2, RightCenter.y - step / 2);
        pathAlcoBack.lineTo(RightUp.x - step, RightUp.y + step);
        pathAlcoBack.lineTo(Center.x + step, Center.y - step / 2);
        pathAlcoBack.close();
        canvas.drawPath(pathAlcoBack, pMainDark);

    }


    private void drawHexInfo(Canvas canvas){
        double hudu = (Math.PI / 180) * 60;
        float healthRadius = (float) (height / (2 * Math.cos(hudu / 2)) * user.health_points / 100);
        float moodRadius = (float) (height / (2 * Math.cos(hudu / 2)) * user.mood / 100);
        float alcoRadius = (float) (height / (2 * Math.cos(hudu / 2)) * user.alco_balance / 100);

        Path path = new Path();
        path.moveTo((float) (width / 2 - healthRadius * Math.cos(hudu * 0)), (float) (height / 2 + healthRadius * Math.sin(hudu * 0)));
        path.lineTo((float) (width / 2 - healthRadius * Math.cos(hudu * 5)), (float) (height / 2 + healthRadius * Math.sin(hudu * 5)));
        path.lineTo(Center.x, Center.y);
        path.close();

        canvas.save();
        canvas.clipPath(pathHealthBack);
        canvas.drawPath(path, pMain);
        canvas.restore();

        path.reset();
        path.moveTo(Center.x, Center.y);
        path.lineTo((float) (width / 2 - moodRadius * Math.cos(hudu * 5)), (float) (height / 2 + moodRadius * Math.sin(hudu * 5)));
        path.lineTo((float) (width / 2 - moodRadius * Math.cos(hudu * 4)), (float) (height / 2 + moodRadius * Math.sin(hudu * 4)));
        path.close();

        canvas.save();
        canvas.clipPath(pathMoodBack);
        canvas.drawPath(path, pMain);
        canvas.restore();

        path.reset();

        path.moveTo((float) (width / 2 - alcoRadius * Math.cos(hudu * 3)), (float) (height / 2 + alcoRadius * Math.sin(hudu * 3)));
        path.lineTo((float) (width / 2 - alcoRadius * Math.cos(hudu * 4)), (float) (height / 2 + alcoRadius * Math.sin(hudu * 4)));
        path.lineTo(Center.x, Center.y);
        path.close();

        canvas.save();
        canvas.clipPath(pathAlcoBack);
        canvas.drawPath(path, pMain);
        canvas.restore();
    }

    private void drawCircle(Canvas canvas){
        Path path = new Path();
        path.addCircle(Center.x, Center.y, step*4, Path.Direction.CW);
        canvas.drawPath(path, pAccent);
        canvas.drawPath(path, pMainStroke);

        double hudu = (Math.PI / 180) * 120;
        float radius = height * 1/4;

        path.reset();
        path.moveTo((float) (Center.x + (radius + step) * Math.sin(hudu * 1.5f)), (float) (Center.y - (radius + step) * Math.cos(hudu * 1.5f)));
        path.lineTo((float) (Center.x + (radius + step*4) * Math.sin(hudu * 1.5f)), (float) (Center.y - (radius + step*4) * Math.cos(hudu * 1.5f)));
        path.close();
        pStroke.setColor(ContextCompat.getColor(context, R.color.newAccent));
        canvas.drawPath(path, pStroke);

        path.reset();
        path.moveTo((float) (Center.x + (radius + step*2) * Math.sin(hudu * 1.2f)), (float) (Center.y - (radius + step*2) * Math.cos(hudu * 1.2f)));
        path.lineTo((float) (Center.x + (radius + step*4) * Math.sin(hudu * 1.2f)), (float) (Center.y - (radius + step*4) * Math.cos(hudu * 1.2f)));
        path.close();

        path.moveTo((float) (Center.x + (radius + step*2) * Math.sin(hudu * 1.3f)), (float) (Center.y - (radius + step*2) * Math.cos(hudu * 1.3f)));
        path.lineTo((float) (Center.x + (radius + step*4) * Math.sin(hudu * 1.3f)), (float) (Center.y - (radius + step*4) * Math.cos(hudu * 1.3f)));
        path.close();

        path.moveTo((float) (Center.x + (radius + step*2) * Math.sin(hudu * 1.4f)), (float) (Center.y - (radius + step*2) * Math.cos(hudu * 1.4f)));
        path.lineTo((float) (Center.x + (radius + step*4) * Math.sin(hudu * 1.4f)), (float) (Center.y - (radius + step*4) * Math.cos(hudu * 1.4f)));
        path.close();

        path.moveTo((float) (Center.x + (radius + step*2) * Math.sin(hudu * 1.6f)), (float) (Center.y - (radius + step*2) * Math.cos(hudu * 1.6f)));
        path.lineTo((float) (Center.x + (radius + step*4) * Math.sin(hudu * 1.6f)), (float) (Center.y - (radius + step*4) * Math.cos(hudu * 1.6f)));
        path.close();

        path.moveTo((float) (Center.x + (radius + step*2) * Math.sin(hudu * 1.7f)), (float) (Center.y - (radius + step*2) * Math.cos(hudu * 1.7f)));
        path.lineTo((float) (Center.x + (radius + step*4) * Math.sin(hudu * 1.7f)), (float) (Center.y - (radius + step*4) * Math.cos(hudu * 1.7f)));
        path.close();

        path.moveTo((float) (Center.x + (radius + step*2) * Math.sin(hudu * 1.8f)), (float) (Center.y - (radius + step*2) * Math.cos(hudu * 1.8f)));
        path.lineTo((float) (Center.x + (radius + step*4) * Math.sin(hudu * 1.8f)), (float) (Center.y - (radius + step*4) * Math.cos(hudu * 1.8f)));
        path.close();

        pStroke.setColor(ContextCompat.getColor(context, R.color.newMainLine));
        canvas.drawPath(path, pStroke);
    }

    private void drawArrow(Canvas canvas){
        double hudu = (Math.PI / 180) * 120;
        float radius = height * 1/4;

        float pb = 1.0f + (float) (100 - user.work_hack_balance) / 200.0f;

        Path path = new Path();
        path.moveTo(Center.x - step * 2, Center.y);
        path.lineTo(Center.x + step * 2, Center.y);
        path.lineTo((float) (Center.x + radius * Math.sin(hudu * pb)), (float) (Center.y - radius * Math.cos(hudu * pb)));
        canvas.drawPath(path, pAccent);
    }

    private void drawHexIcons(Canvas canvas){
        canvas.drawBitmap(bmHealth, width * 3 / 8 - (int) step * 7, height * 2 / 6 - (int) step * 7, pPic);
        canvas.drawBitmap(bmMood, width / 2 - (int) step * 7, height / 6 - (int) step * 7, pPic);
        canvas.drawBitmap(bmAlco, width * 5 / 8 - (int) step * 7, height * 2 / 6 - (int) step * 7, pPic);
        canvas.drawBitmap(bmJob, width * 3 / 8 - (int) step * 7, height * 4 / 6 - (int) step * 7, pPic);
        canvas.drawBitmap(bmHack, width * 5 / 8 - (int) step * 7, height * 4 / 6 - (int) step * 7, pPic);
    }


    private void drawBackground(Canvas canvas){
        Path path = new Path();
        path.moveTo(0f, LeftCenter.y + step/4);
        path.lineTo(0f, height);
        path.lineTo(width, height);
        path.lineTo(width, RightCenter.y + step/4);
        path.close();
        canvas.drawPath(path, pMainStroke);

        drawLeftBack(canvas);
        drawRightBack(canvas);

        drawTimeBack(canvas);
        drawLevelBack(canvas);
    }

    private void drawLeftBack(Canvas canvas){
        Path path = new Path();

        path.moveTo(LeftCenter.x - step, LeftCenter.y - step);
        path.lineTo(LeftUp.x - step, LeftUp.y);
        path.lineTo(LeftUp.x - step * 3, LeftUp.y + step);
        path.lineTo(LeftCenter.x - step * 2, LeftCenter.y - step * 2);
        path.close();
        canvas.drawPath(path, pBackMain);

        path.reset();
        path.moveTo(LeftCenter.x - step, LeftCenter.y - step);
        path.lineTo(LeftCenter.x - step * 2, LeftCenter.y - step * 2);
        path.lineTo(step, LeftCenter.y - step * 2);
        path.lineTo(0f, LeftCenter.y - step);
        path.close();
        canvas.drawPath(path, pBackDark);

        path.reset();
        path.moveTo(0f, LeftCenter.y - step);
        path.lineTo(step, LeftCenter.y - step * 2);
        path.lineTo(step, LeftUp.y + step);
        path.lineTo(0f, LeftUp.y);
        path.close();
        canvas.drawPath(path, pBackLight);

        path.reset();
        path.moveTo(LeftCenter.x - step * 2, LeftCenter.y - step * 2);
        path.lineTo(LeftUp.x - step * 3, LeftUp.y + step);
        path.lineTo(LeftUp.x - step, LeftUp.y);
        path.lineTo(0f, LeftUp.y);
        path.lineTo(step, LeftUp.y + step);
        path.lineTo(step, LeftCenter.y - step * 2);
        path.close();
        canvas.drawPath(path, pMainDark);

    }

    private void drawRightBack(Canvas canvas){
        Path path = new Path();

        path.moveTo(RightCenter.x + step, RightCenter.y - step);
        path.lineTo(RightUp.x + step, RightUp.y);
        path.lineTo(RightUp.x + step * 3, RightUp.y + step);
        path.lineTo(RightCenter.x + step * 2, RightCenter.y - step * 2);
        path.close();
        canvas.drawPath(path, pBackMain);

        path.reset();
        path.moveTo(RightCenter.x + step, RightCenter.y - step);
        path.lineTo(RightCenter.x + step * 2, RightCenter.y - step * 2);
        path.lineTo(width - step, RightCenter.y - step * 2);
        path.lineTo(width, RightCenter.y - step);
        path.close();
        canvas.drawPath(path, pBackDark);

        path.reset();
        path.moveTo(width, RightCenter.y - step);
        path.lineTo(width - step, RightCenter.y - step * 2);
        path.lineTo(width - step, RightUp.y + step);
        path.lineTo(width, RightUp.y);
        path.close();
        canvas.drawPath(path, pBackLight);

        path.reset();
        path.moveTo(RightCenter.x + step * 2, RightCenter.y - step * 2);
        path.lineTo(RightUp.x + step * 3, RightUp.y + step);
        path.lineTo(RightUp.x + step, RightUp.y);
        path.lineTo(width, RightUp.y);
        path.lineTo(width - step, RightUp.y + step);
        path.lineTo(width - step, RightCenter.y - step * 2);
        path.close();
        canvas.drawPath(path, pMainDark);

    }

    private void drawTimeBack(Canvas canvas){
        pFontText.setTextSize(step * 7);
        canvas.drawText("T", step * 3, LeftCenter.y + step * 9, pFontText);
        canvas.drawText("I", step * 12 - pFontText.measureText("I")/2, LeftCenter.y + step * 9, pFontText);
        canvas.drawText("M", step * 21 - pFontText.measureText("M")/2, LeftCenter.y + step * 9, pFontText);
        canvas.drawText("E", step * 30 - pFontText.measureText("E")/2, LeftCenter.y + step * 9, pFontText);

        canvas.drawBitmap(bmRouble, step * 3, LeftCenter.y + step * 21, pPic);

    }

    private void drawLevelBack(Canvas canvas){
        pFontText.setTextSize(step * 7);
        canvas.drawText("L", width - step * 27 - pFontText.measureText("L"), RightCenter.y + step * 9, pFontText);
        canvas.drawText("E", width - step * 21 - pFontText.measureText("E"), RightCenter.y + step * 9, pFontText);
        canvas.drawText("V", width - step * 15 - pFontText.measureText("V"), RightCenter.y + step * 9, pFontText);
        canvas.drawText("E", width - step * 9 - pFontText.measureText("E"), RightCenter.y + step * 9, pFontText);
        canvas.drawText("L", width - step * 3 - pFontText.measureText("L"), RightCenter.y + step * 9, pFontText);

        canvas.drawBitmap(bmBitcoin, width - step * 9, LeftCenter.y + step * 21, pPic);

    }

    private void drawButtons(Canvas canvas){
        canvas.drawBitmap(bmBack, LeftCenter.x/2 - step*14, LeftCenter.y/2 - step*14, pPic);
        canvas.drawBitmap(bmSettings, RightCenter.x + (width - RightCenter.x)/2 - step*14, RightCenter.y/2 - step*14, pPic);
    }

    private void drawTimeInfo(Canvas canvas){
        double hudu = (Math.PI / 180) * 60;
        float radius = (float) ((step * 12) / (2 * Math.cos(hudu / 2)));

        float pb = ((LeftCenter.x + step * 6) - (step * 3))  * user.moves / 500 ;

        Path pbPath = new Path();
        pbPath.moveTo(step * 3, LeftCenter.y + step * 12);
        pbPath.lineTo(step * 3, LeftCenter.y + step * 18);
        pbPath.lineTo(step * 3 + pb, LeftCenter.y + step * 18);
        pbPath.lineTo(step * 3 + pb, LeftCenter.y + step * 12);
        pbPath.close();

        Path strokePath = new Path();
        strokePath.moveTo(step * 3, LeftCenter.y + step * 12);
        strokePath.lineTo(step * 3, LeftCenter.y + step * 18);
        strokePath.lineTo(LeftCenter.x + step * 6, LeftCenter.y + step * 18);
        strokePath.lineTo((float) (LeftCenter.x + step * 6 - radius * Math.cos(hudu * 5)),  (float) (LeftCenter.y + step * 18 + radius * Math.sin(hudu * 5)));
        strokePath.close();

        canvas.save();
        canvas.clipPath(strokePath);
        canvas.drawPath(pbPath, pMainDark);
        canvas.restore();
        canvas.drawPath(strokePath, pMainStroke);

        pFontText.setTextSize(step * 5);
        canvas.drawText(String.valueOf(user.moves), step * 4, LeftCenter.y + step * 17, pFontText);

        canvas.drawText(String.valueOf(user.money_rub), step * 12, LeftCenter.y + step * 27, pFontNumber);
    }

    private void drawLevelInfo(Canvas canvas){
        double hudu = (Math.PI / 180) * 60;
        float radius = (float) ((step * 12) / (2 * Math.cos(hudu / 2)));

        float pb;
        if (user.next_xp != null) {
            pb = ((width - step * 3) - (RightCenter.x - step * 6)) * user.experience_points / user.next_xp;
        } else pb = (width - step * 3) - (RightCenter.x - step * 6);

        Path pbPath = new Path();
        pbPath.moveTo(width - step * 3, RightCenter.y + step * 12);
        pbPath.lineTo(width - step * 3, RightCenter.y + step * 18);
        pbPath.lineTo(width - step * 3 - pb, RightCenter.y + step * 18);
        pbPath.lineTo(width - step * 3 - pb, RightCenter.y + step * 12);
        pbPath.close();

        Path strokePath = new Path();
        strokePath.moveTo(width - step * 3, RightCenter.y + step * 12);
        strokePath.lineTo(width - step * 3, RightCenter.y + step * 18);
        strokePath.lineTo(RightCenter.x - step * 6, RightCenter.y + step * 18);
        strokePath.lineTo((float) (RightCenter.x - step * 6 - radius * Math.cos(hudu * 4)),  (float) (RightCenter.y + step * 18 + radius * Math.sin(hudu * 4)));
        strokePath.close();

        canvas.save();
        canvas.clipPath(strokePath);
        canvas.drawPath(pbPath, pMainDark);
        canvas.restore();
        canvas.drawPath(strokePath, pMainStroke);

        pFontText.setTextSize(step * 5);
        canvas.drawText(String.valueOf(user.level), width - step * 8, LeftCenter.y + step * 17, pFontText);

        canvas.drawText(Double.toString(user.money_btc), RightCenter.x - step * 9, LeftCenter.y + step * 27, pFontNumber);
    }

    public void updateUI(){
        user = Config.user;
        invalidate();
    }

    public void setBackButton(){
        bmBack = mTools.resourcesToBitmap(R.drawable.menu_back, (int) step*28, (int) step*28);
        invalidate();
    }

    public void setAvatar(){
        bmBack = Bitmap.createScaledBitmap(PLAYER_AVATAR, (int) step*28, (int) step*28, false);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!clickable) return true;
        if (isBackClick(event.getX(), event.getY())){
            if (isCooldown) return true;
            blockMenu();
            mTools.playSound();

            ((MainActivity) context).backClick();
        }
        if (isSettingsClick(event.getX(), event.getY())){
            if (isCooldown) return true;
            blockMenu();
            mTools.playSound();

            context.startActivity(new Intent(context, SettingsActivity.class));
        }

        return true;
    }

    private boolean isBackClick(float x, float y){
        return (LeftCenter.x / 2 - step * 14 < x) && (x < LeftCenter.x / 2 + step * 14) && (LeftCenter.y / 2 - step * 14 < y) && (y < LeftCenter.y / 2 + step * 14);
    }

    private boolean isSettingsClick(float x, float y){
        return (RightCenter.x + (width - RightCenter.x)/2 - step*14 < x) && (x < RightCenter.x + (width - RightCenter.x)/2 + step*14) && (LeftCenter.y / 2 - step * 14 < y) && (y < LeftCenter.y / 2 + step * 14);
    }

    private void blockMenu(){
        clickable = false;
        android.os.Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                clickable = true;
            }
        }, 500);
    }
}
