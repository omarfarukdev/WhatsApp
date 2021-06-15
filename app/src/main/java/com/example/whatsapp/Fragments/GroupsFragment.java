package com.example.whatsapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.whatsapp.Activity.GroupChatActivity;
import com.example.whatsapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View view;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups =new ArrayList<>();

    private DatabaseReference GroupRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef= FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeFields();

        RetrieveAndDispalyGroups();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName=parent.getItemAtPosition(position).toString();

                Intent intent=new Intent(getContext(), GroupChatActivity.class);
                intent.putExtra("groupname",currentGroupName);
                startActivity(intent);
            }
        });

        return view;
    }

    private void RetrieveAndDispalyGroups() {

        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String> set=new HashSet<>();
                Iterator iterator=snapshot.getChildren().iterator();
                //Log.d("FFFF",snapshot.getValue().toString());
                Toast.makeText(getContext(), "Calll", Toast.LENGTH_SHORT).show();
                while (iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());

                }
                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

               // Toast.makeText(getContext(), "Calll", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitializeFields() {
        listView=view.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,list_of_groups);
        listView.setAdapter(arrayAdapter);
    }
}