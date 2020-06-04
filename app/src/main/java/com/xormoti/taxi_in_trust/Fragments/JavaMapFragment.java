package com.xormoti.taxi_in_trust.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Location_;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Person_;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.TaxiRequestFirebaseDAO;
import com.xormoti.taxi_in_trust.MainFlow;
import com.xormoti.taxi_in_trust.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JavaMapFragment extends Fragment {

    private String uId;
    private MapboxMap map;
    private MapView mapView;
    private SharedPreferences sharedPreferences;
    private LocationListener listener;
    private LocationManager locationManager;
    private double lat;
    private double lng;
    private CameraPosition position;
    private Icon iconDriver;
    private Icon iconPassenger;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       try {
           Mapbox.getInstance(getContext(),getString(R.string.mapbox_access_token));
           //startLocationService(); //Konum dinleme servisinin başlatılması
           sharedPreferences=getContext().getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE);
           uId= sharedPreferences.getString("uid",null);

           IconFactory iconFactory = IconFactory.getInstance(getContext());
           iconPassenger = iconFactory.fromResource(R.mipmap.ic_passenger);
           iconDriver = iconFactory.fromResource(R.mipmap.ic_taxi);
       }
       catch (Exception e){
           Log.e("onCreate",e.getMessage());
       }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_map,container,false);
        mapView=view.findViewById(R.id.mapView);
       return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                map=mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS);


                map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        try {
                            String state= sharedPreferences.getString("state",null);

                            if (state.equals("driver")) {
                                return false;
                            }

                            doTaxiCall(marker.getTitle(),"wait",marker.getPosition()); //Hairta tıklaması sonucu. TAxi Çağırma dialoğu açılır.

                            return true;
                        }
                        catch (Exception e){
                            Log.e("OnMarkerClickListener",e.getMessage());
                            return false;
                        }

                    }

                });

            }
        });
        checkUserNotPassengerAndDriver();

    }

    /**
     * Active Driverlerin passengerlra haritada gösterilmesi sağlar. canlı
     */
    //DONE
    void listenUserLocationsInFirebase(){

        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        return;
                }

                    map.clear(); //Haritanın temizlenmesi
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

        PersonFirebaseDAO.listenForRealtimeDriverLocations(evntEventListener,getContext());
    }


    /**
     * Driver tarafından kullanılır. Kendisine gelen taxi requestleri dinler.
     */
    //DONE
    void listenForRealtimeTaxiRequest(){
        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        return;
                    }
                    for (final QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                        builder.setTitle("TAXİ REUEST");
                        builder.setMessage("Yeni bir İstek.");
                        builder.setPositiveButton("KABUL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {

                                HashMap<String,String> paramas=new HashMap<>();
                                paramas.put("status", "accept");

                                OnSuccessListener successListener=new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {


                                        Toast.makeText(getContext(),"BAŞARILI",Toast.LENGTH_LONG).show();
                                        listenAndFollowPersonLocation(doc.getString("passenger_id"),iconPassenger);
                                    }
                                };
                                OnFailureListener failureListener=new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(),"BAŞARISIZ",Toast.LENGTH_SHORT).show();
                                    }
                                };
                                TaxiRequestFirebaseDAO.updateTaxiRequest(doc.getId(),paramas,successListener,failureListener);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("REDDET", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {

                                HashMap<String,String> paramas=new HashMap<>();
                                paramas.put("status", "cancel");

                                OnSuccessListener successListener=new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Toast.makeText(getContext(),"BAŞARILI",Toast.LENGTH_LONG).show();

                                    }
                                };
                                OnFailureListener failureListener=new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(),"BAŞARISIZ",Toast.LENGTH_SHORT).show();
                                    }
                                };
                                TaxiRequestFirebaseDAO.updateTaxiRequest(doc.getId(),paramas,successListener,failureListener);
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                    }
                }
                catch (Exception p){
                    Log.e("onEvent in taxiRequest",p.getMessage());
                }
            }
        };
        TaxiRequestFirebaseDAO.listenForRealtimeTaxiRequest(evntEventListener,uId);
    }
    /**
     * Kullanıcı ilk giriş yaptığında kullanıcı bilgileri alınır ve ona göre seçim dialoğu açılır veya açılmaz.
     */
    void checkUserNotPassengerAndDriver(){

        OnSuccessListener successListener=new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                try {
                    DocumentSnapshot doc=(DocumentSnapshot) o;
                    boolean  driver= doc.getBoolean("driver");
                    boolean passenger=doc.getBoolean("passenger");
                    if (driver==false && passenger==false) //Henüz kullanıcı durumunu belli etmediyse
                    {
                        userTypeSelection();
                    }
                    else{
                        if (driver){
                            isExistPassenger();
                            listenForRealtimeTaxiRequest();
                        }
                        else if (passenger){
                            listenWaitingTaxiRequestOfPassenger();
                            listenUserLocationsInFirebase(); //passenger ise driverları gösterirr haritada. //TODO
                        }
                    }
                }
                catch (Exception e){
                    Log.e("onSuccess in checkUs",e.getMessage());

                }
            }
        };

        OnFailureListener failureListener=new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO 1 BAŞARISIZ OLDUĞUNU MESAJ VER TOAST YADA SNACKBAR KULLAN;
                showErrorDialog();
            }
        };

        PersonFirebaseDAO.getUser(uId,successListener,failureListener);
    };


    /**
     * Kullanıcı ilk giriş yaptığında sürücü olup olmadığının belirlenmesi
     */
    void userTypeSelection(){

        AlertDialog.Builder builder =new AlertDialog.Builder(getContext());

        // Set the alert dialog title
        builder.setTitle("Kullanıcı Seçimi");

        // Display a message on alert dialog
        builder.setMessage("Sürücü müsünüz?");
        builder.setCancelable(false);
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {


                OnSuccessListener successListener=new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                       dialog.dismiss();
                       sharedPreferences.edit().putString("state","driver").commit();
                       listenForRealtimeTaxiRequest();
                       isExistPassenger();
                    }
                };

                OnFailureListener failureListener=new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Kullanıcı seçimi başarısız oldu.",Toast.LENGTH_LONG).show();
                    }
                };
                PersonFirebaseDAO.updatePersonField(uId,"driver",true,successListener,failureListener);
            }
        });

        builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {


                OnSuccessListener successListener=new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        dialog.dismiss();
                        sharedPreferences.edit().putString("state","passenger").commit();
                        listenUserLocationsInFirebase();
                    }
                };

                OnFailureListener failureListener=new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Kullanıcı seçimi başarısız oldu.",Toast.LENGTH_LONG).show();
                    }
                };
                PersonFirebaseDAO.updatePersonField(uId,"passenger",true,successListener,failureListener);

            }
        });
        builder.create().show();
    }


    /**
     * Harita üzerinde taxiye tıklandığında  taxi çağırmak için açılacak dialog.
     * @param driverId
     * @param requestStatus
     * @param driverLatLng
     */
    void doTaxiCall(final String driverId, final String requestStatus, final LatLng driverLatLng){

        AlertDialog.Builder builder =new AlertDialog.Builder(getContext());

        // Set the alert dialog title
        builder.setTitle("Taxi Çağırma");

        // Display a message on alert dialog
        builder.setMessage("Seçtiğiniz sürücüyü çağırmak istiyor musunuz?");
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
                        Toast.makeText(getContext(),"ÇAĞRI BAŞARISIZ",Toast.LENGTH_LONG).show();
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

                TaxiRequestFirebaseDAO.newTaxiCall(hashMap,successListener,failureListener);

            }
        });

        builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    void showErrorDialog(){

        AlertDialog.Builder builder =new AlertDialog.Builder(getContext());

        // Set the alert dialog title
        builder.setTitle("BİLGİ");

        // Display a message on alert dialog
        builder.setMessage("BAĞLANTI HATASI");
        builder.setCancelable(false);

        builder.setPositiveButton("Uygulamadan Çık", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        Dialog dialog= builder.create();
        dialog.show();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        doUserLocationPassive();
    }
    @Override
    public void onStart() {
        try {
            super.onStart();
            if (mapView!=null)
                mapView.onStart();
            listenAndWriteMyLocationToFirebase();
        }
        catch (Exception e){
            Log.e("onStart",e.getMessage());
        }
    }
    @Override
    public void onStop() {
        try {
            super.onStop();
            if (mapView!=null)
                mapView.onStop();
        }
        catch (Exception e){
            Log.e("onStop",e.getMessage());
        }
    }
    @Override
    public void onDestroyView() {
        try {
            super.onDestroyView();
            if (mapView!=null)
                mapView.onDestroy();
        }
        catch (Exception e){
            Log.e("onDestroyView",e.getMessage());
        }
    }
    @Override
    public void onResume() {
        try {
            super.onResume();
            if (mapView!=null)
                mapView.onResume();
        }
        catch (Exception e){
            Log.e("onResume",e.getMessage());
        }
    }
    @Override
    public void onLowMemory() {
       try {
           super.onLowMemory();
           if (mapView!=null)
               mapView.onLowMemory();
       }
       catch (Exception e){
           Log.e("onLowMemory",e.getMessage());
       }
    }
    @Override
    public void onPause() {
        try {
            super.onPause();
            if (mapView!=null)
                mapView.onPause();
        }
        catch (Exception e){
            Log.e("onPause",e.getMessage());

        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        try {

            super.onSaveInstanceState(outState);
            if(outState!=null && mapView!=null)
                mapView.onSaveInstanceState(outState);
        }
        catch (Exception e){
            Log.e("onSaveInstanceState",e.getMessage());
        }
    }


    /**
     * Android konum dinlemesi ve her konum değiştiğinde ilgili personun konumunun güncellenmesi
     */

    void listenAndWriteMyLocationToFirebase(){
        if (locationManager == null)
            locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
               try {
                   setCameraPosition(location);
                   Map locationMap=new HashMap<String,Double>();
                   locationMap.put("latitude",location.getLatitude());
                   locationMap.put("longitude",location.getLongitude());
                   locationMap.put("active",true);


                   OnSuccessListener success=new OnSuccessListener() {
                       @Override
                       public void onSuccess(Object o) {
                           lat=location.getLatitude();
                           lng=location.getLongitude();
                       }
                   };
                   OnFailureListener failure=new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {

                       }
                   };
                   if (uId!=null)
                       PersonFirebaseDAO.updatePersonField(uId,"location",locationMap,success,failure);
               }
               catch (Exception e){
                   Log.e("onLocationChanged",e.getMessage());
               }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                int x=3;

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                doUserLocationPassive();
            }
        };
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0.1f, listener);
    }


    /**
     * Android konum dinlemesi durdurma
     */
    void stopWritingLocationToFirebase(){
        locationManager.removeUpdates(listener);
    }


    /**
     * OnStopda çağrılır. Person location passive edilir böylece harita driver olarak gözükmez.
     */
    public void doUserLocationPassive(){
        stopWritingLocationToFirebase();
        OnSuccessListener success=new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {

            }
        };
        OnFailureListener failure=new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        };

        HashMap<String,Object> locationMap=new HashMap<>();
        locationMap.put("active",false);
        locationMap.put("latitude",lat);
        locationMap.put("longitude",lng);

        if (uId!=null)
            PersonFirebaseDAO.updatePersonField(uId,"location",locationMap,success,failure);
    }
    public void setCameraPosition(Location location){
            position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(15)
                    .tilt(20)
                    .build();
            if (map==null)
                return;
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000);
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
                            PersonFirebaseDAO.getListenerForAllPassengersLocation().remove();
                            listenAndFollowPersonLocation(doc.getString("driver_id"),iconDriver);
                        }
                        else
                            return;
                    }
                }
                catch (Exception p){
                    Log.e("onEvent in x",p.getMessage());
                }
            }
        };
            TaxiRequestFirebaseDAO.listenPassengerWaitingTaxiRequest(evntEventListener,uId);
    }


    /**
     * Uıd si parametre olarak verilen personun(Driver/Passenger) konumu canlı olarak dinler ve haritada gösterir.
     * @param paramUId
     */
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
                   Toast.makeText(getContext(),x.getMessage(),Toast.LENGTH_LONG).show();
               }
            }
        };
        TaxiRequestFirebaseDAO.listenAndFollowPersonLocation(evntEventListener,paramUId);
    }

    void isExistPassenger(){

        OnCompleteListener listener= new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.getResult().getDocuments().size()!=0){
                    listenAndFollowPersonLocation(task.getResult().getDocuments().get(0).getString("passenger_id"),iconPassenger);

                }

            }
        };

        TaxiRequestFirebaseDAO.isDriverAvaible(uId,listener);

    }
}
