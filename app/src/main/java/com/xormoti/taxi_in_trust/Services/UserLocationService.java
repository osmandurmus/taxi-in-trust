package com.xormoti.taxi_in_trust.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;
import com.xormoti.taxi_in_trust.Fragments.LoginFragment;

import java.util.HashMap;
import java.util.Map;

public class UserLocationService extends Service {
    private LocationManager locationManager;
    private String uId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
      try {
           uId=getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE).getString("uid",null);

           if (uId==null)
               return;

          createLocationManager();
          requestLocation();
      }catch (Exception e){
          Log.e("error:",e.toString());
      }
    }

    private void createLocationManager() {
        if (locationManager == null)
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }
    void requestLocation(){

        LocationListener listener = new LocationListener() {
            @Override
                public void onLocationChanged(Location location) {
                Map locationMap=new HashMap<String,Double>();
                locationMap.put("latitude",location.getLatitude());
                locationMap.put("longitude",location.getLongitude());


                OnSuccessListener success=new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {

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
                int x=3;

            }

            @Override
            public void onProviderDisabled(String provider) {
                int x=3;

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0.1f, listener);
    }
}
