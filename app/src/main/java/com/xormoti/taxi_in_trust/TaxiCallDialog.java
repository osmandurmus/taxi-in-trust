package com.xormoti.taxi_in_trust;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.TaxiRequestFirebaseDAO;
import com.xormoti.taxi_in_trust.databinding.DialogTaxiCallBinding;

import java.util.HashMap;

public class TaxiCallDialog extends Dialog {

    DialogTaxiCallBinding binding;
    String driverId;  String requestStatus;  LatLng driverLatLng; double lat, lng;
    int score;
    String fullName;
    Context context;
    String uId;
    SharedPreferences sharedPreferences;
    public TaxiCallDialog(@NonNull Context context,String driverId,String requestStatus, LatLng driverLatLng,double lat,double lng,int score,String fullName,String uId,SharedPreferences sharedPreferences) {
        super(context);
        this.driverId=driverId;
        this.requestStatus=requestStatus;
        this.driverLatLng=driverLatLng;
        this.lat=lat;
        this.lng=lng;
        this.score=score;
        this.fullName=fullName;
        this.context=context;
        this.uId=uId;
        this.sharedPreferences=sharedPreferences;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        setTitle("TAKSİ ÇAĞIR");
        binding= DialogTaxiCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ratingBarSurucu.setNumStars(5);
        binding.ratingBarSurucu.setRating(score);
        binding.ratingBarSurucu.setEnabled(false);
        binding.surucuName.setText(fullName);

        binding.taxiCagir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cagir();
            }
        });

        binding.taxiIptal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iptal();
            }
        });

    }

    void cagir(){
        OnSuccessListener successListener=new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                dismiss();
            }
        };

        OnFailureListener failureListener=new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"ÇAĞRI BAŞARISIZ",Toast.LENGTH_LONG).show();
            }
        };
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("driver_id",driverId);
        hashMap.put("passenger_id",uId);
        hashMap.put("status",requestStatus);

        HashMap<String,Double> passengerLocation=new HashMap<>();
        passengerLocation.put("latitude",lat);
        passengerLocation.put("longitude",lng);

        HashMap<String,Double> driverLocation=new HashMap<>();
        driverLocation.put("latitude",driverLatLng.getLatitude());
        driverLocation.put("longitude",driverLatLng.getLongitude());

        hashMap.put("passenger_location",passengerLocation);
        hashMap.put("driver_location",driverLocation);
        hashMap.put("passenger_name",sharedPreferences.getString("full_name","İsim bilinmiyor."));
        hashMap.put("driver_name",fullName);


        TaxiRequestFirebaseDAO.newTaxiCall(hashMap,successListener,failureListener);
    }
    void iptal(){
        dismiss();
    }

}
