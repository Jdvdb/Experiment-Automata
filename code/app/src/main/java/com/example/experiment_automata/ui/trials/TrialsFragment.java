package com.example.experiment_automata.ui.trials;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.experiment_automata.backend.experiments.BinomialExperiment;
import com.example.experiment_automata.backend.experiments.CountExperiment;
import com.example.experiment_automata.backend.experiments.Experiment;
import com.example.experiment_automata.backend.experiments.MeasurementExperiment;
import com.example.experiment_automata.backend.experiments.NaturalCountExperiment;
import com.example.experiment_automata.ui.NavigationActivity;
import com.example.experiment_automata.R;
import com.example.experiment_automata.backend.trials.Trial;
import com.example.experiment_automata.ui.experiments.NavExperimentDetailsFragment;

import java.util.ArrayList;

/**
 * Role/Pattern:
 *
 *       This fragment class inflates the trial list fragment.
 *       Also contains the trail array adapter.
 *       This fragment contains a list of trials which can be ignored or included
 *       in the stats computation.
 *
 * Known Issue:
 *
 *      1. None
 */

public class TrialsFragment extends Fragment {
    private ArrayList<Trial> trialsArrayList;
    private TrialArrayAdapter trialArrayAdapter;

    public TrialsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_trials, container, false);
        ListView trialList = (ListView) root.findViewById(R.id.trial_list);
        trialsArrayList = new ArrayList<>();
        trialArrayAdapter = new TrialArrayAdapter(getActivity(), trialsArrayList, (NavExperimentDetailsFragment) getParentFragment());
        trialList.setAdapter(trialArrayAdapter);
        return root;
    }

    public void updateView() {
        Experiment<?> experiment = ((NavigationActivity) getActivity()).experimentManager
                .getCurrentExperiment();
        trialsArrayList.clear();
        trialsArrayList.addAll(experiment.getTrials());
    }
}