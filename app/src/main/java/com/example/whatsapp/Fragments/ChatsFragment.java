package com.example.whatsapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.Activity.ChatActivity;
import com.example.whatsapp.Models.Contacts;
import com.example.whatsapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatRecyclerView;
    private DatabaseReference chatRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String requestUserImage="default_image";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView= inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        chatRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");

        chatRecyclerView=privateChatView.findViewById(R.id.chats_list);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatRef,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=new
                FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {

                        final String usersIDs=getRef(position).getKey();
                        userRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.exists()){
                                    if (snapshot.hasChild("image")){
                                        requestUserImage=snapshot.child("image").getValue().toString();
                                        Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                    }

                                    final String requestUserName=snapshot.child("name").getValue().toString();
                                    final String requestUserStatus=snapshot.child("status").getValue().toString();
                                    holder.userName.setText(requestUserName);
                                    holder.userStatus.setText("Last Seen: "+"\n"+"Date "+" Time");

                                    if (snapshot.child("userState").hasChild("state")){
                                        String state =snapshot.child("userState").child("state").getValue().toString();
                                        String time =snapshot.child("userState").child("time").getValue().toString();
                                        String date =snapshot.child("userState").child("date").getValue().toString();
                                        if (state.equals("online")){
                                            holder.userStatus.setText("online");
                                        }
                                        else if (state.equals("offline")){
                                            holder.userStatus.setText("Last Seen: "+date+" "+time);
                                        }

                                    }
                                    else {
                                        holder.userStatus.setText("offline");
                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent=new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("visit_user_id",usersIDs);
                                            intent.putExtra("visit_user_name",requestUserName);
                                            intent.putExtra("visit_user_image",requestUserImage);
                                            startActivity(intent);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_dispaly_layout,parent,false);
                        ChatsViewHolder holder=new ChatsViewHolder(view);

                        return holder;
                    }
                };

        chatRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }
    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }
    }
}