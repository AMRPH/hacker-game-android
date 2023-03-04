package com.xlab13.playhacker.utils;

import static com.xlab13.playhacker.Config.cooldownTime;
import static com.xlab13.playhacker.Config.isCooldown;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.xlab13.playhacker.activities.MainActivity;
import com.xlab13.playhacker.fragments.FragmentHealth;

public class Animations {

    public static void animateCooldown(View view, Context context){
        isCooldown = true;
        view.setVisibility(View.VISIBLE);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(showAlphaAnimationCooldown());
        animationSet.addAnimation(showScaleAnimationCooldown());
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isCooldown = false;
                view.setVisibility(View.INVISIBLE);
                ((MainActivity) context).updateUI();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animationSet);
    }

    private static Animation showAlphaAnimationCooldown(){
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(cooldownTime/4);
        return animation;
    }

    private static Animation showScaleAnimationCooldown() {
        ScaleAnimation animation = new ScaleAnimation(
                1f, 0f, 1f, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f);
        animation.setDuration(cooldownTime);
        return animation;
    }
}
