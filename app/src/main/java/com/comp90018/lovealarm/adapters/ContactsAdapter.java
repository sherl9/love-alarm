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
import com.comp90018.lovealarm.model.User;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.contact_avatar);
            text = itemView.findViewById(R.id.contact_name);
        }
    }

    private final List<User> contactList;
    private List<User> list;
    private final Context context;

    public ContactsAdapter(Context context) {
        this.contactList = new ArrayList<>();
        this.list = new ArrayList<>();
        this.context = context;
    }

    // TODO remove this constructor
    public ContactsAdapter(Context context, List<User> contactList) {
        this.contactList = contactList;
        this.list = contactList;
        this.context = context;
    }

    public List<User> getContactList() {
        return contactList;
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
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        holder.text.setText(user.getUserName());
        // TODO: Change image
        holder.icon.setImageResource(R.drawable.ic_heart);

        holder.itemView.setOnClickListener(view -> {
            Intent i = new Intent(context, MessageActivity.class);
            i.putExtra("userid", user.getUserId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
