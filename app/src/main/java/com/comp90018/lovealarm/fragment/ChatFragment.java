package com.comp90018.lovealarm.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.ContactsAdapter;
import com.comp90018.lovealarm.model.ChatList;
import com.comp90018.lovealarm.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    private ContactsAdapter contactsAdapter;
    private List<User> allUser;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<ChatList> chatLists;

    RecyclerView recyclerView;



    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_view2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        chatLists = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(fuser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatLists.clear();
                // loop for all chat list
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    ChatList chat = childSnapshot.getValue(ChatList.class);
                    chatLists.add(chat);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void chatList() {

        // get all the recent chats
        allUser = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUser.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    User user = childSnapshot.getValue(User.class);
                    for (ChatList chat: chatLists){
                        if (user.getUserId().equals(chat.getId())){
                            allUser.add(user);
                        }
                    }
                }
                contactsAdapter = new ContactsAdapter(getContext(), allUser);
                recyclerView.setAdapter(contactsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

}