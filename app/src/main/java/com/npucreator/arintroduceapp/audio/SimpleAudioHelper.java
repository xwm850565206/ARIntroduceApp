package com.npucreator.arintroduceapp.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.MediaPlayer;

import com.npucreator.arintroduceapp.R;

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

        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd("sound/zoe_sing.wav");
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getStartOffset());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void play(AssetManager assetManager, String filename) {

    }


    @Override
    public void play() {
        if(!mediaPlayer.isPlaying()) {
            //mediaPlayer.reset();
            mediaPlayer.start();
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
}
