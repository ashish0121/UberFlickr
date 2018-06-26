package com.example.ashishrmehta.flickr;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by ashish mehta on 05-01-2018.
 */

public class MainViewActivity  extends ActionBarActivity {

    ImageView im;
    ImageView det;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickr_image_main);
        im = (ImageView) findViewById(R.id.img_main);
        det = (ImageView) findViewById(R.id.img_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String pic_id = getIntent().getStringExtra("PIC_ID");
        im.setImageBitmap(FlickrImageSearchAdapter.getBitMap(pic_id));
        det.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Image Details")
                        .setMessage(FlickrImageSearchAdapter.getImageDetails(pic_id))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton("Back", null).show();
            }
        });
    }
}
