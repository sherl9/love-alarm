package com.comp90018.lovealarm.fragment;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.comp90018.lovealarm.R;
import com.comp90018.lovealarm.model.Coordinate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AlarmFragment extends Fragment {
    private GoogleMap mMap;
    private DatabaseReference databaseReference;

    LottieAnimationView lav_heart_origin;
    LottieAnimationView lav_heart_activated;
    TextView tv_fansNum;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;
    private final long RANGE = 5;
    private static final double EARTH_RADIUS = 6378.137;


    private String userId;
    private FirebaseUser user;
    private Coordinate userLocation;
    private List <Coordinate> fansLocations;
    private List <Coordinate> closeFansLocations;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    try {
                        userLocation.setLatitude(location.getLatitude());
                        userLocation.setLongitude(location.getLongitude());
                        databaseReference.child(userId).setValue(userLocation);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

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
        ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        lav_heart_origin = view.findViewById(R.id.heart_origin);
        lav_heart_activated = view.findViewById(R.id.heart_activated);
        tv_fansNum = view.findViewById(R.id.fans_num);

        lav_heart_origin.setVisibility(View.VISIBLE);
        lav_heart_activated.setVisibility(View.INVISIBLE);
        mapFragment.getView().setVisibility(View.GONE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        userLocation = new Coordinate(userId);
        fansLocations = new ArrayList<>();
        closeFansLocations = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Location");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    // from snapshot to coordinate list
                    Log.i(TAG, "onDataChange: " + snapshot.getValue());
                    HashMap<String, JSONObject> snapshotValue = (HashMap<String, JSONObject>) snapshot.getValue();
                    String jsonString = new Gson().toJson(snapshotValue.values());
                    Gson gson = new Gson();
                    fansLocations = gson.fromJson(jsonString, new TypeToken<List<Coordinate>>(){}.getType()); // todo: replace with the fans locations

                    // set markers in map
                    mMap.clear();
                    closeFansLocations.clear();
                    for (Coordinate coordinate : fansLocations) {
                        LatLng latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                        if(coordinate.getUserId().equals(userId)) {
                            mMap.addMarker(new MarkerOptions().position(latLng).title(coordinate.getUserId()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            fansLocations.remove(coordinate);
                        } else if(getDistance(userLocation.getLongitude(), userLocation.getLatitude(), coordinate.getLongitude(), coordinate.getLatitude()) < RANGE){
                            closeFansLocations.add(coordinate);
                            mMap.addMarker(new MarkerOptions().position(latLng).title(coordinate.getUserId()));
                        }
                    }

                    int fansNum = closeFansLocations.size();
                    Log.i(TAG, "onDataChange: nearby fans number: " + fansNum);

                    tv_fansNum.setText(fansNum+"");
                    if (closeFansLocations.size()>0) {
                        lav_heart_origin.setVisibility(View.INVISIBLE);
                        lav_heart_activated.setVisibility(View.VISIBLE);
                    } else {
                        lav_heart_origin.setVisibility(View.VISIBLE);
                        lav_heart_activated.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double lng1 = Math.toRadians(longitude1);
        double lng2 = Math.toRadians(longitude2);
        double a = lat1 - lat2;
        double b = lng1 - lng2;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        s =  s * EARTH_RADIUS;
        System.out.println(s);
        return s;
    }

}