package com.example.whatsapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadignBar;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InitializeFields();
        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginrActivity();
            }
        });
        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccont();
            }
        });

    }

    private void CreateNewAccont() {

        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show();
        }
        else {
            loadignBar.setTitle("Creating New Account");
            loadignBar.setMessage("Please wait, while we are creating new account for you...");
            loadignBar.setCanceledOnTouchOutside(true);
            loadignBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        String currentUser=mAuth.getCurrentUser().getUid();
                        RootRef.child("Users").child(currentUser).setValue("");
                        RootRef.child("Users").child(currentUser).child("device_token").setValue(deviceToken);
                        SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Create Account Successfuly", Toast.LENGTH_SHORT).show();
                        loadignBar.dismiss();
                    }
                    else {
                        String message=task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                        loadignBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); 
    }

    private void InitializeFields() {

        CreateButton=findViewById(R.id.register_button);
        UserEmail=findViewById(R.id.register_email);
        UserPassword=findViewById(R.id.register_password);
        AlreadyHaveAccountLink=findViewById(R.id.already_have_account_link);
        loadignBar=new ProgressDialog(RegisterActivity.this);
    }
    private void SendUserToLoginrActivity() {

        Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}