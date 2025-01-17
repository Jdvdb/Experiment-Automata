package com.example.experiment_automata.backend.users;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.experiment_automata.backend.DataBase;
import com.example.experiment_automata.backend.experiments.Experiment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Role/Pattern:
 *     This is the user. Contains the information that represents a user.
 *
 *  Known Issue:
 *
 *      1. None
 */
public class User implements Serializable {
    private static String DEFAULT_UUID_STRING = "00000000-0000-0000-0000-000000000000";//move this to a constants class later
    private static final String TAG = "User";
    private UUID userId;//changed from int to UUID
    private ContactInformation info;
    //    private SearchController controller;
    private Collection<UUID> ownedExperiments;
    private Collection<UUID> subscribedExperiments;
//    private Collection<Experiment> participatingExperiments;

    /**
     * Creates the user. Assigns a user id automatically.
     * @param preferences
     * the ContactInformation object containing the information for the user
     */
    public User(SharedPreferences preferences) {
        this.userId = UUID.fromString(preferences.getString("userId", UUID.randomUUID().toString()));
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userId", userId.toString());
        editor.apply();
        this.info = new ContactInformation(preferences);
        updateExperimentFromFirestore();
        updateFirestore();
    }

    public User(ContactInformation ci, UUID userId)
    {
        this.info = ci;
        this.userId = userId;
    }

    /**
     * Creates a user by querying the firestore.
     * @param id
     *  The id of the user to get the firestore document
     */
    private User(UUID id) {
        this.userId = id;
        updateContactFromFirestore();
    }

    /**
     * This returns a User instance with the id specified.
     * @param id
     *  The id of an existing user on the firestore
     * @return
     *  The user instance with the information queried from the firestore
     */
    public static User getInstance(UUID id) {
        return new User(id);
    }

