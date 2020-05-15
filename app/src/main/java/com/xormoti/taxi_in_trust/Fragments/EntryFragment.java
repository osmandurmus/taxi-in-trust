package com.xormoti.taxi_in_trust.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xormoti.taxi_in_trust.R;
import com.xormoti.taxi_in_trust.Services.UserLocationService;

/**
 * A simple {@link Fragment} subclass.
 */
public class EntryFragment extends Fragment {

    public EntryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entry,container,false);
    }
}
