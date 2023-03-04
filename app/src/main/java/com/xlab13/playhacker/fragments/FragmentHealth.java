package com.xlab13.playhacker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetHealthQuery;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.HealthAdapter;
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

public class FragmentHealth extends Fragment {


    @BindView(R.id.tabHealth)
    ImageView tabHealth;

    @BindView(R.id.tabAlco)
    ImageView tabAlco;

    @BindView(R.id.tabMood)
    ImageView tabMood;


    @BindView(R.id.rvHealth)
    RecyclerView rv;


    private int tab;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_health, null);
        ButterKnife.bind(this, v);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        tab = 1;
        tabHealth.setImageResource(R.drawable.tab_health_heatlh_red);
        if (rv.getAdapter() == null){
            rv.setAdapter(new HealthAdapter(getContext(), mDataController.getHealthItems()));
        } else getAdapter().updateAllItems(mDataController.getHealthItems());


        startTutorial();
        return v;
    }

    @OnClick({R.id.tabHealth, R.id.tabAlco, R.id.tabMood})
    public void onClick(View v){
        if (isCooldown) return;
        mTools.blockButton(v);
        mTools.playSound();
        setDefaultPic();
        switch (v.getId()){
            case R.id.tabHealth:
                tab = 1;
                tabHealth.setImageResource(R.drawable.tab_health_heatlh_red);
                getAdapter().updateAllItems(mDataController.getHealthItems());
                break;
            case R.id.tabAlco:
                tab = 2;
                tabAlco.setImageResource(R.drawable.tab_health_alco_red);
                getAdapter().updateAllItems(mDataController.getAlcoItems());
                break;
            case R.id.tabMood:
                tab = 3;
                tabMood.setImageResource(R.drawable.tab_health_mood_red);
                getAdapter().updateAllItems(mDataController.getMoodItems());
                break;
        }
    }

    public void updateUI(){
        List<GetHealthQuery.GetHealth> oldItems = getAdapter().getItems();
        List<GetHealthQuery.GetHealth> newItems;
        List<Integer> updatedItems = new ArrayList<>();
        switch (tab){
            case 1:
                newItems = mDataController.getHealthItems();
                break;
            case 2:
                newItems = mDataController.getAlcoItems();
                break;
            case 3:
                newItems = mDataController.getMoodItems();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tab);
        }

        for (int i = 0; i < newItems.size(); i++){
            if (newItems.get(i).hashCode() != oldItems.get(i).hashCode()) {
                updatedItems.add(i);
            }
        }

        getAdapter().updateItems(newItems, updatedItems);
    }

    private void setDefaultPic() {
        switch (tab){
            case 1:
                tabHealth.setImageResource(R.drawable.tab_health_health);
                break;
            case 2:
                tabAlco.setImageResource(R.drawable.tab_health_alco);
                break;
            case 3:
                tabMood.setImageResource(R.drawable.tab_health_mood);
                break;
        }
    }

    private void startTutorial(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "testHealth");

        sequence.setConfig(config);
        sequence.addSequenceItem(tabHealth, getString(R.string.stamina), getString(R.string.stamina_description), "OK");
        sequence.addSequenceItem(tabAlco, getString(R.string.alco), getString(R.string.alco_description), "OK");
        sequence.addSequenceItem(tabMood, getString(R.string.mood), getString(R.string.mood_description), "OK");

        sequence.start();
    }

    private HealthAdapter getAdapter(){
        return (HealthAdapter) rv.getAdapter();
    }
}
