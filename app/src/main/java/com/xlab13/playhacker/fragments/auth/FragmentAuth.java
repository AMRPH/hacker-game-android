package com.xlab13.playhacker.fragments.auth;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xlab13.playhacker.R;
import com.xlab13.playhacker.activities.AuthActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentAuth extends Fragment {
    AuthActivity activity;

    public FragmentAuth(AuthActivity activity){
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_auth, null);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.btnSignIn)
    void signInClick(){
        mTools.playSound();
        activity.openFragmentSignIn();
    }

    @OnClick(R.id.btnSignUp)
    void signUpClick(){
        mTools.playSound();
        activity.openFragmentSignUp();
    }

}
