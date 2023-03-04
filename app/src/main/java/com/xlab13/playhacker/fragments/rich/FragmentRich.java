package com.xlab13.playhacker.fragments.rich;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xlab13.playhacker.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class FragmentRich extends Fragment {


    @BindView(R.id.tabHouse)
    ImageView tabHouse;

    @BindView(R.id.tabCar)
    ImageView tabCar;

    @BindView(R.id.tabGirlfriend)
    ImageView tabGF;


    private int tab;

    FragmentHouse fragHouse;
    FragmentCar fragCar;
    FragmentGirl fragGirl;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_rich, null);
        ButterKnife.bind(this, v);

        fragHouse = new FragmentHouse(getContext());
        fragCar = new FragmentCar(getContext());
        fragGirl = new FragmentGirl(getContext());

        tab = 1;
        tabHouse.setImageResource(R.drawable.tab_house_house_red);
        openFragment(fragHouse, "house");

        startTutorial();

        return v;
    }

    @OnClick({R.id.tabHouse, R.id.tabCar, R.id.tabGirlfriend})
    public void onClick(View v){
        mTools.blockButton(v);
        mTools.playSound();
        setDefaultPic();
        switch (v.getId()){
            case R.id.tabHouse:
                tab = 1;
                tabHouse.setImageResource(R.drawable.tab_house_house_red);
                openFragment(fragHouse, "house");
                break;
            case R.id.tabCar:
                tab = 2;
                tabCar.setImageResource(R.drawable.tab_house_car_red);
                openFragment(fragCar, "car");
                break;
            case R.id.tabGirlfriend:
                tab = 3;
                tabGF.setImageResource(R.drawable.tab_house_girlfriend_red);
                openFragment(fragGirl, "girl");
                break;
        }
    }

    public void updateUI(){
        mTools.debug("updateUI");
        switch (tab){
            case 1:
                fragHouse.updateUI();
                break;
            case 2:
                fragCar.updateUI();
                break;
            case 3:
                fragGirl.updateUI();
                break;
        }
    }

    private void openFragment(final Fragment fragment, String tag){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flRichCont, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    private void setDefaultPic() {
        if (tabHouse.getDrawable() != getContext().getDrawable(R.drawable.tab_house_house))
            tabHouse.setImageResource(R.drawable.tab_house_house);

        if (tabCar.getDrawable() != getContext().getDrawable(R.drawable.tab_house_car))
            tabCar.setImageResource(R.drawable.tab_house_car);

        if (tabGF.getDrawable() != getContext().getDrawable(R.drawable.tab_house_girlfriend))
            tabGF.setImageResource(R.drawable.tab_house_girlfriend);
    }

    private void startTutorial(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "testHouse");

        sequence.setConfig(config);
        sequence.addSequenceItem(tabHouse, getString(R.string.house), getString(R.string.house_description), getString(R.string.ok));
        sequence.addSequenceItem(tabCar, getString(R.string.car), getString(R.string.car_description), getString(R.string.ok));
        sequence.addSequenceItem(tabGF, getString(R.string.love), getString(R.string.love_description), getString(R.string.ok));

        sequence.start();
    }
}