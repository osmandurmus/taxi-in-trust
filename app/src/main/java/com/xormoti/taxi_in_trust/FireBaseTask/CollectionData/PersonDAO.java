package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonDAO {

    private static FirebaseFirestore db;
    public static void addPerson(Person_ person){
        db=FirebaseFirestore.getInstance();
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
}
