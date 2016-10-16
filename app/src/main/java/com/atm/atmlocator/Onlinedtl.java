package com.atm.atmlocator;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

import org.w3c.dom.Text;

public class Onlinedtl extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    double lat, lng;
    private LatLng atmLatLng;
    private String bname, batmname;
    private boolean detFetchSuc;

    //
    private TextView textv_atmName, textv_bankName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlinedtl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init layout components other than map
        textv_atmName = (TextView) findViewById(R.id.textv_atmName);
        textv_bankName = (TextView) findViewById(R.id.textv_bankName);

        // receving lat lng  and other details from previous activity
        detFetchSuc = false;
        if(getIntent() != null){
            lat = getIntent().getDoubleExtra("lat", 23.7917399);
            lng = getIntent().getDoubleExtra("lng", 90.4041357);
            bname = getIntent().getStringExtra("bname");
            batmname = getIntent().getStringExtra("batmname");
            atmLatLng = new LatLng(lat, lng);
            detFetchSuc = true;
        }else{
            Toast.makeText(getApplicationContext(), "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        }

        //setting data received successfully from pre activity
        if(detFetchSuc){
            textv_atmName.setText(batmname);
            textv_bankName.setText(bname);
        }

        //setting the streetview
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.mapstreet);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);


    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        if(atmLatLng != null){
            streetViewPanorama.setPosition(atmLatLng);
            StreetViewPanoramaCamera camera = new StreetViewPanoramaCamera.Builder().bearing(180).build();
            streetViewPanorama.animateTo(camera, 1000);
        }else{
            Toast.makeText(getApplicationContext(), "Sorry could't load Street View!!!", Toast.LENGTH_SHORT).show();
        }
    }
}
