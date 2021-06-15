package com.example.whatsapp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.Activity.ImageViewerActivity;
import com.example.whatsapp.Models.Messages;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Context context;

    public MessageAdapter(List<Messages> userMessagesList, Context context) {
        this.userMessagesList = userMessagesList;
        this.context = context;
    }

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout,parent,false);
        MessageViewHolder holder=new MessageViewHolder(view);
        mAuth=FirebaseAuth.getInstance();

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("image")){
                    String receiverImage=snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).fit().centerCrop().into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.senderMessageText.setVisibility(View.INVISIBLE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text")){

            /*holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);*/

            if (fromUserID.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());
            }
            else {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());
            }
        }
        else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderId)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).fit().centerCrop().into(holder.messageSenderPicture);
            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).fit().centerCrop().into(holder.messageReceiverPicture);
            }
        }
        else if (fromMessageType.equals("docx") || fromMessageType.equals("pdf")) {
            if (fromUserID.equals(messageSenderId)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
               // holder.messageSenderPicture.setBackgroundResource(R.drawable.file);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-4dfda.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=0bf2bcc0-df03-4e96-a6b7-a3ba783064e9").fit().centerCrop().into(holder.messageSenderPicture);
                /*holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });*/

            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
               // holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-4dfda.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=0bf2bcc0-df03-4e96-a6b7-a3ba783064e9").fit().centerCrop().into(holder.messageReceiverPicture);
/*
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });*/

            }
        }

        if (fromUserID.equals(messageSenderId)){

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") ||
                            userMessagesList.get(position).getType().equals("docx")){

                        CharSequence options [] =new CharSequence[]{

                                "Delete for me",
                                "Download and View This Document",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(context, "hhhhhh", Toast.LENGTH_SHORT).show();

                                if (which==0){
                                    deleteSentMessage(position,holder);
                                }
                                else if (which==1){
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (which==2){

                                }
                                else if (which==3){
                                    deleteMessageForEveryOne(position,holder);

                                }
                            }
                        });

                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text")){

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which==0){

                                    deleteSentMessage(position,holder);
                                }
                                else if (which==2){
                                    deleteMessageForEveryOne(position,holder);
                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "View This Image",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which==0){
                                    deleteSentMessage(position,holder);
                                }
                                else if (which==1){
                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (which==3){
                                    deleteMessageForEveryOne(position,holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") ||
                            userMessagesList.get(position).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Download and View This Document",
                                "Cancel",
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which==0){
                                    deleteReceiveMessage(position,holder);
                                }
                                else if (which==1){
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (which==2){

                                }

                            }
                        });

                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text")){

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",

                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which==0){
                                    deleteReceiveMessage(position,holder);
                                }
                                else if (which==2){

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "View This Image",
                                "Cancel",
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which==0){
                                    deleteReceiveMessage(position,holder);
                                }
                                else if (which==1){

                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position,final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void deleteReceiveMessage(final int position,final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void deleteMessageForEveryOne(final int position,final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getMessageId()).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText=(TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_image_view);
        }
    }
}
