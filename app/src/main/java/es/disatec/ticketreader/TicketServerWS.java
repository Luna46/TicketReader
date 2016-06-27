package es.disatec.ticketreader;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Pepe on 27/06/2016.
 */
public class TicketServerWS {
    // Util function
    public static String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }


    public static String getMessage(String UID) {
        String valor = "";
        URL url = null;
        try {
            url = new URL("http://192.168.1.38:8080/TicketWeb/webresources/path");
            //url = new URL("http://www.oracle.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            String responseMessage = urlConnection.getResponseMessage();

            InputStream is = null;
            if (responseCode >= 400) {
                is = urlConnection.getErrorStream();
            } else {
                is = urlConnection.getInputStream();
            }


            //InputStream in = url.openStream();
            valor = streamToString(is);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return valor;
    }


    public static boolean setnewTokenID(String UID, String token) {


        String valor = "";
        URL url = null;
        try {
            url = new URL("http://192.168.1.38:8080/TicketWeb/webresources/path");

            //url = new URL("http://www.oracle.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();
            String responseMessage = urlConnection.getResponseMessage();

            InputStream is = null;
            if (responseCode >= 400) {
                is = urlConnection.getErrorStream();
            } else {
                is = urlConnection.getInputStream();
            }


            //InputStream in = url.openStream();
            valor = streamToString(is);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return true;
    }

    public static boolean SendUIDtoServer (String UID, String token)
    {

        String strToSend = "";
        JSONObject obj = new JSONObject();
        try {
            obj.put("UID", UID);
            obj.put("tocken", token);
            strToSend = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }




        URL url = null;
        try {
            url = new URL("http://192.168.1.38:8080/TicketWeb/webresources/register");

            //url = new URL("http://www.oracle.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Obtener la conexión


        HttpURLConnection con = null;

        try {
            // Construir los datos a enviar
            String data = URLEncoder.encode(strToSend,"UTF-8");

            con = (HttpURLConnection)url.openConnection();

            // Activar método POST
            con.setDoOutput(true);

            // Tamaño previamente conocido
            con.setFixedLengthStreamingMode(data.getBytes().length);

            // Establecer application/x-www-form-urlencoded debido a la simplicidad de los datos
            con.setRequestProperty("Content-Type","application/json");

            OutputStream out = new BufferedOutputStream(con.getOutputStream());

            out.write(data.getBytes());
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(con!=null)
                con.disconnect();
        }

        return true;

    }

}
