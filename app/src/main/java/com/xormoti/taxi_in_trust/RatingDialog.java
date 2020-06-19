package com.xormoti.taxi_in_trust;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.TaxiRequestFirebaseDAO;
import com.xormoti.taxi_in_trust.databinding.RatingbarLayoutBinding;

import java.util.HashMap;
import java.util.Map;

public class RatingDialog extends Dialog {

    RatingbarLayoutBinding binding;
    ArrayAdapter<String> arrayAdapter;
    int rate=3;
    int driverOldScore =-1;
    String documentID;
    String pDocumentIDList;
    Map<String,Object> documentMap;
    public RatingDialog(@NonNull Context context, ArrayAdapter<String> parrayAdapter, Map<String,Object> pdocumentMap,String pDocumentID,String pDocumentIdList) {
        super(context);
        this.arrayAdapter=parrayAdapter;
        documentMap=pdocumentMap;
        this.documentID=pDocumentID;
        this.pDocumentIDList=pDocumentIdList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=RatingbarLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.puanla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String driverId= ((String)documentMap.get("driver_id"));

                    int newScore=(rate+ driverOldScore)/2;
                    Map<String,Integer> map=new HashMap<>();
                    map.put("score",newScore);

                    OnSuccessListener successListener=new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {

                            OnSuccessListener successListener=new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                   try {
                                       Toast.makeText(getContext(),"Score Güncellendi.",Toast.LENGTH_LONG).show();
                                       arrayAdapter.remove(arrayAdapter.getItem(arrayAdapter.getPosition(pDocumentIDList)));
                                       arrayAdapter.notifyDataSetChanged();
                                       dismiss();
                                   }
                                   catch (Exception e){
                                       Toast.makeText(getContext(),e.toString(),Toast.LENGTH_LONG).show();

                                   }
                                }
                            };
                            OnFailureListener failureListener=new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(),"Failure:deleteTaxiRequest",Toast.LENGTH_LONG).show();

                                }
                            };
                            TaxiRequestFirebaseDAO.deleteTaxiRequest(documentID,successListener,failureListener);
                        }
                    };
                    OnFailureListener failureListener=new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(),"Güncellenemedi",Toast.LENGTH_LONG).show();
                        }
                    };

                    PersonFirebaseDAO.updateUserFields(driverId,map,successListener,failureListener);
                }
                catch (Exception e){
                    Toast.makeText(getContext(),e.toString(),Toast.LENGTH_LONG).show();
                }

            }
        });
        binding.vagec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        binding.ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rate=(int) rating;
            }
        });
            OnSuccessListener<DocumentSnapshot> success=new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
               try {
                    Object o= documentSnapshot.get("score");
                    if (o==null)
                        driverOldScore =0;
                    else
                        driverOldScore =((Long)o).intValue();

               }
               catch (Exception e){
                   e.printStackTrace();
               }

            }};
            OnFailureListener failure=new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                RatingDialog.this.dismiss();
            }
            };
           PersonFirebaseDAO.getUser(((String)documentMap.get("driver_id")),success,failure);

    }
}
