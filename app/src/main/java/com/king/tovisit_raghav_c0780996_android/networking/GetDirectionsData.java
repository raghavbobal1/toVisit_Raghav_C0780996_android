package com.king.tovisit_raghav_c0780996_android.networking;

import android.os.AsyncTask;
import android.util.Log;
import com.king.tovisit_raghav_c0780996_android.networking.fetchURL;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class GetDirectionsData extends AsyncTask<Object, String, String> {

    String directionData, url;
    GoogleMap mMap;
    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {
        Log.i(TAG, "doInBackground: GET DIRECTION DATA ");
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        latLng = (LatLng) objects[2];

        fetchURL fetchURL= new fetchURL();
        try{
            directionData = fetchURL.readURL(url);
        } catch (IOException e){
            e.printStackTrace();
        }

        return directionData;
    }
}
