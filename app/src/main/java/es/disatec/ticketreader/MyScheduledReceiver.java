package es.disatec.ticketreader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.net.HttpURLConnection;


/**
 * Created by Pepe on 24/06/2016.
 */
public class MyScheduledReceiver  extends BroadcastReceiver {




    public void onReceive(Context context, Intent intent) {

        context.startService(new Intent(context, ProcessMessageIntentService.class));

        //Intent scheduledIntent = new Intent(context, MainActivity.class);
        //scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(scheduledIntent);
    }



}