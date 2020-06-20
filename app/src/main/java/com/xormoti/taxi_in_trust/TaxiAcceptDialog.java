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
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.TaxiRequestFirebaseDAO;
import com.xormoti.taxi_in_trust.databinding.DialogTaxiAcceptBinding;

import java.util.HashMap;

public class TaxiAcceptDialog extends Dialog {
    String docId;
    DialogTaxiAcceptBinding binding;
    DriverFlow driverFlow;
    SharedPreferences sharedPreferences;
    Context context;
    public TaxiAcceptDialog(@NonNull Context context, String pDocId, DriverFlow driverFlow, SharedPreferences sharedPreferences) {
        super(context);
        setCanceledOnTouchOutside(false);
        this.docId=pDocId;
        this.driverFlow=driverFlow;
        this.sharedPreferences=sharedPreferences;
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DialogTaxiAcceptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.kabul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap<String,String> paramas=new HashMap<>();
                paramas.put("status", "accept");
                paramas.put("driver_name",sharedPreferences.getString("full_name","İsim bilinmiyor."));


                OnSuccessListener successListener=new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        dismiss();
                        Toast.makeText(context,"KABUL EDİLDİ",Toast.LENGTH_LONG).show();
                        driverFlow.isExistPassenger();
                    }
                };
                OnFailureListener failureListener=new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"BAŞARISIZ",Toast.LENGTH_SHORT).show();
                    }
                };
                TaxiRequestFirebaseDAO.updateTaxiRequest(docId,paramas,successListener,failureListener);


            }
        });

        binding.reddet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,String> paramas=new HashMap<>();
                paramas.put("status", "cancel");
                paramas.put("driver_name",sharedPreferences.getString("full_name","İsim bilinmiyor."));

                OnSuccessListener successListener=new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(context,"REDDEDİLDİ",Toast.LENGTH_LONG).show();
                        dismiss();

                    }
                };
                OnFailureListener failureListener=new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"BAŞARISIZ",Toast.LENGTH_SHORT).show();

                    }
                };
                TaxiRequestFirebaseDAO.updateTaxiRequest(docId,paramas,successListener,failureListener);

            }
        });

    }
}
