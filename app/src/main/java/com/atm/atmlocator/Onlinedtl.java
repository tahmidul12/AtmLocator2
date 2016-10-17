package com.atm.atmlocator;

import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import modelClasses.BankModel;

public class Onlinedtl extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    double lat, lng;
    private LatLng atmLatLng;
    private String bname, batmname;
    private boolean detFetchSuc;

    //
    private TextView textv_atmName, textv_bankName, textv_address, textv_city, textv_state;
    private LinearLayout linearv_atm;
    private Cursor cursor;

    //for add
    AdView adView;
    private LinearLayout linearv_add;
    private ImageView imgv_atm;
    //
    private boolean forMyLoc = false;
    private String address, city, state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlinedtl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init layout components other than map
        textv_atmName = (TextView) findViewById(R.id.textv_atmName);
        textv_bankName = (TextView) findViewById(R.id.textv_bankName);
        textv_address = (TextView) findViewById(R.id.textv_address);
        textv_city = (TextView) findViewById(R.id.textv_city);
        textv_state = (TextView) findViewById(R.id.textv_state);
        //
        linearv_atm = (LinearLayout) findViewById(R.id.linearv_atm);
        //for adview
        adView = (AdView) findViewById(R.id.adView);
        linearv_add = (LinearLayout) findViewById(R.id.linearv_add);
        imgv_atm = (ImageView) findViewById(R.id.imgv_atm);
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

        // for users Current location
        if(bname.equalsIgnoreCase("My Location")){
            forMyLoc = true;
        }

        //setting toolbar title based on userlocMarker clicked or atm marker clicked
        if(forMyLoc)
            getSupportActionBar().setTitle("My Location");
        else
            getSupportActionBar().setTitle("Atm Details");
        //setting data received successfully from pre activity
        if(detFetchSuc && !forMyLoc){
            textv_atmName.setText(batmname);
            textv_bankName.setText(bname);
        }

        //setting the streetview
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.mapstreet);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        // cursor loading
        if(!forMyLoc)
            getSupportLoaderManager().initLoader(1, null, this);

        // getting users address city location detals and set to layout
        if(forMyLoc)
            getAndSetUserLoc();

        //setting adview
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        //adView.setOnClickListener((View.OnClickListener) new CustomAdListener(this));
        adView.setAdListener(new CustomAdListener());

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Log.d("SHAKIL", "4m onCreateLoader");
        String URL = "content://com.atmlocator.Bank/atms";
        Uri atmsUri = Uri.parse(URL);
        return new android.support.v4.content.CursorLoader(this, atmsUri, null, null, null, "bank");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;
        addDatatoList();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void addDatatoList() {

        String bname, batmname, baddress, lati, longi, state, city;
        boolean found = false;
        if (cursor.moveToFirst()) {
            do{
                bname = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                batmname = cursor.getString(cursor.getColumnIndex(AtmProvider.ATM_NAME));
                lati = cursor.getString(cursor.getColumnIndex(AtmProvider.LAT));
                longi = cursor.getString(cursor.getColumnIndex(AtmProvider.LONGI));
                baddress = cursor.getString(cursor.getColumnIndex(AtmProvider.ADDRESS));
                city = cursor.getString(cursor.getColumnIndex(AtmProvider.CITY));
                state = cursor.getString(cursor.getColumnIndex(AtmProvider.STATE));
                if(lat == Double.parseDouble(lati) && lng == Double.parseDouble(longi)) {
                    found = true;
                    break;
                }
                //listBank.add(new BankModel(bname, batmname, lat, longi, baddress, city, state, null));
                //adapter.notifyDataSetChanged();
            } while (cursor.moveToNext());
            if(found){
                textv_address.setText(baddress);
                textv_city.setText(city);
                textv_state.setText(state);
                found = false;
            }

        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            Log.d("SHAKIL", "yap back button clicked");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAndSetUserLoc(){
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses.size()>0) {
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
        }else{
            address = "Sorry address not found";
            city = "not found";
            state = "not found";
        }
        textv_atmName.setText("Street View");
        textv_bankName.setText("MY LOCATION");
        textv_address.setText(address);
        textv_city.setText(city);
        textv_state.setText(state);
        //
        if(imgv_atm.getVisibility() == View.VISIBLE || imgv_atm.getVisibility() == View.INVISIBLE)
           imgv_atm.setVisibility(View.GONE);
    }

    private class CustomAdListener extends com.google.android.gms.ads.AdListener {
        @Override
        public void onAdLoaded() {
            if(linearv_add != null && linearv_add.getVisibility() == View.GONE){
                linearv_add.setVisibility(View.VISIBLE);
            }
            Log.d("SHAKIL", "yap add loaded");
            super.onAdLoaded();
        }

        @Override
        public void onAdOpened() {
            Log.d("SHAKIL", "yap add opened");
            super.onAdOpened();
        }

        @Override
        public void onAdClosed() {
            if(linearv_add.getVisibility() == View.VISIBLE){
                //linearv_add.setVisibility(View.GONE);
            }
            Log.d("SHAKIL", "yap add closed");
            super.onAdClosed();
        }

        @Override
        public void onAdFailedToLoad(int i) {
            Log.d("SHAKIL", "yap add fail to load");
            super.onAdFailedToLoad(i);
        }

        @Override
        public void onAdLeftApplication() {
            Log.d("SHAKIL", "yap add left application");
            super.onAdLeftApplication();
        }
    }
}
