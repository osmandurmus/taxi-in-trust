package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.xormoti.taxi_in_trust.Fragments.LoginFragment;

import java.util.Map;

public class PersonFirebaseDAO {

    private static FirebaseFirestore db=FirebaseFirestore.getInstance();

    public static ListenerRegistration getListenerForAllPassengersLocation() {
        return listenerForAllPassengersLocation;
    }

    private static ListenerRegistration listenerForAllPassengersLocation;
    public static void updatePerson(Person_ person, OnSuccessListener success, OnFailureListener failure){

        db.collection("person").document(String.valueOf(person.getId())).set(person)
        .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }
    public static void getUser(String Uid, OnSuccessListener<DocumentSnapshot> success,OnFailureListener failure){
        DocumentReference docRef = db.collection("person").document(Uid);
        docRef.get().addOnSuccessListener(success).addOnFailureListener(failure);
    }
    public static void updateUserFields(String uId, Map map, OnSuccessListener<Void> success, OnFailureListener failure){
        DocumentReference docRef = db.collection("person").document(uId);
        docRef
                .update(map)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }


    public static void updatePersonField(String Uid,String field,Object value,OnSuccessListener<Void> success,OnFailureListener failure){
        DocumentReference docRef = db.collection("person").document(Uid);
        docRef
                .update(field, value)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }
    public static void addNewPerson(Object data, OnSuccessListener<Void> success, OnFailureListener failure){
       try{
           DocumentReference docRef = db.collection("person").document();
           docRef.set(data)
                   .addOnSuccessListener(success)
                   .addOnFailureListener(failure);
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public static void listenForRealtimeDriverLocations(EventListener listener, Context context){
        SharedPreferences sharedPreferences;
        sharedPreferences=context.getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE);
        String state=sharedPreferences.getString("state",null);
        switch (state){

            case "driver": // driver ise passengerlar gelecek. çünkü ulaşım için driver passenger arar, passenger driver arar.
                db.collection("person")
                        .whereEqualTo("passenger",true)
                        .addSnapshotListener(listener);
                break;
            case "passenger":
                listenerForAllPassengersLocation= db.collection("person")
                        .whereEqualTo("driver",true)
                        .whereEqualTo("location.active",true)
                        .addSnapshotListener(listener);
                break;
            default:
                break;

        }
    }
}
