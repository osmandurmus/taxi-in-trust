package com.xormoti.taxi_in_trust.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.xormoti.taxi_in_trust.FireBaseTask.CollectionData.TaxiRequestFirebaseDAO;
import com.xormoti.taxi_in_trust.R;
import com.xormoti.taxi_in_trust.RatingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TaxiRequestFragment extends ListFragment {


    ArrayAdapter<String> arrayAdapter;
    List<String> docIdList;
    String uId;
    HashMap<String, Map<String,Object>> documentMap;
    private Map<String,String> driverMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        docIdList =new ArrayList<>();
        uId= getArguments().getString("uid");
        documentMap=new HashMap<>();
        driverMap=new HashMap<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_taxi_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TaxiRequestFirebaseDAO.getTaxiRequests(uId, "finish",new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String showText=String.format("Taksi Sürücüsü:%s \n:",document.getString("driver_name"));
                        docIdList.add(showText+"\n;"+ document.getId());
                        documentMap.put(document.getId(),document.getData());
                    }
                    arrayAdapter=new ArrayAdapter<String>(getContext(),R.layout.support_simple_spinner_dropdown_item, docIdList);
                    setListAdapter(arrayAdapter);
                } else {
                    //Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String documentID=((String)l.getAdapter().getItem(position)).split(";")[1];
        RatingDialog ratingDialog=new RatingDialog(getContext(),arrayAdapter,documentMap.get(documentID),documentID,((String)l.getAdapter().getItem(position)));
        ratingDialog.show();
    }
}
