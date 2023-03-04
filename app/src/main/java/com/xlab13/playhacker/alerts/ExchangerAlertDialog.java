package com.xlab13.playhacker.alerts;

import static com.xlab13.playhacker.Config.btc_rub;
import static com.xlab13.playhacker.Config.user;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.ExchangeMutation;
import com.example.type.Symbol;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


public class ExchangerAlertDialog implements View.OnClickListener {
    private Context context;

    private int rub;
    private double bitcoin;
    private Symbol type;

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;

    private View layout;
    private ImageView btnRubToBitcion, btnBitcoinToRub;
    private TextView tvExchangeBit, tvExchangeRub, tvCourse;

    private SeekBar sbExchange;

    public ExchangerAlertDialog(Context context){
        this.context = context;
        layout = ((Activity) context).getLayoutInflater().inflate(R.layout.alertdialog_exchanger, null);

        tvExchangeBit = layout.findViewById(R.id.tvExchangeBit);
        tvExchangeRub = layout.findViewById(R.id.tvExchangeRub);
        tvCourse = layout.findViewById(R.id.tvExchangeCourse);

        type = Symbol.RUBBTC;
        tvExchangeBit.setText(user.money_btc+"");
        tvExchangeRub.setText(user.money_rub+"");
        tvCourse.setText("1 B = " + btc_rub + " P");

        sbExchange = layout.findViewById(R.id.sbExchange);
        sbExchange.setMax(user.money_rub/100);
        sbExchange.setOnSeekBarChangeListener(new RubToBitcoinListener());


        btnRubToBitcion = layout.findViewById(R.id.btnRubToBitcion);
        btnBitcoinToRub = layout.findViewById(R.id.btnBitcoinToRub);
        btnRubToBitcion.setOnClickListener(this);
        btnBitcoinToRub.setOnClickListener(this);

        ImageView btnExchangeClose = layout.findViewById(R.id.btnExchangeBack);
        btnExchangeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTools.playSound();
                mDialog.dismiss();
            }
        });

        ImageView btnExchangeOk = layout.findViewById(R.id.btnExchangeOk);
        btnExchangeOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTools.playSound();
                ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                ExchangeMutation exchangeMutation = null;
                switch (type){
                    case RUBBTC:
                        exchangeMutation = ExchangeMutation.builder()
                                .symbol(type)
                                .amount(user.money_rub - rub).build();
                        break;
                    case BTCRUB:
                        exchangeMutation = ExchangeMutation.builder()
                                .symbol(type)
                                .amount(user.money_btc - bitcoin).build();
                        break;
                }


                client.mutate(exchangeMutation).enqueue(new ApolloCall.Callback<ExchangeMutation.Data>() {
                    @Override
                    public void onResponse(@NonNull Response<ExchangeMutation.Data> response) {
                        if (response.hasErrors()) {
                            String error = response.getErrors().get(0).getMessage();

                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    MyAlertDialog alertDialog = new MyAlertDialog(context);
                                    alertDialog.setText(error);
                                    alertDialog.addButton("Ok", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog.dismissDialog();
                                        }
                                    });
                                    alertDialog.showDialog();
                                }
                            });
                        }
                        mDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull ApolloException e) {
                        mDialog.dismiss();
                        mTools.debug(e.getMessage());
                        mTools.startErrorConnectionActivity(context);
                    }
                });
            }
        });

        mBuilder = new AlertDialog.Builder(context, R.style.CustomDialog);
        mBuilder.setCancelable(false);
        mBuilder.setView(layout);
    }

    public void showDialog(){
        mDialog = mBuilder.create();
        mDialog.show();
    }

    @Override
    public void onClick(View v) {
        mTools.playSound();
        switch (v.getId()){
            case R.id.btnRubToBitcion:
                type = Symbol.RUBBTC;
                btnBitcoinToRub.setImageResource(R.drawable.button_ex_bit_rub_iddle);
                btnRubToBitcion.setImageResource(R.drawable.button_ex_rub_bit_active);
                sbExchange.setMax(user.money_rub/100);
                sbExchange.setProgress(0);
                sbExchange.setOnSeekBarChangeListener(new RubToBitcoinListener());
                break;
            case R.id.btnBitcoinToRub:
                type = Symbol.BTCRUB;
                btnBitcoinToRub.setImageResource(R.drawable.button_ex_bit_rub_active);
                btnRubToBitcion.setImageResource(R.drawable.button_ex_rub_bit_iddle);
                sbExchange.setMax((int) (user.money_btc/0.001));
                sbExchange.setProgress(0);
                sbExchange.setOnSeekBarChangeListener(new BitcoinToRubListener());
                break;
        }
    }

    private class RubToBitcoinListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            double exchange = progress * 100;

            bitcoin = user.money_btc + (exchange / btc_rub);
            bitcoin = floorDouble(bitcoin);

            rub = (int) (user.money_rub - exchange);

            tvExchangeBit.setText(bitcoin + "");
            tvExchangeRub.setText(rub + "");
        }

        private double floorDouble(double num) {
            MathContext mathContext = new MathContext(15, RoundingMode.HALF_UP);
            BigDecimal bigDecimal = new BigDecimal(num, mathContext);
            bigDecimal = bigDecimal.setScale(3, BigDecimal.ROUND_DOWN);
            num = bigDecimal.doubleValue();
            return num;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }



    private class BitcoinToRubListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            double exchange = progress * 0.001;

            bitcoin = user.money_btc - (exchange);
            bitcoin = floorDouble(bitcoin);

            rub = (int) (user.money_rub + exchange * btc_rub);

            tvExchangeBit.setText(bitcoin + "");
            tvExchangeRub.setText(rub + "");
        }

        private double floorDouble(double num) {
            MathContext mathContext = new MathContext(15, RoundingMode.HALF_UP); // для double
            BigDecimal bigDecimal = new BigDecimal(num, mathContext);
            bigDecimal = bigDecimal.setScale(3, BigDecimal.ROUND_DOWN);
            num = bigDecimal.doubleValue();
            return num;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }
}
