package com.npucreator.arintroduceapp.audio;


import android.content.res.AssetManager;

import com.npucreator.arintroduceapp.util.Reference;

/**
 * 这个类用来播放介绍音
 */
public interface IAudioHelper {

    public void play();

    public void play(Reference.DetectType type);

    public void stop();

    public void pause();

    public void release();

    String getFilenameFromType(Reference.DetectType type);
}
