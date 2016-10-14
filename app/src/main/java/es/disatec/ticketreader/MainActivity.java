package es.disatec.ticketreader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

import static java.util.Arrays.copyOfRange;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_PDF = "application/pdf";
    public static final String MIME_GIF = "image/gif";

    public static final String TAG = "NfcDemo";

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    private NdefMessage mMessage;

    // Lista de los tickets del usuario, se carga cuando se arranca la aplicación en background




    public static NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    protected void StartSchedule() {
        Intent myIntent = new Intent(getBaseContext(),
                MyScheduledReceiver.class);

        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(getBaseContext(),
                0, myIntent, 0);

        AlarmManager alarmManager
                = (AlarmManager)getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        long interval = 10 * 1000; //
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), interval, pendingIntent);

        //finish();


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);



//        String tockenId = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG, "InstanceID token: " + tockenId );


        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        // Cargo la configuración para
        TicketConstants.UID = PreferenceManager.getDefaultSharedPreferences(this).getString("ticket_UID", "000");

        //startService(new Intent(this, SendUIDIntentService.class));
        //TicketServerWS.setnewTokenID("a","b");
        //mTextView = (TextView) findViewById(R.id.textView_explanation);

        new GetDataAsync().execute("");

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {


            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            handleIntent(getIntent());
            //StartSchedule(); // Si no tenemos NFC funcionamos por triggers del servidor
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            //finish();
            return;

        }




        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disable.", Toast.LENGTH_LONG).show();
        } else {
            //mTextView.setText(R.string.explanation);
        }


        /*WebView webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        //webview.loadData(resultText, "text/html", null);

        String resultText = "<html><head><LINK href=\"ticket.css\" type=\"text/css\" rel=\"stylesheet\"/></head>" +
                "<body style='margin:2px;'>";

        resultText += "<div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;<b></div><div style='float:left; min-height:30px; margin-left:-1px; transform: scale(2,3); ms-transform: scale(2,3); -webkit-transform: scale(2,3); -moz-transform:scale(2,3); -o-transform:scale(2,3); position:relative; transform-origin: top left;-webkit-transform-origin: top left;-moz-transform-origin: top left; -o-transform-origin: top left;'>FREE&nbsp;FLOW&nbsp;ABADES&nbsp;&nbsp;</b></div><div  style='float:left'>\n" +
                "</div></div><br><br><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;A92&nbsp;KM&nbsp;192&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;18300&nbsp;LOJA&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;GRANADA&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TLF:902323800&nbsp;&nbsp;&nbsp;&nbsp;CIF:B18410209&nbsp;\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;<b>GESTION&nbsp;Y&nbsp;EXPLOTACION&nbsp;DE&nbsp;REST.&nbsp;S.L</b>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>----------------------------------------\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'><b>FACTURA&nbsp;SIMPLIFICADA</b>&nbsp;&nbsp;&nbsp;&nbsp;<b>FECHA:</b>03/08/2016\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'><b>SERIE/NUMERO</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>HORA:</b>&nbsp;9:24:12&nbsp;\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>T003/818718&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>SALA:</b>&nbsp;0&nbsp;&nbsp;<b>PAX:</b>&nbsp;&nbsp;0\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'><b>CAMARERO:</b>JUAN&nbsp;PEREZ&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>MESA:</b>&nbsp;<b>1&nbsp;&nbsp;</b><b>CAJA:</b>&nbsp;3\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'><b>CLIENTE:</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'><b>NIF/CIF:</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'><b>UDS</b>&nbsp;&nbsp;<b>DESCRIPCION</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>PVP</b>&nbsp;&nbsp;<b>IMPORTE</b>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>----------------------------------------\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;MEDIA&nbsp;DOCENA&nbsp;HUESO&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;4,50&nbsp;&nbsp;&nbsp;&nbsp;4,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;TARTA&nbsp;ABADES&nbsp;Porci&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2,00&nbsp;&nbsp;&nbsp;&nbsp;2,00\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;DONUTS&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,25&nbsp;&nbsp;&nbsp;&nbsp;1,25\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;PALMERA&nbsp;O&nbsp;CROISSAN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,60&nbsp;&nbsp;&nbsp;&nbsp;1,60\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;MUFFIN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2,80&nbsp;&nbsp;&nbsp;&nbsp;2,80\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;ROSCOS&nbsp;HUEVO&nbsp;TOJUN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;7,00&nbsp;&nbsp;&nbsp;&nbsp;7,00\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;ROSCOS&nbsp;SANTA&nbsp;CLARA&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;4,95&nbsp;&nbsp;&nbsp;&nbsp;4,95\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;ROSCOS&nbsp;ANIS&nbsp;TOJUNT&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;6,00&nbsp;&nbsp;&nbsp;&nbsp;6,00\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;PASTAS&nbsp;TE&nbsp;Uds&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0,60&nbsp;&nbsp;&nbsp;&nbsp;0,60\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;MEDIA&nbsp;DOCENA&nbsp;PIONO&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;8,00&nbsp;&nbsp;&nbsp;&nbsp;8,00\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;HIGOS&nbsp;SECOS&nbsp;BANDEJ&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;5,70&nbsp;&nbsp;&nbsp;&nbsp;5,70\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOLLYCAO&nbsp;NORMAL&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,10&nbsp;&nbsp;&nbsp;&nbsp;1,10\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;DONUTS&nbsp;B&nbsp;2&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,95&nbsp;&nbsp;&nbsp;&nbsp;1,95\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;MEDIA&nbsp;DOCENA&nbsp;PIONO&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;8,00&nbsp;&nbsp;&nbsp;&nbsp;8,00\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;SANDWICH&nbsp;VARIOS&nbsp;ES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3,20&nbsp;&nbsp;&nbsp;&nbsp;3,20\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOCAD.JAMON&nbsp;SERRAN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3,50&nbsp;&nbsp;&nbsp;&nbsp;3,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOCAD.LOMO&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;4,20&nbsp;&nbsp;&nbsp;&nbsp;4,20\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOCAD.TERNERA&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;5,00&nbsp;&nbsp;&nbsp;&nbsp;5,00\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;TARTA&nbsp;ESPECIAL&nbsp;ABA&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2,50&nbsp;&nbsp;&nbsp;&nbsp;2,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOTELLA&nbsp;75CL&nbsp;A ARE&nbsp;&nbsp;&nbsp;&nbsp;10,30&nbsp;&nbsp;&nbsp;10,30\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOCAD.TORTILLA&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3,50&nbsp;&nbsp;&nbsp;&nbsp;3,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOLSA&nbsp;PATATAS&nbsp;MEDI&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,70&nbsp;&nbsp;&nbsp;&nbsp;1,70\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;CARAMELOS&nbsp;MIEL&nbsp;LIM&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3,50&nbsp;&nbsp;&nbsp;&nbsp;3,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOLSA&nbsp;PATATAS&nbsp;GRAN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2,50&nbsp;&nbsp;&nbsp;&nbsp;2,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;YOGOURT&nbsp;DANONE&nbsp;ACT&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,50&nbsp;&nbsp;&nbsp;&nbsp;1,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;YOGOURT&nbsp;DANONE&nbsp;NAT&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,20&nbsp;&nbsp;&nbsp;&nbsp;1,20\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;YOGOURT&nbsp;DANONE&nbsp;SAB&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1,20&nbsp;&nbsp;&nbsp;&nbsp;1,20\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;AGUA&nbsp;EVIAN&nbsp;75CL&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2,30&nbsp;&nbsp;&nbsp;&nbsp;2,30\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;COPA&nbsp;LICOR&nbsp;PACHARA&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2,50&nbsp;&nbsp;&nbsp;&nbsp;2,50\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;1&nbsp;BOTELLA&nbsp;75CL&nbsp;MONTE&nbsp;&nbsp;&nbsp;&nbsp;13,00&nbsp;&nbsp;&nbsp;13,00\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>========================================\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;BASE&nbsp;IMP.&nbsp;&nbsp;&nbsp;106,71\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;IVA&nbsp;10%&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;10,12\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b></div><div style='float:left; min-height:30px; margin-left:-1px; transform: scale(1,2); ms-transform: scale(1,2); -webkit-transform: scale(1,2); -moz-transform:scale(1,2); -o-transform:scale(1,2); position:relative; transform-origin: top left;-webkit-transform-origin: top left;-moz-transform-origin: top left; -o-transform-origin: top left;'>TOTAL</b></div><div  style='float:left'>&nbsp;&nbsp;&nbsp;<b></div><div style='float:left; min-height:30px; margin-left:-1px; transform: scale(1,2); ms-transform: scale(1,2); -webkit-transform: scale(1,2); -moz-transform:scale(1,2); -o-transform:scale(1,2); position:relative; transform-origin: top left;-webkit-transform-origin: top left;-moz-transform-origin: top left; -o-transform-origin: top left;'>&nbsp;&nbsp;&nbsp;&nbsp;117,05</b></div><div  style='float:left'>\n" +
                "</div></div><br><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ENTREGADO&nbsp;&nbsp;&nbsp;117,05\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CAMBIO&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FORMA&nbsp;DE&nbsp;PAGO:&nbsp;&nbsp;&nbsp;CONTADO&nbsp;Euros\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;***&nbsp;GRACIAS&nbsp;POR&nbsp;SU&nbsp;VISITA&nbsp;***\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>informate&nbsp;en&nbsp;www.abades.com</b>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'>\n" +
                "</div></div><div style='min-height:17px;'><div style='float:left'></div></p><br></body></html>";
        //resultText += "<b>Esto es una kk</b><br>Otro";
        webview.loadDataWithBaseURL("file:///android_asset/", resultText, "text/html", "UTF-8", "");

        // Crear un mensaje NDEF para envio en background
        //mMessage = new NdefMessage(
        //        new NdefRecord[] { newTextRecord("NDEF Push Sample ewew", Locale.ENGLISH, true)});

        //mNfcAdapter.setNdefPushMessage(mMessage, this);*/

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        handleIntent(getIntent());

        //StartSchedule();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
        //if (mNfcAdapter != null) mNfcAdapter.enableForegroundNdefPush(this, mMessage);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_favorite:
                TicketConstants.UID = PreferenceManager.getDefaultSharedPreferences(this).getString("ticket_UID", "000");
                startService(new Intent(this, SendUIDIntentService.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // TODO: handle Intent
        TextView mText = (TextView) findViewById(R.id.mTextView);

        mText.setText("");
        //mTextView.setText("Readed content ");
        String action = intent.getAction();
        TextView mTextAction = (TextView) findViewById(R.id.textAction);
        mTextAction.setText(action);


        /**if (TicketConstants.lastTicket != null)
        {
            VirtualPrinter vp = new VirtualPrinter();
            vp.Initialize();

            byte[] message = Base64.decode(TicketConstants.lastTicket.getTicket(), Base64.DEFAULT);
            String resultText = vp.processText(message);
            //String resultText = processText(message);
            WebView webview = (WebView) findViewById(R.id.webview);
            webview.getSettings().setJavaScriptEnabled(true);
            //webview.loadData(resultText, "text/html", null);

            //resultText += "<b>Esto es una kk</b><br>Otro";*/
        /**
            webview.loadDataWithBaseURL("file:///android_asset/", resultText, "text/html", "UTF-8", "");

        }*/

        if (mNfcAdapter == null) {
            return;
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                mText.setText("Plain Text");
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else if (MIME_GIF.equals(type)) {
                mText.setText("GIF IMAGE");
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            }
            else {
                mText.setText("Wrong mime type: " + type);
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            mText.setText("In Tech");
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask().execute(tag);
        }

    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {

        NotificationMessage.SetResumed();

        if (adapter == null)
            return;

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
            filters[0].addDataType(MIME_PDF);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
        //adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }

    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        NotificationMessage.SetPaused();

        if (adapter != null)
            adapter.disableForegroundDispatch(activity);
    }



    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        byte[] message;
        @Override
        protected String doInBackground(Tag... params) {

            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                return "Not NDEF tag " + tag.toString();

                // NDEF is not supported by this Tag.
                //return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            if (ndefMessage == null)
            {
                return "No message";
                //return null;
            }

            NdefRecord[] records = ndefMessage.getRecords();
            if (records == null)
            {
                return "No records";
                //return null;
            }
            for (NdefRecord ndefRecord : records) {
                byte[] RTD_INVOICE = {0X077};

                if (ndefRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA)
                {
                    message = ndefRecord.getPayload();
                    return null;
                    //return "content mime:" + readText(ndefRecord);

                }
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {

                    try {
                        message = readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                else if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), RTD_INVOICE)) {
                    try {
                        message = readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                else {
                    String strType = "";
                    byte[] types = ndefRecord.getType();
                    for (byte type : types)
                    {
                        strType += type + " ,";
                    }
                    return "Record undef:" + ndefRecord.getTnf() + " Type:" + strType;
                }
            }

            return "No message";
        }

        private byte[] readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0x3F;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"
            byte [] byteContent = copyOfRange(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1);
            //String text =new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, "ASCII");

            return byteContent;
            // Get the Text
            //return text;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {


                TextView mText = (TextView) findViewById(R.id.mTextView);

                VirtualPrinter vp = new VirtualPrinter();
                vp.Initialize();
                String resultText = vp.processText(message);
                //String resultText = processText(message);
                WebView webview = (WebView) findViewById(R.id.webview);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.loadDataWithBaseURL("file:///android_asset/", resultText, "text/html", "UTF-8", "");
                //mText.setText(Html.fromHtml(resultText));
            }
            else {
                TextView mText = (TextView) findViewById(R.id.mTextView);
                mText.setText("Error: " + result);

            }
        }
    }

    /**
     * Actuaremos en Background para cargar en nuestro ArrayList<Ticket> colTickets todos los tickets que tenga nuestro                 servidor.
     * Crearemos los fragments después de la ejecución del Background, diremos en que parte del layout se muestran los fragments        y seleccionamos que elemento del array es el primero que queremos mostrar en nuestro primer fragment.
     */
    public class GetDataAsync extends AsyncTask<String, String, Ticket> {

        HttpURLConnection urlConnection;
        @Override
        protected Ticket doInBackground(String... args) {
            //si es nuestra primera vez llamamos al servidor y cargamos todos los tickets qye tenga el servidor
            if (TicketConstants.colTickets == null) {
                TicketConstants.colTickets = TicketServerWS.getTicketsByUID(TicketConstants.UID, false);
            }
            /*Ticket t = (Ticket)TicketConstants.colTickets.get(TicketConstants.colTickets.size() - 1);
            if (t.getTicket() == "") {
                Ticket tLoaded = TicketServerWS.getTicket(t.getIdticket());
                TicketConstants.colTickets.set(TicketConstants.colTickets.size() - 1, tLoaded);
                return tLoaded;
            }
            else
                return t;
*/
            //no queremos devolver nada, si hay que añadir los tickets del servidor al array se añade y sino no se hace nada
            return null;
        }

        @Override
        protected void onPostExecute(Ticket result) {

            /** Getting a reference to the ViewPager defined the layout file */
            ViewPager pager = (ViewPager) findViewById(R.id.pager);


            /** Getting fragment manager */
            FragmentManager fm = getSupportFragmentManager();

            /** Instantiating FragmentPagerAdapter */
            MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(fm,TicketConstants.colTickets);

            /** Setting the pagerAdapter to the pager object */
            pager.setAdapter(pagerAdapter);

            //seleccionamos el primer elemento que queremos mostrar
            pager.setCurrentItem(TicketConstants.colTickets.size()-1);



        }

    }




}
