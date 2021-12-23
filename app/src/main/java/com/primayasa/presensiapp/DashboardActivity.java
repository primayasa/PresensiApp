package com.primayasa.presensiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private int jarak;
    private int lokasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        getJarak();

        TextView tanggalTV = findViewById(R.id.tanggalTV);
        SimpleDateFormat sdfTanggal = new SimpleDateFormat("dd MMMM yyyy");
        String currentDate = sdfTanggal.format(new Date());
        tanggalTV.setText(currentDate);

        String url="http://10.0.2.2:8000/api/auth/profile";
        int method = Request.Method.GET;
        Map<String,String> params=new HashMap<String, String>();

        SharedPreferences sharedPreferences = getSharedPreferences("user_login", MODE_PRIVATE);
        String token = "bearer" + sharedPreferences.getString("access_token", "");

        this.callApi(url, method, params, token, 1);

        displayTime();
    }

    public void presensiBtnOnClick(View view) {
        String url="http://10.0.2.2:8000/api/user/presensi";
        int method = Request.Method.POST;
        Map<String,String> params=new HashMap<String, String>();
        params.put("lokasi", String.valueOf(lokasi));
        params.put("jarak", String.valueOf(jarak));

        SharedPreferences sharedPreferences = getSharedPreferences("user_login", MODE_PRIVATE);
        String token = "bearer" + sharedPreferences.getString("access_token", "");

        this.callApi(url, method, params, token, 2);
    }

    public void logoutBtnOnClick(View view) {
        String url="http://10.0.2.2:8000/api/auth/logout";
        int method = Request.Method.POST;
        Map<String,String> params=new HashMap<String, String>();

        SharedPreferences sharedPreferences = getSharedPreferences("user_login", MODE_PRIVATE);
        String token = "bearer" + sharedPreferences.getString("access_token", "");

        this.callApi(url, method, params, token, 3);
    }

    private void callApi(String url, int method, Map<String,String> params, String token, int updateUI){

        RequestQueue requestQueue= Volley.newRequestQueue(DashboardActivity.this);
        StringRequest stringRequest=new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJSON = new JSONObject(response);
//                    String message = jsonObject.getString("message");
//                    Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_SHORT).show();
//                    Log.d("response", jsonObject.toString());
//                    callback.onSuccessResponse(Response);

                    if(updateUI==1){
                        updateProfile(responseJSON);
                    }else if(updateUI==2){
                        showPresensiStatus(responseJSON);
                    }else if(updateUI==3){
                        backToMainActivity(responseJSON);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(DashboardActivity.this,
                            "Error! Unable to post data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DashboardActivity.this,
                        "Response Failed", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                for (String i : params.keySet()) {
                    params.put(i,params.get(i));
                }

                return params;
            }

            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> params=new HashMap<String, String>();
                params.put("Accept","application/json");
                params.put("Content-Type","application/x-www-form-urlencoded");
                if(!token.equals("")){
                    params.put("Authorization", token);
                }
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void updateProfile(JSONObject responseJSON){
        try{
            JSONObject profile = responseJSON.getJSONObject("profile");

            TextView namaTV = findViewById(R.id.namaTV);
            namaTV.setText(profile.getString("nama"));

            TextView kelasTV = findViewById(R.id.kelasTV);
            kelasTV.setText(profile.getString("kelas"));

            TextView nisTV = findViewById(R.id.nisTV);
            nisTV.setText(profile.getString("nis"));

        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void showPresensiStatus(JSONObject responseJSON){
        try{
            TextView statusTV = findViewById(R.id.statusTV);
            Log.d("cek respon presensi", responseJSON.getString("message"));
            statusTV.setText(responseJSON.getString("message"));
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void backToMainActivity(JSONObject responseJSON) {
        try{
            String message = responseJSON.getString("message");
            Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_SHORT).show();
        }catch(JSONException e){
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        SharedPreferences settings = getSharedPreferences("user_login", Context.MODE_PRIVATE);
        settings.edit().clear().commit();

        finish();
    }

    private void displayTime(){
        final Handler someHandler = new Handler(getMainLooper());
        TextView jamTV = findViewById(R.id.jamTV);
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                jamTV.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);
    }

    private void getJarak(){
        TextView jarakTV = findViewById(R.id.jarakTV);
        TextView lokasiTV = findViewById(R.id.lokasiTV);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(ContextCompat.checkSelfPermission(DashboardActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(DashboardActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(DashboardActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("get jarak", "masuk sini");
                jarak = calculateDistance(location.getLatitude(), location.getLongitude());
                if(jarak<300){
                    lokasi = 1;
                    lokasiTV.setText("Dalam Sekolah");
                }else{
                    lokasi = 0;
                    lokasiTV.setText("Luar Sekolah");
                }

                jarakTV.setText(String.valueOf(jarak));
            }
        });
    }

    private int calculateDistance(double lat2, double lon2) {
        double lat1 = -7.576200056204967;
        double lon1 = 111.54414008193905;
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 1.609344 * 1000;

            Log.d("jarak", String.valueOf(dist));
            return (int) dist;
        }
    }
}