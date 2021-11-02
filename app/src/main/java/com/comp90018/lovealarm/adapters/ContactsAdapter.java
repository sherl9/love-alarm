package com.comp90018.lovealarm.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.activity.ContactProfileActivity;
import com.comp90018.lovealarm.model.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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
    private final List<User> list;

    public ContactsAdapter() {
        this.contactList = new ArrayList<>();
        this.list = new ArrayList<>();
    }

    public List<User> getContactList() {
        return contactList;
    }

    public List<User> getList() {
        return list;
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
        // load username
        holder.text.setText(user.getUserName());
        // load avatar
        if (!"".equals(user.getAvatarName().trim())) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference image = storageReference.child("avatars/" + user.getAvatarName());
            image.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(holder.icon));
        } else {
            // set default avatar
            holder.icon.setImageResource(R.drawable.ic_avatar);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), ContactProfileActivity.class);
            i.putExtra(ContactProfileActivity.KEY_USERID, user.getUserId());
            i.putExtra(ContactProfileActivity.KEY_USERNAME, user.getUserName());
            i.putExtra(ContactProfileActivity.KEY_DATE_OF_BIRTH, user.getDob());
            view.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
