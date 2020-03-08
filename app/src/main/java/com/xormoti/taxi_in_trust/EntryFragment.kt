package com.xormoti.taxi_in_trust

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class EntryFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView= inflater.inflate(R.layout.fragment_entry, container, false)

        rootView.findViewById<Button>(R.id.gotologin).setOnClickListener {
            findNavController().navigate(R.id.action_entryFragment_to_logInFragment)
        }

        return rootView
    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
