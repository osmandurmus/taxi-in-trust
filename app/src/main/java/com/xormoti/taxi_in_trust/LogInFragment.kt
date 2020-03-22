package com.xormoti.taxi_in_trust

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class LogInFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView=inflater.inflate(R.layout.fragment_log_in, container, false);
        rootView.findViewById<Button>(R.id.button_login).setOnClickListener {
            //findNavController().navigate(R.id.driver_navigation)
            findNavController().navigate(R.id.passenger_navigation)


        }
        rootView.findViewById<Button>(R.id.button_register).setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_registerFragment2)
        }

        return rootView;
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
