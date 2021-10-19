package com.comp90018.lovealarm.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.User;
import com.google.android.material.textfield.TextInputEditText;

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

        // TODO get real contacts list
        List<User> contactsList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            contactsList.add(new User("000", "user - " + i, "test@test.com"));
        }

        contactsAdapter = new ContactsAdapter(contactsList);

        recyclerView = view.findViewById(R.id.recycler_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(contactsAdapter);

        searchEdittext = view.findViewById(R.id.search_edit_text);

        searchLocal = view.findViewById(R.id.contacts_search_button);
        searchLocal.setOnClickListener(v -> doSearch());

        return view;
    }

    private void doSearch() {
        Editable text = searchEdittext.getText();
        if (text == null || text.length() == 0) {
            if (contactsAdapter.getContacts() != contactsAdapter.getList()) {
                contactsAdapter.getList().clear();
                contactsAdapter.setList(contactsAdapter.getContacts());
            }
        } else {
            String keyword = text.toString();
            List<User> contacts = contactsAdapter.getContacts();
            List<User> result = new ArrayList<>();
            for (User user : contacts) {
                if (user.getUsername().contains(keyword)) {
                    result.add(user);
                }
            }
            contactsAdapter.setList(result);
        }
        recyclerView.setAdapter(contactsAdapter);
    }
}