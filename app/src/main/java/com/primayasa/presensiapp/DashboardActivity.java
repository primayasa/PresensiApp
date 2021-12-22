package com.primayasa.presensiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    public void presensi(View view) {

    }

    public void lihatRekap(View view) {

    }

    public void logout(View view) {

    }

    private int getJarak(){
        return 1;
    }
}