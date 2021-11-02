package com.comp90018.lovealarm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.activity.ContactAddActivity;
import com.comp90018.lovealarm.activity.ContactRequestActivity;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.User;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactsFragment extends Fragment {
    private TextInputEditText searchEdittext;
    private ActionMenuItemView searchLocalButton;
    private ActionMenuItemView addContactButton;
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton requestButton;

    private ContactsAdapter contactsAdapter;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsAdapter = new ContactsAdapter();

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

        searchLocalButton = view.findViewById(R.id.contacts_search_button);
        searchLocalButton.setOnClickListener(v -> doSearch());

        addContactButton = view.findViewById(R.id.contacts_add_button);
        addContactButton.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ContactAddActivity.class);
            v.getContext().startActivity(i);
        });

        requestButton = view.findViewById(R.id.contacts_request_button);
        requestButton.hide();

        doInitialize();

        return view;
    }

    private void doInitialize() {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // FIXME
//        users.child(currentUserId).get().addOnSuccessListener(dataSnapshot -> {
//            User currentUser = dataSnapshot.getValue(User.class);
//            assert currentUser != null;
//            currentUser.getContactRequestIdList().clear();
//            currentUser.getContactRequestIdList().add("J66QfCpwjyg5MkfoVoKZj0kgvs93");
//            currentUser.getContactIdList().clear();
//            currentUser.getContactIdList().add("J66QfCpwjyg5MkfoVoKZj0kgvs93");
//            currentUser.setAlertUserId("J66QfCpwjyg5MkfoVoKZj0kgvs93");
//            users.child(currentUserId).setValue(currentUser);
//        });

        users.child(currentUserId).child("contactIdList").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> contactIdList = new ArrayList<>();
                for (DataSnapshot child : Objects.requireNonNull(task.getResult()).getChildren()) {
                    contactIdList.add(child.getValue(String.class));
                }

                for (String userId : contactIdList) {
                    users.child(userId).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            User user = Objects.requireNonNull(task1.getResult()).getValue(User.class);
                            contactsAdapter.getContactList().add(user);
                            contactsAdapter.getList().add(user);
                            recyclerView.setAdapter(contactsAdapter);
                        }
                    });
                }
            }
        });

        users.child(currentUserId).child("contactRequestIdList").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot ignored : Objects.requireNonNull(task.getResult()).getChildren()) {
                    requestButton.show();
                    requestButton.setOnClickListener(v -> {
                        Intent i = new Intent(v.getContext(), ContactRequestActivity.class);
                        v.getContext().startActivity(i);
                    });
                    break;
                }
            }
        });
    }

    private void doSearch() {
        contactsAdapter.getList().clear();
        Editable text = searchEdittext.getText();

        if (text == null || text.length() == 0) {
            contactsAdapter.getList().addAll(contactsAdapter.getContactList());
        } else {
            for (User user : contactsAdapter.getContactList()) {
                if (user.getUserName().toLowerCase().contains(text.toString().toLowerCase())) {
                    contactsAdapter.getList().add(user);
                }
            }
        }

        recyclerView.setAdapter(contactsAdapter);
    }
}