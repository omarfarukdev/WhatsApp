package com.example.whatsapp.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;

public class LoginActivity extends AppCompatActivity {

    //private FirebaseUser currentUser;
    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmail,UserPassword;
    private TextView ForgetPasswordLink,NeedNewAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadignBar;
    private DatabaseReference UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeFields();
        mAuth=FirebaseAuth.getInstance();
        //currentUser=mAuth.getCurrentUser();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, PhoneLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetmail=new EditText(v.getContext());
                final AlertDialog.Builder  passwordResetDialod=new AlertDialog.Builder(v.getContext());
                passwordResetDialod.setTitle("Reset Password ?");
                passwordResetDialod.setMessage("Enter Your Email To Received Reset Link. ");
                passwordResetDialod.setView(resetmail);

                passwordResetDialod.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail=resetmail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error ! Reset Link is not sent "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialod.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordResetDialod.show();
            }
        });
    }

    private void InitializeFields() {

        LoginButton=findViewById(R.id.login_button);
        PhoneLoginButton=findViewById(R.id.phone_login_button);
        UserEmail=findViewById(R.id.login_email);
        UserPassword=findViewById(R.id.login_password);
        ForgetPasswordLink=findViewById(R.id.forget_password_link);
        NeedNewAccountLink=findViewById(R.id.need_new_account_link);

        loadignBar=new ProgressDialog(this);


    }

    private void AllowUserToLogin(){
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadignBar.setTitle("Sign In");
            loadignBar.setMessage("Please wait...");
            loadignBar.setCanceledOnTouchOutside(true);
            loadignBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String currentUserId=mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        UserRef.child(currentUserId).child("device_token").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this, "Loggen in Successful...", Toast.LENGTH_SHORT).show();
                                    loadignBar.dismiss();
                                }

                            }
                        });

                    }
                    else {
                        String message=task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                        loadignBar.dismiss();
                    }

                }
            });
        }
    }

  /*  @Override
    protected void onStart() {
        super.onStart();
        if (currentUser!=null){
            SendUserToMainActivity();
        }
    }*/

    private void SendUserToMainActivity() {

        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void SendUserToRegisterActivity() {

        Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}