package com.oleksandr.weshare.players;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.VideoView;

import com.oleksandr.weshare.R;

public class VideoPlayerActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    VideoView videoview;
    String vid_url ="https://firebasestorage.googleapis.com/v0/b/meeting-of-hearts.appspot.com/o/Steve_Carell%3A_NO_GOD!_NO_GOD%2C_PLEASE_NO%2C_NO%2C_NO%2C_NOOOOO_(HD)(720p).mp4?alt=media&token=c498f583-0b43-45cf-b51c-7a5582c41f50";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Intent intent = getIntent();
        if(intent.getStringExtra("video_url") != null)
            vid_url = intent.getStringExtra("video_url");

        videoview = (VideoView) findViewById(R.id.videoView);
        videoview.setMediaController(new MediaController(this){
            public boolean dispatchKeyEvent(KeyEvent event)
            {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                    ((Activity) getContext()).finish();
                return super.dispatchKeyEvent(event);
            }
        });
        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Video Stream");
        pDialog.setMessage("Loading...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
        try {
            MediaController mediacontroller = new MediaController(this);
            mediacontroller.setAnchorView(videoview);
            Uri video = Uri.parse(vid_url);
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        videoview.requestFocus();
        videoview.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            pDialog.dismiss();
            videoview.start();
        });
        videoview.setOnCompletionListener(mp -> finish());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}