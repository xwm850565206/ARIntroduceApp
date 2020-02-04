package com.npucreator.arintroduceapp.audio;


import android.content.res.AssetManager;

/**
 * 这个类用来播放介绍音
 */
public interface IAudioHelper {

    public void play();

    public void play(AssetManager assetManager, String filename);

    public void stop();

    public void pause();

    public void release();

}
