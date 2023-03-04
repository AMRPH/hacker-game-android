package com.xlab13.playhacker.fragments.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.xlab13.playhacker.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentNews extends Fragment {


    @BindView(R.id.tvNewsTitle)
    TextView tvTitle;

    @BindView(R.id.tvNewsText)
    TextView  tvText;


    String title, text;

    FragmentInfo fragmentInfo;


    public FragmentNews(FragmentInfo fragmentInfo){
        this.fragmentInfo = fragmentInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_news, null);
        ButterKnife.bind(this, v);
        tvTitle.setText(title);
        tvText.setText(text);

        return v;
    }

    public void setData(String title, String text){
        this.title = title;
        this.text = text;
    }

    @OnClick(R.id.btnNewsClose)
    public void onCloseClick(View v){
        fragmentInfo.closeFragNews();
    }
}
