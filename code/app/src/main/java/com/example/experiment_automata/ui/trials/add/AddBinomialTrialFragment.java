package com.example.experiment_automata.ui.trials.add;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.experiment_automata.R;
import com.example.experiment_automata.backend.barcode.BarcodeManager;
import com.example.experiment_automata.backend.experiments.BinomialExperiment;
import com.example.experiment_automata.backend.qr.BinomialQRCode;
import com.example.experiment_automata.backend.qr.QRCode;
import com.example.experiment_automata.backend.qr.QRMaker;
import com.example.experiment_automata.backend.qr.QRMalformattedException;
import com.example.experiment_automata.backend.qr.QRType;
import com.example.experiment_automata.backend.trials.BinomialTrial;
import com.example.experiment_automata.ui.NavigationActivity;
import com.example.experiment_automata.ui.qr.ScannerActivity;
import com.example.experiment_automata.ui.qr.ViewQRFragment;
import com.example.experiment_automata.ui.trials.MapDisplay.MapUtility;
import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.views.MapView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddBinomialTrialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddBinomialTrialFragment extends Fragment {
    private ImageButton scanQRButton;
    private ImageButton viewQRButton;
    private CheckBox checkBox;
    private View root;
    private MapView currentMapDisplay;
    private MapUtility utility;


    public AddBinomialTrialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_add_binomial_trial, container, false);
        TextView description = (TextView) root.findViewById(R.id.binomial_trial_experiment_description);
        NavigationActivity parentActivity = ((NavigationActivity) getActivity());
        BinomialExperiment experiment = (BinomialExperiment) parentActivity.experimentManager.getCurrentExperiment();
        description.setText(experiment.getDescription());
        checkBox = root.findViewById(R.id.add_binomial_value);//add_binomial_value

        scanQRButton = root.findViewById(R.id.binomial_trial_experiment_description_qr_button);
        scanQRButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {//open scanner
                Intent intent = new Intent(getActivity(), ScannerActivity.class);
                startActivityForResult(intent,1);

            }
        });

        viewQRButton = root.findViewById(R.id.binomial_trial_experiment_description_qr_generate_button);
        viewQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//display QR
                // add content when done
                Fragment viewQRFragment = new ViewQRFragment();
                Bundle bundle = new Bundle();
                bundle.putString("UUID", experiment.getExperimentId().toString());
                bundle.putString("DESCRIPTION",experiment.getDescription());
                bundle.putString("TYPE", QRType.BinomialTrial.toString());
                bundle.putBoolean("BINVAL", checkBox.isChecked());
                viewQRFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(viewQRFragment,"QR").commit();
            }
        });
        currentMapDisplay = root.findViewById(R.id.add_binomial_trial_map_view);

        parentActivity.currentTrial = new BinomialTrial(parentActivity.loggedUser.getUserId(), false);
        utility = new MapUtility(experiment, currentMapDisplay, getContext(), parentActivity, parentActivity.currentTrial);
        utility.setRevertBack(root.findViewById(R.id.add_binomial_trial_revert_loc_bttn));
        utility.run();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            return;
        }
        String rawQRContent =  data.getStringExtra("QRCONTENTRAW");
        Log.d("ACTIVITYRESULT","val " + data.getStringExtra("QRCONTENTRAW"));
        if(data.getBooleanExtra("IS_QR",true)) {//if is QR
            QRMaker qrMaker = new QRMaker();
            QRCode qrCode;
            try {
                qrCode = qrMaker.decodeQRString(rawQRContent);
                if (qrCode.getType() == QRType.BinomialTrial) {
                    checkBox.setChecked(((BinomialQRCode) qrCode).getValue());
                    Log.d("SCANNER", "Scanned QR Successfully!");
                    Snackbar.make(root, "Scanned QR Successfully!", Snackbar.LENGTH_LONG).show();

                } else {
                    //send error tray message
                    Log.d("SCANNER", "Scanned QR was of incorrect type " + qrCode.getType().toString());
                    Snackbar.make(root, "Scanned QR was of incorrect type", Snackbar.LENGTH_LONG).show();
                }
            } catch (QRMalformattedException qrMalE) {
                //malformatted QR
                qrCode = null;
                Log.d("SCANNER", "Scanned Malformatted QR");
                Snackbar.make(root, "Scanned QR was not an Experiment-Automata QR Code", Snackbar.LENGTH_LONG).show();
            }
        }
        else {//if scanned was barcode
            NavigationActivity parentActivity = ((NavigationActivity) getActivity());
            Location location = parentActivity.currentTrial.getLocation();

            BarcodeManager testBC = parentActivity.barcodeManager;
            BinomialExperiment experiment = (BinomialExperiment) parentActivity.experimentManager.getCurrentExperiment();
            parentActivity.barcodeManager.addBarcode(rawQRContent,experiment.getExperimentId(),checkBox.isChecked(),location);
            Snackbar.make(root, "Scanned Barcode " + rawQRContent + " was associated with this Trials Value", Snackbar.LENGTH_LONG).show();
        }
    }
}