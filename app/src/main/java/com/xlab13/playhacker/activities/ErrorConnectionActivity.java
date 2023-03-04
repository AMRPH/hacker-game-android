package com.xlab13.playhacker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ErrorConnectionActivity extends AppCompatActivity {
    Context context;


    @BindView(R.id.btnResetConnection)
    ImageView btnReset;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_connection);
        ButterKnife.bind(this);
        context = this;

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTools.playSound();
                btnReset.setImageResource(R.drawable.button_error_reset_dark);
                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(NetworkService.statusUrl)
                        .build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            btnReset.setImageResource(R.drawable.button_error_reset_light);
                            showErrorDialog();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                JSONArray data = new JSONObject(response.body().string()).getJSONArray("result");
                                boolean auth = data.getJSONObject(0).getBoolean("auth");
                                boolean hacker = data.getJSONObject(2).getBoolean("hacker");

                                if (auth && hacker){
                                    startActivity(new Intent(context, InitializationActivity.class));
                                    finish();
                                }
                            } catch (JSONException e) {
                                runOnUiThread(() -> {
                                    btnReset.setImageResource(R.drawable.button_error_reset_light);
                                    showErrorDialog();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                btnReset.setImageResource(R.drawable.button_error_reset_light);
                                showErrorDialog();
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    private void showErrorDialog() {
        MyAlertDialog dialog = new MyAlertDialog(context);
        dialog.setText(getString(R.string.server_error));
        dialog.addButton(getString(R.string.close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTools.playSound();
                dialog.dismissDialog();
            }
        });
        dialog.showDialog();
    }

}
