package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonDAO {

    private static FirebaseFirestore db=FirebaseFirestore.getInstance();
    public static void addPerson(Person_ person){

        db.collection("person").document(String.valueOf(person.getId())).set(person)
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
    public static void getUser(String Uid, OnSuccessListener<DocumentSnapshot> success,OnFailureListener failure){
        DocumentReference docRef = db.collection("person").document(Uid);
        docRef.get().addOnSuccessListener(success).addOnFailureListener(failure);

    }
    public static void updatePersonField(String Uid,String field,Object value,OnSuccessListener<Void> success,OnFailureListener failure){
        DocumentReference docRef = db.collection("person").document(Uid);
        docRef
                .update(field, value)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }
}
