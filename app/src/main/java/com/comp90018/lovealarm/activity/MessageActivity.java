package com.comp90018.lovealarm.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.adapters.MessageAdapter;
import com.comp90018.lovealarm.model.Chat;
import com.comp90018.lovealarm.model.User;
import com.comp90018.lovealarm.services.SendMediaService;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    MediaRecorder audioRecorder;
    String audioPath;
    String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    List<String> mPermissionList = new ArrayList<>();

    private final int mRequestCode = 100;

    // ***********
    ImageView imageGallery;

    private ArrayList<String> selectedImages;
    // **********


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Widgets
//        imageView = findViewById(R.id.imageview_profile);
        username = findViewById(R.id.username);
        sendBtn = findViewById(R.id.btn_send);
        msgEditText = findViewById(R.id.text_send);

        // ********* image file ************
        imageGallery = findViewById(R.id.imgGallery);
        imageGallery.setOnClickListener(view -> {
            getGalleryImage();
        });

        // voice recorder
        audioRecorder = new MediaRecorder();
        RecordView recordView = (RecordView) findViewById(R.id.record_view);
        final RecordButton recordButton = (RecordButton) findViewById(R.id.record_button);

        recordButton.setRecordView(recordView);
        initPermission();
        recordButton.setListenForRecord(true);


        recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MessageActivity.this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
                Log.d("RecordButton", "RECORD BUTTON CLICKED");

            }
        });

        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        recordView.setCancelBounds(8);
        recordView.setSmallMicColor(Color.parseColor("#c2185b"));

        //prevent recording under one Second
        recordView.setLessThanSecondAllowed(false);
        recordView.setSlideToCancelText("Slide To Cancel");
        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0);
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");

                setUpRecording();

                try {
                    audioRecorder.prepare();
                    audioRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sendBtn.setVisibility(View.GONE);
                msgEditText.setVisibility(View.GONE);
                imageGallery.setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

                audioRecorder.reset();
                audioRecorder.release();
                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                sendBtn.setVisibility(View.VISIBLE);
                msgEditText.setVisibility(View.VISIBLE);
                imageGallery.setVisibility(View.VISIBLE);
                recordView.setVisibility(View.GONE);

            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                //limitReached to determine if the Record was finished when time limit reached.
                String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "onFinish");

                Log.d("RecordTime", time);

                try {
                    audioRecorder.stop();
                    audioRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sendBtn.setVisibility(View.VISIBLE);
                msgEditText.setVisibility(View.VISIBLE);
                imageGallery.setVisibility(View.VISIBLE);
                recordView.setVisibility(View.GONE);

                sendRecodingMessage(audioPath);
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");

                audioRecorder.reset();
                audioRecorder.release();

                File file = new File(audioPath);
                if (file.exists())
                    file.delete();


                sendBtn.setVisibility(View.VISIBLE);
                msgEditText.setVisibility(View.VISIBLE);
                imageGallery.setVisibility(View.VISIBLE);
                recordView.setVisibility(View.GONE);
            }
        });

        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
            }
        });

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
                if (!msg.equals("")) {
                    Date now = new Date();
                    String strDateFormat = "MM-dd HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    String date = sdf.format(now);
                    sendMessage(fuser.getUid(), userid, msg, date, "text");
                } else {
                    Toast.makeText(MessageActivity.this, "You cannot send an empty message!", Toast.LENGTH_LONG);
                }
                msgEditText.setText("");
                // hide the keyboard
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
                .child(fuser.getUid()).child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
                if (type.equals("audio")) {
                    chatRef.child("lastMessage").setValue("[audio message]"); // audio X 2
                } else {
                    chatRef.child("lastMessage").setValue(message);
                }
                chatRef.child("date").setValue(date);
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
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(fuser.getUid());
                }
                if (type.equals("audio")) {
                    chatRef2.child("lastMessage").setValue("[audio message]"); // X2
                } else {
                    chatRef2.child("lastMessage").setValue(message);
                }
                chatRef2.child("date").setValue(date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void readMessage(final String myID, final String userID, final String imageURL) {

        allChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allChat.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Chat chat = childSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myID) && chat.getSender().equals(userID) ||
                            chat.getReceiver().equals(userID) && chat.getSender().equals(myID)) {
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

    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private void initPermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MessageActivity.this, permissions[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.size() > 0) {
            // ask for permission
            ActivityCompat.requestPermissions(MessageActivity.this, permissions, mRequestCode);
        }
    }

    private void setUpRecording() {

        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(dir, File.separator + System.currentTimeMillis() + ".3gp");

        audioPath = file.getAbsolutePath();

        audioRecorder.setOutputFile(audioPath);
    }

    private void sendRecodingMessage(String audioPath) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("/Media/Recording/" + fuser.getUid() + "/" + System.currentTimeMillis());
        Uri audioFile = Uri.fromFile(new File(audioPath));
        storageReference.putFile(audioFile).addOnSuccessListener(success -> {
            Task<Uri> audioUrl = success.getStorage().getDownloadUrl();
            audioUrl.addOnCompleteListener(path -> {
                if (path.isSuccessful()) {
                    String url = path.getResult().toString(); // url
                    Date now = new Date();
                    String strDateFormat = "MM-dd HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    String date = sdf.format(now);
                    sendMessage(fuser.getUid(), userid, url, date, "audio"); // type == audio

                }
            });
        });

    }


    // ******************** send image files ***************************

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 300) {
            if (data != null) {
                selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

                Intent intent = new Intent(MessageActivity.this, SendMediaService.class);
                intent.putExtra("hisID", userid);
                intent.putExtra("myID", fuser.getUid());
                intent.putStringArrayListExtra("media", selectedImages);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    startForegroundService(intent);
                else startService(intent);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getGalleryImage();
                } else {
                    Toast.makeText(this, "Approve Pix Gallery permissions to select photos", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void getGalleryImage() {

        Options options = Options.init()
                .setRequestCode(300)                                           //Request code for activity results
                .setCount(5)                                                   //Number of images to restict selection count
                .setFrontfacing(true)                                         //Front Facing camera on start
                .setExcludeVideos(true)                                       //Option to exclude videos
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);     //Orientaion
                //.setPath(new ContextWrapper(getApplicationContext()).getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());                                       //Custom Path For media Storage


        if (selectedImages != null) {
            options.setPreSelectedUrls(selectedImages);
        }

        Pix.start(this, options);
    }

}