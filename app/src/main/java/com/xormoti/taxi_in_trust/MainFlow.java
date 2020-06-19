package com.xormoti.taxi_in_trust;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;

public class MainFlow {

    SharedPreferences sharedPreferences;
    String uId;
    FloatingActionButton floatingActionButton;

    public DriverFlow getDriverObject() {
        return driverObject;
    }

    public PassengerFlow getPassengerObject() {
        return passengerObject;
    }

    public void setFloatingActionButton(FloatingActionButton floatingActionButton) {
        this.floatingActionButton = floatingActionButton;
    }

    DriverFlow driverObject;
    PassengerFlow passengerObject;
    Context context;

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setMap(MapboxMap map) {
        this.map = map;
    }

    MapboxMap map;

    public MapboxMap getMap() {
        return map;
    }

    public MainFlow(Context context, MapboxMap map, String uId, SharedPreferences sharedPreferences){
        this.uId=uId;
        this.map=map;
        this.context=context;
        this.sharedPreferences=sharedPreferences;

    }
    public MainFlow(){
    }

    public void start(){
        checkUserNotPassengerAndDriver();
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
                    String fullname=doc.getString("fullName");
                    sharedPreferences.edit().putString("full_name",fullname).commit();

                    if (driver==false && passenger==false) //Henüz kullanıcı durumunu belli etmediyse
                    {
                        userTypeSelection();
                    }
                    else{
                        if (driver){
                            floatingActionButton.hide();
                            driverObject=new DriverFlow(context,map,uId);
                            driverObject.start();
                        }
                        else if (passenger){
                            floatingActionButton.show();
                            passengerObject=new PassengerFlow(context,map,uId);
                            passengerObject.start();
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

        AlertDialog.Builder builder =new AlertDialog.Builder(context);

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
                        driverObject=new DriverFlow(context,map,uId);
                        driverObject.start();
                        floatingActionButton.hide();
                    }
                };

                OnFailureListener failureListener=new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Kullanıcı seçimi başarısız oldu.",Toast.LENGTH_LONG).show();
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
                        passengerObject=new PassengerFlow(context,map,uId);
                        passengerObject.start();
                        floatingActionButton.show();
                    }
                };

                OnFailureListener failureListener=new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Kullanıcı seçimi başarısız oldu.",Toast.LENGTH_LONG).show();
                    }
                };
                PersonFirebaseDAO.updatePersonField(uId,"passenger",true,successListener,failureListener);

            }
        });
        builder.create().show();
    }

    void showErrorDialog(){

        AlertDialog.Builder builder =new AlertDialog.Builder(context);

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
}
