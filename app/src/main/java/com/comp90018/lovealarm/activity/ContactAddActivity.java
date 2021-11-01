package com.comp90018.lovealarm.activity;

import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ContactAddActivity extends AppCompatActivity {
    private TextInputEditText searchEdittext;
    private ActionMenuItemView searchButton;
    private RecyclerView recyclerView;

    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);

        contactsAdapter = new ContactsAdapter();

        recyclerView = findViewById(R.id.recycler_contacts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(contactsAdapter);

        searchButton = findViewById(R.id.contacts_search_button);
        searchButton.setOnClickListener(v -> doSearch());

        searchEdittext = findViewById(R.id.search_edit_text);
        searchEdittext.setOnKeyListener((v, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                doSearch();
            }
            return false;
        });
    }

    private void doSearch() {
        contactsAdapter.getList().clear();
        recyclerView.setAdapter(contactsAdapter);

        Editable text = searchEdittext.getText();
        assert text != null;
        if (text.length() > 0) {
            String keyword = text.toString().toLowerCase();

            DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");
            users.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot child : Objects.requireNonNull(task.getResult()).getChildren()) {
                        User user = child.getValue(User.class);
                        assert user != null;
                        if (user.getUserName().toLowerCase().contains(keyword)) {
                            contactsAdapter.getList().add(user);
                            recyclerView.setAdapter(contactsAdapter);
                        }
                    }
                }
            });
        }
    }
}