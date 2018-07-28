package com.ece480.googlemaps;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.*;
import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Location;
import android.os.AsyncTask;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataOutputStream;
import  java.util.List;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.Objects;
import java.lang.Boolean;
import java.lang.Object;
import java.util.Timer;
import 	java.util.TimerTask;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Console;
import java.net.HttpURLConnection;
import java.net.URL;
import 	android.view.KeyEvent;
import android.net.Uri;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener, DirectionCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private EditText mOrigin;
    private EditText mDestination;
    private LatLng mGlobalOrgin;
    private LatLng mGlobalDest;
    private LatLng mGlobalCurrentLoc;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean mIsRunning = false;
    private Button btnToggleDrive;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private float mZoomLevel = 18.0f;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_map);
        getLocationPermission();

        // get the sensor data
        mIsRunning = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Button btnFindRoute = (Button) findViewById(R.id.btnFindRoute);
        Button btnShowPotholes = (Button) findViewById(R.id.btnShowPotholes);
        btnToggleDrive = (Button) findViewById(R.id.btnToggleDrive);
        Button btnMyLocation = (Button) findViewById(R.id.btnMyLocation);

        
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mOrigin = (EditText) findViewById(R.id.etOrigin);
                mOrigin.setText("Current Location");
                getMyLocation();
            }
        });


        btnFindRoute.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mOrigin = (EditText) findViewById(R.id.etOrigin);
                mDestination = (EditText) findViewById(R.id.etDestination);
                try  {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
                findRoute();
            }
        });

        btnShowPotholes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                getPotholes();
            }
        });

        btnToggleDrive.setOnClickListener(new View.OnClickListener() {
           public void onClick(View view) {
               if (!mIsRunning) {
                   //stop
                   btnToggleDrive.setText("Stop Driving");
                   mZoomLevel = 18.0f;
                   startAccelerometerCapture();
                   startLocationUpdates();


               }else{
                   //run
                   btnToggleDrive.setText("Start Driving");
                   stopAccelerometerCapture();
                   stopLocationUpdates();

               }
           }
        });

    }


    public void getPotholes() {
        new FetchPotholeData().execute();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        Toast.makeText(this, "On Start", Toast.LENGTH_SHORT).show();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        stopAccelerometerCapture();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopAccelerometerCapture();

            Intent intent = new Intent(MapActivity.this,MainActivity.class);
            startActivity(intent);

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    protected void startLocationUpdates() {
        try {

            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Location update started ..............: ");

        } catch (SecurityException e) {
            e.printStackTrace();
        }


    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng((double) (location.getLatitude()),
                    (double) (location.getLongitude()));

            return p1;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return p1;
        }

    }

    public void findRoute() {
        String origin = mOrigin.getText().toString();
        String dest = mDestination.getText().toString();
        LatLng llorigin = getLocationFromAddress(origin);
        LatLng lldest = getLocationFromAddress(dest);

        if (origin.equals("Current Location")) {
            getDeviceLocation();
            llorigin = mGlobalCurrentLoc;
        }


        if (llorigin != null && lldest != null) {
            mGlobalOrgin = llorigin;
            mGlobalDest = lldest;
            GoogleDirection.withServerKey("AIzaSyAGBgNRlKa94Ff7rGScuGDtoXhrI1sbQwM")
                    .from(llorigin)
                    .to(lldest)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
            getPotholes();
        } else {
            Toast.makeText(this, "Valid Addresses must be entered", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Toast.makeText(this, "Success with status : " + direction.getStatus(), Toast.LENGTH_SHORT).show();
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);

            MarkerOptions dest = new MarkerOptions().position(mGlobalDest);
            //dest.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            dest.icon(getMarkerIcon("#00E5EE"));
            mMap.addMarker(dest);

            MarkerOptions orgn = new MarkerOptions().position(mGlobalOrgin);
            //dest.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            orgn.icon(getMarkerIcon("#00E5EE"));
            mMap.addMarker(orgn);

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            //Color tur = Color.valueOf(0,197,205);


            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.CYAN));
            setCameraWithCoordinationBounds(route);

            //btnRequestDirection.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, direction.getStatus(), Toast.LENGTH_SHORT).show();
        }
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    // Initialization of Map
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    // Move Camera to Current Location
    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     * Gets the current storage location
     * for where the sensor data will be saved
     * @return
     */
    private String getStorageDir() {
        return this.getExternalFilesDir(null).getAbsolutePath();
    }

    /**
     * Starts the capture of the accelerometer data
     * Call this function to start recording to a csv
     */
    private void startAccelerometerCapture() {
        //empty accelerometer recording

        mSensorManager.registerListener(MapActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mIsRunning = true;

    }

    /**
     * Algorithm to determine if a pothole was present given the three aceelerometer readings
     * if it was then report to the server
     * @param xA
     * @param yA
     * @param zA
     */
    private void checkForPothole(float xA, float yA, float zA, String timestamp) {
        if (mIsRunning && (Math.abs(xA) >= 10.0 || Math.abs(yA) >= 20.0 || Math.abs(zA) >= 10.0)){
            Toast.makeText(this, "Pothole detected and saved!", Toast.LENGTH_SHORT).show();

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mIsRunning = false;

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 2 seconds
                    mIsRunning = true;
                }
            }, 5000);

            reportPothole(xA, yA, zA,timestamp, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            LatLng latlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            MarkerOptions options = new MarkerOptions().position(latlng);
            options.icon(getMarkerIcon("#ff1744"));
            mMap.addMarker(options);

        }
    }

    private void reportPothole(float xA, float yA, float zA, String timestamp, double lat, double lon){
        final float xv = xA;
        final float yv = yA;
        final float zv = zA;
        final double latv = lat;
        final double lonv = lon;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                URL url = new URL("https://potholedetectionapp.herokuapp.com/api/potholes");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("X-USER-TOKEN", "30b77b0a-cd10-4494-aa9e-a54fdbe7ff99");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JsonObject jsonParam = new JsonObject();
                JsonObject potohleObj = new JsonObject();

                potohleObj.addProperty("gps_lat", latv);
                potohleObj.addProperty("gps_long", lonv);
                potohleObj.addProperty("accel_x", xv);
                potohleObj.addProperty("accel_y", yv);
                potohleObj.addProperty("accel_z", zv);

                jsonParam.add("pothole", potohleObj);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG", conn.getResponseMessage());

                conn.disconnect();


            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });
        thread.start();
    }

    /**county.getText().toString()
     * Writes the accelerometer data to the csv file
     * and calls itself again every .5 seconds
     * @param evt
     */
    public void onSensorChanged(SensorEvent evt) {
        if (mIsRunning) {
            switch(evt.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    //Toast.makeText(this, String.format("Z: %f", evt.values[2]), Toast.LENGTH_SHORT).show();

                    // check the accelerometer params
                    checkForPothole(evt.values[0], evt.values[1], evt.values[2], String.format("%d", evt.timestamp));
                    break;
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    //not needed

                    //mWriter.write(String.format("%d; GYRO_UN; %f; %f; %f; %f; %f; %f\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2], evt.values[3], evt.values[4], evt.values[5]));
                    break;
            }
        }
    }

    /**
     * Sensor event listener
     * @param sensor
     * @param i
     */
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Clear signal data
     * @param sensor
     */
    public void onFlushCompleted(Sensor sensor) {

    }

    /**
     * Stops the capture of accelerometer data and
     * uploads the data to teh server
     */
    private void stopAccelerometerCapture(){
        mIsRunning = false;
        mSensorManager.unregisterListener(MapActivity.this);
    }

    // Get Device Current Location
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            mGlobalCurrentLoc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    15f);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        //startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), mZoomLevel));

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            //startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            //------------------------------------------------------------------------------
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {

            //Toast.makeText(this,
              //      String.valueOf(mCurrentLocation.getLatitude()) + "\n"
                //            + String.valueOf(mCurrentLocation.getLongitude()),
                  //  Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,
                    "mLastLocation == null",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "permission was granted, :)",
                            Toast.LENGTH_LONG).show();
                    getMyLocation();

                } else {
                    Toast.makeText(this,
                            "permission denied, ...:(",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private class FetchPotholeData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("https://potholedetectionapp.herokuapp.com//api/potholes");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("X-USER-TOKEN", "30b77b0a-cd10-4494-aa9e-a54fdbe7ff99");
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                return forecastJsonStr;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            Log.i("json", s);
            JsonParser parser = new JsonParser();
            JsonArray potholearray = (JsonArray)parser.parse(s);
            try
            {
                for (int i = 0; i < potholearray.size(); i++)
                {
                    JsonObject iterate = (JsonObject)potholearray.get(i);
                    float lat = iterate.get("gps_lat").getAsFloat();
                    float lng = iterate.get("gps_long").getAsFloat();
                    LatLng latlng = new LatLng(lat, lng);
                    MarkerOptions options = new MarkerOptions().position(latlng);
                    options.icon(getMarkerIcon("#ff1744"));
                    mMap.addMarker(options);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 5.2f));
                }
            }
            catch(Exception e)
            {
                Log.i("tag","Nothing worked.");
            }
        }
    }

}
