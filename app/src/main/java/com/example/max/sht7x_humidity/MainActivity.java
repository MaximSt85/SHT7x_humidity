package com.example.max.sht7x_humidity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.iid.FirebaseInstanceId;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "debugger";

    TextView textView_humidity1;
    TextView textView_temperature1;
    TextView textView_humidity2;
    TextView textView_temperature2;
    TextView textView_humidity3;
    TextView textView_temperature3;
    TextView textView_humidity4;
    TextView textView_temperature4;
    TextView textView_humidity5;
    TextView textView_temperature5;
    private DatabaseReference database;

    TextView out_of_range1;
    TextView out_of_range2;
    TextView out_of_range3;
    TextView out_of_range4;
    TextView out_of_range5;

    ValueEventListener myValueEventListenerHumidity;
    ValueEventListener myValueEventListenerTemperature;
    ArrayList<String> references = new ArrayList<>();

    private FirebaseUser currentUserAuth;
    private static final int SIGN_IN_REQUEST_CODE = 1;

    private SharedPreferences notificationPreferences;
    boolean onOffNotificationmuteFromPreference;
    double thresholdFromPreference;
    boolean underAboveFromPreference;

    boolean running = true;
    boolean state = true;

    MyThread myThread;;
    boolean humidity1IsOutOfRange = false;
    boolean humidity2IsOutOfRange = false;
    boolean humidity3IsOutOfRange = false;
    boolean humidity4IsOutOfRange = false;
    boolean humidity5IsOutOfRange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_humidity1 = (TextView) this.findViewById(R.id.humidity1);
        textView_temperature1 = (TextView) this.findViewById(R.id.temperature1);
        textView_humidity2 = (TextView) this.findViewById(R.id.humidity2);
        textView_temperature2 = (TextView) this.findViewById(R.id.temperature2);
        textView_humidity3 = (TextView) this.findViewById(R.id.humidity3);
        textView_temperature3 = (TextView) this.findViewById(R.id.temperature3);
        textView_humidity4 = (TextView) this.findViewById(R.id.humidity4);
        textView_temperature4 = (TextView) this.findViewById(R.id.temperature4);
        textView_humidity5 = (TextView) this.findViewById(R.id.humidity5);
        textView_temperature5 = (TextView) this.findViewById(R.id.temperature5);

        out_of_range1 = (TextView) this.findViewById(R.id.out_of_range1);
        out_of_range2 = (TextView) this.findViewById(R.id.out_of_range2);
        out_of_range3 = (TextView) this.findViewById(R.id.out_of_range3);
        out_of_range4 = (TextView) this.findViewById(R.id.out_of_range4);
        out_of_range5 = (TextView) this.findViewById(R.id.out_of_range5);

        references.add("humidity1");
        references.add("humidity2");
        references.add("humidity3");
        references.add("humidity4");
        references.add("humidity5");
        //references.add("time");
        references.add("temperature1");
        references.add("temperature2");
        references.add("temperature3");
        references.add("temperature4");
        references.add("temperature5");

        database = FirebaseDatabase.getInstance().getReference();

        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();

        notificationPreferences = getSharedPreferences(NotificationActivity.PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                makeToast(getResources().getString(R.string.signed_in), this);
                currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
                database.child("users").child(currentUserAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            setOnValueListener();
                        }
                        else {
                            writeNewUser(currentUserAuth.getDisplayName(), currentUserAuth.getUid());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else {
                makeToast(getResources().getString(R.string.could_not_sign_you_in), this);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }

    }

    private void writeNewUser(final String name, final String id) {
        database.child("users").child(currentUserAuth.getUid()).child("id").setValue(currentUserAuth.getUid());
        //String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //database.child("users").child(currentUserAuth.getUid()).child("token").setValue(refreshedToken);
        setOnValueListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_menu_plot){
            Intent myIntent = new Intent(MainActivity.this, Sensor2Activity.class);
            MainActivity.this.startActivity(myIntent);
        }
        if (item.getItemId() == R.id.main_menu_notification){
            Intent myIntent = new Intent(MainActivity.this, NotificationActivity.class);
            MainActivity.this.startActivity(myIntent);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myValueEventListenerHumidity != null) {
            database.removeEventListener(myValueEventListenerHumidity);
        }
        if (myValueEventListenerTemperature != null) {
            database.removeEventListener(myValueEventListenerTemperature);
        }
        running = false;
        out_of_range1.setVisibility(View.INVISIBLE);
        myThread.interrupt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentUserAuth == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        }
        else {
            setOnValueListener();
        }
        onOffNotificationmuteFromPreference = notificationPreferences.getBoolean(NotificationActivity.ON_OFF_NOTIFICATIONS, false);
        thresholdFromPreference = Double.valueOf(notificationPreferences.getString(NotificationActivity.THRESHOLD_NOTIFICATIONS, "50"));
        underAboveFromPreference = notificationPreferences.getBoolean(NotificationActivity.UNDER_ABOVE_NOTIFICATIONS, true);
        running = true;
        myThread = new MyThread();
        myThread.start();
        /*try {
            if (!myThread.isAlive()) {myThread.start();}
        }
        catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }*/

    }

    private void setOnValueListener() {
        database.child("sht75").child("data").child("lastHumidity").addValueEventListener(myValueEventListenerHumidity = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String humidity1 = dataSnapshot.child("humidity1").getValue().toString();
                String humidity2 = dataSnapshot.child("humidity2").getValue().toString();
                String humidity3 = dataSnapshot.child("humidity3").getValue().toString();
                String humidity4 = dataSnapshot.child("humidity4").getValue().toString();
                String humidity5 = dataSnapshot.child("humidity5").getValue().toString();
                textView_humidity1.setText(humidity1 + " %");
                textView_humidity2.setText(humidity2 + " %");
                textView_humidity3.setText(humidity3 + " %");
                textView_humidity4.setText(humidity4 + " %");
                textView_humidity5.setText(humidity5 + " %");

                double humidity1Int = Double.valueOf(humidity1);
                double humidity2Int = Double.valueOf(humidity2);
                double humidity3Int = Double.valueOf(humidity3);
                double humidity4Int = Double.valueOf(humidity4);
                double humidity5Int = Double.valueOf(humidity5);

                if (onOffNotificationmuteFromPreference) {
                    if (underAboveFromPreference) {
                        if (humidity1Int < thresholdFromPreference) {
                            humidity1IsOutOfRange = true;
                        }
                        else {
                            humidity1IsOutOfRange = false;
                        }
                        if (humidity2Int < thresholdFromPreference) {
                            humidity2IsOutOfRange = true;
                        }
                        else {
                            humidity2IsOutOfRange = false;
                        }
                        if (humidity3Int < thresholdFromPreference) {
                            humidity3IsOutOfRange = true;
                        }
                        else {
                            humidity3IsOutOfRange = false;
                        }
                        if (humidity4Int < thresholdFromPreference) {
                            humidity4IsOutOfRange = true;
                        }
                        else {
                            humidity4IsOutOfRange = false;
                        }
                        if (humidity5Int < thresholdFromPreference) {
                            humidity5IsOutOfRange = true;
                        }
                        else {
                            humidity5IsOutOfRange = false;
                        }
                    }
                    else {
                        if (humidity1Int > thresholdFromPreference) {
                            humidity1IsOutOfRange = true;
                        }
                        else {
                            humidity1IsOutOfRange = false;
                        }
                        if (humidity2Int > thresholdFromPreference) {
                            humidity2IsOutOfRange = true;
                        }
                        else {
                            humidity2IsOutOfRange = false;
                        }
                        if (humidity3Int > thresholdFromPreference) {
                            humidity3IsOutOfRange = true;
                        }
                        else {
                            humidity3IsOutOfRange = false;
                        }
                        if (humidity4Int > thresholdFromPreference) {
                            humidity4IsOutOfRange = true;
                        }
                        else {
                            humidity4IsOutOfRange = false;
                        }
                        if (humidity5Int > thresholdFromPreference) {
                            humidity5IsOutOfRange = true;
                        }
                        else {
                            humidity5IsOutOfRange = false;
                        }
                    }
                }
                else {
                    humidity1IsOutOfRange = false;
                    humidity2IsOutOfRange = false;
                    humidity3IsOutOfRange = false;
                    humidity4IsOutOfRange = false;
                    humidity5IsOutOfRange = false;
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.child("sht75").child("data").child("lastTemperature").addValueEventListener(myValueEventListenerTemperature = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String temperature1 = dataSnapshot.child("temperature1").getValue().toString();
                String temperature2 = dataSnapshot.child("temperature2").getValue().toString();
                String temperature3 = dataSnapshot.child("temperature3").getValue().toString();
                String temperature4 = dataSnapshot.child("temperature4").getValue().toString();
                String temperature5 = dataSnapshot.child("temperature5").getValue().toString();
                textView_temperature1.setText(temperature1 + " %");
                textView_temperature2.setText(temperature2 + " %");
                textView_temperature3.setText(temperature3 + " %");
                textView_temperature4.setText(temperature4 + " %");
                textView_temperature5.setText(temperature5 + " %");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while(running) {
                    //Log.d(TAG, "in running");
                    state = !state;
                    if (humidity1IsOutOfRange) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state) {
                                    out_of_range1.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    out_of_range1.setVisibility(View.VISIBLE);
                                }

                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                out_of_range1.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    if (humidity2IsOutOfRange) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state) {
                                    out_of_range2.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    out_of_range2.setVisibility(View.VISIBLE);
                                }

                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                out_of_range2.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    if (humidity3IsOutOfRange) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state) {
                                    out_of_range3.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    out_of_range3.setVisibility(View.VISIBLE);
                                }

                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                out_of_range3.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    if (humidity4IsOutOfRange) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state) {
                                    out_of_range4.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    out_of_range4.setVisibility(View.VISIBLE);
                                }

                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                out_of_range4.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    if (humidity5IsOutOfRange) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state) {
                                    out_of_range5.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    out_of_range5.setVisibility(View.VISIBLE);
                                }

                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                out_of_range5.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    sleep(1000);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void makeToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}












