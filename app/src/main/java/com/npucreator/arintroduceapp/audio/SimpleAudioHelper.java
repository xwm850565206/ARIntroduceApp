package com.npucreator.arintroduceapp.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.util.Log;

import com.npucreator.arintroduceapp.R;
import com.npucreator.arintroduceapp.util.Reference;

import java.io.File;
import java.io.IOException;


/**
 *  简单的音频控制类
 */
public class SimpleAudioHelper implements IAudioHelper {

    MediaPlayer mediaPlayer;
    AssetManager assetManager;

    public SimpleAudioHelper(Context context){
        assetManager = context.getResources().getAssets();
        mediaPlayer = new MediaPlayer();
    }


    @Override
    public void play() {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    public void play(Reference.DetectType type) {

        if (mediaPlayer.isPlaying())
            return;

        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(getFilenameFromType(type));
            mediaPlayer.reset(); // 这里可以优化 判断是否和上次的一样，一样就不用reset了
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getStartOffset());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void stop() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }

    @Override
    public void pause(){
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    @Override
    public void release()
    {
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public String getFilenameFromType(Reference.DetectType type) {

        String name = type.toString();
        name = name.substring(1);

        Log.d("audiohelper", name);

        return "sound" + File.separator + name + ".wav";
    }
}
