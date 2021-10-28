package com.comp90018.lovealarm.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.MessageAdapter;
import com.comp90018.lovealarm.model.Chat;
import com.comp90018.lovealarm.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    TextView username;
    ImageView imageView;

    RecyclerView recyclerView;
    EditText msgEditText;
    ImageButton sendBtn;

    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;

    MessageAdapter messageAdapter;
    List<Chat> allChat;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Widgets
//        imageView = findViewById(R.id.imageview_profile);
        username = findViewById(R.id.username);
        sendBtn = findViewById(R.id.btn_send);
        msgEditText = findViewById(R.id.text_send);

        // go back
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUserName());
//                imageView.setImageResource(R.drawable.ic_heart);

                readMessage(fuser.getUid(), userid, user.getAvatarName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = msgEditText.getText().toString();
                if (!msg.equals("")){
                    Date now = new Date();
                    String strDateFormat = "MM-dd HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    String date = sdf.format(now);
                    sendMessage(fuser.getUid(), userid, msg, date);
                }
                else{
                    Toast.makeText(MessageActivity.this, "You cannot send an empty message!", Toast.LENGTH_LONG);
                }
                msgEditText.setText("");
                // hide the keyboard
            }
        });

    }


    private void sendMessage(String sender, String receiver, String message, String date){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("date", date);

        reference.child("Chats").push().setValue(hashMap);

        // adding user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid()).child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
                else{
                    chatRef.child("lastMessage").setValue(message);
                    chatRef.child("date").setValue(date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(userid).child(fuser.getUid());

        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef2.child("id").setValue(fuser.getUid());
                }
                else{
                    chatRef2.child("lastMessage").setValue(message);
                    chatRef2.child("date").setValue(date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void readMessage(final String myID, final String userID, final String imageURL){

        allChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allChat.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    Chat chat = childSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myID) && chat.getSender().equals(userID) ||
                    chat.getReceiver().equals(userID) && chat.getSender().equals(myID)){
                        allChat.add(chat);
                    }

                }
                messageAdapter = new MessageAdapter(MessageActivity.this, allChat, imageURL);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}