package com.comp90018.lovealarm.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.comp90018.lovealarm.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileFragment extends Fragment {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;

    ImageView avatarImage;
    TextView editButton;
    TextView dateView;
    DatePickerDialog datePicker;
    String currentPhotoPath;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatarImage = view.findViewById(R.id.profileAvatar);
        editButton = view.findViewById(R.id.editPicture);
//        datePicker = view.findViewById(R.id.profileDatePicker);
        dateView = view.findViewById(R.id.profileDOBField);
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });
    }

    private void showBottomSheetDialog(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_profile);
        LinearLayout camera = bottomSheetDialog.findViewById(R.id.cameraLinearLayout);
        LinearLayout gallery = bottomSheetDialog.findViewById(R.id.galleryLinearLayout);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForCameraPermission();
                bottomSheetDialog.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Copy is Clicked ", Toast.LENGTH_LONG).show();
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    private void askForCameraPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERM_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               openCamera();
            } else {
                Toast.makeText(getActivity(), "Camera Permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera(){
//        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(camera, CAMERA_REQUEST_CODE);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.comp90018.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            File f = new File(currentPhotoPath);
            avatarImage.setImageURI(Uri.fromFile(f));
            Log.d("tag", "Absolute url of image is" + Uri.fromFile(f));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            getActivity().sendBroadcast(mediaScanIntent);
        }

        if(requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri contentUri = data.getData();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_" + getFileExt(contentUri);
            Log.d("tag", "Absolute url of image is" + imageFileName);
            avatarImage.setImageURI(contentUri);
        }
    }

    private String getFileExt(Uri contentUri){
        ContentResolver c = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


}