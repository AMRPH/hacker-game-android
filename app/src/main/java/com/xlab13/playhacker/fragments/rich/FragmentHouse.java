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
import com.example.BuyHouseMutation;
import com.example.GetHousingQuery;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentHouse extends Fragment {
    private Context context;


    @BindView(R.id.tvHouseName)
    TextView tvHouseName;

    @BindView(R.id.tvHouseCost)
    TextView tvHouseCost;

    @BindView(R.id.tvHouseLevel)
    TextView tvHouseLevel;

    @BindView(R.id.clHouseCost)
    ConstraintLayout clHouseCost;

    @BindView(R.id.ivHouse)
    ImageView ivHouse;

    @BindView(R.id.flHouseAnim)
    FrameLayout flAnimCooldown;


    public FragmentHouse(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_house, null);
        ButterKnife.bind(this, v);

        updateUI();

        return v;
    }

    public void updateUI(){
        mTools.debug("FragmentHouse");
        GetHousingQuery.GetHousing house = mDataController.getHouseItems().get(Config.user.house-1);

        tvHouseName.setText(house.title());
        tvHouseLevel.setText(house.level() +"");

        SvgLoader.pluck()
                .with(getActivity())
                .setPlaceHolder(R.drawable.icon_no_house, R.drawable.icon_no_house)
                .load(NetworkService.imgUrl + house.image_path(), ivHouse);

        if (!(Config.user.house == 10)){
            clHouseCost.setVisibility(View.VISIBLE);

            GetHousingQuery.GetHousing nextHouse = mDataController.getHouseItems().get(Config.user.house);

            tvHouseCost.setText(nextHouse.price_rub() +"");

            switch (nextHouse.status()){
                case "available":
                    clHouseCost.setBackground(context.getDrawable(R.drawable.style_button_light));
                    clHouseCost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            BuyHouseMutation buyHouseMutation = BuyHouseMutation.builder()
                                    .id(nextHouse.id())
                                    .build();

                            client.mutate(buyHouseMutation).enqueue(new ApolloCall.Callback<BuyHouseMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<BuyHouseMutation.Data> response) {
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
                    clHouseCost.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    clHouseCost.setOnClickListener((v -> {
                        mTools.playSound();
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextHouse.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
                case "blocked":
                    clHouseCost.setBackground(context.getDrawable(R.drawable.style_button_red));
                    clHouseCost.setOnClickListener((v -> {
                        mTools.playSound();
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextHouse.message());
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
