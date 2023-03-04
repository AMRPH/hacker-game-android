package com.xlab13.playhacker.alerts;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.ApplyGiftMutation;
import com.example.GetGiftsQuery;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;

import java.util.HashMap;
import java.util.Map;

import static com.xlab13.playhacker.Config.gifts;
import static com.xlab13.playhacker.Config.isGiftReceived;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;


public class GiftAlertDialog {
    private Context context;

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;

    private RelativeLayout layout;
    private ImageView btn1, btn2, btn3, btn4, btn5, btn6;

    private ImageView[] buttons;

    private Map<ImageView, Integer> buttonsToId;


    public GiftAlertDialog(Context context){
        this.context = context;

        layout = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.alertdialog_gift, null);

        btn1 = layout.findViewById(R.id.btnOne);
        btn2 = layout.findViewById(R.id.btnTwo);
        btn3 = layout.findViewById(R.id.btnThree);
        btn4 = layout.findViewById(R.id.btnFourth);
        btn5 = layout.findViewById(R.id.btnFive);
        btn6 = layout.findViewById(R.id.btnSix);
        buttons = new ImageView[] {btn1, btn2, btn3, btn4, btn5, btn6};

        buttonsToId = new HashMap<>();
        buttonsToId.put(btn1, 1);
        buttonsToId.put(btn2, 2);
        buttonsToId.put(btn3, 3);
        buttonsToId.put(btn4, 4);
        buttonsToId.put(btn5, 5);
        buttonsToId.put(btn6, 6);

        GiftListener gl = new GiftListener();
        for (ImageView button : buttons){
            button.setOnClickListener(gl);
        }

        mBuilder = new AlertDialog.Builder(context, R.style.CustomDialog);
        mBuilder.setView(layout);
    }

    private class GiftListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mTools.playSound();
            for (ImageView button : buttons) {
                button.setClickable(false);
            }
            isGiftReceived = true;

            mDialog.dismiss();

            int id = buttonsToId.get(v);

            ApplyGiftMutation applyGiftMutation = ApplyGiftMutation.builder().id(id).build();

            NetworkService.getInstance().getGameClientWithToken().mutate(applyGiftMutation)
                    .enqueue(new ApolloCall.Callback<ApplyGiftMutation.Data>() {
                        @Override
                        public void onResponse(@NonNull Response<ApplyGiftMutation.Data> response) {
                            if (!response.hasErrors()) {

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String message = "Вы получили ";
                                        GetGiftsQuery.Gift gift = gifts.get(id - 1);
                                        switch (gift.title()) {
                                            case "money_rub":
                                                message += gift.amount().intValue() + " RUB";
                                                break;
                                            case "money_btc":
                                                message += gift.amount() + " BTC";
                                                break;
                                            case "experience_points":
                                                message += gift.amount().intValue() + " XP";
                                                break;
                                        }

                                        MyAlertDialog alertDialog = new MyAlertDialog(context);
                                        alertDialog.setText(message);
                                        alertDialog.addButton(context.getString(R.string.ok), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mTools.playSound();
                                                alertDialog.dismissDialog();
                                            }
                                        });
                                        alertDialog.showDialog();
                                    }
                                });
                            } else {
                                ((Activity) context).runOnUiThread(() -> {
                                    String error = response.getErrors().get(0).getMessage();

                                    mTools.showErrorDialog(context, error);
                                });
                            }
                        }

                        @Override
                        public void onFailure(@NonNull ApolloException e) {
                            mTools.debug(e.getMessage());
                            mTools.startErrorConnectionActivity(context);
                        }
                    });
        }
    }

    public void showDialog() {
        if (mBuilder != null) {
            mDialog = mBuilder.create();
            mDialog.show();
        }
    }

}
