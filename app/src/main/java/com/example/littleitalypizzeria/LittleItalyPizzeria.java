package com.example.littleitalypizzeria;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class LittleItalyPizzeria extends Application {
//This class is used to setup offline capabilities of the Picasso library

    @Override
    public void onCreate() {
        super.onCreate();

        //Setting offline capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();

        //Show if picture is offline or not (hidden now)
        built.setIndicatorsEnabled(false);
        Picasso.setSingletonInstance(built);
    }
}
