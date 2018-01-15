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
import android.view.View;
import android.widget.TextView;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    DatabaseReference database;

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

        database = FirebaseDatabase.getInstance().getReference();

        database.child("humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot.getChildren();
                List<Sensor> sensors = new ArrayList<>();
                for (DataSnapshot sensorDataSnapshot : dataSnapshot.getChildren()) {
                    String humidity = String.valueOf(sensorDataSnapshot.child("humidity").getValue());
                    String temperature = String.valueOf(sensorDataSnapshot.child("temperature").getValue());
                    //Sensor sensor = sensorDataSnapshot.getValue(Sensor.class);
                    Sensor sensor = new Sensor(humidity, temperature);
                    sensors.add(sensor);
                }
                textView_humidity1.setText(sensors.get(0).getHumidity());
                textView_temperature1.setText(sensors.get(0).getTemperature());
                textView_humidity2.setText(sensors.get(1).getHumidity());
                textView_temperature2.setText(sensors.get(1).getTemperature());
                textView_humidity3.setText(sensors.get(2).getHumidity());
                textView_temperature3.setText(sensors.get(2).getTemperature());
                textView_humidity4.setText(sensors.get(3).getHumidity());
                textView_temperature4.setText(sensors.get(3).getTemperature());
                textView_humidity5.setText(sensors.get(4).getHumidity());
                //textView_humidity5.setText(sensors.get(sensors.size() - 1).getHumidity());
                textView_temperature5.setText(sensors.get(4).getTemperature());
                //Log.v(TAG, "1111111111111111111111111111111111111111" + dataSnapshot.getChildren());
                //Log.v(TAG, "1111111111111111111111111111111111111111" + dataSnapshot.getValue());
                //Log.v(TAG, "1111111111111111111111111111111111111111" + sensors.get(0).getHumidity());
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
}
