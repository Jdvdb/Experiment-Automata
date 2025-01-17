package com.example.experiment_automata.backend.experiments;

import androidx.annotation.NonNull;

import com.example.experiment_automata.backend.DataBase;
import com.example.experiment_automata.backend.trials.Trial;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Role/Pattern:
 *       Base experiment class: the building blocks of all experiment types
 *
 * Known Issue:
 *
 *      1. None
 */
public abstract class Experiment<T extends Trial<?>> implements Serializable, StatSummary, Graphable, Comparable {
    private String description;
    private int minTrials;
    private UUID experimentId; // changed from UML to better match project
    private UUID ownerId; // // changed from UML to better match project
    protected boolean active; // changed from UML for style
    private boolean published; // changed from UML for style
    private boolean requireLocation; // added to align with storyboard
    private ExperimentType type; // todo: do we need type here if an experiment has a type? (yes makes it easy)
    protected Collection<T> results;

    /**
     * Experiment constructor to be used by ExperimentMaker when creating an experiment from the AddExperimentFragment
     * @param description
     *   the description of the experiment
     * @param minTrials
     *   the minimum number of trials for this experiment
     * @param requireLocation
     *   a boolean for whether or not the trials need a location
     * @param acceptNewResults
     *   a boolean for whether this trial should be accepting new requests or not
     */
    protected Experiment(String description, int minTrials, boolean requireLocation, boolean acceptNewResults, UUID ownerId, ExperimentType type) {
        this.description = description;
        this.minTrials = minTrials;
        this.requireLocation = requireLocation;
        this.published = false;
        this.active = acceptNewResults;
        this.ownerId = ownerId;
        this.experimentId = UUID.randomUUID();
        this.type = type;
        results = new ArrayList<>();
        postExperimentToFirestore();
    }

    /**
     * Experiment constructor to be used by ExperimentMaker when creating an experiment from the Firestore
     * @param description
     *   the description of the experiment
     * @param minTrials
     *   the minimum number of trials for this experiment
     * @param requireLocation
     *   a boolean for whether or not the trials need a location
     * @param acceptNewResults
     *   a boolean for whether this trial should be accepting new requests or not
     */
    protected Experiment(String description, int minTrials, boolean requireLocation, boolean acceptNewResults, UUID ownerId, ExperimentType type, Boolean published, UUID experimentId) {//MAKE SURE WE ADD QUESTIONS TO THIS CONSTRUCTOR
        this.description = description;
        this.minTrials = minTrials;
        this.requireLocation = requireLocation;
        this.published = published;
        this.active = acceptNewResults;
        this.ownerId = ownerId;
        this.experimentId = experimentId;
        this.type = type;
        results = new ArrayList<>();
    }

    /**
     * This method will check if an experiment has the same id as another
     * @param experiment
     *   The experiment you want to compare with
     * @return
     *   A boolean based on whether the ids are the same
     */
    public boolean compare(Experiment<T> experiment) {
        return experimentId.equals(experiment.experimentId);
    }

