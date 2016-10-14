package es.disatec.ticketreader;

/**
 * Created by Pepe on 27/06/2016.
 */


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

public class FirebaseMessagingServiceImp extends FirebaseMessagingService {




    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        Map<String, String> m = remoteMessage.getData();
        //damos los valores declarados por la clase Ticket al nuevo ticket que está siendo enviado
        Integer id = -1;
        String comercio = m.get("comercio");
        String grupo = m.get("grupo");
        String lTicket = m.get("ticket");
        Date fecha = null;
        Ticket t = new Ticket();
        t.setComercio(comercio);
        t.setGrupo(grupo);

        //decodicificamos el mensaje que nos viene en "bruto" por la impresora a un lenguaje HTML
        VirtualPrinter vp = new VirtualPrinter();
        vp.Initialize();
        byte[] message = Base64.decode(lTicket, Base64.DEFAULT);
        String resultText = vp.processText(message);
        t.setTicket(resultText);
        t.setFecha(fecha);
        t.setIdticket(id);

        //damos el valor del ticket enviado al famoso lastTicket
        TicketConstants.lastTicket = t;
        //añadimos a nuestro arrayList el nuevo Ticket en camino
        TicketConstants.colTickets.add(t);

        NotificationMessage.showNotification(this, grupo, comercio);



    }
    // [END receive_message]

}
