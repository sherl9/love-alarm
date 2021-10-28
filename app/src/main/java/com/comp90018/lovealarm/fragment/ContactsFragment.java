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
    private List<User> allUsers;    // all the users displayed in the contact fragment

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);


        // TODO get real contacts list
//        List<User> contactsList = new ArrayList<>();
//        for (int i = 0; i < 50; i++) {
//            contactsList.add(new User("000", "user - " + i, "test@test.com"));
//        }


//        contactsAdapter = new ContactsAdapter(contactsList);



        recyclerView = view.findViewById(R.id.recycler_contacts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        allUsers = new ArrayList<>();
        ReadUsers();
//        recyclerView.setAdapter(contactsAdapter);



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

    private void ReadUsers(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for(DataSnapshot snapshotchild : snapshot.getChildren()){
                        User user = snapshotchild.getValue(User.class);
                        assert user != null;
                        if (!user.getUserId().equals(firebaseUser.getUid())){
                            allUsers.add(user);
                        }
//                        break;

//                    }


                }
                contactsAdapter = new ContactsAdapter(getContext(), allUsers);
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
            if (contactsAdapter.getContacts() != contactsAdapter.getList()) {
                contactsAdapter.getList().clear();
                contactsAdapter.setList(contactsAdapter.getContacts());
            }
        } else {
            String keyword = text.toString();
            List<User> contacts = contactsAdapter.getContacts();
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