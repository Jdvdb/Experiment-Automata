package com.example.experiment_automata.ui.qr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.experiment_automata.R;
import com.example.experiment_automata.backend.qr.BinomialQRCode;
import com.example.experiment_automata.backend.qr.CountQRCode;
import com.example.experiment_automata.backend.qr.ExperimentQRCode;
import com.example.experiment_automata.backend.qr.MeasurementQRCode;
import com.example.experiment_automata.backend.qr.NaturalQRCode;
import com.example.experiment_automata.backend.qr.QRCode;

import java.util.UUID;

/**
 * Role/Pattern:
 *
 *       This class displays a QR code representing the current Experiment
 *
 * Known Issue:
 *
 *      1. Doesn't generate a correct QR code. Generates one with a random Experiment ID. Will be fixed once parent activity has proper UUID implementation.
 */
public class ViewQRFragment extends DialogFragment {

    private ImageView qrImageView;
    private TextView qrValue;
    private Button backButton;
    private CheckBox checkBox;
    private String experimentUUIDString;
    private QRCode qrCode;
    private Bitmap qrCodeImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_qr,container, true);
        backButton = view.findViewById(R.id.qr_code_back_button);
        qrImageView = view.findViewById(R.id.qr_code_imageView);
        qrValue = view.findViewById(R.id.qr_value_textView);
        QRCode qrCode;
        // = new QRCodeManager();

        Bundle bundle = getArguments();
        String description = bundle.getString("DESCRIPTION");
        UUID experimentUUID = UUID.fromString(bundle.getString("UUID"));
        String typeString = bundle.getString("TYPE");
        //use switch case and string bundles instead of serializing QR
        switch (typeString){
            case "Experiment":
                qrCode = new ExperimentQRCode(experimentUUID);
                break;
            case "BinomialTrial":
                boolean binomialVal = bundle.getBoolean("BINVAL");
                qrCode = new BinomialQRCode(experimentUUID,binomialVal);
                break;
            case "CountTrial":
                qrCode = new CountQRCode(experimentUUID);
                break;
            case "MeasurementTrial":
                float measurementVal = bundle.getFloat("MEASVAL");
                qrCode = new MeasurementQRCode(experimentUUID,measurementVal);
                break;
            case "NaturalCountTrial":
                int naturalCount = bundle.getInt("NATVAL");
                qrCode = new NaturalQRCode(experimentUUID,naturalCount);
                break;
            default:
                qrCode = null;
        }


        qrCodeImage = qrCode.getQrCodeImage();
        qrImageView.setImageBitmap(qrCodeImage);//qr_value_textView
        qrValue.setText(description);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }


}

