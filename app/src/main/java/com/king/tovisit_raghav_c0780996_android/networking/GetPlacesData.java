package com.king.tovisit_raghav_c0780996_android.networking;

import android.os.AsyncTask;
import com.king.tovisit_raghav_c0780996_android.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetPlacesData extends AsyncTask<Object, String, String> {

    GoogleMap googleMap;
    String placeData, url;

    @Override
    protected String doInBackground(Object... objects) {


        googleMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        fetchURL FetchURL= new fetchURL();
        try{
            placeData = FetchURL.readURL(url);
        } catch (IOException e){
            e.printStackTrace();
        }

        return placeData;
    }
    @Override
    protected void onPostExecute(String s)
    {

        List<HashMap<String, String>> nearByPlaceList = null;
        DataParser parser = new DataParser();
        nearByPlaceList = parser.parseData(s);
        showNearbyPlaces(nearByPlaceList);
    }


    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList){

        for(int i=0; i<nearbyPlacesList.size(); i++){
            HashMap<String, String> place = nearbyPlacesList.get(i);

            String placeName = place.get("placeName");
            String vicinity = place.get("vicinity");
            double latitude = Double.parseDouble(place.get("lat"));
            double longitude = Double.parseDouble(place.get("lng"));
            String reference = place.get("reference");
            LatLng latLng = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placeName + "\n" + vicinity)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.nearbyplaces));
            googleMap.addMarker(markerOptions);

        }
    }
}
