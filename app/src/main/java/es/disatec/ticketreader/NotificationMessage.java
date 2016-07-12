package es.disatec.ticketreader;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Pepe on 12/07/2016.
 */
public class NotificationMessage {

    private static Boolean isForeground = false;


    /**
     * For foreground proposites
     */
    public static void SetResumed()
    {
        isForeground = false;
    }

    public static void SetPaused()
    {
        isForeground = true;
    }



    public static void showNotification(Context context, String grupo, String comercio) {
        // Si está visible, se muestra el mensaje directamente, sino se crea una notificación
        if (!isForeground) {
            Intent i = new Intent();
            i.setClass(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(i);
        }
        else {
            NotificationMessage.sendNotification(context, grupo, comercio);
        }
    }

    public static void sendNotification(Context context, String grupo, String comercio) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_action_name)
                .setContentTitle("Nuevo ticket de " + grupo)
                .setContentText(comercio)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }



}
