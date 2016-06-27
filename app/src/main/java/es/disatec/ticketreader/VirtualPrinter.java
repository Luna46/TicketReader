package es.disatec.ticketreader;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by Pepe on 10/06/2016.
 */
public class VirtualPrinter {
    int [] cadenasDeControl = {10,27,28,29};


    int [] frmSinFormato = {27,33,0};
    int [] frmNegrita = {27,33,8};
    int [] frmSubrallado = {27,33,128};
    int [] frmSubralladoNegrita = {27,33,136};
    int [] frmAlta = {27,33,16};
    int [] frmAltaNegrita = {27,33,24};
    int [] frmAltaSubrallado = {27,33,144};
    int [] frmAltaNegritaSubrallado = {27,33,152};
    int [] frmAncho = {27,33,32};
    int [] frmAnchoNegrita = {27,33,40};
    int [] frmAnchoSubrallado = {27,33,160};
    int [] frmAnchoNegritaSubrallado = {27,33,168};
    int [] frmAnchoAlta = {27,33,48};
    int [] frmAnchoNegritaAlta = {27,33,56};
    int [] frmAnchoAltaSubrallado = {27,33,176};
    int [] frmAnchoAltaSubralladoNegrita = {27,33,184};
    int [] frmSecuenciaCambioPagina = {12};
    int [] frmSecuenciaInicial = {24,27,64,27,33,0};
    int [] frmSecuenciaFinal = {29,86,65,0,27,64};
    int [] frmSimboloEuro = {27,116,19,213};
    int [] frmComprimida = {27,33,1};
    int [] frmSeparacionEntreLineas = {27,51,18};
    int [] frmSeparacionEntreLineasDefecto = {27,50};
    int [] frmTamanoFuenteNumeroServicio = {27,97,49,29,33,67};

    // Configuración con parametros configurables -1 'configurable' ,  -2 , 'indica los siguientes n bytes a leer'
    int [] frmAnchoCodigoBarras = {29, 'w', -1};  // Set barcode width
    /* The module width is as follows:
                        n	Module width for multilevel barcode	Binary level barcode
                        Thin element width	Thick element width
                        2	0.282 mm {0.011 inch}	0.282 mm {0.011 inch}	0.706 mm {0.028 inch}
                        3	0.423 mm {0.017 inch}	0.423 mm {0.017 inch}	1.129 mm {0.044 inch}
                        4	0.564 mm {0.022 inch}	0.564 mm {0.022 inch}	1.411 mm {0.056 inch}
                        5	0.706 mm {0.028 inch}	0.706 mm {0.028 inch}	1.834 mm {0.072 inch}
                        6	0.847 mm {0.033 inch}	0.847 mm {0.033 inch}	2.258 mm {0.089 inch}
                        */
    int [] frmAlturaCodigoBarras = {29, 'h', -1}; // Set barcode height,  A set unit is one dot. One dot corresponds to 0.141 mm {1/180 inch}.
    int [] frmSelectPrintPositionCode = {29, 'H', -1};
        /* Selects the print position of Human Readable Interpretation (HRI) characters when printing a barcode, using n as follows:
                                n	Print position
                                0, 48	Not printed
                                1, 49	Above the barcode
                                2, 50	Below the barcode
                                3, 51	Both above and below the barcode
                                */

    int [] frmPrintBarCode = {29, 'k', 69, -2}; // prints bar code, nos dice la cantidad de caracteres que están dentro del barcode,

    int [][] tiposPosibles;
    boolean bold = false;
    boolean doublestrike = false;
    boolean doublewidth = false;
    boolean underlined = false;
    int extraline = 0;

    int anchuraCodigoBarras = 2;
    int alturaCodigoBarras = 2;
    int positionCode = 0;



