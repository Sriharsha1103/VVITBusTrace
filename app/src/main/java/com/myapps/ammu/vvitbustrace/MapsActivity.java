package com.myapps.ammu.vvitbustrace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng location1=new LatLng(16.31713,80.47105);
    private LatLng location2=new LatLng(16.31713,80.47105);
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    String bus_route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        bus_route = i.getStringExtra("Bus_route").toString();
        Toast.makeText(this,"Bus Route No."+bus_route,Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location = new LatLng(16.31713,80.47105);
        CameraPosition position = CameraPosition.builder()
                .target(location)
                .zoom( 16f )
                .bearing( 0.0f )
                .tilt( 0.0f )
                .build();
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.animateCamera( CameraUpdateFactory
                .newCameraPosition( position ), null );
        Timer timer=new Timer();
        TimerTask timerTask= new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Background Perform",
                                "-------> Text from Background Perform");
                        //new DownloadTask().execute();
                        try {
                            cd = new ConnectionDetector(getApplicationContext());
                            isInternetPresent = cd.isConnectingToInternet();
                            if(isInternetPresent) {
                                downloadUrl();

                                Log.d("MSG","executed Download");
                            }
                            else
                                Toast.makeText(getApplicationContext(), "Internet Connection Error Please connect to working Internet connection and refresh", Toast.LENGTH_LONG).show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 60*1000);


        /*final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            DownloadTask jsonTask = new DownloadTask();
                            jsonTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 300*1000);*/

    }
    private void downloadUrl() throws IOException{
        Toast.makeText(MapsActivity.this, "Updating Location", Toast.LENGTH_SHORT).show();
        Log.d("MSG","executing DownloadURL Function");
        JSONParser parser = new JSONParser();
        String fileName="stream.json";
        String url="https://data.sparkfun.com/output/wp0Z3NGwQMiM6r1O0l7N.json?gte[timestamp]=2016-08-22T12:44:16.033Z&eq[bus_route_no]="+bus_route+"";
        File file;
        file = new File(this.getCacheDir(), fileName);
        System.out.println(" URL :"+url);
        try {
            BufferedInputStream in = null;
            FileOutputStream fout = null;
            try {
                in = new BufferedInputStream(new URL(url).openStream());
                if(in!=null) {
                    fout = new FileOutputStream(file);
                    byte data[] = new byte[1024];
                    int count;
                    while ((count = in.read(data, 0, 1024)) != -1) {
                        fout.write(data, 0, count);
                    }
                }
            }
            catch(NullPointerException e) {
                Toast.makeText(getApplicationContext(),"Please select a valid bus route",Toast.LENGTH_SHORT).show();
            }
                finally {
                if (in != null)
                    in.close();
                if (fout != null)
                    fout.close();
            }
            if(file.length()>2) {
                Object obj = parser.parse(new FileReader(file));

                JSONArray jsonArray = (JSONArray) obj;

                JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                String lat = (String) jsonObject.get("latitude_n");
                String longt = (String) jsonObject.get("longitude_e");
                location1 = new LatLng(Double.parseDouble(lat), Double.parseDouble(longt));

                jsonObject = (JSONObject) jsonArray.get(1);
                lat = (String) jsonObject.get("latitude_n");
                longt = (String) jsonObject.get("longitude_e");
                location2 = new LatLng(Double.parseDouble(lat), Double.parseDouble(longt));


                mMap.moveCamera(CameraUpdateFactory.newLatLng(location1));
               // Toast.makeText(this, "Location1 :- " + location1.latitude + ":" + location1.longitude + " ; Location2 :- " + location2.latitude + " :" + location2.longitude, Toast.LENGTH_SHORT);
                mMap.addPolyline(new PolylineOptions().add(location1, location2).width(5).color(Color.BLUE).geodesic(true));
            }
            else
            {
                Toast.makeText(this, "Please select a valid bus route", Toast.LENGTH_SHORT);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();

        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
    }
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try{
                // Fetching the data from web service
                cd = new ConnectionDetector(getApplicationContext());
                isInternetPresent = cd.isConnectingToInternet();
                if(isInternetPresent) {
                    downloadUrl();
                    Log.d("MSG","executed Download");
                }
                else
                    Toast.makeText(getApplicationContext(), "Internet Connection Error Please connect to working Internet connection and refresh", Toast.LENGTH_LONG).show();
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

}
class ConnectionDetector {
    private Context _context;

    public ConnectionDetector(Context context) {
        this._context = context;
    }

    public boolean isConnectingToInternet() {
        if (networkConnectivity()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(
                        "https://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(3000);
                urlc.setReadTimeout(4000);
                urlc.connect();
                // networkcode2 = urlc.getResponseCode();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                return (false);
            }
        } else
            return false;

    }

    private boolean networkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}