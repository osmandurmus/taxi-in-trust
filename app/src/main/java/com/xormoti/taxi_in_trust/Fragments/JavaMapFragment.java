package com.xormoti.taxi_in_trust.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Person_;
import com.xormoti.taxi_in_trust.R;
import com.xormoti.taxi_in_trust.Services.UserLocationService;

import java.util.ArrayList;

public class JavaMapFragment extends Fragment {
    private ArrayList<Person_> persons;
    private String uId;
    private MapboxMap map;
    private Intent intent;
    private MapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(),getString(R.string.mapbox_access_token));

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
    }

    public void startLocationService(){
        intent=new Intent(getContext(),UserLocationService.class);
        getContext().startService(intent);
    }

    public void stopLocationService(){
        if(intent!=null)
            getContext().stopService(intent);
    }

    //Show Alert Diyalogdan devam edilecek.

}
