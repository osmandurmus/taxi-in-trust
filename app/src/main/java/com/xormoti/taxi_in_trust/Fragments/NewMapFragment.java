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

public class NewMapFragment extends Fragment {

    private String uId;
    private MapView mapView;
    private SharedPreferences sharedPreferences;
    private LocationListener listener;
    private LocationManager locationManager;
    private double lat;
    private double lng;
    private CameraPosition position;
    MainFlow mainFlow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mainFlow=new MainFlow();
            Mapbox.getInstance(getContext(),getString(R.string.mapbox_access_token));
            sharedPreferences=getContext().getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE);
            uId= sharedPreferences.getString("uid",null);
            mainFlow.setuId(uId);
            mainFlow.setContext(getContext());
            mainFlow.setSharedPreferences(sharedPreferences);
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
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
                mainFlow.setMap(mapboxMap);
                mainFlow.start();

                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        try {
                            String state= sharedPreferences.getString("state",null);

                            if (state.equals("driver")) {
                                Map<String,String> keyVal=new HashMap<>();
                                keyVal.put("status","finish");
                                mainFlow.getDriverObject().showTaxiRequestDialog(keyVal,marker.getTitle());
                                return false;
                            }
                            mainFlow.getPassengerObject().doTaxiCall(marker.getTitle(),"wait",marker.getPosition(),lat,lng); //Hairta tıklaması sonucu. TAxi Çağırma dialoğu açılır.

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
        if (mainFlow.getMap()==null)
            return;
        mainFlow.getMap().animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000);
    }

}
