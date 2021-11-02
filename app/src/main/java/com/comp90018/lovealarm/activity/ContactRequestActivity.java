package com.comp90018.lovealarm.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ContactRequestActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_request);

        contactsAdapter = new ContactsAdapter();

        recyclerView = findViewById(R.id.recycler_contacts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(contactsAdapter);

        updateContactRequestList();
    }

    private void updateContactRequestList() {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Update request list automatically
        users.child(currentUserId).child("contactRequestIdList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsAdapter.getList().clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String userId = child.getValue(String.class);
                    assert userId != null;

                    users.child(userId).get().addOnSuccessListener(dataSnapshot -> {
                        synchronized (ContactRequestActivity.this) {
                            User user = dataSnapshot.getValue(User.class);
                            contactsAdapter.getList().add(user);
                            recyclerView.setAdapter(contactsAdapter);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}