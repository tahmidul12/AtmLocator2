package com.atm.atmlocator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class Online extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    private boolean mapReady = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("SHAKIL", "yap map ready");
        mapReady = true;
        mMap = googleMap;
        LatLng dhaka = new LatLng(23.7917399,90.4041357);
        LatLng mOffice = new LatLng(23.7936268,90.4005859);
        CameraPosition target = CameraPosition.builder().target(dhaka).zoom(15).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 5000, null);
    }
}
