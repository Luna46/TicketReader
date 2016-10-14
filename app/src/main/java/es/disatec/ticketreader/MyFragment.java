package es.disatec.ticketreader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.net.HttpURLConnection;

/**
 * Created by MiguelLuna on 30/09/2016.
 */
public class MyFragment extends Fragment {

    /**
     * posición en la que nos encontramos en nuestro ArrayList<Ticket> colTickets.
     */
    int mCurrentPage;
    /**
     * Variable característica para encontrar el ticket en colTickets (caso de que lo busquemos en el servidor).
     */
    int idTicket;
    /**
     * Vista con la que cargaremos el Ticket en nuestro content_main.xml.
     */
    View vContent;
    /**
     * Obtenemos la información necesaria para trabajar y la guardamos en las variables creadas.
     * @param savedInstanceState
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /** Getting the arguments to the Bundle object */
        Bundle data = getArguments();
        /** Getting integer data of the key current_page from the bundle */
        mCurrentPage = data.getInt("current_page", 0);
        /** Getting integer data of the variable idTicket from the bundle */
        idTicket = data.getInt("idTicket",0);
    }

    /**
     * Decidimos el layout en el que vamos a cargar la vista.
     * Dependiendo de donde hayamos obtenido el ticket (servidor o alarma de google/NFC) actuaremos de una manera o de otra.
     * En el caso de que queramos volver a ver un ticket cargado anteriormente en el servidor lo cargaremos directamente,             evitando procesos innecesarios.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return v
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = null;

            v = inflater.inflate(R.layout.content_main, container, false);
            vContent = v;

            //Inicializamos a TicketActual al Ticket que este en la posicion que marque la variable mCurrentPage.
            Ticket ticketActual = TicketConstants.colTickets.get(mCurrentPage);

            //si no hay nada cargado aun llamamos  a "GetTickedId" para que nos cargue el ticket deseado.
            if (ticketActual.getTicket () == null || ticketActual.getTicket () == "")
            {
                new GetTicketId().execute(String.valueOf(ticketActual.getIdticket()), String.valueOf(mCurrentPage) );
            }

            //si esta cargado, lo mostramos (evitamos trabajo innecesario).
            else {
                WebView webview = (WebView) vContent.findViewById(R.id.webview);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.loadDataWithBaseURL("file:///android_asset/", ticketActual.getTicket(), "text/html", "UTF-8", "");
            }

        return v;
    }
   public class GetTicketId extends AsyncTask<String, String, Ticket> {

       /**
        * Actuaremos en Background para saber que Ticket cargamos y actualizaremos colTickets (el usuario no se enterará de este
        proceso).
        * Pintaremos en nuestro Layout el ticket de turno (Después de haberse realizado el Background).
        * @param args
        * @return tLoaded
        */
       @Override
       protected Ticket doInBackground(String... args) {

           //cargamos el Ticket con el ID que nos venga por parametro (caso alarma de google/NFC vendrá con valor -1).
           Ticket tLoaded = TicketServerWS.getTicket(Integer.parseInt(args[0]));
           //importante saber en que mCurrentPage estamos para poder actualizar de forma correcta nuestro ArrayList<Ticket>.                colTickets
           int position = Integer.parseInt(args[1]);
           //actualizamos el array.
           TicketConstants.colTickets.set(position, tLoaded);
           return tLoaded;

       }

       @Override
       protected void onPostExecute(Ticket result) {

           WebView webview = (WebView) vContent.findViewById(R.id.webview);
           webview.getSettings().setJavaScriptEnabled(true);
           webview.loadDataWithBaseURL("file:///android_asset/", result.getTicket(), "text/html", "UTF-8", "");

       }

   }

}
