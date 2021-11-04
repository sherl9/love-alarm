package com.comp90018.lovealarm.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.comp90018.lovealarm.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SendMediaService extends Service {

    // **********
    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private String hisID, myID;
    private int MAX_PROGRESS;
    private ArrayList<String> images;
    // **********


    public SendMediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // ********* send image service ***************
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        hisID = intent.getStringExtra("hisID");
        myID = intent.getStringExtra("myID");
        images = intent.getStringArrayListExtra("media");
        MAX_PROGRESS = images.size();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createChannel();
        }

        startForeground(100, getNotification().build());

        for (int a = 0; a < images.size(); a++) {

            String fileName = images.get(a);
            uploadImage(fileName);
            builder.setProgress(MAX_PROGRESS, a + 1, false);
            manager.notify(600, builder.build());

        }

        builder.setContentTitle("Completed!")
                .setProgress(0, 0, false);
        manager.notify(600, builder.build());
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }


    // Progress Notifications

    private NotificationCompat.Builder getNotification() {

        builder = new NotificationCompat.Builder(this, "android")
                .setContentText("Sending photos")
                .setProgress(MAX_PROGRESS, 0, false)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(600, builder.build());
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel("android", "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.setLightColor(R.color.colorPrimary);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setDescription("Sending photos");
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

    }


    private void uploadImage(String fileName) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("/Media/Images/" + myID + "/" + System.currentTimeMillis());
        Uri uri = Uri.fromFile(new File(fileName));
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                task.addOnCompleteListener(uri -> {
                    if (uri.isSuccessful()) {
                        String url = uri.getResult().toString();

                        Date now = new Date();
                        String strDateFormat = "MM-dd HH:mm:ss";
                        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                        String date = sdf.format(now);
                        sendMessage(myID, hisID, url, date, "image"); // type == image
                    }
                });
            }
        });
    }


    private void sendMessage(String sender, String receiver, String message, String date, String type) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("date", date);
        hashMap.put("type", type);

        reference.child("Chats").push().setValue(hashMap);

        // adding user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myID).child(hisID);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(hisID);
                }
                chatRef.child("lastMessage").setValue("[image]");
                chatRef.child("date").setValue(date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(hisID).child(myID);

        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(myID);
                }
                chatRef2.child("lastMessage").setValue("[image]");
                chatRef2.child("date").setValue(date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


}