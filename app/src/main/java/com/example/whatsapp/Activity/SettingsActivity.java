package com.example.whatsapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdatedAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    Toolbar toolbar;

    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private static final int GallaryPick=1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef=FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        UpdatedAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,GallaryPick);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GallaryPick && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
           if (resultCode == RESULT_OK) {

               loadingBar.setTitle("Set Profile Image");
               loadingBar.setMessage("Please wait, your profile image is updating...");
               loadingBar.setCanceledOnTouchOutside(false);
               loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath=UserProfileImageRef.child(currentUserId+".jpg");

                /*filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            //final String downloaedUrl=task.getResult().getUploadSessionUri().toString();
                            final String downloaedUrl= task.getResult().getDownloadUrl().toString();

                            RootRef.child("Users").child(currentUserId).child("image").setValue(downloaedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful()){
                                       Toast.makeText(SettingsActivity.this, "Image save in Database, Successfully", Toast.LENGTH_SHORT).show();
                                       loadingBar.dismiss();
                                   }
                                   else {
                                       String message=task.getException().toString();
                                       Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                       loadingBar.dismiss();
                                   }
                                }
                            });
                        }
                        else {
                            String message=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });*/

               filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                       filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                               Log.d("Image",uri.toString());
                               Toast.makeText(SettingsActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                               RootRef.child("Users").child(currentUserId).child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful()){
                                           Toast.makeText(SettingsActivity.this, "Image save in Database, Successfully", Toast.LENGTH_SHORT).show();
                                           loadingBar.dismiss();
                                       }
                                       else {
                                           String message=task.getException().toString();
                                           Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                           loadingBar.dismiss();
                                       }
                                   }
                               });
                           }
                       });

                   }
               });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void RetrieveUserInfo() {

        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image"))){

                    String retrieveUserName=snapshot.child("name").getValue().toString();
                    String retrieveStatus=snapshot.child("status").getValue().toString();
                    String retrieveImage=snapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    /*Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-4adbc.appspot.com/o/Profile%20Images%2FxOHUehAGjCepF37IUuk6sr5w0kZ2.jpg?alt=media&token=847f2fb6-5c65-43a1-9be2-a513ddbaf7c2").resize(50, 50)
                            .centerCrop().into(userProfileImage);*/
                    Picasso.get().load(retrieveImage).into(userProfileImage);

                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name"))){
                    String retrieveUserName=snapshot.child("name").getValue().toString();
                    String retrieveStatus=snapshot.child("status").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please set & update your profile information....", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        }
        else {

            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String message=task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }
    private void SendUserToMainActivity() {
        Intent intent=new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void InitializeFields() {
        UpdatedAccountSettings=findViewById(R.id.update_settings_button);
        userName=findViewById(R.id.set_user_name);
        userStatus=findViewById(R.id.set_profile_status);
        userProfileImage=findViewById(R.id.profile_image);
        loadingBar=new ProgressDialog(this);
        toolbar=findViewById(R.id.settings_toolbar);
    }
}