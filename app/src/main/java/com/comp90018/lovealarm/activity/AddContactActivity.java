package com.comp90018.lovealarm.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class AddContactActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

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

        users.child(currentUserId).child("contactRequestIdList").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot child : Objects.requireNonNull(task.getResult()).getChildren()) {
                    String userId = child.getValue(String.class);
                    if (userId == null) continue;

                    users.child(userId).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful() && task1.getResult() != null) {
                            User user = task1.getResult().getValue(User.class);
                            contactsAdapter.getList().add(user);
                            recyclerView.setAdapter(contactsAdapter);
                        }
                    });
                }
            }
        });
    }
}