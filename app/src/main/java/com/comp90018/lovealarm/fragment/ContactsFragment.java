package com.comp90018.lovealarm.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.User;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {
    private RecyclerView recyclerView;

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

        ContactsAdapter adapter = new ContactsAdapter(contactsList);
        recyclerView = view.findViewById(R.id.recycler_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}