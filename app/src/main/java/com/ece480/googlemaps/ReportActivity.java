package com.ece480.googlemaps;

import android.text.TextUtils;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Location;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataOutputStream;
public class ReportActivity extends AppCompatActivity {
    public EditText region;
    public EditText interstate;
    public EditText direction;
    public EditText county;
    public EditText municipality;
    public EditText nearest;
    public EditText message;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view)
            {
                /*if(TextUtils.isEmpty(region.getText().toString()) && TextUtils.isEmpty(interstate.getText().toString()) && TextUtils.isEmpty(direction.getText().toString()) && TextUtils.isEmpty(county.getText().toString()) && TextUtils.isEmpty(municipality.getText().toString()) && TextUtils.isEmpty(nearest.getText().toString())) {
                    sendPost();

                    Intent intent = new Intent(ReportActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(getBaseContext(), "Pothole Report Submitted.", Toast.LENGTH_SHORT).show();

                }
                else
                    {
                        Toast.makeText(getBaseContext(), "Some Required Fields are blank.", Toast.LENGTH_SHORT).show();
                    }*/
                Intent intent = new Intent(ReportActivity.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(getBaseContext(), "Pothole Report Submitted.", Toast.LENGTH_SHORT).show();
                sendPost();



            }
        });
    }

    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://potholedetectionapp.herokuapp.com//api/potholes/report");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("X-USER-TOKEN", "30b77b0a-cd10-4494-aa9e-a54fdbe7ff99");

                    region = (EditText) findViewById(R.id.regionEdit);
                    interstate = (EditText) findViewById(R.id.interstateEdit);
                    direction = (EditText) findViewById(R.id.directionEdit);
                    county = (EditText) findViewById(R.id.countyEdit);
                    municipality = (EditText) findViewById(R.id.municipalityEdit);
                    nearest = (EditText) findViewById(R.id.nearestEdit);
                    message = (EditText) findViewById(R.id.messageEdit);

                    JsonObject jsonParam = new JsonObject();
                    JsonObject potholeParam = new JsonObject();
                    potholeParam.addProperty("county", county.getText().toString());
                    potholeParam.addProperty("town", municipality.getText().toString());
                    potholeParam.addProperty("street", nearest.getText().toString());
                    potholeParam.addProperty("message", message.getText().toString());
                    potholeParam.addProperty("interstate", interstate.getText().toString());
                    potholeParam.addProperty("direction_of_travel", direction.getText().toString());
                    potholeParam.addProperty("region", region.getText().toString());
                    jsonParam.add("pothole", potholeParam );


                    Log.i("JSON", jsonParam.toString());
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
}
