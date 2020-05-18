package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class PersonFirebaseDAOTest {

    @Test
    public void addPerson() {
          FirebaseFirestore db=FirebaseFirestore.getInstance();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("fullName","Mehmet Yazıcı");
        hashMap.put("driver",true);
        hashMap.put("passenger",false);

        HashMap<String,Double> location=new HashMap<>();
        location.put("latitude",32.8);
        location.put("longitude",32.9);
        hashMap.put("location",location);

        OnSuccessListener successListener=new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                System.out.println("success");
            }
        };
        OnFailureListener failureListener=new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("failure");
            }
        };


        PersonFirebaseDAO.addPerson(hashMap,successListener,failureListener);
    }

    @Test
    public void getUser() {
    }

    @Test
    public void updatePersonField() {
    }

    @Test
    public void testAddPerson() {
    }

    @Test
    public void listenForRealtimePersonLocations() {
    }
}