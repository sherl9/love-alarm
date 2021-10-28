package com.comp90018.lovealarm.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String userId = "";
    private String userName = "";
    private String email = "";
    private String avatarName = "";
    private String bio = "";
    private String dob = "";
<<<<<<< Updated upstream
    private String alertUserId = "";
    private List<String> admirerIdList = new ArrayList<>();
=======
    private List<String> admirerIds;
>>>>>>> Stashed changes

    public User(String userId, String userName, String email, String avatarName, String bio, String dob) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.avatarName = avatarName;
        this.bio = bio;
        this.dob = dob;
        this.admirerIds = new ArrayList<>();;
    }

    public User() {
    }

    public User(String userId, String userName, String email) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarName = avatarURL;
    }

<<<<<<< Updated upstream
    public String getAlertUserId() {
        return alertUserId;
    }

    public void setAlertUserId(String alertUserId) {
        this.alertUserId = alertUserId;
    }

    public List<String> getAdmirerIdList() {
        return admirerIdList;
    }

    public void setAdmirerIdList(List<String> admirerIdList) {
        this.admirerIdList = admirerIdList;
=======
    public List<String> getAdmirerIds() {
        return admirerIds;
    }

    public void setAdmirerIds(List<String> admirerIds) {
        this.admirerIds = admirerIds;
>>>>>>> Stashed changes
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("avatarName", avatarName);
        result.put("dob", dob);
        result.put("bio", bio);
        return result;
    }
}
