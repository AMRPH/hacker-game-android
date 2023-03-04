package com.xlab13.playhacker.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xlab13.playhacker.fragments.auth.FragmentAuth;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.fragments.auth.FragmentSignIn;
import com.xlab13.playhacker.fragments.auth.FragmentSignUp;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class AuthActivity extends AppCompatActivity {


    private FragmentAuth fragmentAuth;
    private FragmentSignIn fragmentSignIn;
    private FragmentSignUp fragmentSignUp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        mTools.startMusic(R.raw.game_ost_action_1);

        fragmentAuth = new FragmentAuth(this);
        fragmentSignIn = new FragmentSignIn();
        fragmentSignUp = new FragmentSignUp();

        openFragmentAuth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTools.playMusic();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTools.stopMusic();
    }

    public void openFragmentAuth(){
        openFragment(fragmentAuth, "auth");
    }

    public void openFragmentSignIn(){
        openFragment(fragmentSignIn, "signIn");
    }

    public void openFragmentSignUp(){
        openFragment(fragmentSignUp, "signUp");
    }


    private void openFragment(final Fragment fragment, String tag){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flCont, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onBackPressed() {
        Fragment myFragment = getSupportFragmentManager().findFragmentByTag("auth");
        if (myFragment == null || !myFragment.isVisible()) {
            openFragmentAuth();
        }
    }
}
