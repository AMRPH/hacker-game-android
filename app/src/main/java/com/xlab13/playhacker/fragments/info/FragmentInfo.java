package com.xlab13.playhacker.fragments.info;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetJobsQuery;
import com.example.GetNewsQuery;
import com.xlab13.playhacker.adapters.job.GamesAdapter;
import com.xlab13.playhacker.adapters.job.JobAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.NewsAdapter;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.xlab13.playhacker.Config.isCooldown;
import static com.xlab13.playhacker.Config.mDataController;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import java.util.ArrayList;
import java.util.List;

public class FragmentInfo extends Fragment {


    @BindView(R.id.tabStatistic)
    ImageView tabStatistic;

    @BindView(R.id.tabNews)
    ImageView tabNews;

    @BindView(R.id.tabPartners)
    ImageView tabPartners;

    @BindView(R.id.flInfoCont)
    FrameLayout flEduCont;


    private int tab;

    FragmentNews fragNews;
    FragmentStatistic fragStat;
    RecyclerView rv;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_info, null);
        ButterKnife.bind(this, v);

        fragNews = new FragmentNews(this);
        fragStat = new FragmentStatistic();

        tabStatistic.setImageResource(R.drawable.tab_edu_statistic_red);
        openFragment(fragStat, "statistic");
        return v;
    }

    @OnClick({R.id.tabNews, R.id.tabStatistic, R.id.tabPartners})
    public void onClick(View v){
        if (isCooldown) return;
        mTools.blockButton(v);
        mTools.playSound();
        setDefaultPic();
        switch (v.getId()){
            case R.id.tabStatistic:
                tab = 1;
                tabStatistic.setImageResource(R.drawable.tab_edu_statistic_red);
                openFragment(fragStat, "statistic");
                startTutorial();
                rv = null;
                break;
            case R.id.tabNews:
                tab = 2;
                tabNews.setImageResource(R.drawable.tab_edu_news_red);
                setRecyclerView();
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                rv.setAdapter(new NewsAdapter(getContext(), mDataController.getNewItems(), fragNews));
                break;
            case R.id.tabPartners:
                tab = 3;
                tabPartners.setImageResource(R.drawable.tab_edu_partners_red);
                setRecyclerView();
                break;
        }
    }


    public void updateUI(){
        switch (tab){
            case 2:
                updateNews();
                break;
            case 3:
                break;
        }
    }

    private void updateNews(){
        List<GetNewsQuery.GetNew> oldItems = ((NewsAdapter) rv.getAdapter()).getItems();
        List<GetNewsQuery.GetNew> newItems = mDataController.getNewItems();
        List<Integer> updatedItems = new ArrayList<>();

        for (int i = 0; i < newItems.size(); i++){
            if (newItems.get(i).hashCode() != oldItems.get(i).hashCode()) {
                updatedItems.add(i);
            }
        }

        ((NewsAdapter) rv.getAdapter()).updateItems(newItems, updatedItems);
    }

    private void setRecyclerView(){
        if (rv != null){
            rv.setAdapter(null);
            return;
        }
        flEduCont.removeAllViews();

        ScrollView scrollView = new ScrollView(getContext());
        rv = new RecyclerView(getContext());

        ViewGroup.LayoutParams lpMATCH = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrollView.setLayoutParams(lpMATCH);
        rv.setLayoutParams(lpMATCH);

        scrollView.addView(rv);

        flEduCont.addView(scrollView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void openFragment(final Fragment fragment, String tag){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flInfoCont, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    public void closeFragNews(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragNews);
        transaction.addToBackStack(null);
        transaction.commit();

        setRecyclerView();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new NewsAdapter(getContext(), mDataController.getNewItems(), fragNews));
    }

    private void setDefaultPic() {
        if (tabStatistic.getDrawable() != getContext().getDrawable(R.drawable.tab_edu_statistic))
            tabStatistic.setImageResource(R.drawable.tab_edu_statistic);

        if (tabNews.getDrawable() != getContext().getDrawable(R.drawable.tab_edu_news))
            tabNews.setImageResource(R.drawable.tab_edu_news);

        if (tabPartners.getDrawable() != getContext().getDrawable(R.drawable.tab_edu_partners))
            tabPartners.setImageResource(R.drawable.tab_edu_partners);
    }

    private void startTutorial(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "testEdu");

        sequence.setConfig(config);
        sequence.addSequenceItem(tabStatistic, getString(R.string.stats), getString(R.string.stats_description), getString(R.string.ok));
        sequence.addSequenceItem(tabNews, getString(R.string.news), getString(R.string.news_description), getString(R.string.ok));
        sequence.addSequenceItem(tabPartners, getString(R.string.partners), getString(R.string.partners_description), getString(R.string.ok));

        sequence.start();
    }
}