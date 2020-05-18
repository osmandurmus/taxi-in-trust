package com.xormoti.taxi_in_trust.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.xormoti.taxi_in_trust.R;
import com.xormoti.taxi_in_trust.Services.UserLocationService;

/**
 * A simple {@link Fragment} subclass.
 */
public class EntryFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entry,container,false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ImageView img = (ImageView)view.findViewById(R.id.imageView);
        Animation aniFade = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
        aniFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Navigation.findNavController(img).navigate(R.id.action_entryFragment_to_loginFragment);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        img.startAnimation(aniFade);

    }
}
