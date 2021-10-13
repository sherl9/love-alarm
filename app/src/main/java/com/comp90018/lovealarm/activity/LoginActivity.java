package com.comp90018.lovealarm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.comp90018.lovealarm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText userETLogin, passETLogin;
    Button loginBtn, registerBtn;

    // Firebase:
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userETLogin = findViewById(R.id.enterEmail);
        passETLogin = findViewById(R.id.enterPassword);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        // Firebase Auth:
        auth = FirebaseAuth.getInstance();

        // Register Button:
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        // Login Button:
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_text = userETLogin.getText().toString();
                String pass_text = passETLogin.getText().toString();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();

//                // Checking if it is empty
//                if (TextUtils.isEmpty(email_text) || TextUtils.isEmpty(pass_text)) {
//                    Toast.makeText(LoginActivity.this, "Please fill the fields", Toast.LENGTH_SHORT).show();
//                } else {
//                    auth.signInWithEmailAndPassword(email_text, pass_text)
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()) {
//                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        startActivity(i);
//                                        finish();
//                                    } else {
//                                        Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                }
            }
        });

    }
}