package com.example.max.sht7x_humidity;

/**
 * Created by Max on 16.08.2017.
 */

import android.app.Notification;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.support.v4.app.NotificationCompat;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.app.NotificationChannel;



public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //sendNotification(remoteMessage.getNotification().getBody());
        //String text = remoteMessage.getNotification().getBody();
        //String title = "Notification from Firebase";
        //notifyThis(title, text);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        //String id = "my_channel_01";
        //String name = "my_channel_01";
        //int importance = NotificationManager.IMPORTANCE_LOW;
        //NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        //notificationManager.createNotificationChannel(mChannel);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
