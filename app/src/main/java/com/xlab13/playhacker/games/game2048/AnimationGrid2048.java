package com.xlab13.playhacker.games.game2048;

import java.util.ArrayList;


public class AnimationGrid2048 {
    public final ArrayList<AnimationCell2048> globalAnimation = new ArrayList<>();
    private final ArrayList<AnimationCell2048>[][] field;
    private int activeAnimations = 0;
    private boolean oneMoreFrame = false;

    public AnimationGrid2048(int x, int y) {
        field = new ArrayList[x][y];

        for (int xx = 0; xx < x; xx++) {
            for (int yy = 0; yy < y; yy++) {
                field[xx][yy] = new ArrayList<>();
            }
        }
    }

    public void startAnimation(int x, int y, int animationType, long length, long delay, int[] extras) {
        AnimationCell2048 animationToAdd = new AnimationCell2048(x, y, animationType, length, delay, extras);
        if (x == -1 && y == -1) {
            globalAnimation.add(animationToAdd);
        } else {
            field[x][y].add(animationToAdd);
        }
        activeAnimations = activeAnimations + 1;
    }

    public void tickAll(long timeElapsed) {
        ArrayList<AnimationCell2048> cancelledAnimations = new ArrayList<>();
        for (AnimationCell2048 animation : globalAnimation) {
            animation.tick(timeElapsed);
            if (animation.animationDone()) {
                cancelledAnimations.add(animation);
                activeAnimations = activeAnimations - 1;
            }
        }

        for (ArrayList<AnimationCell2048>[] array : field) {
            for (ArrayList<AnimationCell2048> list : array) {
                for (AnimationCell2048 animation : list) {
                    animation.tick(timeElapsed);
                    if (animation.animationDone()) {
                        cancelledAnimations.add(animation);
                        activeAnimations = activeAnimations - 1;
                    }
                }
            }
        }

        for (AnimationCell2048 animation : cancelledAnimations) {
            cancelAnimation(animation);
        }
    }

    public boolean isAnimationActive() {
        if (activeAnimations != 0) {
            oneMoreFrame = true;
            return true;
        } else if (oneMoreFrame) {
            oneMoreFrame = false;
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<AnimationCell2048> getAnimationCell(int x, int y) {
        return field[x][y];
    }

    public void cancelAnimations() {
        for (ArrayList<AnimationCell2048>[] array : field) {
            for (ArrayList<AnimationCell2048> list : array) {
                list.clear();
            }
        }
        globalAnimation.clear();
        activeAnimations = 0;
    }

    private void cancelAnimation(AnimationCell2048 animation) {
        if (animation.getX() == -1 && animation.getY() == -1) {
            globalAnimation.remove(animation);
        } else {
            field[animation.getX()][animation.getY()].remove(animation);
        }
    }

}
