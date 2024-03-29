package com.comp90018.lovealarm.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.fragment.AlarmFragment;
import com.comp90018.lovealarm.fragment.ChatFragment;
import com.comp90018.lovealarm.fragment.ContactsFragment;
import com.comp90018.lovealarm.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.alarm);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment menuFragment = null;
        switch(item.getItemId()){
            case R.id.alarm:
                menuFragment = new AlarmFragment();
                break;
            case R.id.profile:
                menuFragment = new ProfileFragment();
                break;
            case R.id.contacts:
                menuFragment = new ContactsFragment();
                break;
            case R.id.chat:
                menuFragment = new ChatFragment();
                break;
        }
        if(menuFragment != null){
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            ft.replace(R.id.flFragment, menuFragment).commit();
            return true;
        }
        return false;
    }
}