    /**
     * Update the user information in the Firestore
     */
    public void updateFirestore() {
        // convert collection of UUIDs to collection of Strings
        Collection<String> owned = new ArrayList<>();
        if (this.ownedExperiments != null) {
            for (UUID experimentId : this.ownedExperiments) {
                owned.add(experimentId.toString());
            }
        }
        Collection<String> subscriptions = new ArrayList<>();
        if (this.subscribedExperiments != null) {
            for (UUID experimentId : this.subscribedExperiments) {
                subscriptions.add(experimentId.toString());
            }
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", this.info.getName());
        userInfo.put("email", this.info.getEmail());
        userInfo.put("phone", this.info.getPhone());
        userInfo.put("owned", owned);
        userInfo.put("subscriptions", subscriptions);

        DataBase dataBase = DataBase.getInstance();
        FirebaseFirestore db = dataBase.getFireStore();
        if(!dataBase.isTestMode()) {
            db.collection("users").document(this.userId.toString())
                    .set(userInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "User info successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }
    }

    /**
     * Update the user information from the Firestore.
     */
    protected void updateFromFirestore() {
        DataBase dataBase = DataBase.getInstance();
        FirebaseFirestore db = dataBase.getFireStore();
        this.ownedExperiments = new ArrayList<>();
        this.subscribedExperiments = new ArrayList<>();
        try {
            DocumentReference documentReference = db.collection("users").document(this.userId.toString());
            if (documentReference != null) {
                Task<DocumentSnapshot> task = documentReference.get();
                // wait until the task is complete
                while (!task.isComplete()) ;
                DocumentSnapshot document = task.getResult();
                String name = (String) document.get("name");
                String email = (String) document.get("email");
                String phone = (String) document.get("phone");
                Collection<String> owned = (List<String>) document.get("owned");
                if (owned == null) owned = new ArrayList<>();
                Collection<String> subscribed = (List<String>) document.get("subscriptions");
                if (subscribed == null) subscribed = new ArrayList<>();
                // Convert Collection of String to Collection of UUIDs
                for (String experimentId : owned) {
                    this.ownedExperiments.add(UUID.fromString(experimentId));
                }
                for (String experimentId : subscribed) {
                    this.subscribedExperiments.add(UUID.fromString(experimentId));
                }

                this.info = new ContactInformation(name, email, phone);
            }
        }
        catch (Exception e) {}
    }

    /**
     * Update the user experiments from the Firestore.
     */
    protected void updateExperimentFromFirestore() {
        DataBase dataBase = DataBase.getInstance();
        FirebaseFirestore db = dataBase.getFireStore();
        this.ownedExperiments = new ArrayList<>();
        this.subscribedExperiments = new ArrayList<>();
        try {
            DocumentReference documentReference = db.collection("users").document(this.userId.toString());
            Task<DocumentSnapshot> task = documentReference.get();
            // wait until the task is complete
            while (!task.isComplete()) ;
            DocumentSnapshot document = task.getResult();
            Collection<String> ownedExperiments = (List<String>) document.get("owned");
            if (ownedExperiments == null) ownedExperiments = new ArrayList<>();
            Collection<String> subscribedExperiments = (List<String>) document.get("subscriptions");
            if (subscribedExperiments == null) subscribedExperiments = new ArrayList<>();
            // Convert Collection of String to Collection of UUIDs
            for (String experimentId : ownedExperiments) {
                this.ownedExperiments.add(UUID.fromString(experimentId));
            }
            for (String experimentId : subscribedExperiments) {
                this.subscribedExperiments.add(UUID.fromString(experimentId));
            }
        }
        catch (Exception e) {}
    }

    /**
     * Update the user contact information from the Firestore.
     */
    protected void updateContactFromFirestore() {
        DataBase dataBase = DataBase.getInstance();
        FirebaseFirestore db = dataBase.getFireStore();
        try {
            DocumentReference documentReference = db.collection("users").document(this.userId.toString());
            Task<DocumentSnapshot> task = documentReference.get();
            // wait until the task is complete
            while (!task.isComplete()) ;
            DocumentSnapshot document = task.getResult();
            String name = (String) document.get("name");
            String email = (String) document.get("email");
            String phone = (String) document.get("phone");
            this.info = new ContactInformation(name, email, phone);
        }catch (Exception e)
        {}
    }

    /**
     * Get the user's id
     * @return
     *  The id
     */
    public UUID getUserId()
    {
        return this.userId;
    }

    /**
     * Get the user's information
     * @return
     * The user's contact information object
     */
    public ContactInformation getInfo() {
        return this.info;
    }

    /**
     * Get a collection of all the owned experiment IDs.
     * @return
     *  The IDs of owned experiments
     */
    public Collection<UUID> getOwnedExperiments() { return ownedExperiments; }

    /**
     * Get a collection of the experiment IDs the user is subscribed to.
     * @return
     *  The IDs of experiments
     */
    public Collection<UUID> getSubscriptions() { return subscribedExperiments; }

    /**
     * Add the experiment reference to the owned experiments
     * @param experimentId
     *  the UUID of the experiment
     */
    public void addExperiment(UUID experimentId) {
        this.ownedExperiments.add(experimentId);
        updateFirestore();
    }

    /**
     * Adds/removes the experiment reference to the subscribed experiments.
     * Also updates the firestore.
     * If already subscribed, unsubscribes
     * If not subscribed, subscribes
     * @param experimentId
     *  the UUID of the experiment
     */
    public void subscribeExperiment(UUID experimentId) {
        if (subscribedExperiments.contains(experimentId)) {
            subscribedExperiments.remove(experimentId);
        } else {
            subscribedExperiments.add(experimentId);
        }
        updateFirestore();
    }

    /**
     * set the subscribed experiments
     * @param subs the new subs
     */
    public void setSubscribedExperiments(Collection<UUID> subs)
    {
        if(subs == null)
            return;
        this.subscribedExperiments.clear();
        this.subscribedExperiments.addAll(subs);

    }

    /**
     * sets the owned experiments
     * @param owned the new owned experiments
     */
    public void setOwnedExperiments(Collection<UUID> owned)
    {
        if(owned == null)
            return;
        this.ownedExperiments.clear();
        this.ownedExperiments.addAll(owned);
    }

    /**
     * the the contact information
     * @param info the new contact infromation
     */
    public void setContactInformation(ContactInformation info)
    {
        this.info = info;
    }
}
