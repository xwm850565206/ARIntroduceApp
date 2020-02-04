package com.npucreator.arintroduceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.npucreator.arintroduceapp.R;
import com.npucreator.arintroduceapp.activity.DetectActivity;
import com.npucreator.unity.UnityPlayerActivity;


/**
 * 应用程序入口
 */
public class MainActivity extends UnityPlayerActivity {

    private Button start_btn;

    protected void startAR(){
        Intent intent = new Intent(this, DetectActivity.class);
        //Intent intent = new Intent(this, UnityPlayerActivity.class);
        startActivity(intent);
        //mUnityPlayer.UnitySendMessage("Zoe_dance", "playDance", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(mUnityPlayer);
        setContentView(R.layout.activity_main);
        //addContentView(mUnityPlayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        //        ViewGroup.LayoutParams.MATCH_PARENT));
        start_btn = (Button) findViewById(R.id.ar_start);
        start_btn.bringToFront();
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAR();
            }
        });

        //mUnityPlayer.UnitySendMessage("Zoe_dance", "playDance", "");
    }
}
