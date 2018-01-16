package com.example.max.sht7x_humidity;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by stassevm on 1/15/18.
 */

public class MApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users");
        database.keepSynced(true);
    }
}
