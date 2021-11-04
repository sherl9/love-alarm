package com.comp90018.lovealarm.fragment;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.activity.ContactProfileActivity;
import com.comp90018.lovealarm.model.Coordinate;
import com.comp90018.lovealarm.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlarmFragment extends Fragment {
    private GoogleMap mMap;
    private DatabaseReference dbRef;

    LottieAnimationView lav_heart_origin;
    LottieAnimationView lav_heart_activated;
    LottieAnimationView lav_heart_lover;
    TextView tv_admirersNum;
    ImageButton btn_map;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private SoundPool soundPool;

    private String userId;
    private User user;
    private Coordinate userLocation;
    private boolean isLoverNear;
    private User lover = new User();
    private List <Coordinate> admirersLocations;
    private List <Coordinate> nearbyAdmirersLocations;

    private final long MIN_TIME = 500;
    private final long MIN_DIST = 1;
    private final long RANGE = 5;
    private static final double EARTH_RADIUS = 6378.137;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

            // update the user location when the location has changed
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    try {
                        userLocation.setLatitude(location.getLatitude());
                        userLocation.setLongitude(location.getLongitude());
                        dbRef.child(userId).setValue(userLocation);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            // setup location manager
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        lav_heart_origin = view.findViewById(R.id.heart_origin);
        lav_heart_activated = view.findViewById(R.id.heart_activated);
        lav_heart_lover = view.findViewById(R.id.heart_lover);
        tv_admirersNum = view.findViewById(R.id.admirers_num);
        btn_map = view.findViewById(R.id.btn_map);

        // ask for location permissions
        ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        //load sound
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(getActivity(), R.raw.beep_2, 1);

        // hide the map
        mapFragment.getView().setVisibility(View.GONE);

        // get current user and user location
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userLocation = new Coordinate(userId);

        admirersLocations = new ArrayList<>();
        nearbyAdmirersLocations = new ArrayList<>();

        dbRef = FirebaseDatabase.getInstance().getReference("Location");

        // setup admirers
        FirebaseDatabase.getInstance().getReference("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting user data", task.getException());
                }
                else {
                    user = task.getResult().getValue(User.class);
                    admirersLocations.add(userLocation);

                    // determine if the person you like also likes you
                    FirebaseDatabase.getInstance().getReference("Users").child(user.getAlertUserId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting lover data", task.getException());
                            }
                            else {
                                // get lover if there is one
                                User alertUser = task.getResult().getValue(User.class);
                                if(alertUser.getAlertUserId().equals(userId)) {
                                    lover = alertUser;
                                }

                                // admirers locations setup
                                dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        } else {
                                            // get all user locations
                                            HashMap<String, JSONObject> snapshotValue = (HashMap<String, JSONObject>) task.getResult().getValue();
                                            String jsonString = new Gson().toJson(snapshotValue.values());
                                            Gson gson = new Gson();
                                            List <Coordinate> userLocations = gson.fromJson(jsonString, new TypeToken<List<Coordinate>>(){}.getType());

                                            // get admirers locations
                                            for(Coordinate coordinate : userLocations) {
                                                if (user.getAdmirerIdList().contains(coordinate.getUserId())) {
                                                    admirersLocations.add(coordinate);
                                                }
                                            }
                                            getNearbyAdmirers(admirersLocations);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        // update the map and status when location changed
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    // get all user locations
                    HashMap<String, JSONObject> snapshotValue = (HashMap<String, JSONObject>) snapshot.getValue();
                    String jsonString = new Gson().toJson(snapshotValue.values());
                    Gson gson = new Gson();
                    List <Coordinate> userLocations = gson.fromJson(jsonString, new TypeToken<List<Coordinate>>(){}.getType());

                    // update admirers locations
                    for (Coordinate coordinate : userLocations) {
                        if (user.getAdmirerIdList().contains(coordinate.getUserId()) || userId.equals(coordinate.getUserId())) {
                            for (int i=0;i<admirersLocations.size();i++) {
                                if (admirersLocations.get(i).equals(coordinate.getUserId())) {
                                    admirersLocations.set(i,coordinate);
                                }
                            }
                        }
                    }
                    getNearbyAdmirers(admirersLocations);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // show map
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.getView().setVisibility(View.VISIBLE);
                btn_map.setVisibility(View.INVISIBLE);
            }
        });
    }

    // update nearby admirers
    public void getNearbyAdmirers(List <Coordinate> admirersLocations) {
        mMap.clear();
        nearbyAdmirersLocations.clear();
        isLoverNear = false;

        // display markers and circle on the map
        for (Coordinate coordinate : admirersLocations) {
            LatLng latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
            if(coordinate.getUserId().equals(userId)) {
                mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(500)
                        .strokeWidth(3f)
                        .strokeColor(Color.rgb(241, 147, 156))
                        .fillColor(Color.argb(50, 241, 147, 156)));
                mMap.addMarker(new MarkerOptions().position(latLng).title("You")
                        .icon(BitmapFromVector(getActivity(), R.drawable.ic_marker_my)));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            } else if(getDistance(userLocation.getLongitude(), userLocation.getLatitude(), coordinate.getLongitude(), coordinate.getLatitude()) < RANGE){
                nearbyAdmirersLocations.add(coordinate);
                if (coordinate.getUserId().equals(lover.getUserId())) {
                    isLoverNear = true;
                    Marker loverMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapFromVector(getActivity(), R.drawable.ic_marker_lover)));
                    loverMarker.setTag(isLoverNear);
                } else {
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapFromVector(getActivity(), R.drawable.ic_marker_admirer)));
                }
            }
        }

        // update admirers number
        int admirersNum = nearbyAdmirersLocations.size();
        int formerNum = Integer.parseInt(tv_admirersNum.getText().toString());
        tv_admirersNum.setText(admirersNum+"");

        // notification sound
        if (formerNum < admirersNum || isLoverNear) {
            soundPool.play(1,1,1,0,0,1);
        }

        // update heart animation
        if (nearbyAdmirersLocations.size()>0) {
            if (isLoverNear) {
                lav_heart_origin.setVisibility(View.INVISIBLE);
                lav_heart_activated.setVisibility(View.INVISIBLE);
                lav_heart_lover.setVisibility(View.VISIBLE); // animation with lover
            } else {
                lav_heart_origin.setVisibility(View.INVISIBLE);
                lav_heart_activated.setVisibility(View.VISIBLE); // animation with admires
                lav_heart_lover.setVisibility(View.INVISIBLE);
            }
        } else {
            lav_heart_origin.setVisibility(View.VISIBLE); // animation without admires
            lav_heart_activated.setVisibility(View.INVISIBLE);
            lav_heart_lover.setVisibility(View.INVISIBLE);
        }

        // if click lover marker, go to profile page (only for lover)
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Boolean haveLover = (Boolean) (marker.getTag());
                if(haveLover != null && haveLover && lover != null) {
                    Intent i = new Intent(getActivity().getApplication(), ContactProfileActivity.class);
                    i.putExtra(ContactProfileActivity.KEY_USERID, lover.getUserId());
                    i.putExtra(ContactProfileActivity.KEY_USERNAME, lover.getUserName());
                    i.putExtra(ContactProfileActivity.KEY_DATE_OF_BIRTH, lover.getDob());
                    i.putExtra(ContactProfileActivity.KEY_AVATAR_NAME, lover.getAvatarName());
                    i.putExtra(ContactProfileActivity.KEY_BIO, lover.getBio());
                    startActivity(i);
                }
                return false;
            }
        });
    }



    // calculate the distance between two nodes based on their coordinates
    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double lng1 = Math.toRadians(longitude1);
        double lng2 = Math.toRadians(longitude2);
        double a = lat1 - lat2;
        double b = lng1 - lng2;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        s =  s * EARTH_RADIUS;
        System.out.println("Distance: " + s);
        return s;
    }

    // draw marker function
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}