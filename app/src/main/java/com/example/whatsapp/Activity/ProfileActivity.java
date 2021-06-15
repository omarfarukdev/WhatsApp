package com.example.whatsapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,senderUserID,Curront_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,declineMessageRequestButton;
    
    private DatabaseReference UserRef,ChatRequestRef,ContactsRef,NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notification");
        mAuth=FirebaseAuth.getInstance();
        senderUserID=mAuth.getCurrentUser().getUid();
        

        userProfileName=findViewById(R.id.visit_profile_name);
        userProfileStatus=findViewById(R.id.visit_profile_status);
        userProfileImage=findViewById(R.id.visit_profile_image);
        sendMessageRequestButton=findViewById(R.id.send_message_request_button);
        declineMessageRequestButton=findViewById(R.id.decline_message_request_button);
        Curront_State="new";

        RetrieveUserInfo();
        
    }

    private void RetrieveUserInfo() {

        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.hasChild("image")){

                    String userName=snapshot.child("name").getValue().toString();
                    String userStatus=snapshot.child("status").getValue().toString();
                    String userIamge=snapshot.child("image").getValue().toString();

                    Picasso.get().load(userIamge).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
                else {
                    String userName=snapshot.child("name").getValue().toString();
                    String userStatus=snapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests() {

        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.hasChild(receiverUserId)){
                            String request_type=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent")){
                                Curront_State="request_sent";
                                sendMessageRequestButton.setText("Cencel Chat Request");
                            }
                            else if (request_type.equals("received")){
                                Curront_State="request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");
                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);
                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancleChatRequest();
                                    }
                                });
                            }
                        }
                        else {

                            ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(receiverUserId)){

                                        Curront_State="friends";
                                        sendMessageRequestButton.setText("Remove this Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserID.equals(receiverUserId)){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendMessageRequestButton.setEnabled(false);

                    if (Curront_State.equals("new")){

                        SendChatRequest();
                    }
                    if(Curront_State.equals("request_sent")){
                        CancleChatRequest();
                    }
                    if(Curront_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (Curront_State.equals("friends")){

                        RemoveSpecificContact();
                    }
                }
            });

        }
        else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {

        ContactsRef.child(senderUserID).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    ContactsRef.child(receiverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                sendMessageRequestButton.setEnabled(true);
                                Curront_State="new";
                                sendMessageRequestButton.setText("Send Message");

                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);

                            }

                        }
                    });
                }

            }
        });

    }

    private void AcceptChatRequest() {

        ContactsRef.child(senderUserID).child(receiverUserId).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    ContactsRef.child(receiverUserId).child(senderUserID).child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){


                                        ChatRequestRef.child(senderUserID).child(receiverUserId).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    ChatRequestRef.child(receiverUserId).child(senderUserID).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            sendMessageRequestButton.setEnabled(true);
                                                            Curront_State="friends";
                                                            sendMessageRequestButton.setText("Remove this Contacts");
                                                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                            declineMessageRequestButton.setEnabled(false);
                                                        }
                                                    });
                                                }

                                            }
                                        });
                                    }

                                }
                            });
                }

            }
        });


    }

    private void CancleChatRequest() {

        ChatRequestRef.child(senderUserID).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    ChatRequestRef.child(receiverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                sendMessageRequestButton.setEnabled(true);
                                Curront_State="new";
                                sendMessageRequestButton.setText("Send Message");

                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);

                            }

                        }
                    });
                }

            }
        });

    }

    private void SendChatRequest() {

        ChatRequestRef.child(senderUserID).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ChatRequestRef.child(receiverUserId).child(senderUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                HashMap<String,String> chatNotificationMap=new HashMap<>();
                                                chatNotificationMap.put("from",senderUserID);
                                                chatNotificationMap.put("type","request");

                                                NotificationRef.child(receiverUserId).push().setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){

                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    Curront_State="request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                                                }

                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}