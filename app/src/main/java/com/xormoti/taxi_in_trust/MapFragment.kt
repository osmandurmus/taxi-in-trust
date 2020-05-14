package com.xormoti.taxi_in_trust

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonDAO
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Person_
import kotlin.system.exitProcess


class MapFragment : Fragment() {
    lateinit var uId:String
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) };
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
        val success=OnSuccessListener<DocumentSnapshot> {
            //it.toObject(Person_::class.java)
            val  driver= it?.get("driver")
            val passenger=it?.get("passenger")
            if (driver==false && passenger==false)
            {
                showAlerDialog()
            }
        }
        val failure= OnFailureListener {

        }

        uId= context?.getSharedPreferences(LoginFragment.sharedtaxiintrust,Context.MODE_PRIVATE)?.getString("uid",null)?:"";
        PersonDAO.getUser(uId,success,failure)
    }
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
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
            }
            val failure= OnFailureListener {
                Toast.makeText(context,"Bir Hata Oluştu",Toast.LENGTH_LONG).show()
            }


            PersonDAO.updatePersonField(uId,"driver",true,success,failure)

        }
        builder.setNegativeButton("Hayır"){dialog,which ->

            val success= OnSuccessListener<Void> {
                dialog.dismiss()
            }
            val failure= OnFailureListener {
                Toast.makeText(context,"Bir Hata Oluştu",Toast.LENGTH_LONG).show()
            }
            PersonDAO.updatePersonField(uId,"passenger",true,success,failure)

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

}
