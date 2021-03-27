package com.example.experiment_automata.backend.qr;

import java.util.UUID;

/**
 * Role/Pattern:
 *     Class that decodes scanned QR codes and creates a QRCode object from the given qr codes content
 *
 *  Known Issue:
 *
 *      1. None
 */
public class QRMaker {
    /**
     * Decodes a raw string that has been scanned from a custom QR code
     * @param rawContent
     * raw string from scanned QR code to be decoded
     * @return
     * returns a QRCode
     */
    public QRCode decodeQRString(String rawContent){
        if (!checkQRHeader(rawContent)){
            //not valid raise exception
        }
        //mask uuid
        UUID experimentUUID;
        experimentUUID = UUID.fromString(rawContent.substring(4, 41));
        //Build based on type
        String typeSpecifier;
        typeSpecifier = rawContent.substring(41,42);
        String content = rawContent.substring(42);
        QRCode qrCode;
        switch (typeSpecifier){
            case QRCode.EXPERIMENT_ONLY_ID:
                qrCode = new ExperimentQRCode(experimentUUID);
                break;
            case QRCode.BINOMIAL_ID:
                //bin
                Boolean binVal = false;
                switch (content){
                    case BinomialQRCode.BINOMIAL_TRUE:
                        binVal = true;
                        break;
                    case BinomialQRCode.BINOMIAL_FALSE:
                        binVal = false;
                        break;
                }
                qrCode = new BinomialQRCode(experimentUUID,binVal);
                break;
            case QRCode.COUNT_ID:
                qrCode = new CountQRCode(experimentUUID);
                break;
            case QRCode.MEASUREMENT_ID:
                String resultM = rawContent.substring(43);
                qrCode = new MeasurementQRCode(experimentUUID,Float.parseFloat(resultM));
                break;
            case QRCode.NATURALC_ID:
                String resultN = rawContent.substring(43);
                qrCode = new NaturalQRCode(experimentUUID, Integer.parseInt(resultN));
                break;
            default://incorrect type specifier
                qrCode = null;
        }
        return qrCode;
    }
    /**
     * Checks a string for the custom Automata QR identifier
     * @param qrString
     * raw string from QR to check
     * @return
     * returns a QRCode
     */
    public boolean checkQRHeader(String qrString){
        return qrString.substring(0,4).equals(QRCode.AUTOMATA_QR_HEADER);
    }
}