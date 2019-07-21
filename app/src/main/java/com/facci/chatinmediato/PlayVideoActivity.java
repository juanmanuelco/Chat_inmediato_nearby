package com.facci.chatinmediato;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideoActivity extends Activity {

    private VideoView videoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        Intent intent = getIntent();
        String filePath = (String) intent.getStringExtra("filePath");
        System.out.println("PlayVideoActivity   " + filePath);

        videoPlayer = (VideoView) findViewById(R.id.videoPlayer);
        videoPlayer.setVideoPath(filePath);
        videoPlayer.setMediaController(new MediaController(this));

        videoPlayer.start();
    }
}