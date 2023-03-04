package com.xlab13.playhacker.fragments.rich;

import static com.xlab13.playhacker.Config.isCooldown;
import static com.xlab13.playhacker.Config.mDataController;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.ahmadrosid.svgloader.SvgLoader;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.BuyCarMutation;
import com.example.GetCarsQuery;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentCar extends Fragment {
    private Context context;


    @BindView(R.id.tvCarName)
    TextView tvCarName;

    @BindView(R.id.tvCarCost)
    TextView tvCarCost;

    @BindView(R.id.tvCarLevel)
    TextView tvCarLevel;

    @BindView(R.id.clCarCost)
    ConstraintLayout clCarCost;

    @BindView(R.id.ivCar)
    ImageView ivCar;

    @BindView(R.id.flCarAnim)
    FrameLayout flAnimCooldown;


    public FragmentCar(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_car, null);
        ButterKnife.bind(this, v);

        updateUI();

        return v;
    }

    public void updateUI(){
        mTools.debug("FragmentCar");
        mTools.debug(String.valueOf(Config.user.car));
        GetCarsQuery.GetCar car = mDataController.getCarItems().get(Config.user.car-1);

        tvCarName.setText(car.title());
        tvCarLevel.setText(car.level() +"");

        SvgLoader.pluck()
                .with(getActivity())
                .setPlaceHolder(R.drawable.icon_no_car, R.drawable.icon_no_car)
                .load(NetworkService.imgUrl + car.image_path(), ivCar);

        if (!(Config.user.car == 10)){
            clCarCost.setVisibility(View.VISIBLE);

            GetCarsQuery.GetCar nextCar = mDataController.getCarItems().get(Config.user.car);

            tvCarCost.setText(nextCar.price_rub() + "");

            switch (nextCar.status()){
                case "available":
                    clCarCost.setBackground(context.getDrawable(R.drawable.style_button_light));
                    clCarCost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            BuyCarMutation buyCarMutation = BuyCarMutation.builder()
                                    .id(nextCar.id())
                                    .build();

                            client.mutate(buyCarMutation)
                                    .enqueue(new ApolloCall.Callback<BuyCarMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<BuyCarMutation.Data> response) {
                                            if (response.hasErrors()) {
                                                getActivity().runOnUiThread(() -> {
                                                    String error = response.getErrors().get(0).getMessage();

                                                    mTools.showErrorDialog(getContext(), error);
                                                });
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull ApolloException e) {
                                            mTools.startErrorConnectionActivity(context);
                                        }
                                    });
                        }
                    });
                    break;
                case "notAvailable":
                    clCarCost.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    clCarCost.setOnClickListener((v -> {
                        mTools.playSound();
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextCar.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
                case "blocked":
                    clCarCost.setBackground(context.getDrawable(R.drawable.style_button_red));
                    clCarCost.setOnClickListener((v -> {
                        mTools.playSound();
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextCar.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
            }
        }
    }
}
