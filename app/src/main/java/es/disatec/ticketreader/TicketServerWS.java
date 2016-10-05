package es.disatec.ticketreader;

import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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


    /**
     * Get complete ticket by ID
     * @param idTicket
     * @return
     */
    public static Ticket getTicket(int idTicket) {
        Ticket t = null;
        URL url = null;
        try {
            url = new URL("http://192.168.1.38:8080/TicketWeb/webresources/ticketPersistence/getTicketId/" + idTicket);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            urlConnection.addRequestProperty("Content-Type", "application/json");

            int responseCode = urlConnection.getResponseCode();
            String responseMessage = urlConnection.getResponseMessage();

            InputStream is = null;
            if (responseCode >= 400) {
                is = urlConnection.getErrorStream();
            } else {
                is = urlConnection.getInputStream();
            }


            //InputStream in = url.openStream();
            String valor = streamToString(is);


            JSONObject js = new JSONObject(valor);

            t = new Ticket(js);


        } catch (IOException e) {
            e.printStackTrace();
        }
          catch (JSONException e) {
              e.printStackTrace();
          }
          finally {
            urlConnection.disconnect();
        }

        return t;
    }

    /**
     * Devuelve los tickets de un usuario, TODOS!!!
     * @param UID
     * @param bIncludeAllTicket Si es TRUE, devuelve la información del ticket, sino solo devuelve los IDs
     * @return
     */
    public static ArrayList<Ticket> getTicketsByUID(String UID, Boolean bIncludeAllTicket) {

        ArrayList<Ticket> tickets = new ArrayList<Ticket>();


        String valor = "";
        URL url = null;
        try {
            url = new URL("http://192.168.1.38:8080/TicketWeb/webresources/ticketPersistence/getResources/"+ UID + "/" + bIncludeAllTicket);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            urlConnection.addRequestProperty("Content-Type", "application/json");
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

            JSONArray js = new JSONArray(valor);

            for(int i=0; i<js.length(); i++){
                JSONObject ticketJSON = js.getJSONObject(i);
                Ticket t = new Ticket();
                t.setIdticket(ticketJSON.getInt("id"));
                //t.setTicket(ticketJSON.getString("ticket"));
                tickets.add(t);
            }

            // Procesar el array de JSON

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }

        return tickets;
    }


    /**
     * Función de prueba para probar el servidor y la conexión
     * @return
     */
    public boolean testGet() {

        String valor = "";
        URL url = null;
        try {
            //url = new URL("https://api.github.com/users/dmnugent80/repos");
            url = new URL("http://192.168.1.38:8080/TicketWeb/webresources/path");

            //url = new URL("http://www.oracle.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            //urlConnection.setDoInput(true);
            //urlConnection.setDoOutput(true);
            //urlConnection.setRequestProperty("Content-Type","application/json");
            //urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.setReadTimeout(15*1000);
            urlConnection.connect();

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

