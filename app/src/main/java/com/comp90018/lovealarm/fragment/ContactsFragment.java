package com.comp90018.lovealarm.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {
    private TextInputEditText searchEdittext;
    private View searchLocal;
    private RecyclerView recyclerView;

    private ContactsAdapter contactsAdapter;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsAdapter = new ContactsAdapter(getContext());
        autoUpdateContactList();

        recyclerView = view.findViewById(R.id.recycler_contacts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchEdittext = view.findViewById(R.id.search_edit_text);
        searchEdittext.setOnKeyListener((v, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                doSearch();
            }
            return false;
        });

        searchLocal = view.findViewById(R.id.contacts_search_button);
        searchLocal.setOnClickListener(v -> doSearch());

        return view;
    }

    private void autoUpdateContactList() {
        // FIXME get contacts list
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsAdapter.getContactList().clear();
                for (DataSnapshot snapshotChild : snapshot.getChildren()) {
                    User user = snapshotChild.getValue(User.class);
                    if (user != null && !currentUser.getUid().equals(user.getUserId())) {
                        contactsAdapter.getContactList().add(user);
                    }
                }

                contactsAdapter.getList().clear();
                contactsAdapter.getList().addAll(contactsAdapter.getContactList());
                recyclerView.setAdapter(contactsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void doSearch() {
        Editable text = searchEdittext.getText();
        if (text == null || text.length() == 0) {
            if (contactsAdapter.getContactList() != contactsAdapter.getList()) {
                contactsAdapter.getList().clear();
                contactsAdapter.setList(contactsAdapter.getContactList());
            }
        } else {
            String keyword = text.toString();
            List<User> contacts = contactsAdapter.getContactList();
            List<User> result = new ArrayList<>();
            for (User user : contacts) {
                if (user.getUserName().contains(keyword)) {
                    result.add(user);
                }
            }
            contactsAdapter.setList(result);
        }
        recyclerView.setAdapter(contactsAdapter);
    }
}