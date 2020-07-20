package com.king.tovisit_raghav_c0780996_android.activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.king.tovisit_raghav_c0780996_android.R;
import com.king.tovisit_raghav_c0780996_android.databaseHelper.MapsDatabase;
import com.king.tovisit_raghav_c0780996_android.helperClass.Places;
import com.king.tovisit_raghav_c0780996_android.networking.fetchURL;
import com.king.tovisit_raghav_c0780996_android.networking.DataParser;
import com.king.tovisit_raghav_c0780996_android.networking.GetDirectionsData;
import com.king.tovisit_raghav_c0780996_android.networking.GetPlacesData;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private static final long WAIT_TIME = 5L;
    private static final int REQUEST_CODE = 1;
    private static final String TAG = "MAP";
    private static final int RADIUS = 1800;

    Location userLocation;
    Marker favoritePlace, startingLocation, User;
    Boolean isEditing = false;
    AlertDialog markerClickMenu;

    MapsDatabase mDatabase;
    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;
    Spinner mapTypeSpinner, nearbySelector;

    private String place_name;
    private Object[] dataTransfer;

    String s = null;
    Places mPlace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapTypeSpinner = findViewById(R.id.mapType);
        mapTypeSpinner.setSelection(1);

        nearbySelector = findViewById(R.id.nearByPlaces);
        nearbySelector.setSelection(0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDatabase = new MapsDatabase(this);

        findViewById(R.id.userLocationBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUpdater(userLocation);
            }
        });

        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMap.setMapType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        nearbySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Boolean isFvt = (mPlace != null);
                    mMap.clear();
                    if (User != null) {
                        User.remove();
                    }
                    User = null;
                    if (isFvt) {
                        FocusLocation(new LatLng(mPlace.getLat(), mPlace.getLng()));
                        mMap.addMarker(new MarkerOptions().position(new LatLng(mPlace.getLat(), mPlace.getLng()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.nearbyPlaces))
                                .title(mPlace.getName())).showInfoWindow();
                        userLocationMarker(userLocation);
                    } else {
                        CameraUpdater(userLocation);
                    }

                    String searchPlace = nearbySelector.getSelectedItem().toString();
                    String url = isFvt ? getUrl(mPlace.getLat(), mPlace.getLng(), searchPlace) :
                            getUrl(userLocation.getLatitude(), userLocation.getLongitude(), searchPlace);
                    dataTransfer = new Object[2];
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;
                    GetPlacesData getPlacesData = new GetPlacesData();
                    getPlacesData.execute(dataTransfer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        geocoder = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    userLocation = location;
                    userLocationMarker(userLocation);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private String getUrl(double lat, double lng, String nearByPlace) {
        StringBuilder placeUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        placeUrl.append("location=" + lat + "," + lng);
        placeUrl.append("&radius=" + RADIUS);
        placeUrl.append("&type=" + nearByPlace);
        placeUrl.append("&key=" + getString(R.string.google_maps_key));
        Log.d(TAG, "getDirectionUrl: " + placeUrl);
        return placeUrl.toString();
    }

    private String getDirectionUrl() {
        if (startingLocation == null) {
            System.out.println("Starting Location is null");
        }
        StringBuilder directionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        directionUrl.append("origin=" + startingLocation.getPosition().latitude + "," + startingLocation.getPosition().longitude);
        directionUrl.append("&destination=" + favoritePlace.getPosition().latitude + "," + favoritePlace.getPosition().longitude);
        directionUrl.append("&key=" + getString(R.string.google_maps_key));
        return directionUrl.toString();
    }

    @SuppressLint("MissingPermission")
    public void setUpActivity() {

        reqLocationUpdate();
        userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        userLocationMarker(userLocation);

        Intent i = getIntent();
        isEditing = i.getBooleanExtra("EDIT", false);
        Log.i(TAG, "isEditing: " + isEditing);
        mPlace = (Places) i.getSerializableExtra("selectedPlace");

        if (mPlace != null) {

            Log.i(TAG, "onMapReady: Place is not null good job");
            LatLng pos = new LatLng(mPlace.getLat(), mPlace.getLng());
            favoritePlace = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.fav))
                    .title(isEditing ? "Drag to change location" : mPlace.getName()).draggable(isEditing));

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(pos)
                    .zoom(15)
                    .bearing(0)
                    .tilt(45)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {

            CameraUpdater(userLocation);
        }

        if (!isEditing) {
            if (favoritePlace != null) {
                favoritePlace.showInfoWindow();
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    favoritePlace = marker;
                    dataTransfer = new Object[3];
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = getDirectionUrl();
                    Log.i(TAG, "directionURL: " + getDirectionUrl());
                    dataTransfer[2] = favoritePlace.getPosition();

                    GetDirectionsData getDirectionData = new GetDirectionsData();

                    getDirectionData.execute(dataTransfer);

                    try {
                        s = getDirectionData.get(WAIT_TIME, TimeUnit.SECONDS);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }

                    HashMap<String, String> distanceHashMap = null;
                    DataParser distanceParser = new DataParser();
                    distanceHashMap = distanceParser.parseDistance(s);
                    showMarkerClickedAlert(marker.getTitle(), distanceHashMap.get("distance"), distanceHashMap.get("duration"));
                    return true;
                }
            });

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                //Add new marker on long click
                public void onMapLongClick(LatLng latLng) {
                    MarkerOptions options = new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerrrr));
                    favoritePlace = mMap.addMarker(options);
                    favoritePlace.setTitle(getAddress(favoritePlace));
                    favoritePlace.showInfoWindow();
                }
            });

        } else {
            // When the user edits a location
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    favoritePlace = marker;
                }
            });

            //Unhiding the elements that allow user to update a location and set it as visited
            findViewById(R.id.editModeLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.nearByPlaces).setVisibility(View.INVISIBLE);
            final CheckBox visited = findViewById(R.id.visitedCheckBox);
            visited.setChecked(mPlace.getVisited());
            // update button
            findViewById(R.id.updateBTN).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newPlaceName = getAddress(favoritePlace);
                    Boolean success = mDatabase.updatePlace(mPlace.getId(), newPlaceName, visited.isChecked(),
                            favoritePlace.getPosition().latitude, favoritePlace.getPosition().longitude);
                    favoritePlace.setTitle(newPlaceName);
                    favoritePlace.showInfoWindow();
                    Toast.makeText(MapsActivity.this, success ? "Updated" : "Update failed", Toast.LENGTH_SHORT).show();
                    Intent mIntent = new Intent(MapsActivity.this, FavActivity.class);
                    startActivity(mIntent);
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!checkPermission()) {
            requestPermission();
        } else {
            setUpActivity();
        }
    }

    private void showMarkerClickedAlert(String address, String distance, String duration) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
        final View v = LayoutInflater.from(MapsActivity.this).inflate(R.layout.menu, null);
        alert.setView(v);

        TextView tvPlace = v.findViewById(R.id.place_name);
        TextView tvDist = v.findViewById(R.id.distance);
        TextView tvDur = v.findViewById(R.id.duration);

        tvPlace.setText(address);
        tvDist.setText("Is " + distance + " away");
        tvDur.setText("Takes " + duration + " to get here");

        if (mDatabase.numberOfResults(favoritePlace.getPosition().latitude, favoritePlace.getPosition().longitude) > 0) {
            Button b = v.findViewById(R.id.addToFvtBtn);
            b.setEnabled(false);
            b.setText("SAVED");
        }
        markerClickMenu = alert.create();
        markerClickMenu.show();
    }

    private void displayDirections(String[] directionsList) {
        int count = directionsList.length;

        for (int i = 0; i < count; i++) {
            PolylineOptions options = new PolylineOptions()
                    .color(Color.RED)
                    .width(8)
                    .addAll(PolyUtil.decode(directionsList[i]));
            mMap.addPolyline(options);
        }
    }

    public void saveFavorite() {
        place_name = favoritePlace.getTitle();
        if (mDatabase.addPlace(place_name, false, favoritePlace.getPosition().latitude, favoritePlace.getPosition().longitude)) {
            Toast.makeText(this, place_name + " has been added", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Addition unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, 100, 100);
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public String getAddress(Marker m) {
        try {
            List<Address> addresses = geocoder.getFromLocation(m.getPosition().latitude, m.getPosition().longitude, 1);
            return addresses.get(0).getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String format = simpleDateFormat.format(new Date());
        Log.d("MainActivity", "Current Timestamp: " + format);
        return format;
    }

    private boolean checkPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void reqLocationUpdate() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
    }

    private void CameraUpdater(Location location) {
        mPlace = null;
        userLocationMarker(location);

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(15)
                .bearing(0)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void FocusLocation(LatLng latLng) {
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(latLng)
                .zoom(15)
                .bearing(0)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void onMarkerClick(View view) {
        switch (view.getId()) {
            case R.id.addToFvtBtn:
                saveFavorite();
                break;
            case R.id.getDirBtn:

                mMap.clear();
                User = null;

                String[] directionsList;
                DataParser directionParser = new DataParser();
                directionsList = directionParser.parseDirections(s);
                displayDirections(directionsList);
                startingLocation = mMap.addMarker(new MarkerOptions().position(startingLocation.getPosition())
                        .title(startingLocation.getTitle()));
                favoritePlace = mMap.addMarker(new MarkerOptions().position(favoritePlace.getPosition())
                        .title(favoritePlace.getTitle()));
                favoritePlace.showInfoWindow();
                break;
            default:
                break;
        }
        markerClickMenu.dismiss();
    }

    private void userLocationMarker(Location l) {
        if (User != null) {
            User.remove();
            User = null;
        }

        LatLng home = new LatLng(l.getLatitude(), l.getLongitude());
        startingLocation = mMap.addMarker(new MarkerOptions()
                .position(home)
                .title("Current User Location")
                .icon(bitmapDescriptorFromVector(this, R.drawable.userloc))

        );
        User = startingLocation;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpActivity();
            } else {
                Toast.makeText(this, "Permission is required to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
