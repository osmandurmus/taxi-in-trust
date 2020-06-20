package com.xormoti.taxi_in_trust;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.TaxiRequestFirebaseDAO;
import com.xormoti.taxi_in_trust.Fragments.LoginFragment;

import java.util.HashMap;
import java.util.Map;

public class DriverFlow {

    private Icon iconDriver;
    private Icon iconPassenger;
    private Context context;
    private MapboxMap map;
    private String uId;
    SharedPreferences sharedPreferences;
    TaxiAcceptDialog acceptDialog;

    DriverFlow(Context context, MapboxMap map, String uId){
        this.context=context;
        this.map=map;
        IconFactory iconFactory = IconFactory.getInstance(context);
        iconDriver = iconFactory.fromResource(R.mipmap.ic_taxi);
        iconPassenger = iconFactory.fromResource(R.mipmap.ic_passenger);
        sharedPreferences= context.getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE);
        this.uId=uId;
    }
    public void start(){
        ((AppCompatActivity)context).getSupportActionBar().setTitle("Sürücü Harita");
        isExistPassenger();
        listenForRealtimeTaxiRequest();
    }
    void listenForRealtimeTaxiRequest(){
        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        return;
                    }
                    for (final QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        if (acceptDialog!=null && acceptDialog.isShowing()){
                            Toast.makeText(context,"BAŞARILI",Toast.LENGTH_LONG).show();
                            return;
                        }

                        acceptDialog=new TaxiAcceptDialog(context,doc.getId(),DriverFlow.this,sharedPreferences);
                        acceptDialog.show();

                        /*AlertDialog.Builder builder=new AlertDialog.Builder(context);
                        builder.setTitle("TAXİ İSTEĞİ");
                        builder.setMessage("Yeni bir taksi isteği geldi.");
                        builder.setPositiveButton("KABUL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {

                                HashMap<String,String> paramas=new HashMap<>();
                                paramas.put("status", "accept");
                                paramas.put("driver_name",sharedPreferences.getString("full_name","İsim bilinmiyor."));


                                OnSuccessListener successListener=new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        dialog.dismiss();
                                        Toast.makeText(context,"BAŞARILI",Toast.LENGTH_LONG).show();
                                        isExistPassenger();
                                    }
                                };
                                OnFailureListener failureListener=new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(context,"BAŞARISIZ",Toast.LENGTH_SHORT).show();
                                    }
                                };
                                TaxiRequestFirebaseDAO.updateTaxiRequest(doc.getId(),paramas,successListener,failureListener);
                            }
                        });
                        builder.setNegativeButton("REDDET", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {

                                HashMap<String,String> paramas=new HashMap<>();
                                paramas.put("status", "cancel");
                                paramas.put("driver_name",sharedPreferences.getString("full_name","İsim bilinmiyor."));

                                OnSuccessListener successListener=new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Toast.makeText(context,"BAŞARILI",Toast.LENGTH_LONG).show();
                                        dialog.dismiss();

                                    }
                                };
                                OnFailureListener failureListener=new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context,"BAŞARISIZ",Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                };
                                TaxiRequestFirebaseDAO.updateTaxiRequest(doc.getId(),paramas,successListener,failureListener);

                            }
                        });
                        builder.create().show();*/
                    }
                }
                catch (Exception p){
                    Log.e("onEvent in taxiRequest",p.getMessage());
                }
            }
        };
        TaxiRequestFirebaseDAO.listenForRealtimeTaxiRequest(evntEventListener,uId);
    }
    void listenAndFollowPersonLocation(String paramUId, final Icon icon){

        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<DocumentSnapshot>(){
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                try {
                    if (e!=null)
                        return;

                    map.clear();
                    Object plocation=documentSnapshot.get("location");
                    double pLatitude=((HashMap<String,Double>)plocation).get("latitude");
                    double pLongitude=((HashMap<String,Double>)plocation).get("longitude");

                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(pLatitude,pLongitude)).setTitle(documentSnapshot.getString("id")).setIcon(icon));
                }
                catch (Exception x){
                    Log.e("onEvent in nAndFollowPe",x.getMessage());
                    Toast.makeText(context,x.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        };
        TaxiRequestFirebaseDAO.listenAndFollowPersonLocation(evntEventListener,paramUId);
    }
    void isExistPassenger(){

        OnCompleteListener listener= new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                map.clear();
                if(task.getResult().getDocuments().size()!=0){
                    for (int i=0;i<task.getResult().getDocuments().size();i++){

                        Object plocation=task.getResult().getDocuments().get(i).get("passenger_location");
                        double pLatitude=((HashMap<String,Double>)plocation).get("latitude");
                        double pLongitude=((HashMap<String,Double>)plocation).get("longitude");
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(pLatitude,pLongitude)).setTitle(task.getResult().getDocuments().get(i).getId()).setIcon(iconPassenger));

                    }
                }
            }
        };

        TaxiRequestFirebaseDAO.isDriverAvaible(uId,listener);
    }
     void updateTaxiRequest(String docId, Map map){

        OnSuccessListener successListener=new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                isExistPassenger();

            }
        };
        OnFailureListener failureListener=new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"BAŞARISIZ",Toast.LENGTH_SHORT).show();
            }
        };
        TaxiRequestFirebaseDAO.updateTaxiRequest(docId,map,successListener,failureListener);
    }

    public void showTaxiRequestDialog(final Map map, final String docId){

        AlertDialog.Builder builder =new AlertDialog.Builder(context);

        // Set the alert dialog title
        builder.setTitle("İstek Sonlandır");

        // Display a message on alert dialog
        builder.setMessage("Taxi isteğini sonlandırmak istiyor musunuz?");


        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               updateTaxiRequest(docId,map);
            }
        });

        Dialog dialog= builder.create();
        dialog.show();
    }





}
