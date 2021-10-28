package com.comp90018.lovealarm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.comp90018.lovealarm.R;

public class ContactProfileActivity extends AppCompatActivity {
    public static final String KEY_USERNAME = "key_contact_profile_username";
    public static final String KEY_USERID = "key_contact_profile_userid";

    private TextView usernameTextView;
    private Button chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        Intent intent = getIntent();

        usernameTextView = findViewById(R.id.contact_profile_username);
        usernameTextView.setText(intent.getStringExtra(KEY_USERNAME));

        chatButton = findViewById(R.id.contact_profile_chat);
        chatButton.setOnClickListener(view -> {
            Intent i = new Intent(ContactProfileActivity.this, MessageActivity.class);
            i.putExtra("userid", intent.getStringExtra(KEY_USERID));
            startActivity(i);
        });
    }
}