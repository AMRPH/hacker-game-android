package com.xlab13.playhacker.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.xlab13.playhacker.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewClanActivity extends Activity {
    Context context;

    private ImageView ivAvatar;
    private EditText etName;

    private Bitmap avatar;

    private List<String> namesOfClans = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_clan);
        context = this;

        etName = findViewById(R.id.etNewClanName);
        ivAvatar = findViewById(R.id.ivNewClanAvatar);

        Button btnCreate = findViewById(R.id.btnNewClanCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (avatar != null && !etName.getText().toString().replace(" ", "").isEmpty() ) createClan();
            }
        });
        btnCreate.setVisibility(View.INVISIBLE);

        Button btnDefault = findViewById(R.id.btnNewClanDefault);
        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.icon_avatar_clan);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    drawable = (DrawableCompat.wrap(drawable)).mutate();
                }
                avatar = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(avatar);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                ivAvatar.setImageBitmap(avatar);
                btnCreate.setVisibility(View.VISIBLE);
            }
        });

        Button btnSelect = findViewById(R.id.btnNewClanSelect);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAvatar();
                btnCreate.setVisibility(View.VISIBLE);
            }
        });
    }


    private void pickAvatar(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_pic)), 0);
    }

    private void createClan(){

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && data!=null){

            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                avatar = scaleCenterCrop(bitmap, 256, 256);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ivAvatar.setImageBitmap(avatar);
        }
    }


    private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);


        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;


        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;


        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }
}
