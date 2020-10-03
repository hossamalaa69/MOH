package com.sufnatech.meetingofhearts.players;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Fade;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sufnatech.meetingofhearts.R;

public class ImageViewerActivity extends AppCompatActivity {

    ImageView large_usr_img;
    Uri uri = null;
    String url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_image);

        large_usr_img = findViewById(R.id.large_usr_img);

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        receiveIntent();

        if(url != null) {
            postponeEnterTransition();
            Glide.with(this)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            startPostponedEnterTransition();
                            return false;
                        }
                    })
                    .into(large_usr_img);
        }else if(uri != null){
            large_usr_img.setImageURI(uri);
        }
    }

    private void receiveIntent() {
        if(getIntent().getStringExtra("uri") != null)
            uri = Uri.parse(getIntent().getStringExtra("uri"));
        else if(getIntent().getStringExtra("url") != null)
            url = getIntent().getStringExtra("url");
    }

}