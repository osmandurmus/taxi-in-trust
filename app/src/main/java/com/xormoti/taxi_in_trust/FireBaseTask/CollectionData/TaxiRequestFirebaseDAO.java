package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class TaxiRequestFirebaseDAO {
    private static FirebaseFirestore db=FirebaseFirestore.getInstance();

    public static void newTaxiCall(Object data, OnSuccessListener<Void> success, OnFailureListener failure){
        try{
            DocumentReference docRef = db.collection("taxi_request").document();
            docRef.set(data)
                    .addOnSuccessListener(success)
                    .addOnFailureListener(failure);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Driver listen coming requests.
     * @param listener
     * @param uId
     */
    public static void listenForRealtimeTaxiRequest(EventListener listener, String uId){
             db.collection("taxi_request")
                        .whereEqualTo("driver_id",uId)
                        .addSnapshotListener(listener);
        }


    public static void updateTaxiRequest(String docId, Map map, OnSuccessListener<Void> success, OnFailureListener failure){
        DocumentReference docRef = db.collection("taxi_request").document(docId);
        docRef
                .update(map)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }

    /**
     * Passengers listen their taxş taxi-request whether it is accepted.
     * When driver accepted, passenger's request.
     * @param listener
     * @param uId
     */
    public static void listenPassengerWaitingTaxiRequest(EventListener listener, String uId){
        db.collection("taxi_request")
                .whereEqualTo("passenger_id",uId)
                .whereEqualTo("status","wait")
                .addSnapshotListener(listener);
    }
    public static void listenAndFollowPersonLocation(EventListener listener, String uId){
        db.collection("person").document(uId).addSnapshotListener(listener);
    }
}
