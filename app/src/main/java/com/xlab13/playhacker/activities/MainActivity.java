package com.xlab13.playhacker.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.ProfileSubscription;
import com.xlab13.playhacker.activities.main.MenuView;
import com.xlab13.playhacker.alerts.GiftAlertDialog;
import com.xlab13.playhacker.alerts.MenuAlertDialog;
import com.xlab13.playhacker.fragments.info.FragmentInfo;
import com.xlab13.playhacker.fragments.FragmentHacker;
import com.xlab13.playhacker.fragments.FragmentHealth;
import com.xlab13.playhacker.fragments.rich.FragmentRich;
import com.xlab13.playhacker.fragments.FragmentJob;
import com.xlab13.playhacker.fragments.FragmentShop;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.xlab13.playhacker.Config.isCooldown;
import static com.xlab13.playhacker.Config.isGiftReceived;
import static com.xlab13.playhacker.Config.user;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;
import static com.xlab13.playhacker.Config.PLAYER_AVATAR;

public class MainActivity extends AppCompatActivity {
    Context context;


    @BindView(R.id.btnHealth)
    ImageView btnHealth;

    @BindView(R.id.btnJob)
    ImageView btnJob;

    @BindView(R.id.btnShop)
    ImageView btnShop;

    @BindView(R.id.btnInfo)
    ImageView btnInfo;

    @BindView(R.id.btnRich)
    ImageView btnRich;

    @BindView(R.id.menuView)
    MenuView menuView;


    //@BindView(R.id.btnBack)
    //ImageView btnBack;

    private FragmentHacker fragHacker;
    private FragmentHealth fragHealth;
    private FragmentJob fragJob;
    private FragmentShop fragShop;
    private FragmentInfo fragEdu;
    private FragmentRich fragRich;

    private ApolloClient client;

    MenuAlertDialog menuDialog;

    int frag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;

        client = NetworkService.getInstance().getGameClientWithToken();

        fragHacker = new FragmentHacker();
        fragHealth = new FragmentHealth();
        fragJob = new FragmentJob();
        fragShop = new FragmentShop();
        fragEdu = new FragmentInfo();
        fragRich = new FragmentRich();

        if (PLAYER_AVATAR != null) menuView.setAvatar();
        frag = 0;
        openFragment(fragHacker, "hacker");

        updateUI();

        subscribeProfile();

        menuDialog = new MenuAlertDialog(this);

