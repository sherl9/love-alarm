package com.comp90018.lovealarm.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.activity.MessageActivity;
import com.comp90018.lovealarm.model.ChatList;
import com.comp90018.lovealarm.model.User;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView text;
        public final TextView lastMessage;
        public final TextView lastTime;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.contact_avatar);
            text = itemView.findViewById(R.id.contact_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            lastTime = itemView.findViewById(R.id.last_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    private final List<User> contacts;
    private List<User> list;
    private Context context;

    private List<ChatList> chatLists; // chat list

    public ChatAdapter(List<User> contacts) {
        this.contacts = contacts;
        this.list = contacts;
    }

    public ChatAdapter(Context context, List<User> contacts, List<ChatList> chatLists) {
        this.contacts = contacts;
        this.context = context;
        this.chatLists = chatLists;
    }


    public List<User> getContacts() {
        return contacts;
    }

    public List<User> getList() {
        return list;
    }

    public void setList(List<User> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_item, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User users = contacts.get(position);
        ChatList chatList = chatLists.get(position);

        holder.text.setText(users.getUserName()); // user name
        holder.lastMessage.setText(chatList.getLastMessage());
        holder.lastTime.setText(chatList.getDate());
        // TODO: Change image
        holder.icon.setImageResource(R.drawable.ic_heart);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra("userid", users.getUserId());
                context.startActivity(i);

            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
