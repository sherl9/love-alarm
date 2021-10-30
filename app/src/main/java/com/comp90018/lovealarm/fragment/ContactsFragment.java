package com.comp90018.lovealarm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
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
    private View searchLocal;
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

        searchLocal = view.findViewById(R.id.contacts_search_button);
        searchLocal.setOnClickListener(v -> doSearch());

        requestButton = view.findViewById(R.id.contacts_request_button);
//        addButton.shrink();
        requestButton.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ContactRequestActivity.class);
            v.getContext().startActivity(i);
        });

        getContactList();

        return view;
    }

    private void getContactList() {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
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