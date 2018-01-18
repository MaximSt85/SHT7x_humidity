package com.example.max.sht7x_humidity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    private TextView underAboveTextView;

    private SharedPreferences notificationPreferences;
    private SharedPreferences.Editor editor;
    public static String PREFERENCES_NOTIFICATIONS = "preferencesNotifications";
    public static String ON_OFF_NOTIFICATIONS = "onOffNotifications";
    public static String MUTE_NOTIFICATIONS = "muteNotifications";
    public static String THRESHOLD_NOTIFICATIONS = "thresholdNotifications";
    public static String UNDER_ABOVE_NOTIFICATIONS = "underAboveNotifications";

    private FirebaseUser currentUserAuth;
    private DatabaseReference database;

    String thresholdFromPreference;
    boolean underAboveFromPreference;

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
        underAboveTextView = (TextView) findViewById(R.id.under_above_text_view);

        boolean onOffNotificationmuteFromPreference = notificationPreferences.getBoolean(ON_OFF_NOTIFICATIONS, false);
        notificationOnOff.setChecked(onOffNotificationmuteFromPreference);
        hideShow(onOffNotificationmuteFromPreference);
        notificationOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                notificationOnOff.setChecked(isChecked);
                hideShow(isChecked);
            }
        });

        boolean muteNotificationFromPreference = notificationPreferences.getBoolean(MUTE_NOTIFICATIONS, true);
        notificationMute.setChecked(muteNotificationFromPreference);
        muteNotifications(muteNotificationFromPreference);
        notificationMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                notificationMute.setChecked(isChecked);
                muteNotifications(isChecked);
            }
        });

        thresholdFromPreference = notificationPreferences.getString(THRESHOLD_NOTIFICATIONS, "50");
        threshold.setText(thresholdFromPreference);
        database.child("users").child(currentUserAuth.getUid()).child("threshold").setValue(thresholdFromPreference);
        threshold.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                thresholdFromPreference = editable.toString();
                database.child("users").child(currentUserAuth.getUid()).child("threshold").setValue(thresholdFromPreference);
            }
        });

        underAboveFromPreference = notificationPreferences.getBoolean(UNDER_ABOVE_NOTIFICATIONS, true);
        if (underAboveFromPreference) {
            underAbove.setSelection(0);
            database.child("users").child(currentUserAuth.getUid()).child("underAbove").setValue("Under");
        }
        else {
            underAbove.setSelection(1);
            database.child("users").child(currentUserAuth.getUid()).child("underAbove").setValue("Above");
        }
        underAbove.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    database.child("users").child(currentUserAuth.getUid()).child("underAbove").setValue("Under");
                    underAboveFromPreference = true;
                }
                else {
                    database.child("users").child(currentUserAuth.getUid()).child("underAbove").setValue("Above");
                    underAboveFromPreference = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void hideShow (boolean hideShow) {
        if (!hideShow) {
            notificationMute.setVisibility(View.GONE);
            threshold.setVisibility(View.GONE);
            underAbove.setVisibility(View.GONE);
            threshold.setVisibility(View.GONE);
            thresholdTextView.setVisibility(View.GONE);
            underAboveTextView.setVisibility(View.GONE);

            database.child("users").child(currentUserAuth.getUid()).child("token").removeValue();
        }
        else {
            notificationMute.setVisibility(View.VISIBLE);
            threshold.setVisibility(View.VISIBLE);
            underAbove.setVisibility(View.VISIBLE);
            threshold.setVisibility(View.VISIBLE);
            thresholdTextView.setVisibility(View.VISIBLE);
            underAboveTextView.setVisibility(View.VISIBLE);
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            database.child("users").child(currentUserAuth.getUid()).child("token").setValue(refreshedToken);
        }
    }

    private void muteNotifications (boolean mute) {
        if (!mute) {
            database.child("users").child(currentUserAuth.getUid()).child("mute").setValue(false);
        }
        else {
            database.child("users").child(currentUserAuth.getUid()).child("mute").setValue(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putBoolean(ON_OFF_NOTIFICATIONS, notificationOnOff.isChecked());
        editor.putBoolean(MUTE_NOTIFICATIONS, notificationMute.isChecked());
        editor.putString(THRESHOLD_NOTIFICATIONS, thresholdFromPreference);
        editor.putBoolean(UNDER_ABOVE_NOTIFICATIONS, underAboveFromPreference);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        if (thresholdFromPreference.equals("")) {
            MainActivity.makeToast(getResources().getString(R.string.set_threshold), this);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (thresholdFromPreference.equals("")) {
                MainActivity.makeToast(getResources().getString(R.string.set_threshold), this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
