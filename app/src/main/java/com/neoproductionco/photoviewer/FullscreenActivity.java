package com.neoproductionco.photoviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Neo on 01.09.2016.
 */
public class FullscreenActivity extends Activity {
    ArrayList<ImageItem> photos;
    Button btnPrevPhoto;
    Button btnNextPhoto;
    ImageView ivImage;
    TextView tvPhoto;
    TextView tvName;
    TextView tvAuthor;
    TextView tvCamera;

    int current_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        ivImage = (ImageView) findViewById(R.id.ivImage);
        tvPhoto = (TextView) findViewById(R.id.tvPhoto);
        tvName = (TextView) findViewById(R.id.tvName);
        tvAuthor = (TextView) findViewById(R.id.tvAuthor);
        tvCamera = (TextView) findViewById(R.id.tvCamera);
        btnPrevPhoto = (Button) findViewById(R.id.btnPrev);
        btnNextPhoto = (Button) findViewById(R.id.btnNext);
        btnPrevPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_photo <= 0)
                    return;
                current_photo--;
                refresh();
            }
        });
        btnNextPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_photo >= photos.size()-1)
                    return;
                current_photo++;
                refresh();
            }
        });

        Intent intent = getIntent();
        photos = (ArrayList<ImageItem>) intent.getSerializableExtra("photos");
        current_photo = intent.getIntExtra("selected", 0);
        refresh();
    }

    private void refresh(){
        tvName.setText(photos.get(current_photo).getTitle());
        tvAuthor.setText("Author:"+photos.get(current_photo).getAuthor());
        tvCamera.setText("Camera"+photos.get(current_photo).getCamera());
        ivImage.setImageBitmap(photos.get(current_photo).getImage());
        tvPhoto.setText("Photo "+current_photo);
    }
}
