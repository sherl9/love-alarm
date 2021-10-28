package com.comp90018.lovealarm.fragment;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.model.Coordinate;
import com.comp90018.lovealarm.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
    TextView tv_admirersNum;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;
    private final long RANGE = 5;
    private static final double EARTH_RADIUS = 6378.137;


    private String userId;
    private User user;
    private Coordinate userLocation;
    private List <Coordinate> admirersLocations;
    private List <Coordinate> nearbyAdmirersLocations;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

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
        tv_admirersNum = view.findViewById(R.id.admirers_num);

        // ask for location permissions
        ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

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
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    user = task.getResult().getValue(User.class);
                    admirersLocations.add(userLocation);

                    // todo: for test, you can [1] uncomment the code below [2] set your location nearby "C9J5H968+JM" on the controller
//                    List<String> list = new ArrayList<>();
//                    list.add("GCRPrmtc1RQEpTEf9mHLh6ZocnA3");
//                    list.add("kKA1sTUHWRT339gE3fGjEEk3p2F3");
//                    user.setAdmirerIdList(list);

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
                            }
                        }
                    });
                    getNearbyAdmirers(admirersLocations);
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
    }

    // update nearby admirers
    public void getNearbyAdmirers(List <Coordinate> admirersLocations) {
        mMap.clear();
        nearbyAdmirersLocations.clear();

        // display markers on the map (not visible yet)
        for (Coordinate coordinate : admirersLocations) {
            LatLng latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
            if(coordinate.getUserId().equals(userId)) {
                mMap.addMarker(new MarkerOptions().position(latLng).title(coordinate.getUserId()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                admirersLocations.remove(coordinate);
            } else if(getDistance(userLocation.getLongitude(), userLocation.getLatitude(), coordinate.getLongitude(), coordinate.getLatitude()) < RANGE){
                nearbyAdmirersLocations.add(coordinate);
                mMap.addMarker(new MarkerOptions().position(latLng).title(coordinate.getUserId()));
            }
        }

        // update admirers number
        int admirersNum = nearbyAdmirersLocations.size();
        tv_admirersNum.setText(admirersNum+"");

        // update heart animation
        if (nearbyAdmirersLocations.size()>0) {
            lav_heart_origin.setVisibility(View.INVISIBLE);
            lav_heart_activated.setVisibility(View.VISIBLE);
        } else {
            lav_heart_origin.setVisibility(View.VISIBLE);
            lav_heart_activated.setVisibility(View.INVISIBLE);
        }
    }

    // calculate the distance between two nodes based on their coodinates
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

}