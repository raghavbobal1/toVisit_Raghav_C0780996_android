package com.king.tovisit_raghav_c0780996_android.networking;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static android.content.ContentValues.TAG;

public class fetchURL
{
    public String readURL(String myUrl) throws IOException {

        Log.i(TAG, "readURL: ");

        String data = "";
        InputStream inputStream = null;

        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(myUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = reader.readLine()) != null)
                stringBuffer.append(line);
            data = stringBuffer.toString();
            reader.close();
        } catch(MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }

        return data;
    }
}
