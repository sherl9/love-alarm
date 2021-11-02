package com.comp90018.lovealarm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.model.User;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactProfileActivity extends AppCompatActivity {
    public static final String KEY_USERNAME = "key_contact_profile_username";
    public static final String KEY_USERID = "key_contact_profile_userid";
    public static final String KEY_DATE_OF_BIRTH = "key_contact_profile_date_of_birth";
    public static final String KEY_AVATAR_NAME = "key_contact_profile_avatar_name";
    public static final String KEY_BIO = "key_contact_profile_bio";

    private TextView usernameTextView;
    private TextView dateOfBirthTextView;
    private TextView bioTextView;
    private CircleImageView avatar;
    private Button button;
    private SwitchMaterial alertSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        Intent intent = getIntent();
        String username = intent.getStringExtra(KEY_USERNAME);
        String userId = intent.getStringExtra(KEY_USERID);
        String dateOfBirth = intent.getStringExtra(KEY_DATE_OF_BIRTH);
        String avatarName = intent.getStringExtra(KEY_AVATAR_NAME);
        String bio = intent.getStringExtra(KEY_BIO);

        usernameTextView = findViewById(R.id.contact_profile_username);
        usernameTextView.setText(username);

        dateOfBirthTextView = findViewById(R.id.contact_profile_date_of_birth);
        dateOfBirthTextView.setText(dateOfBirth);

        bioTextView = findViewById(R.id.contact_profile_bio);
        bioTextView.setText(bio);

        button = findViewById(R.id.contact_profile_button);

        alertSwitch = findViewById(R.id.contact_profile_set_alert);

        avatar = findViewById(R.id.contact_profile_avatar);
        // load avatar
        if (!"".equals(avatarName.trim())) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference image = storageReference.child("avatars/" + avatarName);
            image.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(avatar));
        }

        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        users.child(currentUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User currentUser = Objects.requireNonNull(task.getResult()).getValue(User.class);
                assert currentUser != null;

                // set alert switch
                if (userId.equals(currentUser.getAlertUserId())) {
                    alertSwitch.setChecked(true);
                }

                alertSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // update current user
                    currentUser.setAlertUserId(isChecked ? userId : "");
                    users.child(currentUserId).setValue(currentUser);

                    // update target user
                    users.child(userId).get().addOnSuccessListener(dataSnapshot -> {
                        User user = dataSnapshot.getValue(User.class);
                        assert user != null;
                        List<String> admirerIdList = user.getAdmirerIdList();
                        if (isChecked) {
                            if (!admirerIdList.contains(currentUserId)) {
                                admirerIdList.add(currentUserId);
                            }
                        } else {
                            admirerIdList.remove(currentUserId);
                        }
                        users.child(userId).setValue(user);
                    });
                });

                // set button
                if (currentUser.getContactIdList().contains(userId)) {
                    button.setText("Chat");
                    button.setOnClickListener(view -> {
                        Intent i = new Intent(ContactProfileActivity.this, MessageActivity.class);
                        i.putExtra("userid", userId);
                        startActivity(i);
                    });
                } else if (currentUser.getContactRequestIdList().contains(userId)) {
                    button.setText("Accept");
                    button.setOnClickListener(view -> {
                        // TODO: add friend
                        currentUser.getContactRequestIdList().remove(userId);
                        if (!currentUser.getContactIdList().contains(userId)) {
                            currentUser.getContactIdList().add(userId);
                        }
                        users.child(currentUserId).setValue(currentUser);

                        users.child(userId).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                User user = Objects.requireNonNull(task1.getResult()).getValue(User.class);
                                assert user != null;
                                user.getContactRequestIdList().remove(currentUserId);
                                if (!user.getContactIdList().contains(currentUserId)) {
                                    user.getContactIdList().add(currentUserId);
                                }
                                users.child(userId).setValue(user);
                            }
                        });

                        button.setText("Chat");
                        button.setOnClickListener(v -> {
                            Intent i = new Intent(ContactProfileActivity.this, MessageActivity.class);
                            i.putExtra("userid", userId);
                            startActivity(i);
                        });
                    });
                } else {
                    button.setText("Add");
                    button.setOnClickListener(view -> users.child(userId).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            User user = Objects.requireNonNull(task2.getResult()).getValue(User.class);
                            assert user != null;
                            if (!user.getContactRequestIdList().contains(currentUserId)) {
                                user.getContactRequestIdList().add(currentUserId);
                                users.child(userId).setValue(user);
                            }
                        }
                    }));
                }
            }
        });
    }
}