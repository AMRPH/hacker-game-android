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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.BuyGirlItemMutation;
import com.example.GetGirlItemsQuery;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentGirl extends Fragment {
    private Context context;


    @BindView(R.id.ivGirl)
    ImageView ivGirl;


    @BindView(R.id.btnAppearance)
    ConstraintLayout btnAppearance;

    @BindView(R.id.tvAppearance)
    TextView tvAppearance;

    @BindView(R.id.tvAppearanceCost)
    TextView tvAppearanceCost;

    @BindView(R.id.tvAppearanceLvl)
    TextView tvAppearanceLvl;


    @BindView(R.id.btnClothes)
    ConstraintLayout btnClothes;

    @BindView(R.id.tvClothes)
    TextView tvClothes;

    @BindView(R.id.tvClothesCost)
    TextView tvClothesCost;

    @BindView(R.id.tvClothesLvl)
    TextView tvClothesLvl;


    @BindView(R.id.btnJewelry)
    ConstraintLayout btnJewelry;

    @BindView(R.id.tvJewelry)
    TextView tvJewelry;

    @BindView(R.id.tvJewelryCost)
    TextView tvJewelryCost;

    @BindView(R.id.tvJewelryLvl)
    TextView tvJewelryLvl;


    @BindView(R.id.btnLeisure)
    ConstraintLayout btnLeisure;

    @BindView(R.id.tvLeisure)
    TextView tvLeisure;

    @BindView(R.id.tvLeisureCost)
    TextView tvLeisureCost;

    @BindView(R.id.tvLeisureLvl)
    TextView tvLeisureLvl;


    @BindView(R.id.btnSport)
    ConstraintLayout btnSport;

    @BindView(R.id.tvSport)
    TextView tvSport;

    @BindView(R.id.tvSportCost)
    TextView tvSportCost;

    @BindView(R.id.tvSportLvl)
    TextView tvSportLvl;

    @BindView(R.id.flGirlAnim)
    FrameLayout flAnimCooldown;


    public FragmentGirl(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_girl, null);
        ButterKnife.bind(this, v);

        updateUI();

        return v;
    }

    public void updateUI() {
        mTools.debug("FragmentGirl");
        List<GetGirlItemsQuery.GetGirlItem> items = mDataController.getGirlItems();

        GetGirlItemsQuery.GetGirlItem appearance = items.get(0);
        if (Config.user.girl_appearance == 10) {
            btnAppearance.setVisibility(View.INVISIBLE);
        } else {
            GetGirlItemsQuery.Item item = appearance.items().get(Config.user.girl_appearance - 1);
            GetGirlItemsQuery.Item nextItem = appearance.items().get(Config.user.girl_appearance);

            tvAppearance.setText(item.title());
            tvAppearanceLvl.setText(item.level() + "");
            tvAppearanceCost.setText(nextItem.price_rub() + "");

            switch (nextItem.status()) {
                case "available":
                    btnAppearance.setBackground(context.getDrawable(R.drawable.style_button_light));
                    btnAppearance.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            BuyGirlItemMutation buyGirlItemMutation = BuyGirlItemMutation.builder()
                                    .id(nextItem.id())
                                    .build();

                            client.mutate(buyGirlItemMutation)
                                    .enqueue(new ApolloCall.Callback<BuyGirlItemMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<BuyGirlItemMutation.Data> response) {
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
                    btnAppearance.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    btnAppearance.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
                case "blocked":
                    btnAppearance.setBackground(context.getDrawable(R.drawable.style_button_red));
                    btnAppearance.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
            }
        }

        GetGirlItemsQuery.GetGirlItem clothes = items.get(1);
        if (Config.user.girl_clothes == 10) {
            btnClothes.setVisibility(View.INVISIBLE);
        } else {
            GetGirlItemsQuery.Item item = clothes.items().get(Config.user.girl_clothes - 1);
            GetGirlItemsQuery.Item nextItem = clothes.items().get(Config.user.girl_clothes);

            tvClothes.setText(item.title());
            tvClothesLvl.setText(item.level() + "");
            tvClothesCost.setText(nextItem.price_rub() + "");

            switch (nextItem.status()) {
                case "available":
                    btnClothes.setBackground(context.getDrawable(R.drawable.style_button_light));
                    btnClothes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            BuyGirlItemMutation buyGirlItemMutation = BuyGirlItemMutation.builder()
                                    .id(nextItem.id())
                                    .build();

                            client.mutate(buyGirlItemMutation)
                                    .enqueue(new ApolloCall.Callback<BuyGirlItemMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<BuyGirlItemMutation.Data> response) {
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
                    btnClothes.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    btnClothes.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
                case "blocked":
                    btnClothes.setBackground(context.getDrawable(R.drawable.style_button_red));
                    btnClothes.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
            }
        }

        GetGirlItemsQuery.GetGirlItem jewelry = items.get(2);
        if (Config.user.girl_jewelry == 10) {
            btnJewelry.setVisibility(View.INVISIBLE);
        } else {
            GetGirlItemsQuery.Item item = jewelry.items().get(Config.user.girl_jewelry - 1);
            GetGirlItemsQuery.Item nextItem = jewelry.items().get(Config.user.girl_jewelry);

            tvJewelry.setText(item.title());
            tvJewelryLvl.setText(item.level() + "");
            tvJewelryCost.setText(nextItem.price_rub() + "");

            switch (nextItem.status()) {
                case "available":
                    btnJewelry.setBackground(context.getDrawable(R.drawable.style_button_light));
                    btnJewelry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            BuyGirlItemMutation buyGirlItemMutation = BuyGirlItemMutation.builder()
                                    .id(nextItem.id())
                                    .build();

                            client.mutate(buyGirlItemMutation)
                                    .enqueue(new ApolloCall.Callback<BuyGirlItemMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<BuyGirlItemMutation.Data> response) {
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
                    btnJewelry.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    btnJewelry.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
                case "blocked":
                    btnJewelry.setBackground(context.getDrawable(R.drawable.style_button_red));
                    btnJewelry.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
            }
        }

        GetGirlItemsQuery.GetGirlItem leisure = items.get(3);
        if (Config.user.girl_leisure == 10) {
            btnLeisure.setVisibility(View.INVISIBLE);
        } else {
            GetGirlItemsQuery.Item item = leisure.items().get(Config.user.girl_leisure - 1);
            GetGirlItemsQuery.Item nextItem = leisure.items().get(Config.user.girl_leisure);

            tvLeisure.setText(item.title());
            tvLeisureLvl.setText(item.level() + "");
            tvLeisureCost.setText(nextItem.price_rub() + "");

            switch (nextItem.status()) {
                case "available":
                    btnLeisure.setBackground(context.getDrawable(R.drawable.style_button_light));
                    btnLeisure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            BuyGirlItemMutation buyGirlItemMutation = BuyGirlItemMutation.builder()
                                    .id(nextItem.id())
                                    .build();

                            client.mutate(buyGirlItemMutation)
                                    .enqueue(new ApolloCall.Callback<BuyGirlItemMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<BuyGirlItemMutation.Data> response) {
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
                    btnLeisure.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    btnLeisure.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
                case "blocked":
                    btnLeisure.setBackground(context.getDrawable(R.drawable.style_button_red));
                    btnLeisure.setOnClickListener((v -> {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
            }
        }

        GetGirlItemsQuery.GetGirlItem sport = items.get(4);
        if (Config.user.girl_sport == 10) {
            btnSport.setVisibility(View.INVISIBLE);
        } else {
            GetGirlItemsQuery.Item item = sport.items().get(Config.user.girl_sport - 1);
            GetGirlItemsQuery.Item nextItem = sport.items().get(Config.user.girl_sport);

            tvSport.setText(item.title());
            tvSportLvl.setText(item.level() + "");
            tvSportCost.setText(nextItem.price_rub() + "");

            switch (nextItem.status()) {
                case "available":
                    btnSport.setBackground(context.getDrawable(R.drawable.style_button_light));
                    btnSport.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            BuyGirlItemMutation buyGirlItemMutation = BuyGirlItemMutation.builder()
                                    .id(nextItem.id())
                                    .build();

                            client.mutate(buyGirlItemMutation)
                                    .enqueue(new ApolloCall.Callback<BuyGirlItemMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<BuyGirlItemMutation.Data> response) {
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
                    btnSport.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    btnSport.setOnClickListener((v -> {
                        mTools.playSound();
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
                        myAlertDialog.addButton(getContext().getString(R.string.close), (v1) -> {
                            myAlertDialog.dismissDialog();
                        });
                        myAlertDialog.showDialog();
                    }));
                    break;
                case "blocked":
                    btnSport.setBackground(context.getDrawable(R.drawable.style_button_red));
                    btnSport.setOnClickListener((v -> {
                        mTools.playSound();
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                        myAlertDialog.setText(nextItem.message());
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
