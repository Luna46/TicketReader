package es.disatec.ticketreader;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a sample APDU Service which demonstrates how to interface with the card emulation support
 * added in Android 4.4, KitKat.
 *
 * <p>This sample replies to any requests sent with the string "Hello World". In real-world
 * situations, you would need to modify this code to implement your desired communication
 * protocol.
 *
 * <p>This sample will be invoked for any terminals selecting AIDs of 0xF11111111, 0xF22222222, or
 * 0xF33333333. See src/main/res/xml/aid_list.xml for more details.
 *
 * <p class="note">Note: This is a low-level interface. Unlike the NdefMessage many developers
 * are familiar with for implementing Android Beam in apps, card emulation only provides a
 * byte-array based communication channel. It is left to developers to implement higher level
 * protocol support as needed.
 */
public class CardService extends HostApduService {
    private static final String TAG = "CardService";
    // AID for our loyalty card service.
    private static final String SAMPLE_LOYALTY_CARD_AID = "F22511D2B5C3A1A2D7FF";
    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String SELECT_APDU_HEADER = "00A40400";
    // "OK" status word sent in response to SELECT AID command (0x9000)
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    private byte[] SELECT_START_ACCOUNT = {0x01, 0x03}; // 0x03 envia al servidor y por HCE
                                                        // 0X01 solo lo envia por HCE
                                                        //0X02 solo guarda
                                                        // 0x04 envia alarma desde el servidor

    // "UNKNOWN" status word sent in response to invalid APDU command (0x0000)
    private static final byte[] UNKNOWN_CMD_SW = HexStringToByteArray("0000");
    private static final byte[] SELECT_APDU = BuildSelectApdu(SAMPLE_LOYALTY_CARD_AID);

    private static boolean bStartTicket = true;
    List<Byte> messageBytes = new ArrayList<Byte>();

    // grupo y comercio, me lo transmitirán por HCE
    private static String grupo = "";
    private static String comercio = "";


    /**
     * Called if the connection to the NFC card is lost, in order to let the application know the
     * cause for the disconnection (either a lost link, or another AID being selected by the
     * reader).
     *
     * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
     */
    @Override
    public void onDeactivated(int reason) { }

    /**
     * This method will be called when a command APDU has been received from a remote device. A
     * response APDU can be provided directly by returning a byte-array in this method. In general
     * response APDUs must be sent as quickly as possible, given the fact that the user is likely
     * holding his device over an NFC reader when this method is called.
     *
     * <p class="note">If there are multiple services that have registered for the same AIDs in
     * their meta-data entry, you will only get called if the user has explicitly selected your
     * service, either as a default or just for the next tap.
     *
     * <p class="note">This method is running on the main thread of your application. If you
     * cannot return a response APDU immediately, return null and use the {@link
     * #sendResponseApdu(byte[])} method later.
     *
     * @param commandApdu The APDU that received from the remote device
     * @param extras A bundle containing extra data. May be null.
     * @return a byte-array containing the response APDU, or null if no response APDU can be sent
     * at this point.
     */
    // BEGIN_INCLUDE(processCommandApdu)
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.i(TAG, "Received APDU: " + ByteArrayToHexString(commandApdu));
        // If the APDU matches the SELECT AID command for this service,
        // send the loyalty card account number, followed by a SELECT_OK status trailer (0x9000).
        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            bStartTicket = true;
            messageBytes.clear();
            //TicketConstants.lastTicket = "";
            String UID = TicketConstants.UID;
            String modeSendStr = PreferenceManager.getDefaultSharedPreferences(this).getString("mode_list", "4");
            // Lo cargo en el momento de enviarlo
            TicketConstants.modeSend = Integer.parseInt(modeSendStr);
            byte mode = (byte)TicketConstants.modeSend;
            byte[] accountBytes = UID.getBytes();
            Log.i(TAG, "Sending UID number: " + UID);
            SELECT_START_ACCOUNT[1] = mode;
            return ConcatArrays(SELECT_START_ACCOUNT, accountBytes, SELECT_OK_SW);
        } else {
            if ((commandApdu[1] & 0xFF) >= 0xDA && (commandApdu[1] & 0xFF) <0XFD) {

                Log.i(TAG, "Write command received");

                for(int i= 2;i<commandApdu.length;i++)
                    messageBytes.add(commandApdu[i]);

                byte[] bytesReturn = {0x00, commandApdu[1]};
                return ConcatArrays(bytesReturn, SELECT_OK_SW);

            } else if ((commandApdu[1] & 0xFF) == 0xFD) {
                Log.i(TAG, "FD ommand received");

                for(int i= 2;i<commandApdu.length;i++)
                    messageBytes.add(commandApdu[i]);

                byte[] myArray = new byte[messageBytes.size()];

                for (int i = 0; i < messageBytes.size(); i++) {
                    myArray[i] = messageBytes.get(i);
                }
                TicketConstants.lastTicket = new Ticket();
                TicketConstants.lastTicket.setTicket(Base64.encodeToString(myArray, Base64.DEFAULT));
                //TicketConstants.lastTicket = byteFinal;

                NotificationMessage.showNotification(this, grupo,comercio);
                // Notificar el nuevo ticket


                byte[] bytesReturn = {0x00, commandApdu[1]};
                return ConcatArrays(bytesReturn, SELECT_OK_SW);
            }
            // Datos de la configuración, grupo y comercio
            else if ((commandApdu[1] & 0xFF) == 0xC1) {
                Log.i(TAG, "C1 commiand received");

                int codigoRaspberry = (commandApdu[2] << 8) + commandApdu[3];
                int codigoComercio = (commandApdu[4] << 8) + commandApdu[5];

                grupo = "";
                for(int i= 7;i<7+commandApdu[6];i++)
                    grupo += (char)commandApdu[i];

                int posicionComercio = 7+commandApdu[6];

                comercio = "";
                for (int i= posicionComercio+1; i<commandApdu.length;i++)
                {
                    comercio += (char)commandApdu[i];
                }
                // Nos quedamos con el comercio y el grupo para posteriormente mostrarlo

                byte[] bytesReturn = {0x00, commandApdu[1]};
                return ConcatArrays(bytesReturn, SELECT_OK_SW);
            }
            else {

                Log.i(TAG, "Unknown command ");
                byte[] bytesReturn = {0x00, commandApdu[1]};
                return ConcatArrays(bytesReturn, SELECT_OK_SW);

                //Toast.makeText(this, 1000);
                //return UNKNOWN_CMD_SW;
            }
        }


    }




    // END_INCLUDE(processCommandApdu)

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
    }

    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws java.lang.IllegalArgumentException if input length is incorrect
     */
    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Utility method to concatenate two byte arrays.
     * @param first First array
     * @param rest Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