    public void Initialize()
    {
        tiposPosibles = new int[][] {frmSinFormato, frmNegrita, frmSubrallado, frmSubralladoNegrita, frmAlta,
                frmAltaNegrita,frmAltaSubrallado, frmAltaNegritaSubrallado, frmAncho, frmAnchoNegrita,  frmAnchoSubrallado, frmAnchoNegritaSubrallado,
                frmAnchoAlta,frmAnchoNegritaAlta, frmAnchoAltaSubrallado, frmAnchoAltaSubralladoNegrita, frmSecuenciaCambioPagina, frmSecuenciaInicial, frmSecuenciaFinal,
                frmSimboloEuro, frmComprimida,frmSeparacionEntreLineas, frmSeparacionEntreLineasDefecto, frmTamanoFuenteNumeroServicio,
                frmAnchoCodigoBarras, frmAlturaCodigoBarras, frmPrintBarCode, frmSelectPrintPositionCode};
    }


    public String printCodigoBarras(int anchura, int altura, String codigoBarras)
    {
        String strResult = "</div><div>";
        strResult += "<svg id=\"barcode\"></svg>";
        strResult += "<script src=\"JsBarcode.code39.min.js\"></script>";
        String strNoDisplayText = "";
        if (positionCode == 0)
            strNoDisplayText = ",displayValue: false";

        String strDisplayTop = "";
        if (positionCode == 1)
            strDisplayTop = ", textPosition: \"top\"";

        strResult += "<script>JsBarcode(\"#barcode\", \""+codigoBarras+"\", {width: "+anchura/1.5+", height:"+altura/1.5+ strNoDisplayText + strDisplayTop +"});</script>";
        strResult += "</div>";
        return strResult;
    }

    public String EvaluarSecuencia (int[] tipoEvaluar, Vector<Integer> parameters) {
        String resultText = "";
        if (tipoEvaluar == frmSimboloEuro) {
            return "&euro;";
        }
        else if (tipoEvaluar == frmPrintBarCode) {
            String strBarCode = "";
            for (int i=1;i<parameters.size();i++)
            {
                strBarCode += (char)parameters.elementAt(i).intValue();
            }

            return printCodigoBarras(anchuraCodigoBarras, alturaCodigoBarras, strBarCode);


        }

        else if (tipoEvaluar == frmAlturaCodigoBarras)
        {
            alturaCodigoBarras = parameters.elementAt(0).intValue();
            return "";
        }
        else if (tipoEvaluar == frmAnchoCodigoBarras)
        {
            anchuraCodigoBarras = parameters.elementAt(0).intValue();
            return "";
        }

        else if (tipoEvaluar == frmSelectPrintPositionCode)
        {
            positionCode = parameters.elementAt(0).intValue();
            return "";
        }


        else if (tipoEvaluar == frmSinFormato) {
            if (underlined) {
                resultText += "</u>";
                underlined = false;
            }

            if (bold)
            {
                resultText += "</b>";
                bold = false;
            }

            if (doublestrike || doublewidth)
            {
                resultText += "</div><div  style='float:left'>";
                doublestrike = false;
                doublewidth = false;

            }
        } else if (tipoEvaluar == frmNegrita) {
            bold = true;
        } else if (tipoEvaluar == frmSubrallado) {
            underlined = true;
        } else if (tipoEvaluar == frmSubralladoNegrita) {
            underlined = true;
            bold = true;
        } else if (tipoEvaluar == frmAlta) {
            doublestrike= true;
        } else if (tipoEvaluar == frmAltaNegrita) {
            bold = true;
            doublestrike = true;
        } else if (tipoEvaluar == frmAltaSubrallado) {
            underlined = true;
            doublestrike = true;
        } else if (tipoEvaluar == frmAltaNegritaSubrallado) {
            underlined = true;
            bold = true;
            doublestrike = true;
        } else if (tipoEvaluar == frmAncho) {
            doublewidth= true;
        } else if (tipoEvaluar == frmAnchoNegrita) {
            doublewidth = true;
            bold = true;
        } else if (tipoEvaluar == frmAnchoNegritaSubrallado) {
            underlined = true;
            bold = true;
            doublewidth = true;
        }else if (tipoEvaluar == frmAnchoAlta) {
            doublewidth = true;
            doublestrike= true;
        } else if (tipoEvaluar == frmAnchoNegritaAlta) {
            doublewidth = true;
            bold = true;
            doublestrike = true;
        } else if (tipoEvaluar == frmAnchoAltaSubrallado) {
            underlined = true;
            doublestrike = true;
            doublewidth = true;
        } else if (tipoEvaluar == frmAnchoAltaSubralladoNegrita) {
            underlined = true;
            doublestrike = true;
            doublewidth = true;
            bold = true;
        }


        /***** Se evaluan todas las combinaciones de posibles fuentes *****/
        if (bold) {
            resultText+="<b>";
        }

        if (underlined) {
            resultText += "<u>";
        }

        if (doublewidth || doublestrike)
        {
            extraline = 0;
            String scaleX = doublewidth? "2" : "1";
            String scaleY = doublestrike? doublewidth? "3" : "2" : "1";
                                    /*String marginBottom = "1px";
                                    if (doublestrike && doublewidth)
                                        marginBottom = "2px";*/

            String scale = "scale("+scaleX+","+scaleY+")";
            resultText += "</div><div style='float:left; min-height:30px; margin-left:-1px; transform: "+scale+"; ms-transform: "+scale+"; -webkit-transform: "+scale+"; -moz-transform:"+scale+"; -o-transform:"+scale;
            //resultText += "'>";
            resultText +="; position:relative; transform-origin: top left;-webkit-transform-origin: top left;-moz-transform-origin: top left; -o-transform-origin: top left;'>";
        }

        if (doublestrike)
            extraline++;

        if (doublewidth)
            extraline++;




        return resultText;

    }

