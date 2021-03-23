package com.example.experiment_automata.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.experiment_automata.R;
import com.example.experiment_automata.backend.experiments.ExperimentType;
import com.example.experiment_automata.backend.trials.Trial;
import com.example.experiment_automata.ui.experiments.AddExperimentFragment;
import com.example.experiment_automata.backend.experiments.BinomialExperiment;
import com.example.experiment_automata.backend.experiments.CountExperiment;
import com.example.experiment_automata.backend.experiments.ExperimentManager;
import com.example.experiment_automata.ui.experiments.NavExperimentDetailsFragment;
import com.example.experiment_automata.backend.experiments.Experiment;

import com.example.experiment_automata.ui.question.AddQuestionFragment;
import com.example.experiment_automata.ui.question.QuestionDisplay;
import com.example.experiment_automata.backend.questions.Question;
import com.example.experiment_automata.backend.questions.QuestionManager;
import com.example.experiment_automata.backend.questions.Reply;

import com.example.experiment_automata.backend.experiments.MeasurementExperiment;
import com.example.experiment_automata.backend.experiments.NaturalCountExperiment;

import com.example.experiment_automata.backend.users.User;
import com.example.experiment_automata.backend.trials.BinomialTrial;
import com.example.experiment_automata.backend.trials.CountTrial;
import com.example.experiment_automata.backend.trials.MeasurementTrial;
import com.example.experiment_automata.backend.trials.NaturalCountTrial;
import com.example.experiment_automata.ui.Screen;
import com.example.experiment_automata.ui.home.HomeFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Role/Pattern:
 *     The main launch activity where everything is being held and maintained. 
 *
 *  Known Issue:
 *
 *      1. When searching for an item, the label at the top stays as whatever the current
 *         screen is. Should update to 'published' while still in search fragment
 *      2. Search must be cleared each time or a bug appears where 2 searches are performed.
 */

