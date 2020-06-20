package com.xormoti.taxi_in_trust;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Location_;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Person_;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.TaxiRequestFirebaseDAO;
import com.xormoti.taxi_in_trust.Fragments.LoginFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

public class PassengerFlow {

    private Icon iconDriver;
    private Icon iconDriverYellow;
    private Icon iconPassenger;
    private Context context;
    private MapboxMap map;
    private String uId;
    SharedPreferences sharedPreferences;
    TaxiCallDialog taxiCallDialog;


    public Map<String, DocumentSnapshot> getDriverOnMapHashMap() {
        return driverOnMapHashMap;
    }

    private Map<String,DocumentSnapshot> driverOnMapHashMap;

    PassengerFlow(Context context, MapboxMap map, String uId){
        this.context=context;
        this.map=map;
        this.uId=uId;
        IconFactory iconFactory = IconFactory.getInstance(context);
        iconDriver = iconFactory.fromResource(R.mipmap.ic_taxi);
        iconPassenger = iconFactory.fromResource(R.mipmap.ic_passenger);
        iconDriverYellow=iconFactory.fromResource(R.mipmap.ic_passenger_yellow_round);
        driverOnMapHashMap=new HashMap<>();
        sharedPreferences= context.getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE);

    }
    public void start(){
        ((AppCompatActivity)context).getSupportActionBar().setTitle("Yolcu Harita");
        listenWaitingTaxiRequestOfPassenger();
        listenUserLocationsInFirebase(); //passenger ise driverları gösterirr haritada. //TODO
    }

    /**
     * Harita üzerinde active olan driverları passengera gösterir.
     */
    void listenUserLocationsInFirebase(){

        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        return;
                    }

                    map.clear(); //Haritanın temizlenmesi
                    driverOnMapHashMap.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        boolean pdriver=doc.getBoolean("driver");
                        boolean ppassenger=doc.getBoolean("passenger");
                        String pfullName=doc.getString("fullName");
                        String pid=doc.getString("id");
                        Object plocation=doc.get("location");
                        double pLatitude=((HashMap<String,Double>)plocation).get("latitude");
                        double pLongitude=((HashMap<String,Double>)plocation).get("longitude");

                        Person_ person_=new Person_(pid,pfullName,pdriver,ppassenger);
                        Location_ location_=new Location_(pLatitude,pLongitude);
                        person_.setLocation(location_);

                        driverOnMapHashMap.put(pid,doc);

                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(pLatitude,pLongitude))
                                .title(doc.getId()).setIcon(iconDriver));

                    }
                }
                catch (Exception p){
                    Log.e("onEvent in listenUser",p.getMessage());
                }


            }
        };

        PersonFirebaseDAO.listenForRealtimeDriverLocations(evntEventListener,context);
    }

    public void doTaxiCall(final String driverId, final String requestStatus, final LatLng driverLatLng,final double lat, final double lng){

        AlertDialog.Builder builder =new AlertDialog.Builder(context);

        // Set the alert dialog title
        builder.setTitle("Taksi Çağırma");

        final String fullName= driverOnMapHashMap.get(driverId).getString("fullName");
        Object o= driverOnMapHashMap.get(driverId).get("score");
        int score;

        if (o==null){
            score=0;
        }
        else {
            score= ((Long)o).intValue();
        }

        taxiCallDialog=new TaxiCallDialog(context,driverId,requestStatus,driverLatLng,lat,lng,score,fullName,uId,sharedPreferences);
        taxiCallDialog.show();


        /*// Display a message on alert dialog
        builder.setMessage(String.format("%s isimli taksi sürücüsünü çağırmak istiyor musunuz? \n Sürücünün Puanı: %d",fullName,score));
        builder.setCancelable(false);
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {


                OnSuccessListener successListener=new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        dialog.dismiss();
                        //TODO 1 SÜRÜCÜYE İSTEK GERÇEKLEŞTİĞİNDE HARİTA ÜSTÜNDE YAPILACAK İŞLEMLER.
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
        });

        builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();*/
    }

   public void stopFirebaseListeners(){
        try{
            if (TaxiRequestFirebaseDAO.getListenerAlreadyTaxiRequestForPassenger()!=null)
            TaxiRequestFirebaseDAO.getListenerAlreadyTaxiRequestForPassenger().remove();

            if ( PersonFirebaseDAO.getListenerForAllPassengersLocation()!=null)
                PersonFirebaseDAO.getListenerForAllPassengersLocation().remove();

            if (TaxiRequestFirebaseDAO.getListenerForPersonLocation()!=null)
                TaxiRequestFirebaseDAO.getListenerForPersonLocation().remove();

            if (TaxiRequestFirebaseDAO.getListenerForTaxiRequestDocument()!=null)
            TaxiRequestFirebaseDAO.getListenerForTaxiRequestDocument().remove();

        }catch (Exception e){

        }
    }

    /**
     * Passengerın bekleyen isteklerini dinler, eğer bu istek kabul edilmişse, kabul edilmiş olan driver harita canlı olarak izlenir.
     */
    void listenWaitingTaxiRequestOfPassenger(){

        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        String status= doc.getString("status");
                        if (status.equals("accept")){
                            TaxiRequestFirebaseDAO.getListenerAlreadyTaxiRequestForPassenger().remove();
                            PersonFirebaseDAO.getListenerForAllPassengersLocation().remove();
                            listenAndFollowPersonLocation(doc.getString("driver_id"),iconDriverYellow);
                            listenDocument(doc.getId());
                        }
                        else
                            return;
                    }
                }
                catch (Exception p){
                    Log.i("onEvent in x",p.getMessage());
                }
            }
        };
        TaxiRequestFirebaseDAO.listenPassengerWaitingTaxiRequest(evntEventListener,uId);
    }
    void listenAndFollowPersonLocation(String paramUId, final Icon icon){

        map.clear();

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

    void listenDocument(String docId){

        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<DocumentSnapshot>(){
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                try {
                    if (e!=null)
                        return;

                    String status=documentSnapshot.getString("status");
                    if (status.equals("finish")){
                        TaxiRequestFirebaseDAO.getListenerForPersonLocation().remove();
                        listenUserLocationsInFirebase();
                        TaxiRequestFirebaseDAO.getListenerForTaxiRequestDocument().remove();
                        listenWaitingTaxiRequestOfPassenger();
                    }

                }
                catch (Exception x){
                    Log.e("onEvent in nAndFollowPe",x.getMessage());
                    Toast.makeText(context,x.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        };
        TaxiRequestFirebaseDAO.listenTaxiRequestDocument(evntEventListener,docId);
    }



}
