package com.xormoti.taxi_in_trust;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;

import org.junit.Test;

import java.util.HashMap;

public class FirebaseTest {

    @Test
    public void test(){

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

}
