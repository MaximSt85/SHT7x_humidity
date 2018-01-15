package com.example.max.sht7x_humidity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class NotificationActivity extends AppCompatActivity {

    private Switch notificationOnOff;
    private Switch notificationMute;
    private EditText threshold;
    private Spinner underAbove;
    private TextView thresholdTextView;

    private SharedPreferences notificationPreferences;
    private SharedPreferences.Editor editor;
    public static String PREFERENCES_NOTIFICATIONS = "preferencesNotifications";
    public static String ON_OFF_NOTIFICATIONS = "onOffNotifications";
    public static String MUTE_NOTIFICATIONS = "muteNotifications";
    public static String THRESHOLD_NOTIFICATIONS = "thresholdNotifications";
    public static String UNDER_ABOVE_NOTIFICATIONS = "underAboveNotifications";

    private FirebaseUser currentUserAuth;
    private DatabaseReference database;

    String thresholdToWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        database = FirebaseDatabase.getInstance().getReference();
        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();

        notificationPreferences = getSharedPreferences(PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE);
        editor = notificationPreferences.edit();

        notificationOnOff = (Switch) findViewById(R.id.on_off_notifications);
        notificationMute = (Switch) findViewById(R.id.mute_notifications);
        threshold = (EditText) findViewById(R.id.threshold);
        underAbove = (Spinner) findViewById(R.id.under_above);
        thresholdTextView = (TextView) findViewById(R.id.threshold_textview);

        boolean onOffNotification = notificationPreferences.getBoolean(ON_OFF_NOTIFICATIONS, false);
        notificationOnOff.setChecked(onOffNotification);
        notificationOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                notificationOnOff.setChecked(isChecked);
                if (!isChecked) {
                    notificationMute.setVisibility(View.GONE);
                    threshold.setVisibility(View.GONE);
                    underAbove.setVisibility(View.GONE);
                    threshold.setVisibility(View.GONE);
                    thresholdTextView.setVisibility(View.GONE);
                    database.child("users").child(currentUserAuth.getUid()).child("token").removeValue();
                }
                else {
                    notificationMute.setVisibility(View.VISIBLE);
                    threshold.setVisibility(View.VISIBLE);
                    underAbove.setVisibility(View.VISIBLE);
                    threshold.setVisibility(View.VISIBLE);
                    thresholdTextView.setVisibility(View.VISIBLE);
                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    database.child("users").child(currentUserAuth.getUid()).child("token").setValue(refreshedToken);
                }
            }
        });

        boolean muteNotification = notificationPreferences.getBoolean(MUTE_NOTIFICATIONS, true);
        notificationMute.setChecked(muteNotification);

        notificationMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                notificationMute.setChecked(isChecked);
                if (!isChecked) {
                    database.child("users").child(currentUserAuth.getUid()).child("mute").setValue(false);
                }
                else {
                    database.child("users").child(currentUserAuth.getUid()).child("mute").setValue(true);
                }
            }
        });

        boolean underAboveFromPreference = notificationPreferences.getBoolean(UNDER_ABOVE_NOTIFICATIONS, true);
        if (underAboveFromPreference) {
            underAbove.setSelection(0);
        }
        else {
            underAbove.setSelection(1);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putBoolean(ON_OFF_NOTIFICATIONS, notificationOnOff.isChecked());
        editor.putBoolean(MUTE_NOTIFICATIONS, notificationMute.isChecked());
        thresholdToWrite = threshold.getText().toString();
        editor.putString(THRESHOLD_NOTIFICATIONS, thresholdToWrite);
        database.child("users").child(currentUserAuth.getUid()).child("threshold").setValue(thresholdToWrite);
        boolean underAboveToWrite;
        if (String.valueOf(underAbove.getSelectedItem()).equals("Under")) {
            database.child("users").child(currentUserAuth.getUid()).child("underAbove").setValue("Under");
            underAboveToWrite = true;
        }
        else {
            database.child("users").child(currentUserAuth.getUid()).child("underAbove").setValue("Above");
            underAboveToWrite = false;
        }
        editor.putBoolean(UNDER_ABOVE_NOTIFICATIONS, underAboveToWrite);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        thresholdToWrite = threshold.getText().toString();
        if (thresholdToWrite.equals("")) {
            MainActivity.makeToast(getResources().getString(R.string.set_threshold), this);
        }
        else {
            super.onBackPressed();
        }
    }
}
