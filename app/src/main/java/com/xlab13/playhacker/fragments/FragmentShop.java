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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetHardwareQuery;
import com.example.GetJobsQuery;
import com.example.GetSoftwareQuery;
import com.xlab13.playhacker.adapters.job.JobAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.shop.HardwareAdapter;
import com.xlab13.playhacker.adapters.shop.SoftwareAdapter;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.xlab13.playhacker.Config.mDataController;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import java.util.ArrayList;
import java.util.List;

public class FragmentShop extends Fragment {


    private ApolloClient client;


    @BindView(R.id.tabHardware)
    ImageView tabHardware;

    @BindView(R.id.tabSoftware)
    ImageView tabSoftware;

    @BindView(R.id.tabPerks)
    ImageView tabPerks;


    @BindView(R.id.rvShop)
    RecyclerView rv;


    private int tab;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_shop, null);
        ButterKnife.bind(this, v);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        tab = 1;
        tabHardware.setImageResource(R.drawable.tab_shop_hardware_red);
        if (rv.getAdapter() == null){
            rv.setAdapter(new HardwareAdapter(getContext(), mDataController.getHardwareItems()));
        } else ((HardwareAdapter) rv.getAdapter()).updateAllItems(mDataController.getHardwareItems());

        return v;
    }

    @OnClick({R.id.tabHardware, R.id.tabSoftware, R.id.tabPerks})
    public void onClick(View v){
        mTools.blockButton(v);
        mTools.playSound();
        setDefaultPic();
        rv.setAdapter(null);
        switch (v.getId()){
            case R.id.tabHardware:
                tab = 1;
                tabHardware.setImageResource(R.drawable.tab_shop_hardware_red);
                rv.setAdapter(new HardwareAdapter(getContext(), mDataController.getHardwareItems()));
                break;
            case R.id.tabSoftware:
                tab = 2;
                tabSoftware.setImageResource(R.drawable.tab_shop_software_red);
                rv.setAdapter(new SoftwareAdapter(getContext(), mDataController.getSoftwareItems()));
                break;
            case R.id.tabPerks:
                tab = 3;
                tabPerks.setImageResource(R.drawable.tab_shop_perks_red);
                rv.setAdapter(null);
                break;
        }
    }

    public void updateUI(){
        switch (tab){
            case 1:
                updateHardware();
                break;
            case 2:
                updateSoftware();
                break;
            case 3:
                updatePerks();
                break;
        }
    }

    public void updateHardware(){
        List<GetHardwareQuery.GetHardware> oldItems = ((HardwareAdapter) rv.getAdapter()).getItems();
        List<GetHardwareQuery.GetHardware> newItems = mDataController.getHardwareItems();
        List<Integer> updatedItems = new ArrayList<>();

        for (int i = 0; i < newItems.size(); i++){
            if (newItems.get(i).hashCode() != oldItems.get(i).hashCode()) {
                updatedItems.add(i);
            }
        }

        ((HardwareAdapter) rv.getAdapter()).updateItems(newItems, updatedItems);
    }

    public void updateSoftware(){
        List<GetSoftwareQuery.GetSoftware> oldItems = ((SoftwareAdapter) rv.getAdapter()).getItems();
        List<GetSoftwareQuery.GetSoftware> newItems = mDataController.getSoftwareItems();
        List<Integer> updatedItems = new ArrayList<>();

        for (int i = 0; i < newItems.size(); i++){
            if (newItems.get(i).hashCode() != oldItems.get(i).hashCode()) {
                updatedItems.add(i);
            }
        }

        ((SoftwareAdapter) rv.getAdapter()).updateItems(newItems, updatedItems);
    }

    public void updatePerks(){

    }

    private void setDefaultPic() {
        if (tabHardware.getDrawable() != getContext().getDrawable(R.drawable.tab_shop_hardware))
            tabHardware.setImageResource(R.drawable.tab_shop_hardware);

        if (tabSoftware.getDrawable() != getContext().getDrawable(R.drawable.tab_shop_software))
            tabSoftware.setImageResource(R.drawable.tab_shop_software);

        if (tabPerks.getDrawable() != getContext().getDrawable(R.drawable.tab_shop_perks))
            tabPerks.setImageResource(R.drawable.tab_shop_perks);
    }

    private void startTutorial(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "testShop");

        sequence.setConfig(config);
        sequence.addSequenceItem(tabHardware, getString(R.string.hardware), getString(R.string.hardware_description), getString(R.string.ok));
        sequence.addSequenceItem(tabSoftware, getString(R.string.software), getString(R.string.software_description), getString(R.string.ok));
        sequence.addSequenceItem(tabPerks, getString(R.string.perks), getString(R.string.perks_description), getString(R.string.ok));

        sequence.start();
    }

}
