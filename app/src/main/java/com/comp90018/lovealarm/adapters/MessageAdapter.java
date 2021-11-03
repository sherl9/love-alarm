package com.comp90018.lovealarm.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.activity.MainActivity;
import com.comp90018.lovealarm.activity.MessageActivity;
import com.comp90018.lovealarm.model.Chat;
import com.comp90018.lovealarm.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView profileImage;
        public final TextView showMessage;
        public final TextView sentTime;
        public final VoicePlayerView voicePlayerView;
        public final ImageView imageMessage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            showMessage = itemView.findViewById(R.id.show_message);
            sentTime = itemView.findViewById(R.id.sent_time);
            voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
            imageMessage = itemView.findViewById(R.id.img_message);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    private  List<Chat> allChat;
    private Context context;
    private String imgURL;

    // firebase
    FirebaseUser fuser;

    private static final int MSG_TYPE_LEFT_TEXT = 0;
    private static final int MSG_TYPE_RIGHT_TEXT = 1;
    private static final int MSG_TYPE_LEFT_AUDIO = 2;
    private static final int MSG_TYPE_RIGHT_AUDIO = 3;
    private static final int MSG_TYPE_LEFT_IMAGE = 4;
    private static final int MSG_TYPE_RIGHT_IMAGE = 5;


    public MessageAdapter(Context context, List<Chat> allChat, String imgURL) {
        this.allChat = allChat;
        this.context = context;
        this.imgURL = imgURL;
    }



    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // view type, load xml

        if (viewType == MSG_TYPE_RIGHT_TEXT){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_right, null, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else if (viewType == MSG_TYPE_RIGHT_AUDIO){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.audio_item_right, null, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else if (viewType == MSG_TYPE_LEFT_TEXT){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_left, null, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else if (viewType == MSG_TYPE_LEFT_IMAGE){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.img_item_left, null, false);
            return new MessageAdapter.ViewHolder(view);
        } //
        else if (viewType == MSG_TYPE_RIGHT_IMAGE){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.img_item_right, null, false);
            return new MessageAdapter.ViewHolder(view);
        } //
        else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.audio_item_left, null, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = allChat.get(position);
        if (chat.getType().equals("text")){
            holder.showMessage.setText(chat.getMessage());
        }
        else if (chat.getType().equals("audio")){
            holder.voicePlayerView.setAudio(chat.getMessage()); // load media audio file
        }
        else if (chat.getType().equals("image")){
//            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
//            StorageReference image = storageReference.child("/Media/Images/");
//            image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
                    Picasso.get().load(chat.getMessage()).into(holder.imageMessage);
//                }
//            });
        }
        holder.sentTime.setText(chat.getDate());
        // TODO: Change image
        if (imgURL.equals("")){
            holder.profileImage.setImageResource(R.drawable.ic_heart);
        }
        else{
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference image = storageReference.child("avatars/" + imgURL);
            image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(holder.profileImage);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return allChat.size();
    }

    @Override
    public int getItemViewType(int position) { // left or right, test or audio
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (allChat.get(position).getSender().equals(fuser.getUid()) && allChat.get(position)
                .getType().equals("text")){
            return MSG_TYPE_RIGHT_TEXT;
        }
        else if (allChat.get(position).getSender().equals(fuser.getUid()) && allChat.get(position)
                .getType().equals("audio")){
            return MSG_TYPE_RIGHT_AUDIO;
        }
        else if (allChat.get(position).getReceiver().equals(fuser.getUid()) && allChat.get(position)
                .getType().equals("text")){
            return MSG_TYPE_LEFT_TEXT;
        } else if (allChat.get(position).getSender().equals(fuser.getUid()) && allChat.get(position)
                .getType().equals("image")){ //
            return MSG_TYPE_RIGHT_IMAGE;
        } else if (allChat.get(position).getReceiver().equals(fuser.getUid()) && allChat.get(position)
                .getType().equals("image")){ //
            return MSG_TYPE_LEFT_IMAGE;
        }
        else{
            return MSG_TYPE_LEFT_AUDIO; // receiver audio
        }
    }
}
