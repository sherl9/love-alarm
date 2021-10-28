package com.comp90018.lovealarm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.comp90018.lovealarm.R;

public class ContactProfileActivity extends AppCompatActivity {
    public static final String KEY_USERNAME = "key_contact_profile_username";

    private TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        usernameTextView = findViewById(R.id.contact_profile_username);
        usernameTextView.setText(getIntent().getStringExtra(KEY_USERNAME));
    }
}