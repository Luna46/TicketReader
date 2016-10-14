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

    int mCurrentPage;//llamar servidor para saber cuantos tickets tengo
    int idTicket;
    //int longitud = 1;
    View vContent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /** Getting the arguments to the Bundle object */
        Bundle data = getArguments();

        /** Getting integer data of the key current_page from the bundle */
        mCurrentPage = data.getInt("current_page", 0);//guardar en la variable el numero de tickets
        idTicket = data.getInt("idTicket",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = null;

            v = inflater.inflate(R.layout.content_main, container, false);
            vContent = v;

            //Inicializamos a TicketActual al Ticket que este en la posicion que marque la variable mCurrentPage
            Ticket ticketActual = TicketConstants.colTickets.get(mCurrentPage);
            //si no hay nada cargado aun llamamos  a "GetTickedId" para que nos cargue el ultimo ticket
            if (ticketActual.getTicket () == null || ticketActual.getTicket () == "")
            {
                new GetTicketId().execute(String.valueOf(ticketActual.getIdticket()), String.valueOf(mCurrentPage) );
            }
            //si esta cargado, lo mostramos (evitamos trabajo innecesario)
            else {
                WebView webview = (WebView) vContent.findViewById(R.id.webview);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.loadDataWithBaseURL("file:///android_asset/", ticketActual.getTicket(), "text/html", "UTF-8", "");
            }

        return v;
    }
   public class GetTicketId extends AsyncTask<String, String, Ticket> {

       HttpURLConnection urlConnection;

       /**
        *
        * @param args
        * @return
        */
       @Override
       protected Ticket doInBackground(String... args) {



           //cargamos el Ticket con el ID que nos venga por parametro
           Ticket tLoaded = TicketServerWS.getTicket(Integer.parseInt(args[0]));
           int position = Integer.parseInt(args[1]);
           //actualizamos la carga en el array con el nuevo ticket cargado
           TicketConstants.colTickets.set(position, tLoaded);
           return tLoaded;

       }

       @Override
       protected void onPostExecute(Ticket result) {

           //cargamos el contenido del ticket
           WebView webview = (WebView) vContent.findViewById(R.id.webview);
           webview.getSettings().setJavaScriptEnabled(true);
           webview.loadDataWithBaseURL("file:///android_asset/", result.getTicket(), "text/html", "UTF-8", "");


       }

   }

}
