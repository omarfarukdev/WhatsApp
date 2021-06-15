package com.example.whatsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.whatsapp.Activity.FindFriendsActivity;
import com.example.whatsapp.Activity.LoginActivity;
import com.example.whatsapp.Activity.SettingsActivity;
import com.example.whatsapp.Adapters.TabsAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout myTabLayout;
    private ViewPager myViewPager;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();
        mAuth.setLanguageCode("fr");
        Log.d("GGGG",RootRef.toString());

        toolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("WathsApp");

        myTabLayout=findViewById(R.id.main_tabs);
        myViewPager=findViewById(R.id.main_tabs_pager);

        myTabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        if (currentUser==null){
            SendUserToLoginActivity();
        }
        else {

            updateUserStatus("online");

            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (currentUser!=null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser!=null){
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {

        String currentUserId=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("name").exists()){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SendUserToLoginActivity() {

        Intent loginIntent=new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent  );
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.options_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

         if (item.getItemId() == R.id.main_logout_option){

             updateUserStatus("offline");
             mAuth.signOut();
             SendUserToLoginActivity();

         }
        if (item.getItemId() == R.id.main_settings_option){

            SendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_find_friends_option){

            SendUserToFindFriendsActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option){

            RequestNewGroup();
        }

        return true;
    }

    private void SendUserToFindFriendsActivity() {
        Intent intent=new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(intent);
    }

    private void RequestNewGroup() {

        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");
        final EditText groupNameField=new EditText(this);
        groupNameField.setHint("e.g Coding Cafe");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                String groupName=groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group name....", Toast.LENGTH_SHORT).show();
                }
                else {
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+" group is Created Successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToSettingsActivity() {

        Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent );
        //finish();
    }

    private void updateUserStatus(String state){

        String saveCurrentTime,saveCurrentDate;

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).child("userState").updateChildren(onlineStateMap);
    }
}