        startTutorial();
    }

    @Override
    protected void onResume() {
        mTools.startMusic(R.raw.game_ost_chill_1);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mTools.stopMusic();
        super.onPause();
    }

    @OnClick({R.id.btnHealth})
    public void onHealthClick(View v){
        if (isCooldown) return;
        defaultClick(v);

        if (frag == 0) menuView.setBackButton();
        setDefaultState();
        frag = 1;

        btnHealth.setImageResource(R.drawable.button_health_red);

        openFragment(fragHealth, "health");
    }

    @OnClick({R.id.btnJob})
    public void onJobClick(View v){
        if (isCooldown) return;
        defaultClick(v);

        if (frag == 0) menuView.setBackButton();
        setDefaultState();
        frag = 2;

        btnJob.setImageResource(R.drawable.button_job_red);

        openFragment(fragJob, "job");
    }

    @OnClick({R.id.btnShop})
    public void onShopClick(View v){
        if (isCooldown) return;
        defaultClick(v);

        if (frag == 0) menuView.setBackButton();
        setDefaultState();
        frag = 3;

        btnShop.setImageResource(R.drawable.button_shop_red);

        openFragment(fragShop, "shop");
    }

    @OnClick({R.id.btnInfo})
    public void onEduClick(View v){
        if (isCooldown) return;
        defaultClick(v);

        if (frag == 0) menuView.setBackButton();
        setDefaultState();
        frag = 4;

        btnInfo.setImageResource(R.drawable.button_info_red);

        openFragment(fragEdu, "edu");
    }

    @OnClick({R.id.btnRich})
    public void onRichClick(View v){
        if (isCooldown) return;
        defaultClick(v);

        if (frag == 0) menuView.setBackButton();
        setDefaultState();
        frag = 5;

        btnRich.setImageResource(R.drawable.button_house_red);

        openFragment(fragRich, "house");
    }

    public void backClick(){
        if (frag != 0){
            menuView.setAvatar();
            setDefaultState();
            frag = 0;

            openFragment(fragHacker, "hacker");
        } else {
            menuDialog.showDialog();
        }
    }

    private void defaultClick(View v){
        mTools.blockButton(v);
        mTools.playSound();
    }


    private void setDefaultState(){
        switch (frag){
            case 1:
                btnHealth.setImageResource(R.drawable.button_health);
                break;
            case 2:
                btnJob.setImageResource(R.drawable.button_job);
                break;
            case 3:
                btnShop.setImageResource(R.drawable.button_shop);
                break;
            case 4:
                btnInfo.setImageResource(R.drawable.button_info);
                break;
            case 5:
                btnRich.setImageResource(R.drawable.button_rich);
                break;
        }
    }


    public void subscribeProfile(){
        client.subscribe(new ProfileSubscription()).execute(new ApolloSubscriptionCall.Callback<ProfileSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<ProfileSubscription.Data> response) {
                mTools.debug("profile onResponse");
                if (!response.hasErrors()){
                    ProfileSubscription.Profile profile = response.getData().profile();


                    user.user_id = profile.user_id();
                    user.username = profile.username();
                    user.alco_balance = profile.alco_balance();
                    user.clan_id = profile.clan_id();
                    user.clan = profile.clan();
                    user.experience_points = profile.experience_points();
                    user.next_xp = profile.next_xp();
                    user.health_points = profile.health_points();
                    if (profile.job_end() == null) user.job_end = null;
                    else user.job_end = profile.job_end().longValue();
                    user.level = profile.level();
                    user.money_btc = profile.money_btc();
                    user.money_rub = profile.money_rub();
                    user.mood = profile.mood();
                    user.moves = profile.moves();
                    user.rating = profile.rating();
                    user.work_hack_balance = profile.work_hack_balance();
                    user.house = profile.house();
                    user.car = profile.car();
                    user.girl_appearance = profile.girl_appearance();
                    user.girl_clothes = profile.girl_clothes();
                    user.girl_jewelry = profile.girl_jewelry();
                    user.girl_leisure = profile.girl_leisure();
                    user.girl_level = profile.girl_level();
                    user.girl_sport = profile.girl_sport();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isCooldown) updateUI();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
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

            @Override
            public void onCompleted() {
                mTools.debug("profile onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("profile onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("profile onConnected");
            }
        });
    }

    public void updateUI() {
        if (user == null){
            return;
        }
        updateUIFrag();

        menuView.updateUI();
    }

    private void updateUIFrag(){
        switch (frag){
            case 1:
                fragHealth.updateUI();
                break;
            case 2:
                fragJob.updateUI();
                break;
            case 3:
                fragShop.updateUI();
                break;
            case 4:
                fragEdu.updateUI();
                break;
            case 5:
                fragRich.updateUI();
                break;
        }
    }


    private void startTutorial(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "testMain");

        sequence.setConfig(config);
        sequence.addSequenceItem(btnHealth, getString(R.string.health), getString(R.string.health_description), getString(R.string.ok));
        sequence.addSequenceItem(btnJob, getString(R.string.work_hack), getString(R.string.work_hack_description), getString(R.string.ok));
        sequence.addSequenceItem(btnShop, getString(R.string.shop), getString(R.string.shop_description), getString(R.string.ok));
        sequence.addSequenceItem(btnInfo, getString(R.string.info), getString(R.string.info_description), getString(R.string.ok));
        sequence.addSequenceItem(btnRich, getString(R.string.rich), getString(R.string.rich_description), getString(R.string.ok));

        sequence.start();
    }

    private void openFragment(final Fragment fragment, String tag){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frgmCont, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onBackPressed(){
    }

}