package com.xlab13.playhacker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloClient;
import com.example.GetJobsQuery;
import com.example.GetLongtermJobsQuery;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.adapters.job.GamesAdapter;
import com.xlab13.playhacker.adapters.job.JobAdapter;
import com.xlab13.playhacker.adapters.job.JobLongtermAdapter;
import com.xlab13.playhacker.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.xlab13.playhacker.Config.isCooldown;
import static com.xlab13.playhacker.Config.mDataController;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentJob extends Fragment {


    private ApolloClient client;


    @BindView(R.id.tabJob)
    ImageView tabJob;

    @BindView(R.id.tabMinigames)
    ImageView tabMinigames;

    @BindView(R.id.tabJobLong)
    ImageView tabJobLong;


    @BindView(R.id.rvJob)
    RecyclerView rv;

    private int tab;


    //public static RewardedVideoAd mRewardedVideoAd;
    //private static String idAd = "ca-app-pub-8059131308960326/4872609341";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_job, null);
        ButterKnife.bind(this, v);

        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        tab = 1;
        tabJob.setImageResource(R.drawable.tab_health_heatlh_red);
        if (rv.getAdapter() == null){
            rv.setAdapter(new JobAdapter(getContext(), mDataController.getJobItems()));
        } else ((JobAdapter) rv.getAdapter()).updateAllItems(mDataController.getJobItems());

        startTutorial();

        /*
        initializeAd(getContext());
        initializeAdDialog();
         */

        return v;
    }

    @OnClick({R.id.tabJob, R.id.tabMinigames, R.id.tabJobLong})
    public void onClick(View v){
        if (isCooldown) return;
        mTools.blockButton(v);
        mTools.playSound();
        setDefaultPic();
        switch (v.getId()){
            case R.id.tabJob:
                tab = 1;
                tabJob.setImageResource(R.drawable.tab_job_job_red);
                rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
                rv.setAdapter(new JobAdapter(getContext(), mDataController.getJobItems()));
                break;
            case R.id.tabMinigames:
                tab = 2;
                tabMinigames.setImageResource(R.drawable.tab_job_minigames_red);
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                rv.setAdapter(new GamesAdapter(getContext(), mDataController.getGameItems()));
                break;
            case R.id.tabJobLong:
                tab = 3;
                tabJobLong.setImageResource(R.drawable.tab_job_longtherm_red);
                startTimer();
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                rv.setAdapter(new JobLongtermAdapter(getContext(), mDataController.getJobLongtermItems()));
                break;
        }
    }

    public void updateUI(){
        switch (tab){
            case 1:
                updateJob();
                break;
            case 3:
                updateJobLongterm();
                break;
        }
    }

    private void updateJob(){
        List<GetJobsQuery.GetJob> oldItems = ((JobAdapter) rv.getAdapter()).getItems();
        List<GetJobsQuery.GetJob> newItems = mDataController.getJobItems();
        List<Integer> updatedItems = new ArrayList<>();

        for (int i = 0; i < newItems.size(); i++){
            if (newItems.get(i).hashCode() != oldItems.get(i).hashCode()) {
                updatedItems.add(i);
            }
        }

        ((JobAdapter) rv.getAdapter()).updateItems(newItems, updatedItems);
    }

    private void updateJobLongterm(){
        startTimer();
        List<GetLongtermJobsQuery.GetLongtermJob> oldItems = ((JobLongtermAdapter) rv.getAdapter()).getItems();
        List<GetLongtermJobsQuery.GetLongtermJob> newItems = mDataController.getJobLongtermItems();
        List<Integer> updatedItems = new ArrayList<>();

        for (int i = 0; i < newItems.size(); i++){
            if (newItems.get(i).hashCode() != oldItems.get(i).hashCode()) {
                updatedItems.add(i);
            }
        }

        ((JobLongtermAdapter) rv.getAdapter()).updateItems(newItems, updatedItems);
    }

    private void startTimer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (Config.user.job_end != null){
                    Date currentDate = new Date(System.currentTimeMillis());
                    Date jobDate = new Date(Config.user.job_end);

                    long  duration = jobDate.getTime() - currentDate.getTime();

                    SimpleDateFormat dataFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());


                    getActivity().runOnUiThread(() -> {
                        mDataController.setJobTimer(dataFormat.format(new Date(duration-10800000)));
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void setDefaultPic() {
        switch (tab){
            case 1:
                tabJob.setImageResource(R.drawable.tab_job_job);
                break;
            case 2:
                tabMinigames.setImageResource(R.drawable.tab_job_minigames);
                break;
            case 3:
                tabJobLong.setImageResource(R.drawable.tab_job_longtherm);
                break;
        }
    }

    private void startTutorial(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "testJob");

        sequence.setConfig(config);
        sequence.addSequenceItem(tabJob, getString(R.string.job), getString(R.string.job_description), getString(R.string.ok));
        sequence.addSequenceItem(tabMinigames, getString(R.string.minigames), getString(R.string.minigames_description), getString(R.string.ok));
        sequence.addSequenceItem(tabJobLong, getString(R.string.farm), getString(R.string.farm__description), getString(R.string.ok));

        sequence.start();
    }


    /*
    static MyAlertDialog adDialog;
    private void initializeAdDialog(){
        adDialog = new MyAlertDialog(getContext());
        adDialog.setText(getString(R.string.you_get) + " 1000 " + getString(R.string.rub));
        adDialog.addButton(getString(R.string.close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adDialog.dismissDialog();
            }
        });
    }

    public static void initializeAd(Context context){
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
            }

            @Override
            public void onRewardedVideoAdOpened() {
            }

            @Override
            public void onRewardedVideoStarted() {
            }

            @Override
            public void onRewardedVideoAdClosed() {
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                player.addRubles(1000);
                player.updateData();
                adDialog.showDialog();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
            }

            @Override
            public void onRewardedVideoCompleted() {
            }
        });

        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
               new AdRequest.Builder().build());
    }

     */
}