package com.example.whatsapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.Adapters.MessageAdapter;
import com.example.whatsapp.Models.Messages;
import com.example.whatsapp.R;
import com.example.whatsapp.SendNotificationPack.APIService;
import com.example.whatsapp.SendNotificationPack.Client;
import com.example.whatsapp.SendNotificationPack.Data;
import com.example.whatsapp.SendNotificationPack.MyResponse;
import com.example.whatsapp.SendNotificationPack.NotificationSender;
import com.example.whatsapp.SendNotificationPack.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderId;
    private TextView userName,userLastSeen;
    private CircleImageView userProfileImage;
    private Toolbar chatToolbar;
    private EditText messageInputText;
    private ImageButton sendMessageBtn,sendFileBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;
    String saveCurrentTime,saveCurrentDate;
    private String checker="",myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        messageReceiverId=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage=getIntent().getExtras().get("visit_user_image").toString();

        Toast.makeText(ChatActivity.this, ""+messageReceiverId, Toast.LENGTH_SHORT).show();
        Toast.makeText(ChatActivity.this, ""+messageReceiverName, Toast.LENGTH_SHORT).show();
        IntializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userProfileImage);

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        DisplayLastSeen();

        sendFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options [] =new CharSequence[]{

                        "Images",
                        "PDF Files",
                        "MS Word Files"
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which==0){
                            checker="image";

                            Intent intent =new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);

                        }
                        if (which==1){
                            checker="pdf";

                            Intent intent =new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF File"),438);
                        }
                        if (which==2){
                            checker="docx";

                            Intent intent =new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select MS Word File"),438);
                        }
                    }
                });

                builder.show();
            }
        });

       // UpdateToken();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            loadingBar.setTitle("Send File");
            loadingBar.setMessage("Please wait, we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri=data.getData();

            if (!checker.equals("image")){

                //Toast.makeText(this, "OMAR FARUK", Toast.LENGTH_SHORT).show();

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageSenderRef = "Messages/"+messageSenderId+"/"+messageReceiverId;
                String messageReceivrRef= "Messages/"+messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef= RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();

                String messagePushID=userMessageKeyRef.getKey();

                StorageReference filePath=storageReference.child(messagePushID+"."+checker);



                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map messageTextBody=new HashMap();
                                messageTextBody.put("message",uri.toString());
                                messageTextBody.put("name",fileUri.getLastPathSegment());
                                messageTextBody.put("type",checker);
                                messageTextBody.put("from",messageSenderId);
                                messageTextBody.put("to",messageReceiverId);
                                messageTextBody.put("messageID",messagePushID);
                                messageTextBody.put("time",saveCurrentTime);
                                messageTextBody.put("date",saveCurrentDate);

                                Map messageBodyDetails=new HashMap();
                                messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                                messageBodyDetails.put(messageReceivrRef+"/"+messagePushID,messageTextBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double p=(100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p+" %  Uploading...");
                    }
                });

            }
            else if (checker.equals("image")){

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef = "Messages/"+messageSenderId+"/"+messageReceiverId;
                String messageReceivrRef= "Messages/"+messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef= RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();

                String messagePushID=userMessageKeyRef.getKey();

                StorageReference filePath=storageReference.child(messagePushID+"."+"jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUrl=downloadUri.toString();

                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverId);
                            messageTextBody.put("messageID",messagePushID);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);

                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                            messageBodyDetails.put(messageReceivrRef+"/"+messagePushID,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });

            }
            else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesList.clear();
        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages=snapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendMessage() {

        String messageText=messageInputText.getText().toString();

        if (messageText.isEmpty()){
            Toast.makeText(this, "first write your message....", Toast.LENGTH_SHORT).show();
        }
        else {
            String messageSenderRef = "Messages/"+messageSenderId+"/"+messageReceiverId;
            String messageReceivrRef= "Messages/"+messageReceiverId+"/"+messageSenderId;

            DatabaseReference userMessageKeyRef= RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();

            String messagePushID=userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("to",messageReceiverId);
            messageTextBody.put("messageID",messagePushID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceivrRef+"/"+messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();

                        FirebaseDatabase.getInstance().getReference().child("Users").child(messageReceiverId).child("device_token")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        String usertoken=snapshot.getValue(String.class);
                                        Log.d("FFFFFF ",usertoken);
                                        sendNotification(usertoken,"Message",messageText);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });


        }

    }

    private void sendNotification(String usertoken, String title, String message) {

        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        //Log.d("DDD","Call");
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    Log.d("DDD","Call");
                    if (response.body().success != 1) {
                        Toast.makeText(ChatActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }
    private void UpdateToken() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Token token = new Token(refreshToken);
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        Toast.makeText(this, "Call", Toast.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

    }
        private void IntializeControllers() {

        chatToolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userName=findViewById(R.id.custom_profile_name);
        userLastSeen=findViewById(R.id.custom_user_last_seen);
        userProfileImage=findViewById(R.id.custom_profile_image);
        messageInputText=findViewById(R.id.input_message);
        sendMessageBtn=findViewById(R.id.send_message_btn);
        sendFileBtn=findViewById(R.id.send_file_btn);

        loadingBar=new ProgressDialog(this);

        messageAdapter=new MessageAdapter(messagesList,ChatActivity.this);
        userMessageList=findViewById(R.id.private_messages_list_of_user);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

    }
    private void DisplayLastSeen(){

        RootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("userState").hasChild("state")){
                    String state =snapshot.child("userState").child("state").getValue().toString();
                    String time =snapshot.child("userState").child("time").getValue().toString();
                    String date =snapshot.child("userState").child("date").getValue().toString();
                    if (state.equals("online")){
                        userLastSeen.setText("online");
                    }
                    else if (state.equals("offline")){
                        userLastSeen.setText("Last Seen: "+date+" "+time);
                    }

                }
                else {
                    userLastSeen.setText("offline");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}