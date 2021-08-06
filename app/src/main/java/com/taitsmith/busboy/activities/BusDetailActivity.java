package com.taitsmith.busboy.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.taitsmith.busboy.R;

import com.taitsmith.busboy.fragments.BusDetailFragment;

public class BusDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BusDetailFragment.newInstance())
                    .commitNow();
        }
    }
}
