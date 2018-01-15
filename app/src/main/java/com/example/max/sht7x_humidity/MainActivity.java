package com.example.max.sht7x_humidity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    ValueEventListener myValueEventListener;
    ArrayList<String> references = new ArrayList<>();

    private FirebaseUser currentUserAuth;
    private static final int SIGN_IN_REQUEST_CODE = 1;


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
        if (myValueEventListener != null) {
            database.removeEventListener(myValueEventListener);
        }
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
    }

    private void setOnValueListener() {
        database.child("sht75").child("humidity").addValueEventListener(myValueEventListener = new ValueEventListener() {
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

                textView_humidity1.setText(String.valueOf(humidities.get(0)));
                textView_temperature1.setText(temperatures.get(0));
                textView_humidity2.setText(String.valueOf(humidities.get(1)));
                textView_temperature2.setText(temperatures.get(1));
                textView_humidity3.setText(String.valueOf(humidities.get(2)));
                textView_temperature3.setText(temperatures.get(2));
                textView_humidity4.setText(String.valueOf(humidities.get(3)));
                textView_temperature4.setText(temperatures.get(3));
                textView_humidity5.setText(String.valueOf(humidities.get(4)));
                textView_temperature5.setText(temperatures.get(4));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("MainActivity Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public void sensor1(View v) {
        Intent myIntent = new Intent(MainActivity.this, Sensor2Activity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public static void makeToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}