/*database.child("sht75").child("humidity").addValueEventListener(myValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Double> humidities = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    DataSnapshot humidityDataSnapshot = dataSnapshot.child(references.get(i));
                    GenericTypeIndicator<List<Double>> humidityGeneric = new GenericTypeIndicator<List<Double>>(){};
                    ArrayList<Double> humidity = (ArrayList<Double>) humidityDataSnapshot.getValue(humidityGeneric);
                    humidities.add(humidity.get(humidity.size() - 1));
                }

                ArrayList<String> temperatures = new ArrayList<>();
                for (int i = 5; i < references.size(); i++) {
                    DataSnapshot temperatureDataSnapshot = dataSnapshot.child(references.get(i));
                    temperatures.add(String.valueOf(dataSnapshot.child(references.get(i)).getValue()));
                }

                textView_humidity1.setText(String.format("%.2f", humidities.get(0)) + " %");
                textView_temperature1.setText(temperatures.get(0) + " \u2103");
                textView_humidity2.setText(String.format("%.2f", humidities.get(1)) + " %");
                textView_temperature2.setText(temperatures.get(1) + " \u2103");
                textView_humidity3.setText(String.format("%.2f", humidities.get(2)) + " %");
                textView_temperature3.setText(temperatures.get(2) + " \u2103");
                textView_humidity4.setText(String.format("%.2f", humidities.get(3)) + " %");
                textView_temperature4.setText(temperatures.get(3) + " \u2103");
                textView_humidity5.setText(String.format("%.2f", humidities.get(4)) + " %");
                textView_temperature5.setText(temperatures.get(4) + " \u2103");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/




