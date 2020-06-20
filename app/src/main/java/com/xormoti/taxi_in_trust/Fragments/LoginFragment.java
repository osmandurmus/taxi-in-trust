package com.xormoti.taxi_in_trust.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.PersonFirebaseDAO;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.Person_;
import com.xormoti.taxi_in_trust.R;

import java.util.Arrays;
import java.util.List;

public class LoginFragment extends Fragment {

    public static final String sharedtaxiintrust="taxi_in_trust";
    public static final int RC_SIGN_IN=10;
    private View mainView;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences=getContext().getSharedPreferences(sharedtaxiintrust, Context.MODE_PRIVATE);
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         mainView= inflater.inflate(R.layout.fragment_login, container, false);
         return  mainView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                OnSuccessListener successListener=new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        boolean isWritten= sharedPreferences.edit().putString("uid",user.getUid()).commit();
                        if (isWritten)
                        Navigation.findNavController(mainView).navigate(R.id.action_loginFragment_to_mapFragment);
                    }
                };
                OnFailureListener failureListener=new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"BAŞARISIZ:"+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                };

                String uId= sharedPreferences.getString("uid",null);
                if (uId!=null && uId.equals(user.getUid()))
                    Navigation.findNavController(mainView).navigate(R.id.action_loginFragment_to_mapFragment);
                else{
                    Person_ person_=new Person_(user);
                    PersonFirebaseDAO.updatePerson(person_,successListener,failureListener);
                }
            } else {

                Toast.makeText(getContext(),"GİRİŞ YAPILAMADI",Toast.LENGTH_LONG).show();
            }
        }
    }
}
