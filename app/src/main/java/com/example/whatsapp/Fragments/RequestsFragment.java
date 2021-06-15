package com.example.whatsapp.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.Models.Contacts;
import com.example.whatsapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestRef,userRef,contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestList= requestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatRequestRef.child(currentUserId),Contacts.class).build();

        /*FirebaseRecyclerAdapter<Contacts,RequestsViewHolder> adapter =new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>() {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model) {

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }
        }*/

        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder> adapter = new
                FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);

                final String list_user_id= getRef(position).getKey();
                //DatabaseReference getTypeRef= getRef(position).child("").getRef();
                DatabaseReference getTypeRef= getRef(position).child("request_type").getRef();
               /* Log.d("OOOO",getTypeRef.toString());

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                       // if (snapshot.exists())
                        Log.d("FFFF","Test  "+snapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            String type=snapshot.getValue().toString();
                            Log.d("FFFF",type);

                            if (type.equals("received")){

                                userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.hasChild("image")){
                                           /* final String requestUserName=snapshot.child("name").getValue().toString();
                                            final String requestUserStatus=snapshot.child("status").getValue().toString();*/
                                            final String requestUserImage=snapshot.child("image").getValue().toString();
                                          /*  holder.userName.setText(requestUserName);
                                            holder.userStatus.setText(requestUserStatus);*/
                                            Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }
                                      //  else {
                                            final String requestUserName=snapshot.child("name").getValue().toString();
                                            final String requestUserStatus=snapshot.child("status").getValue().toString();
                                            holder.userName.setText(requestUserName);
                                           // holder.userStatus.setText(requestUserStatus);// wants to connect with you
                                            holder.userStatus.setText("wants to connect with you");
                                        //}

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]{

                                                        "Accept",
                                                        "Cancel"
                                                };
                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName + "Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if (which==0){

                                                            contactsRef.child(currentUserId).child(list_user_id).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        contactsRef.child(list_user_id).child(currentUserId).child("Contacts")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){

                                                                                    chatRequestRef.child(currentUserId).child(list_user_id)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();

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
                                                            });

                                                        }
                                                        if (which==1){

                                                            chatRequestRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){

                                                                                    Toast.makeText(getContext(), "Contact Delete", Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            else if (type.equals("sent")){

                                Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req Sent");
                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);


                                userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.hasChild("image")){
                                           /* final String requestUserName=snapshot.child("name").getValue().toString();
                                            final String requestUserStatus=snapshot.child("status").getValue().toString();*/
                                            final String requestUserImage=snapshot.child("image").getValue().toString();
                                          /*  holder.userName.setText(requestUserName);
                                            holder.userStatus.setText(requestUserStatus);*/
                                            Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }
                                        //  else {
                                        final String requestUserName=snapshot.child("name").getValue().toString();
                                        final String requestUserStatus=snapshot.child("status").getValue().toString();
                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("you have sent a request to "+requestUserStatus);
                                        //}

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]{

                                                        "Cancel Chat Request"
                                                };
                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                     /*   if (which==0){

                                                            contactsRef.child(currentUserId).child(list_user_id).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        contactsRef.child(list_user_id).child(currentUserId).child("Contacts")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){

                                                                                    chatRequestRef.child(currentUserId).child(list_user_id)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();

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
                                                            });

                                                        }*/
                                                        if (which==0){

                                                            chatRequestRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){

                                                                                    Toast.makeText(getContext(), "you have cancelled the chat request.", Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_dispaly_layout,parent,false);
                RequestsViewHolder holder=new RequestsViewHolder(view);

                return holder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }
    /*public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton,cancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            acceptButton=itemView.findViewById(R.id.request_accept_btn);
            cancelButton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }*/
    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton,cancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            acceptButton=itemView.findViewById(R.id.request_accept_btn);
            cancelButton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}