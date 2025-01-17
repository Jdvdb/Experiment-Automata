package com.example.experiment_automata.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.experiment_automata.backend.experiments.Experiment;
import com.example.experiment_automata.ui.experiments.NavExperimentDetailsFragment;
import com.example.experiment_automata.ui.NavigationActivity;
import com.example.experiment_automata.R;
import com.example.experiment_automata.ui.Screen;
import com.example.experiment_automata.ui.experiments.ExperimentListAdapter;


import java.util.ArrayList;
import java.util.UUID;

/**
 * Role/Pattern:
 *     Provides the main view control for when the user first enters the home screen.
 *
 *  Known Issue:
 *
 *      1. None
 */

public class HomeFragment extends Fragment {
    private ArrayList<Experiment<?>> experimentsArrayList;
    private ArrayAdapter<Experiment<?>> experimentArrayAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("HomeFragment", "Entering home fragment: " + getArguments().getString("mode"));
        NavigationActivity parentActivity = ((NavigationActivity) getActivity());

        parentActivity.experimentManager.getAllFromFirestore();
        getActivity().findViewById(R.id.fab_button).setVisibility(View.VISIBLE);

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        parentActivity.setCurrentScreen(Screen.ExperimentList);
        parentActivity.setCurrentFragment(this);
        ListView experimentList = (ListView) root.findViewById(R.id.experiment_list);
        experimentsArrayList = new ArrayList<>();
        populateList();
        experimentArrayAdapter = new ExperimentListAdapter(getActivity(),
                experimentsArrayList, getArguments().getString("mode"), ((NavigationActivity) getActivity()).userManager);
        experimentList.setAdapter(experimentArrayAdapter);

        Bundle bundle = new Bundle();
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        ((ListView) root.findViewById(R.id.experiment_list)).setOnItemClickListener((parent, view, position, id) -> {

            String experimentID  = ((TextView)view.findViewById(R.id.experiment__id)).getText().toString();
            // String values
            bundle.putString(NavExperimentDetailsFragment.CURRENT_EXPERIMENT_ID, experimentID);

            // set current experiment
            Experiment<?> experiment = parentActivity.experimentManager.query(UUID.fromString(experimentID));
            parentActivity.experimentManager.setCurrentExperiment(experiment);

            //nav_experiment_details
            navController.navigate(R.id.nav_experiment_details, bundle);

        });

        return root;
    }

    /**
     * Populate the ArrayList with experiments
     */
    public void populateList() {
        NavigationActivity parentActivity = ((NavigationActivity) getActivity());
        experimentsArrayList.clear();
        Log.d("MODE", getArguments().getString("mode"));
        switch (getArguments().getString("mode")) {
            case "owned":
                experimentsArrayList.addAll(parentActivity.experimentManager
                        .queryExperiments(parentActivity.loggedUser.getOwnedExperiments()));
                break;
            case "published":
                ((NavigationActivity)getActivity()).userManager.getAllUsersFromFireStore();
                experimentsArrayList.addAll(parentActivity.experimentManager
                        .getPublishedExperiments());
                break;
            case "subscribed":
                experimentsArrayList.addAll(parentActivity.experimentManager
                        .queryExperiments(parentActivity.loggedUser.getSubscriptions()));
                break;
            case "search":
                String query = getArguments().getString("query");
                experimentsArrayList.addAll(parentActivity.experimentManager
                        .queryPublishedExperiments(query));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Update the list
     */
    public void updateScreen() {
        populateList();
        experimentArrayAdapter.notifyDataSetChanged();
    }
}