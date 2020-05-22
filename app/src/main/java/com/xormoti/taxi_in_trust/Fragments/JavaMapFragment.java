package com.xormoti.taxi_in_trust.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Location_;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Person_;
import com.xormoti.taxi_in_trust.R;
import com.xormoti.taxi_in_trust.Services.UserLocationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JavaMapFragment extends Fragment {
    private ArrayList<Person_> persons;
    private String uId;
    private MapboxMap map;
    private MapView mapView;
    private SharedPreferences sharedPreferences;
    private LocationListener listener;
    private LocationManager locationManager;
    private double lat;
    private double lng;
    private CameraPosition position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(),getString(R.string.mapbox_access_token));
        //startLocationService(); //Konum dinleme servisinin başlatılması
        uId= getContext().getSharedPreferences(LoginFragment.sharedtaxiintrust, Context.MODE_PRIVATE).getString("uid",null);

        listenUserLocationsInFirebase();
        sharedPreferences=getContext().getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE);
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
            }
        });
        checkUserNotPassengerAndDriver();

    }

    /**
     * Driver veya passengerların konumlarının dinlenip harita üzerindde gösterilmesi
     */
    void listenUserLocationsInFirebase(){

        EventListener evntEventListener=new com.google.firebase.firestore.EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        return;
                }

                    persons =new ArrayList<Person_>();
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
                                        .title(pfullName));

                        persons.add(person_);
                    }
                }
                catch (Exception p){
                    p.printStackTrace();
                }


            }
        };

        PersonFirebaseDAO.listenForRealtimePersonLocations(evntEventListener,getContext());
    }
    void checkUserNotPassengerAndDriver(){

        OnSuccessListener successListener=new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                DocumentSnapshot doc=(DocumentSnapshot) o;
                boolean  driver= doc.getBoolean("driver");
                boolean passenger=doc.getBoolean("passenger");
                if (driver==false && passenger==false) //Henüz kullanıcı durumunu belli etmediyse
                {
                    userTypeSelection();
                }
                else{
                    listenUserLocationsInFirebase();
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
                       listenUserLocationsInFirebase();
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
    }
    @Override
    public void onStart() {
        super.onStart();
        if (mapView!=null)
        mapView.onStart();
        listenAndWriteMyLocationToFirebase();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mapView!=null)
            mapView.onStop();
        doUserLocationPassive();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView!=null)
            mapView.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mapView!=null)
            mapView.onResume();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView!=null)
            mapView.onLowMemory();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mapView!=null)
            mapView.onPause();
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null && mapView!=null)
            mapView.onSaveInstanceState(outState);

    }



    void listenAndWriteMyLocationToFirebase(){
        if (locationManager == null)
            locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
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
    void stopWritingLocationToFirebase(){
        locationManager.removeUpdates(listener);
    }
    public void doUserLocationPassive(){
        stopWritingLocationToFirebase();
        OnSuccessListener success=new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                o.toString();
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
        locationMap.put("latitude",1);
        locationMap.put("longitude",1);

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
}