    /**
     * Post the current experiment to firestore
     */
    public void postExperimentToFirestore(){
        //add key field?
        DataBase dataBase = DataBase.getInstance();
        Experiment<T> experiment = this;
        FirebaseFirestore db = dataBase.getFireStore();
        Map<String, Object> experimentData = new HashMap<>();
        String experimentUUIDString = experiment.getExperimentId().toString();
        HashMap<String, Object> resultsData = buildResultsmap();

        experimentData.put("accepting-new-results", experiment.isActive());
        experimentData.put("description", experiment.getDescription());
        experimentData.put("location-required", experiment.isRequireLocation());
        experimentData.put("min-trials", experiment.getMinTrials());
        experimentData.put("owner", experiment.getOwnerId().toString());
        experimentData.put("type", experiment.getType().toString());//enum to string
        experimentData.put("published", experiment.isPublished());
        experimentData.put("results", resultsData);

        if(!dataBase.isTestMode()) {
            db.collection("experiments").document(experimentUUIDString)
                    .set(experimentData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    /**
     * Gets the description of an experiment
     * @return String with the description of an experiment
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the owner of an experiment
     * @return UUID with the owner of an experiment
     */
    public UUID getOwnerId() {
        return ownerId;
    }

    /**
     * Gets the id of the experiment
     * @return UUID of the experiment
     */
    public UUID getExperimentId() {
        return experimentId;
    }

    /**
     * Activity status of experiment
     * @return Boolean with whether the experiment is active or not
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Published status of the experiment
     * @return Boolean whether the experiment is published or not
     */
    public boolean isPublished() { return published; }

    /**
     * Set published status of the experiment
     * @param p
     *  Boolean whether the experiment is published or not
     */
    public void setPublished(boolean p) {
        published = p;
        postExperimentToFirestore();
    }

    /**
     * set the description of experiment
     * @param description value to be set
     */
    public void setDescription(String description) {
        this.description = description;
        postExperimentToFirestore();
    }

    /**
     * sets the min trials for the experiment
     * @param minTrials value to be set
     */
    public void setMinTrials(int minTrials) {
        this.minTrials = minTrials;
        postExperimentToFirestore();
    }

    /**
     * sets the activity for each of the experiments
     * @param active value to be set
     */
    public void setActive(boolean active) {

        this.active = active;
        postExperimentToFirestore();
    }

    /**
     * sets if we need a location
     * @param requireLocation value to be set
     */
    public void setRequireLocation(boolean requireLocation) {
        this.requireLocation = requireLocation;
        postExperimentToFirestore();
    }

    /**
     * get the current experiments set type
     * @return
     *  current experiments type
     */
    public ExperimentType getType() {
        return type;
    }

    /**
     * get the min trials
     * @return the int min trial value
     */
    public Integer getMinTrials() {
        return minTrials;
    }

    /**
     * check if a location is required by the experiment
     * @return the result
     */
    public boolean isRequireLocation() {
        return requireLocation;
    }

    /**
     * Makes a new uuid if the one generated is in use by the system.
     */
    public void makeNewUUID() {
        this.experimentId = UUID.randomUUID();
    }

    /**
     * Gets the size of the trials. Does not include ignored trials.
     * @return size of the trials
     */
    public Integer getSize(){
        int size = 0;
        for (T trial : results) {
            if (!trial.isIgnored()) {
                size++;
            }
        }
        return size;
    }

    /**
     * Add a trial to an experiment.
     * @param trial the trial to add
     */
    public void recordTrial(T trial) {
        if (active) {
            results.add(trial);
            postExperimentToFirestore();
        } else {
            throw new IllegalStateException("Experiment is not accepting new results.");
        }
    }

    /**
     * Add a trial to an experiment.
     * @param trial the trial to add
     * @param fromFirestore whether to pull the trial from the firestore or not
     */
    public void recordTrial(Trial<?> trial, @NotNull Boolean fromFirestore) {
        if (fromFirestore) results.add((T) trial);
    }

    /**
     * Get all the recorded trials for an experiment.
     * @return the recorded trials
     */
    public ArrayList<T> getRecordedTrials() {
        return new ArrayList<>(results);
    }

    /**
     * Build hashmap for results
     */
    public HashMap<String,Object> buildResultsmap(){
        HashMap<String,Object> resultsData = new HashMap<String, Object>();
        if (results == null) {
            return resultsData;
        }
        for(T trial : results) {
            HashMap<String,Object> singleResult = new HashMap<String, Object>();
            singleResult.put("owner-id",trial.getUserId().toString());
            if (trial.getLocation() != null){//maybe move to a method in superclass
                singleResult.put("latitude",trial.getLocation().getLatitude());
                singleResult.put("longitude",trial.getLocation().getLongitude());
            }
            singleResult.put("date",trial.getDate().toString());
            singleResult.put("ignore",trial.isIgnored());
            singleResult.put("result",trial.getResult());
            resultsData.put(trial.getTrialId().toString(),singleResult);
        }
        return resultsData;
    }

    /**
     * Compares experiments to give them some sort of order in this
     * case it is a lexicographical order.
     * @param o the other experiment
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    public int compareTo(Object o) {
        Experiment<?> ec = (Experiment<?>) o;
        return this.getDescription().toLowerCase().compareTo(ec.getDescription().toLowerCase());
    }

    /**
     * Get the trials made about this experiment.
     * @return the trials
     */
    public Collection<T> getTrials() { return results; }
}
