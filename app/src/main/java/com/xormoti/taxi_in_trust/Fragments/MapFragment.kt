package com.xormoti.taxi_in_trust.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Location_
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Person_
import com.xormoti.taxi_in_trust.R
import com.xormoti.taxi_in_trust.Services.UserLocationService
import java.lang.Exception
import java.util.HashMap
import kotlin.system.exitProcess


class MapFragment : Fragment() {
    lateinit var  persons:ArrayList<Person_>
    lateinit var uId:String
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) };
        startLocationService()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_map, container, false)
        mapView= view.findViewById<MapView>(R.id.mapView);
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            }
        }


        val eventListener= object :EventListener<QuerySnapshot>{
            override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {

                try {
                    if (p1 != null) {
                        return
                    }

                    persons = ArrayList<Person_>()
                    for (doc in p0!!) {

                        val pdriver=doc.getBoolean("driver")
                        val ppassenger=doc.getBoolean("passenger")
                        val pfullName=doc.getString("fullName")
                        val pid=doc.getString("id")

                        val plocation=doc.get("location")


                        val pLatitude=(plocation as HashMap<String,Double>).get("latitude")
                        val pLongitude=plocation.get("longitude")


                        val person_=Person_(pid,pfullName,pdriver as Boolean,ppassenger as Boolean)
                        val location_=Location_(pLatitude as Double,pLongitude as Double)
                        person_.location_=location_
                        persons.add(person_)
                    }
                }
                catch (e:Exception){
                    e.printStackTrace()
                }

            }
        }



        val success=OnSuccessListener<DocumentSnapshot> {
            //it.toObject(Person_::class.java)
            val  driver= it?.get("driver")
            val passenger=it?.get("passenger")
            if (driver==false && passenger==false)
            {
                showAlerDialog()
            }
            else{
                PersonFirebaseDAO.listenForRealtimePersonLocations(eventListener,context)
            }
        }
        val failure= OnFailureListener {
                showMessageDialog()
        }

        uId= context?.getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE)?.getString("uid",null)?:"";
        PersonFirebaseDAO.getUser(uId,success,failure)
    }
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView?.onStart()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
           mapView.onSaveInstanceState(outState)
        }
    }
    fun showAlerDialog(){

        val builder = AlertDialog.Builder(context)

        // Set the alert dialog title
        builder.setTitle("Kullanıcı Seçimi")

        // Display a message on alert dialog
        builder.setMessage("Sürücü müsünüz?")
        builder.setCancelable(false)
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("Evet"){dialog, which ->

            val success= OnSuccessListener<Void> {
                dialog.dismiss()
                val sharedPreferences = context!!.getSharedPreferences(
                    LoginFragment.sharedtaxiintrust,
                    Context.MODE_PRIVATE
                )
                val editor=sharedPreferences.edit()
                editor.putString("state", "driver")
                editor.commit()
            }
            val failure= OnFailureListener {
                Toast.makeText(context,"Bir Hata Oluştu",Toast.LENGTH_LONG).show()
            }
            PersonFirebaseDAO.updatePersonField(uId,"driver",true,success,failure)
        }
        builder.setNegativeButton("Hayır"){dialog,which ->

            val success= OnSuccessListener<Void> {
                dialog.dismiss()
                val sharedPreferences = context!!.getSharedPreferences(
                    LoginFragment.sharedtaxiintrust,
                    Context.MODE_PRIVATE
                )
                val editor=sharedPreferences.edit()
                editor.putString("state","passenger")
                editor.commit()
            }
            val failure= OnFailureListener {
                Toast.makeText(context,"Bir Hata Oluştu",Toast.LENGTH_LONG).show()
            }
            PersonFirebaseDAO.updatePersonField(uId,"passenger",true,success,failure)

        }
        val dialog= builder.create()
        dialog.show()
    }
    fun showMessageDialog(){

        val builder = AlertDialog.Builder(context)

        // Set the alert dialog title
        builder.setTitle("BİLGİ")

        // Display a message on alert dialog
        builder.setMessage("BAĞLANTI HATASI")
        builder.setCancelable(false)
        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("Uygulamadan Çık"){dialog, which ->
                exitProcess(0)
        }

        val dialog= builder.create()
        dialog.show()


    }
    fun startLocationService(){
        val intent = Intent(context, UserLocationService::class.java)
        context!!.startService(intent)
    }

}