public class NavigationActivity extends AppCompatActivity implements
        AddExperimentFragment.OnFragmentInteractionListener,
        AddQuestionFragment.OnFragmentInteractionListener {

    private AppBarConfiguration mAppBarConfiguration;
    public final ExperimentManager experimentManager = new ExperimentManager();
    public QuestionManager questionManager = QuestionManager.getInstance();

    private Screen currentScreen;
    public Fragment currentFragment;
    public User loggedUser;
    public Experiment currentExperiment;

    // Location and Map Flags and Request Codes
    public static final int LOCATION_PERMISSION_REQUEST = 10;
    private boolean canMakeLocationTrials = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    ArrayList<Trial> trials = new ArrayList<>();

    /**
     * Method called when creating NavigationActivity
     * @param savedInstanceState
     *  A bundle with stored information if required
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SharedPreferences preferences = getSharedPreferences("experiment_automata", MODE_PRIVATE);
        loggedUser = new User(preferences);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_owned_experiments, R.id.nav_subscriptions, R.id.nav_published, R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        FloatingActionButton addExperimentButton = findViewById(R.id.add_experiment_button);
        addExperimentButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Deal with the FAB when clicked
             * @param view
             *  The current view being used
             */
            @Override
            public void onClick(View view) {
                switch (currentScreen) {
                    case ExperimentList:
                        new AddExperimentFragment().show(getSupportFragmentManager(), "ADD_EXPERIMENT");
                        break;
                    case ExperimentDetails:
                        switch (experimentManager.getCurrentExperiment().getType()) {
                            case Count:
                                navController.navigate(R.id.nav_add_count_trial);
                                break;
                            case NaturalCount:
                                navController.navigate(R.id.nav_add_natural_count_trial);
                                break;
                            case Binomial:
                                navController.navigate(R.id.nav_add_binomial_trial);
                                break;
                            case Measurement:
                                navController.navigate(R.id.nav_add_measurement_trial);
                                break;
                        }
                        currentScreen = Screen.Trial;
                        break;
                    case Trial:
                        Experiment experiment = experimentManager.getCurrentExperiment();
                        Snackbar snackbar = Snackbar.make(view, "No value was given", Snackbar.LENGTH_SHORT);
                        try {
                            switch (experiment.getType()) {
                                case Count:
                                    CountExperiment countExperiment = (CountExperiment) experiment;
                                    CountTrial countTrial = new CountTrial(loggedUser.getUserId());
                                    addTrial(countExperiment, countTrial);
                                    break;
                                case NaturalCount:
                                    NaturalCountExperiment naturalCountExperiment = (NaturalCountExperiment) experiment;
                                    // get value
                                    EditText naturalCountInput = (EditText) findViewById(R.id.add_natural_count_value);
                                    final int naturalCount = Integer.parseInt(naturalCountInput.getText().toString());
                                    NaturalCountTrial naturalCountTrial = new NaturalCountTrial(loggedUser.getUserId(), naturalCount);
                                    addTrial(naturalCountExperiment, naturalCountTrial);
                                    break;
                                case Binomial:
                                    BinomialExperiment binomialExperiment = (BinomialExperiment) experiment;
                                    // get value
                                    CheckBox passedInput = (CheckBox) findViewById(R.id.add_binomial_value);
                                    final boolean passed = passedInput.isChecked();
                                    BinomialTrial binomialTrial = new BinomialTrial(loggedUser.getUserId(), passed);
                                    addTrial(binomialExperiment, binomialTrial);
                                    break;
                                case Measurement:
                                    MeasurementExperiment measurementExperiment = (MeasurementExperiment) experiment;
                                    // get value
                                    EditText measurementInput = (EditText) findViewById(R.id.add_measurement_value);
                                    final float measurement = Float.parseFloat(measurementInput.getText().toString());
                                    MeasurementTrial measurementTrial = new MeasurementTrial(loggedUser.getUserId(), measurement);
                                    addTrial(measurementExperiment, measurementTrial);
                                    break;
                            }
                            currentScreen = Screen.ExperimentDetails;
                            navController.navigateUp();
                        } catch (NumberFormatException ignored) {
                            // if no value was given
                            snackbar.show();
                        }
                        break;
                    case Questions:
                        ((QuestionDisplay) currentFragment).makeQuestion();
                        break;
                }
            }
        });
    }

    /**
     * Prepare search bar functionality
     * @param menu
     *  A menu to help create the object
     * @return
     *  A boolean based on success of creation
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);

        // https://developer.android.com/training/search/setup#create-sc
        // Associate searchable
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.search_hint));
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Called when the user submits the query.
             * @param query the query text that is to be submitted
             * @return true if the query has been handled by the listener, false to let the
             * SearchView perform the default action.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                navController.navigate(R.id.nav_search, bundle);
                searchView.setQuery("", false);
                return false;
            }

            /**
             * Called when the query text is changed by the user.
             * @param newText the new content of the query text field.
             * @return false if the SearchView should perform the default action of showing any
             * suggestions if available, true if the action was handled by the listener.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    /**
     * Deals with when a user navigates up in the application
     * @return
     * A boolean based on the success of the operation
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Tells the application how to respond when OK is pressed in experiment creation
     * @param experiment
     *  Experiment to be added to the experiment manager
     */
    @Override
    public void onOkPressed(Experiment experiment) {

        boolean added = false;
        while (!added) {
            try {
                experimentManager.add(experiment.getExperimentId(), experiment);
                added = true;
            } catch (IllegalArgumentException badUUID) {
                experiment.makeNewUUID();
            }
        }

        loggedUser.addExperiment(experiment.getExperimentId());
        if (currentScreen == Screen.ExperimentList) {
            ((HomeFragment) currentFragment).updateScreen();
        }
    }

    /**
     * Edits an existing experiment with the information given
     * @param experimentDescription
     *  New description of the experiment
     * @param experimentTrials
     *  New minimum trials for this experiment
     * @param experimentLocation
     *  Boolean for if this experiment requires a location
     * @param experimentNewResults
     *  Boolean for if this experiment accepts new results
     * @param currentExperiment
     *  The current experiment being modified
     */
    @Override
    // todo: this functionality should be moved into something else in the future
    public void onOKPressedEdit(String experimentDescription, int experimentTrials,
                                boolean experimentLocation, boolean experimentNewResults,
                                Experiment currentExperiment) {
        currentExperiment.setDescription(experimentDescription);
        currentExperiment.setMinTrials(experimentTrials);
        currentExperiment.setRequireLocation(experimentLocation);
        currentExperiment.setActive(experimentNewResults);
        ((NavExperimentDetailsFragment) currentFragment).updateScreen();
    }

    /**
     * Sets the current screen variable
     * @param currentScreen
     *  The screen to set as currentScreen
     */
    public void setCurrentScreen(Screen currentScreen) {
        this.currentScreen = currentScreen;
    }

    /**
     * Sets the current fragment variable
     * @param currentFragment
     *  The fragment to set as currentFragment
     */
    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
    }

    /**
     * Gives the singleton Experiment Manager used throughout
     * @return
     */
    public ExperimentManager getExperimentManager() {
        return experimentManager;
    }

    /**
     * Creates a question from QuestionDialog and adds it to QuestionManager
     * @param question
     *  The message of the question
     * @param experimentId
     *  The ID of the experiment it's related to.
     */
    @Override
    public void onOkPressedQuestion(String question, UUID experimentId) {
        Question newQuestion = new Question(question, loggedUser.getUserId(), experimentId);
        Log.d("question id", newQuestion.getExperimentId().toString());
        questionManager.addQuestion(experimentId, newQuestion);

        ((QuestionDisplay) currentFragment).updateQuestionsList();

        Log.d("current screen", currentScreen + "");
    }

    /**
     * Creates a reply from the QuestionDialog and adds it to QuestionManager
     * @param reply
     *  The message of the reply
     * @param questionId
     *  The ID of the associated Question
     */
    @Override
    public void onOkPressedReply(String reply, UUID questionId) {
        Reply newReply = new Reply(reply, questionId);
        questionManager.addReply(questionId, newReply);

        ((QuestionDisplay) currentFragment).updateQuestionsList();
        Log.d("Reply is ", reply);
        Log.d("current screen", currentScreen + "");
    }

    /**
     * records a trial to an experiment while also dealing with location sensitive data.
     * If we encounter an error the trial is not recorded and an error is prompted.
     * @param experiment the experiment we want to add the trial
     * @param trial the trial we wish to add to the experiment
     */
    public void addTrial(Experiment experiment,  Trial trial)
    {
        if (experiment.isRequireLocation())
        {
            addLocationToTrial(trial);
            if(trial.getLocation() != null) {
                experiment.recordTrial(trial);
                Log.d("Recorded Location is ->", trial.getLocation().getLatitude() + "");
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Error: Trial recorded due to error: try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
        else {
            experiment.recordTrial(trial);
        }
        trials.add(trial);
    }

    /**
     * Gets the current location that the user is in with 100 ms in wait time from the device
     * then marks that trial with a location.
     *
     * @param currentTrial the trial we want to add to an experiment
     *
     *
     * Source/Citation:
     *        1.
     *          Author: https://stackoverflow.com/users/1371853/swiftboy
     *          Editor: https://stackoverflow.com/users/202311/ianb
     *          Full Source: https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android
     */
    @SuppressLint("MissingPermission")
    public void addLocationToTrial(Trial currentTrial) {
        requestLocationPermissions();
        if(canMakeLocationTrials)
        {
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new com.example.experiment_automata.backend.Location.LocationServices();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 30, locationListener);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else {
            currentLocation = null;
        }
        currentTrial.setLocation(currentLocation);
    }

    /**
     * Requests permissions from the device if the user does not currently allow for us to use the
     * current users location.
     *
     * Source/Citation:
     *      1.
     *          Author: Alphabet LLC
     *          Editor: Alphabet LLC
     *          Full Source: https://developer.android.com/training/permissions/requesting#java
     */
    public void requestLocationPermissions()
    {
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
        else
            canMakeLocationTrials = true; 
    }

    /**
     * Deals with the response to the user denying or allowing us to use the needed resources
     * to complete needed tasks.
     *
     * @param requestCode The code the we set for the needed resource
     * @param permissions What resource we're asking the system give us access to.
     * @param grantResults The result from the system
     *
     *Source/Citation:
     *      1.
     *          Author: Alphabet LLC
     *          Editor: Alphabet LLC
     *          Full Source: https://developer.android.com/training/permissions/requesting#java
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    canMakeLocationTrials = true;
                return;

        }
    }
}