    /***
     * Process text in ESC POS format and return html
     * @param text
     * @return html
     */
    public String processText(byte[] text)
    {
        String resultText = "<html style=' font-family: \"Courier\"; font-size: 14px'><body style='margin:2px;'>";

        resultText += "<div style='min-height:17px;'><div style='float:left'>";
        for (int i=0; i<text.length; i++) {
            boolean bMismatch = false;

            int idx = Arrays.binarySearch(cadenasDeControl, text[i] & 0xff);
            // Si es una cadena de control, busco si hay más coincidencias
            if (idx >= 0) {

                Vector<Integer> parameters = new Vector<Integer>();
                for (int iType = 0; iType < tiposPosibles.length && !bMismatch; iType++) {
                    int[] tipoEvaluar = tiposPosibles[iType];
                    int extraChars = 0;
                    bMismatch = true;
                    for (int iChar = 0; iChar < tipoEvaluar.length && bMismatch; iChar++) {
                        // Si coincidide o es un parametro, sigue coincidiendo
                        if (tipoEvaluar[iChar] == -1)  // parámetro fijo
                        {
                            parameters.add((int)(text[i+iChar] & 0xff));
                        }
                        else if (tipoEvaluar[iChar] == -2) // parametro que indica que hay que leer los siguientes 'n' caracteres
                        {
                            int nroCharsToRead = (int)(text[i+iChar] & 0xff);
                            //parameters.add(nroCharsToRead);
                            for (int j=0; j< nroCharsToRead;j++)
                            {
                                parameters.add((int)(text[i+iChar+j] & 0xff));
                            }

                            extraChars += nroCharsToRead;



                        }
                        else if (i + iChar >= text.length || (text[i + iChar] & 0xff) != tipoEvaluar[iChar] ) {
                            bMismatch = false;
                        }
                    }

                    if (bMismatch) {
                        resultText += EvaluarSecuencia(tipoEvaluar, parameters);
                        i += tipoEvaluar.length + extraChars - 1;
                    }
                }
            }

            if (!bMismatch) {
                byte ch = text[i];
                switch (ch) {
                    // retornos de carro
                    case '\n':
                        resultText += "</div></div>";
                        for (int iExtra=0;iExtra<extraline;iExtra++)
                            resultText += "<br>";
                        resultText += "<div style='min-height:17px;'><div style='float:left'>";
                        extraline = 0;
                        break;

                    // espacios en blanco
                    case 32:
                        resultText += "&nbsp;";
                        break;

                    default:
                        resultText+=(char)ch;
                }
            }

        }

        resultText += "</div></p><br></body></html>";
        return resultText;
    }

}

