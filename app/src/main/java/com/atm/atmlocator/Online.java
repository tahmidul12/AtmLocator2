package com.atm.atmlocator;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import apiconstant.ApiSearch;
import apiconstant.Constant;

import static com.atm.atmlocator.R.id.imButtonDir;
import static com.atm.atmlocator.R.id.map;
import static com.atm.atmlocator.R.id.myFabOnline;
import static com.atm.atmlocator.R.id.relateV;
import static com.atm.atmlocator.R.id.viewA;

/*
   using google map we have to implement the OnMapReadyCallback so when the map will be ready then we can add necessary attribute
   ex: circle, marker to the map
 */
public class Online extends AppCompatActivity implements OnMapReadyCallback , LoaderManager.LoaderCallbacks<Cursor>
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap mMap;
    private boolean mapReady = false;
    private TextView textv_seekOnline;
    private SeekBar seekBarOnline;
    private Circle circle;
    private List<Polygon> listPoly;
    private List<Marker> listMarker;
    private List<Polyline> listPolyline;
    private ImageButton imButtonDir;
    private RelativeLayout relateV;
    private LinearLayout lineout;
    private FloatingActionButton myFabOnline, myFabCurrentLoc;
    // for search menu on toolbar
    private ArrayList<String> stringArrayList;
    private ArrayAdapter<String> adapter;

    //as array adapter will not work
    private static final String[] SUGGESTIONS = {
            "Bauru", "Sao Paulo", "Rio de Janeiro",
            "Bahia", "Mato Grosso", "Minas Gerais",
            "Tocantins", "Rio Grande do Sul"
    };
    private final String cmarkerTitle = "Current Position";
    private SimpleCursorAdapter mAdapter;
    MatrixCursor c;
    public Cursor cursor;
    Button button;
    Marker marker;
    private LatLng clickedMarkerLatlng;
    Animation show_dirButton_anim, hide_diButton_anim, dirButton_rotate;
    private boolean showAnim = false;

    //added for custom infowindow
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;
    MapWrapperLayout mapWrapperLayout;
    //for user location
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double lat, lng;
    private LatLng userLatLng;
    private boolean apiConnected;
    private boolean uLocDetected;
    private boolean myLocMarkerAvailable = false;
    //for add
    AdView adView;
    private LinearLayout linearv_add;
    // for track user last circle latlng previous circle lat long
    LatLng preCirLatLng;
    // for app exit handle
    private boolean backPressedOnce = false;
    //
    Marker markerCentre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(R.style.FadeOnline);
        setContentView(R.layout.activity_onlines);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation((float)5);
        }
        //getSupportActionBar().setElevation(4);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(map);
        //added for custom infowindow
        mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);

        View mapView = mapFragment.getView();
        mapView.setOnTouchListener(new MapTouchListener());
        mapFragment.getMapAsync(this);

        //initialize layout components
        //added for custom infowindow
        this.infoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.info_window, null);
        this.infoTitle = (TextView)infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView)infoWindow.findViewById(R.id.snippet);
        this.infoButton = (Button)infoWindow.findViewById(R.id.button);
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.mipmap.infobtns),
                getResources().getDrawable(R.mipmap.infobtns))
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                //giving direction on Click this dir button
                LatLng origin = circle.getCenter();
                LatLng dest = marker.getPosition();

                String url = getDirectionsUrl(origin, dest);

                DownloadTask downloadTask = new DownloadTask();

                downloadTask.execute(url);

                Toast.makeText(Online.this, marker.getTitle() + "direction coming....", Toast.LENGTH_SHORT).show();
            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);

        //init variables for detecting user location
        uLocDetected = false;
        apiConnected = false;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks( this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = LocationRequest.create();
        /*button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContentResolver().delete(AtmProvider.CONTENT_URI, null, null);
            }
        });*/
        //for adview
        adView = (AdView) findViewById(R.id.adView);
        linearv_add = (LinearLayout) findViewById(R.id.linearv_add);
        //setting adview
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        //adView.setOnClickListener((View.OnClickListener) new CustomAdListener(this));
        adView.setAdListener(new CustomAdListener());
        //
        lineout = (LinearLayout) findViewById(R.id.viewA);
        relateV = (RelativeLayout) findViewById(R.id.relateV);
        myFabOnline = (FloatingActionButton) findViewById(R.id.myFabOnline);
        myFabOnline.setBackgroundTintList(ColorStateList.valueOf(Color
                .parseColor("#ffffff")));
        myFabCurrentLoc = (FloatingActionButton) findViewById(R.id.myFabCurrentLoc);

        myFabCurrentLoc.setBackgroundTintList(ColorStateList.valueOf(Color
                .parseColor("#ffffff")));
        myFabOnline.setOnClickListener(new ButtonClickListener());
        myFabCurrentLoc.setOnClickListener(new ButtonClickListener());
        imButtonDir = (ImageButton) findViewById(R.id.imButtonDir);
        imButtonDir.setOnClickListener(new ButtonClickListener());
        textv_seekOnline = (TextView) findViewById(R.id.textv_seekOnline);
        seekBarOnline = (SeekBar) findViewById(R.id.seekBarOnline);
        if (seekBarOnline != null) {
            seekBarOnline.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                int inprogress = 0;
                int previousProgress = 0;
                LatLngBounds bound;
                CameraUpdate cu;
                @Override
                public void onProgressChanged(SeekBar seekBar, int nowProgress, boolean b) {
                    //Log.d("SHAKIL", "progress i = "+nowProgress);
                    int diff = nowProgress - previousProgress;
                    double incrementInCirRadius = 0, decrementInCirRadius = 0;
                    double cirCurrentRadius = circle.getRadius();
                    double progressUnit = ((double) nowProgress * Constant.CIRCLE_INCREMENT_UNIT);
                    if(diff>0) {
                        if(cirCurrentRadius > progressUnit)
                            incrementInCirRadius = cirCurrentRadius - progressUnit;
                        else
                            incrementInCirRadius = progressUnit - cirCurrentRadius;
                        double radOfCir = circle.getRadius() + incrementInCirRadius;
                        // check for the radius max limit
                        if(radOfCir > Constant.CIRCLE_RADIUS_MAX)
                            radOfCir = Constant.CIRCLE_RADIUS_MAX;

                        //if(radOfCir > 0)
                        circle.setRadius(radOfCir);
                        //now adjust zoom to keep circle inside map and animate
                        bound = toBounds(circle.getCenter(), radOfCir);
                        mMap.setPadding(10, 10, 10, 30);
                        cu = CameraUpdateFactory.newLatLngBounds(bound, 0);
                        for(Polygon poly : listPoly) {
                            //poly.remove();
                            //Log.d("SHAKIL", "removed no:");
                        }
                        mMap.animateCamera(cu, 2000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                //setPolygon();
                                addMarker();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });



                    }else{
                        if(cirCurrentRadius > progressUnit)
                            decrementInCirRadius = cirCurrentRadius - progressUnit;
                        else
                            decrementInCirRadius = progressUnit - cirCurrentRadius;
                        double radOfCir = circle.getRadius() - decrementInCirRadius;
                        // check for neg radius neg radius will give error
                        if(radOfCir < Constant.CIRCLE_RADIUS_MIN)
                            radOfCir = Constant.CIRCLE_RADIUS_MIN;
                        circle.setRadius(radOfCir);
                        //now adjust zoom to keep circle inside map and animate
                        bound = toBounds(circle.getCenter(), radOfCir);
                        mMap.setPadding(10, 10, 10, 30);
                        cu = CameraUpdateFactory.newLatLngBounds(bound, 0);
                        for(Polygon poly : listPoly) {
                            //poly.remove();
                            //Log.d("SHAKIL", "removed no:");
                        }
                        mMap.animateCamera(cu, 2000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                //setPolygon();
                                removeMarker();
                                //removing the polylines outside circle
                                cutPolylineOutsideCircle();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });

                    }
                    previousProgress = nowProgress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //Log.d("SHAKIL", "on start tracking touch");
                    seekBar.setAlpha(1);
                    inprogress = seekBar.getProgress();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBar.setAlpha((float) 0.5);
                    removeMarker();
                    //newly modified
                    addMarker();
                    //Log.d("SHAKIL", "on stop tracking touch");
                }
            });
        }
        //init variables
        show_dirButton_anim = AnimationUtils.loadAnimation(getApplication(), R.anim.dir_button_show);
        hide_diButton_anim = AnimationUtils.loadAnimation(getApplication(), R.anim.dir_button_hide);
        dirButton_rotate = AnimationUtils.loadAnimation(getApplication(), R.anim.dir_button_rotate);
        listPoly = new ArrayList<Polygon>();
        listMarker = new ArrayList<Marker>();
        listPolyline = new ArrayList<Polyline>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringArrayList);

        //
        //initCursor();
        final String[] from = new String[] {"address"};
        final int[] to = new int[] {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        getSupportLoaderManager().initLoader(1, null, this);
    }

    // this is for setting the floating button properly for setting it device independently
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        int height = relateV.getMeasuredHeight();
        CoordinatorLayout.LayoutParams floatLayoutParams = (CoordinatorLayout.LayoutParams) myFabOnline.getLayoutParams();
        CoordinatorLayout.LayoutParams floatLayoutParams2 = (CoordinatorLayout.LayoutParams) myFabCurrentLoc.getLayoutParams();
        floatLayoutParams.topMargin = height - (int)(height/3);
        floatLayoutParams2.topMargin = height - (int)(height/3);
        //Log.d("SHAKIL", "now top margin of ="+floatLayoutParams.topMargin+ " and relate height="+height);
        myFabOnline.setLayoutParams(floatLayoutParams);
        myFabCurrentLoc.setLayoutParams(floatLayoutParams2);
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("SHAKIL", "yap map ready");
        mapReady = true;
        mMap = googleMap;
        LatLng dhaka = new LatLng(23.7917399,90.4041357);
        LatLng mOffice = new LatLng(23.7936268,90.4005859);
        //creating and adding a circle
        final CircleOptions circleOptions = new CircleOptions().center(preCirLatLng).radius(Constant.CIRCLE_RADIUS_MIN)
                .strokeColor(Color.BLUE).fillColor(0x5500ff00)
                .strokeWidth(3);
        circle = mMap.addCircle(circleOptions);

        //marker adding new for handling if previous circle is at users current location
        if(uLocDetected && userLatLng != null){
            if(preCirLatLng.longitude == userLatLng.longitude && preCirLatLng.latitude == userLatLng.latitude){
                MarkerOptions centreMarker = new MarkerOptions().position(circle.getCenter()).title("My Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.muloc));
                Marker marker1 = mMap.addMarker(centreMarker);
            }
            else{
                MarkerOptions centreMarker = new MarkerOptions().position(circle.getCenter()).title(cmarkerTitle)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.muloc));
                Marker marker1 = mMap.addMarker(centreMarker);
                listMarker.add(marker1);
            }
        }else{
        MarkerOptions centreMarker = new MarkerOptions().position(circle.getCenter()).title(cmarkerTitle)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.muloc));
        Marker marker1 = mMap.addMarker(centreMarker);
        listMarker.add(marker1);
        }
        // added
        //new
        mMap.setOnCameraChangeListener(new CamerachangeListener());
        mMap.setOnMapLongClickListener(new onMapLongClickListener());
        // adding marker click listener in googleMap
        mMap.setOnMarkerClickListener(new MarkerClickListener());

        //added for custom infowindow
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText(marker.getSnippet());
                infoButtonListener.setMarker(marker);

                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(!marker.getTitle().equalsIgnoreCase(cmarkerTitle) && !marker.getTitle().equalsIgnoreCase("My Location")) {
                LatLng mLatLng = marker.getPosition();
                Intent intent = new Intent(Online.this, Onlinedtl.class);
                intent.putExtra("bname", marker.getTitle());
                intent.putExtra("batmname", marker.getSnippet());
                intent.putExtra("lat", mLatLng.latitude);
                intent.putExtra("lng", mLatLng.longitude);
                startActivity(intent);
                }else if(marker.getTitle().equalsIgnoreCase("My Location")){
                    if(uLocDetected && userLatLng != null){
                        //
                        LatLng mLatLng = marker.getPosition();
                        Intent intent = new Intent(Online.this, Onlinedtl.class);
                        intent.putExtra("bname", marker.getTitle());
                        intent.putExtra("batmname", marker.getSnippet());
                        intent.putExtra("lat", mLatLng.latitude);
                        intent.putExtra("lng", mLatLng.longitude);
                        startActivity(intent);
                        //
                    }
                }else{

                }
                //Toast.makeText(getApplicationContext(), "info Window clicked", Toast.LENGTH_SHORT).show();
            }
        });
        //added finshed for custom infowindow
        //setting the camera with the specified location and animate in map
        CameraPosition target = CameraPosition.builder().target(preCirLatLng).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 1800, null);
    }
    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    public void setPolygon() {
        PolygonOptions polygonOptions = null;
        Polygon polygon = null;
        VisibleRegion bounds = mMap.getProjection().getVisibleRegion();
        polygonOptions =  new PolygonOptions()
                .add(new LatLng(bounds.latLngBounds.northeast.latitude, bounds.latLngBounds.northeast.longitude))
                .add(new LatLng(bounds.latLngBounds.southwest.latitude, bounds.latLngBounds.northeast.longitude))
                .add(new LatLng(bounds.latLngBounds.southwest.latitude, bounds.latLngBounds.southwest.longitude))
                .add(new LatLng(bounds.latLngBounds.northeast.latitude, bounds.latLngBounds.southwest.longitude))
                .strokeColor(Color.BLUE)
                .fillColor(0x5500ff00);

        polygon = mMap.addPolygon(polygonOptions);
        listPoly.add(polygon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_home, menu);
        //menu.findItem(R.id.action_search);

        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        View actionView = myActionMenuItem.getActionView();
        //AutoCompleteTextView searchView = (AutoCompleteTextView) actionView.findViewById(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setSuggestionsAdapter(mAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Log.d("SHAKIL", "select position = "+position );
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                if(marker != null){
                    marker.remove();
                    listMarker.remove(marker);
                    //check if this block may create a bug or not
                }
                //Log.d("SHAKIL", "click position = "+c.getString(position) );
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String feedName = cursor.getString(1);
                String lati = cursor.getString(2);
                String loti = cursor.getString(3);
                String bankName = cursor.getString(4);
                LatLng location = new LatLng(Double.parseDouble(lati), Double.parseDouble(loti));
                //adding a marker
                //newly added
                if(circle != null)
                    circle.remove();
                //remove polyline
                removePoly();
                circle = mMap.addCircle(new CircleOptions().center(location).radius(Constant.CIRCLE_RADIUS_MIN).strokeColor(Color.BLUE)
                        .fillColor(0x5500ff00)
                        .strokeWidth(3));
                //newly added
                MarkerOptions myOffice = new MarkerOptions().position(location).title(bankName)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.muloc));
                marker = mMap.addMarker(myOffice);
                listMarker.add(marker);
                //adjusting seekbar with newly created circle by setting it to 0
                seekBarOnline.setProgress(0);
                searchView.setQuery(feedName, false);
                searchView.clearFocus();
                //
                //     CameraPosition target = CameraPosition.builder().target(location).zoom(16).build();
                //     mMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 2000, null);
                //above two lines code are replaced by the following codes
                LatLngBounds bound;
                CameraUpdate cu;
                bound = toBounds(circle.getCenter(), circle.getRadius());
                mMap.setPadding(10, 10, 10, 30);
                cu = CameraUpdateFactory.newLatLngBounds(bound, 0);
                mMap.animateCamera(cu, 1000, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        removeMarker();
                        //herea a conflict may arise handle it
                        addMarker();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryLocation) {

                Geocoder gc = new Geocoder(getApplicationContext());
                boolean addGetSucful = true;
                LatLng foundLatLng;
                List<Address> listAddress = new ArrayList<Address>();
                LatLngBounds bound;
                CameraUpdate cu;

                try {
                    listAddress = gc.getFromLocationName(queryLocation + "Bangladesh", 1);
                } catch (IOException e) {
                    addGetSucful = false;
                    e.printStackTrace();
                }
                if(addGetSucful && listAddress != null){
                    Address locAdrs = null;
                    if(listAddress.size()>0) {
                        locAdrs = listAddress.get(0);
                        foundLatLng = new LatLng(locAdrs.getLatitude(), locAdrs.getLongitude());
                        Log.d("SHAKIL", "found latlng = " + foundLatLng + " and locality" + locAdrs.getLocality());
                        //now circe adding and map animating to that cir and also add marker to map and list
                        if (circle != null)
                            circle.remove();
                        //remove polyline
                        removePoly();
                        circle = mMap.addCircle(new CircleOptions().center(foundLatLng).radius(Constant.CIRCLE_RADIUS_MIN).strokeColor(Color.BLUE)
                                .fillColor(0x5500ff00)
                                .strokeWidth(3));
                        //newly added
                        MarkerOptions mySearchLocMarker = new MarkerOptions().position(foundLatLng).title(cmarkerTitle)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.muloc));
                        marker = mMap.addMarker(mySearchLocMarker);
                        listMarker.add(marker);
                        //now bound and animate to marker
                        bound = toBounds(circle.getCenter(), circle.getRadius());
                        mMap.setPadding(10, 10, 10, 30);
                        cu = CameraUpdateFactory.newLatLngBounds(bound, 0);
                        mMap.animateCamera(cu, 1000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                removeMarker();
                                //herea a conflict may arise handle it
                                addMarker();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext(), "Sorry, couldn't find any match", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Sorry, couldn't find any match", Toast.LENGTH_SHORT).show();
                }
                Log.d("SHAKIL", "yap query submitted");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                    Log.d("SHAKIL", "text being changed");
                   filterData(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_dbbl:
                //Log.d("SHAKIL", "bank selected="+item.getTitle().toString());
                Marker mar = getMarkerCentre();
                listMarker.clear();
                //Log.d("SHAKIL", "yap marker centre found="+mar.getPosition());
                listMarker.add(mar);
                //removeMarkerByBankName(item.getTitle().toString());
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_brack:
                Marker mar1 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar1);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_ab:
                //Log.d("SHAKIL", "bank selected="+item.getTitle().toString());
                //Log.d("SHAKIL", "bank selected="+item.getTitle().toString());
                Marker mar2 = getMarkerCentre();
                listMarker.clear();
                //Log.d("SHAKIL", "yap marker centre found="+mar2.getPosition());
                listMarker.add(mar2);
                //removeMarkerByBankName(item.getTitle().toString());
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_city:
                Marker mar3 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar3);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_ebl:
                Marker mar4 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar4);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_exim:
                Marker mar5 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar5);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_hsbc:
                Marker mar6 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar6);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_ific:
                Marker mar7 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar7);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_one:
                Marker mar8 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar8);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_premier:
                Marker mar9 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar9);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_prime:
                Marker mar10 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar10);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_scb:
                Marker mar11 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar11);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_seb:
                Marker mar12 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar12);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_ucbl:
                Marker mar13 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar13);
                addMarkerByBankName(item.getTitle().toString());
                break;
            case R.id.menu_dhaka:
                Marker mar14 = getMarkerCentre();
                listMarker.clear();
                listMarker.add(mar14);
                addMarkerByBankName(item.getTitle().toString());
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterInit() {
        c = new MatrixCursor(new String[]{ BaseColumns._ID, "cityName" });
        for (int i=0; i<SUGGESTIONS.length; i++) {
            //if (SUGGESTIONS[i].toLowerCase().startsWith(query.toLowerCase()))
                c.addRow(new Object[] {i, SUGGESTIONS[i]});
            Log.d("SHAKIL", "yap filteringadded " + SUGGESTIONS[i]);
        }
        mAdapter.changeCursor(c);
    }
    private void initCursor(){
        c = new MatrixCursor(new String[]{ BaseColumns._ID, "cityName" });
        for (int i=0; i<SUGGESTIONS.length; i++) {
            //if (SUGGESTIONS[i].toLowerCase().startsWith(query.toLowerCase()))
            c.addRow(new Object[] {i, SUGGESTIONS[i]});
            //Log.d("SHAKIL", "yap filteringadded " + SUGGESTIONS[i]);
        }
    }
    private void filter(String query) {
        c = new MatrixCursor(new String[]{ BaseColumns._ID, "cityName" });
        for (int i=0; i<SUGGESTIONS.length; i++) {
            if (SUGGESTIONS[i].toLowerCase().startsWith(query.toLowerCase()))
                c.addRow(new Object[] {i, SUGGESTIONS[i]});
            Log.d("SHAKIL", "yap filtering");
        }
        mAdapter.changeCursor(c);
    }
    private void filterData(String query){
        int i=0;
        c = new MatrixCursor(new String[]{ BaseColumns._ID, "address", "lat", "longi", "bank"});
        // next line has a bug potential so handle it
        if (cursor.moveToFirst()) {
            do{
                String addrs = cursor.getString(cursor.getColumnIndex(AtmProvider.ADDRESS));
                String lat = cursor.getString(cursor.getColumnIndex(AtmProvider.LAT));
                String longi = cursor.getString(cursor.getColumnIndex(AtmProvider.LONGI));
                String bankName = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                //Log.d("SHAKIL", "address="+addrs);
                if(addrs.toLowerCase().contains(query.toLowerCase()) || addrs.toLowerCase().contains(query)){
                    c.addRow(new Object[] {i, addrs, lat, longi, bankName});
                    //Log.d("SHAKIL", "total similar matches="+i);
                }
                i++;
            } while (cursor.moveToNext());
        }
        //Log.d("SHAKIL", "total similar found="+c.getCount());
        mAdapter.changeCursor(c);
    }
    private void addMarker () {
        if (cursor.moveToFirst()) {
            do{
                double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(AtmProvider.LAT)));
                double longi = Double.parseDouble(cursor.getString(cursor.getColumnIndex(AtmProvider.LONGI)));
                String bankName = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                String batmNmae = cursor.getString(cursor.getColumnIndex(AtmProvider.ATM_NAME));
                LatLng from = circle.getCenter();
                LatLng to = new LatLng(lat, longi);
                if(isInsideCircle(from, to) && from.latitude != to.latitude && from.longitude != to.longitude) {
                    //adding a marker
                    MarkerOptions myOffice = new MarkerOptions().position(to).snippet(batmNmae).title(bankName)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.litemlogo));
                    Marker marker1 = mMap.addMarker(myOffice);
                    listMarker.add(marker1);
                }
            } while (cursor.moveToNext());
        }
    }
    private void removeMarker(){
        LatLng from = circle.getCenter();
        Log.d("SHAKIL", "now circle centre = "+from);
        for (Marker marker : listMarker){
            LatLng to = marker.getPosition();

            if(!isInsideCircle(from, to))
                marker.remove();
            //new added
            if(uLocDetected && userLatLng != null) {
                if (to.latitude == userLatLng.latitude && to.longitude == userLatLng.longitude)
                    marker.remove();
            }

            //to remove the previous circles centre marker
            if(to.latitude != from.latitude && to.longitude != from.longitude && marker.getTitle().equalsIgnoreCase(cmarkerTitle)) {
                marker.remove();
                Log.d("SHAKIL", "yap here pcircle = "+to+" and new circle = "+from+" and now it is to be removed to will be removed");
            }
        }
    }

    private void removeMarkerByBankName(String bankName){
        if(listMarker.size()>0){
            Log.d("SHAKIL", "now listMarker size = "+listMarker.size());
            for(Marker marker : listMarker){

                if(!marker.getTitle().equalsIgnoreCase(cmarkerTitle)){
                    //listMarker.remove(marker);
                    marker.remove();
                }else{
                    markerCentre = marker;
                }
            }
            Log.d("SHAKIL", "now after remoging listMarker size = "+listMarker.size());
        }
    }

    private Marker getMarkerCentre() {
        Marker markerc = null;
        if(listMarker.size()>0){

            Log.d("SHAKIL", "now listMarker size = "+listMarker.size());
            for(Marker marker : listMarker){

                if(marker.getTitle().equalsIgnoreCase(cmarkerTitle)) {
                    markerc = marker;
                }else{
                    marker.remove();
                }
            }

            //Log.d("SHAKIL", ""+listMarker.size());
        }
        return markerc;
    }

    private void addMarkerByBankName(String bankNames){
        if (cursor.moveToFirst()) {
            do{
                double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(AtmProvider.LAT)));
                double longi = Double.parseDouble(cursor.getString(cursor.getColumnIndex(AtmProvider.LONGI)));
                String bankName = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                String batmNmae = cursor.getString(cursor.getColumnIndex(AtmProvider.ATM_NAME));
                LatLng from = circle.getCenter();
                LatLng to = new LatLng(lat, longi);
                if(isInsideCircle(from, to) && from.latitude != to.latitude && from.longitude != to.longitude && bankName.equalsIgnoreCase(bankNames)) {
                    //adding a marker
                    MarkerOptions myOffice = new MarkerOptions().position(to).snippet(batmNmae).title(bankName)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.litemlogo));
                    Marker marker1 = mMap.addMarker(myOffice);
                    listMarker.add(marker1);
                }
            } while (cursor.moveToNext());
        }
        /*if(listMarker.size()>0){
            for(Marker marker: listMarker){
                if(marker.getTitle().equalsIgnoreCase(bankName)){
                    Marker markers = mMap.addMarker(new MarkerOptions().position(marker.getPosition()).snippet(marker.getSnippet()).title(marker.getTitle())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.litemlogo)));
                    //listMarker.add(markers);
                }
            }
        }*/
    }

    //below 6 functions for handling user current location

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

        boolean catched = false;
        try {
            preCirLatLng = new LatLng(circle.getCenter().latitude, circle.getCenter().longitude);
        } catch (Exception e) { e.printStackTrace(); catched = true;}
        if(catched){
            preCirLatLng = new LatLng(Constant.DEFAULT_CIRCLE_LAT, Constant.DEFAULT_CIRCLE_LNG);
        }

        SharedPreferences preferences = getSharedPreferences(Constant.SHARED_PREFERENCE_ATMLOCATOR, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constant.CIRCLE_LAST_LAT, Double.toString(preCirLatLng.latitude));
        editor.putString(Constant.CIRCLE_LAST_LNG, Double.toString(preCirLatLng.longitude));
        editor.commit();
        Log.d("SHAKIL", "yap last circle details inserted");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        apiConnected = true;
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100000);
        mLocationRequest.setSmallestDisplacement(20);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else{
//            LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiConnected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        apiConnected = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            Log.d("SHAKIL", "yap now location = "+location.toString());

            lat = location.getLatitude();
            lng = location.getLongitude();
            userLatLng = new LatLng(lat, lng);
            uLocDetected = true;
        }
    }

    private class CamerachangeListener implements GoogleMap.OnCameraChangeListener{

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            //Log.d("SHAKIL", "camera pos changed");
        }
    }
    private class MapTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    Log.d("SHAKIL", "map touched started//////////");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("SHAKIL", "map touched stopped..........");
                    break;
            }
            return false;
        }
    }

    /*
     loader to load cursor which will provide the data for searchview as it needs a cursoradapter for suggestionAdapter
     so a cursor containing all atms data should be provided
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("SHAKIL", "4m onCreateLoader");
        String URL = "content://com.atmlocator.Bank/atms";
        Uri atmsUri = Uri.parse(URL);
        return new android.support.v4.content.CursorLoader(this, atmsUri, null, null, null, "bank");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Toast.makeText(getApplicationContext(), "cursor row no : "+data.getCount(), Toast.LENGTH_SHORT).show();
        if(data.getCount() <1)
            new DataAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            cursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /*
    this will execute to load the data from server to the sqlite databse if cursor.getCount=0 means
    no data is in the sqlite table atms
     */

    private class DataAsync extends AsyncTask<Void, Void, String> {
        String result = null;
        JSONObject jsonObject;
        JSONArray jsonArray;
        //but what the hack is going
        // but also for the last unknown situation
        @Override
        protected String doInBackground(Void... params) {
            String url = ApiSearch.SEARCHVIEW_API;
            result = new HttpDataHandler().GetHTTPData(url);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            String bank = null, atm_name = null, lat = null, longi = null, address = null
                    , city = null, state = null, country = null;
            String grade = null;
            //Log.d("SHAKIL", "yap students data = "+ result + "\n now time is=" + DateFormat.getDateTimeInstance().format(new Date()));
            //stringBuffer.append(DateFormat.getDateTimeInstance().format(new Date())  + "\n");
            if(result != null) {
                try {
                    jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        bank = jsonArray.getJSONObject(i).getString("Bank");
                        atm_name = jsonArray.getJSONObject(i).getString("Atm_Name");
                        lat = jsonArray.getJSONObject(i).getString("Lat");
                        longi = jsonArray.getJSONObject(i).getString("Longi");
                        address = jsonArray.getJSONObject(i).getString("Address");
                        city = jsonArray.getJSONObject(i).getString("City");
                        state = jsonArray.getJSONObject(i).getString("State");
                        country = jsonArray.getJSONObject(i).getString("Country");
                        String ids = jsonArray.getJSONObject(i).getString("Id");
                        //Log.d("SHAKIL", "yap retrieved id = "+ids);
                        ContentValues values = new ContentValues();

                        values.put(AtmProvider.BANK, bank);
                        values.put(AtmProvider.ATM_NAME, atm_name);
                        values.put(AtmProvider.LAT, lat);
                        values.put(AtmProvider.LONGI, longi);
                        values.put(AtmProvider.ADDRESS, address);
                        values.put(AtmProvider.CITY, city);
                        values.put(AtmProvider.STATE, state);
                        values.put(AtmProvider.COUNTRY, country);
                        values.put(AtmProvider.IDS, ids);


                        Uri uri = getContentResolver().insert(
                                AtmProvider.CONTENT_URI, values);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                restartLoader();
            }else{
                Toast.makeText(getApplicationContext(), "Check your net connection Please", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }
    private void restartLoader() {
        Log.d("SHAKIL", "yap restarting the loader after getting data from server");
        getSupportLoaderManager().restartLoader(1, null, this);
    }
    private void resetDb(View v){
        getContentResolver().delete(AtmProvider.CONTENT_URI, null, null);
    }
    private boolean isInsideCircle(LatLng from, LatLng to){
        double dis = SphericalUtil.computeDistanceBetween(from, to);
        double rad = circle.getRadius();
        if(dis <= rad)
            return true;
        return false;
    }

   private class MarkerClickListener implements GoogleMap.OnMarkerClickListener {

       @Override
       public boolean onMarkerClick(Marker marker) {
           //remove poly on click of another marker
           removePoly();

           imButtonDir.setVisibility(View.VISIBLE);
           if(!imButtonDir.isClickable())
               imButtonDir.setClickable(true);
           if(!showAnim){
               RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imButtonDir.getLayoutParams();
               layoutParams.rightMargin += (int) (imButtonDir.getWidth() * 1.17);
               imButtonDir.setLayoutParams(layoutParams);
               imButtonDir.startAnimation(show_dirButton_anim);
               showAnim = true;
           }else{
               imButtonDir.startAnimation(dirButton_rotate);
           }
           clickedMarkerLatlng = marker.getPosition();
           return false;
       }
   }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.imButtonDir) {
                LatLng origin = circle.getCenter();
                LatLng dest = clickedMarkerLatlng;

                String url = getDirectionsUrl(origin, dest);

                DownloadTask downloadTask = new DownloadTask();

                downloadTask.execute(url);

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imButtonDir.getLayoutParams();
                layoutParams.rightMargin -= (int) (imButtonDir.getWidth() * 1.17);
                imButtonDir.setLayoutParams(layoutParams);
                imButtonDir.startAnimation(hide_diButton_anim);
                imButtonDir.setClickable(false);
                //imButtonDir.setAlpha(0);
                showAnim = false;
                if(imButtonDir.getVisibility() == View.VISIBLE)
                     imButtonDir.setVisibility(View.INVISIBLE);
                //then animate camera and zoom to those two points only for a better view of the fetched direction from marker to circle centre
                LatLngBounds bound;
                CameraUpdate cu;
                bound = toBounds(origin, SphericalUtil.computeDistanceBetween(origin, dest));
                mMap.setPadding(10, 10, 10, 30);
                cu = CameraUpdateFactory.newLatLngBounds(bound, 0);
                mMap.animateCamera(cu, 1000, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
            else if(v.getId() == R.id.myFabOnline){
                Intent intent = new Intent(Online.this, Offline.class);
                startActivity(intent);
                //check
                finish();
                //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }else if(v.getId() == R.id.myFabCurrentLoc){
                showUserLocOnMap();
            }

        }
    }

    // onclick of myFabCurrent loc user location will be shown on the map
    private void showUserLocOnMap(){
        LatLngBounds bound;
        CameraUpdate cu;
       if(uLocDetected && mapReady){
           // check to remove polyline here and when searchview click happen as a new circle add we should remove polylines
           // removing poly created on previous circle
           removePoly();
           if(circle != null)
               circle.remove();
           circle = mMap.addCircle(new CircleOptions().center(userLatLng).radius(Constant.CIRCLE_RADIUS_MIN).strokeColor(Color.BLUE).fillColor(0x5500ff00)
                   .strokeWidth(3));
           // adding a marker at the centre of the newly created circle
           // when user will click the current user loc button only then marker will be added later on as the marker is added so no need to add
           // marker anymore as it is not removed once set
           if(!myLocMarkerAvailable) {
               MarkerOptions centreMarker = new MarkerOptions().position(circle.getCenter()).snippet("").title("My Location")
                       .icon(BitmapDescriptorFactory.fromResource(R.mipmap.muloc));
               Marker marker1 = mMap.addMarker(centreMarker);
               myLocMarkerAvailable = true;
           }
           // not added to the list as it will remain forever even if a new circle is added
           //adjusting seekbar with newly created circle by setting it to 0
           seekBarOnline.setProgress(0);
           bound = toBounds(circle.getCenter(), circle.getRadius());
           mMap.setPadding(10, 10, 10, 30);
           cu = CameraUpdateFactory.newLatLngBounds(bound, 0);
           mMap.animateCamera(cu, 1000, new GoogleMap.CancelableCallback() {
               @Override
               public void onFinish() {
                   removeMarker();
                   //herea a conflict may arise handle it
                   addMarker();
               }

               @Override
               public void onCancel() {

               }
           });
       }else{
           Toast.makeText(getApplicationContext(), "Sorry Couldn't detect your location!!!", Toast.LENGTH_SHORT).show();
       }
    }
    @Override
    protected void onPause() {
        imButtonDir.setVisibility(View.INVISIBLE);
        super.onPause();
    }

    // added all for directions
    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("SHAKIL", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(2);
                    lineOptions.color(Color.RED);
                }

                // Drawing polyline in the Google Map for the i-th route
                Polyline polyline = mMap.addPolyline(lineOptions);
                listPolyline.add(polyline);
                Log.d("SHAKIL", "added polyline to list");
            } else {
                Toast.makeText(getApplicationContext(), "Something went wrong!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class onMapLongClickListener implements GoogleMap.OnMapLongClickListener{
        private LatLngBounds bound;
        private CameraUpdate cu;

        @Override
        public void onMapLongClick(LatLng latLng) {
            // if long click is inside circle no new circle should be made handle this
            // check to remove polyline here and when searchview click happen as a new circle add we should remove polylines
            if(circle != null)
                circle.remove();

            //remove polyline
            removePoly();
            circle = mMap.addCircle(new CircleOptions().center(latLng).radius(Constant.CIRCLE_RADIUS_MIN).strokeColor(Color.BLUE).fillColor(0x5500ff00)
                    .strokeWidth(3));
            // adding a marker at the centre of the newly created circle
            MarkerOptions centreMarker = new MarkerOptions().position(circle.getCenter()).title(cmarkerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.muloc));
            Marker marker1 = mMap.addMarker(centreMarker);
            listMarker.add(marker1);
            //adjusting seekbar with newly created circle by setting it to 0
            seekBarOnline.setProgress(0);
            Log.d("SHAKIL", "new circle latlng = "+marker1.getPosition());

            //now animate the camera to the newly made circle
            bound = toBounds(circle.getCenter(), circle.getRadius());
            mMap.setPadding(10, 10, 10, 30);
            cu = CameraUpdateFactory.newLatLngBounds(bound, 0);
            mMap.animateCamera(cu, 1000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    removeMarker();
                    //herea a conflict may arise handle it
                    addMarker();
                }

                @Override
                public void onCancel() {

                }
            });
            //circle.setClickable(true);

        }
    }

    private class CustomAdListener extends com.google.android.gms.ads.AdListener {
        @Override
        public void onAdLoaded() {
            if(linearv_add != null && linearv_add.getVisibility() == View.GONE){
                linearv_add.setVisibility(View.VISIBLE);
                Log.d("SHAKIL", "yap visibility set to visible");
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

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }


    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences(Constant.SHARED_PREFERENCE_ATMLOCATOR, 0);
        String lat = preferences.getString(Constant.CIRCLE_LAST_LAT, null);
        String lng = preferences.getString(Constant.CIRCLE_LAST_LNG, null);
        if(lat != null && lng != null) {
            preCirLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            Log.d("SHAKIL", "yap got saved circle centre");
        }else{
            preCirLatLng = new LatLng(Constant.DEFAULT_CIRCLE_LAT, Constant.DEFAULT_CIRCLE_LNG);
            Log.d("SHAKIL", "nope default given circle centre");
        }
        //Home api key: AIzaSyDorQ-vpTTQMP5ypVhCEc-Zxfo5M6ZfF8U
        //Office api key: AIzaSyBwelRv3SDaXh4oJw-tJFfpJZlV9FHnzDI
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(backPressedOnce){
            finish();
            super.onBackPressed();
        }else{
            Toast.makeText(getApplicationContext(), "Press back again to exit.",Toast.LENGTH_LONG).show();
            backPressedOnce = true;
        }

    }

    private void removePoly(){
        if(listPolyline.size() > 0) {
            for (Polyline polyline : listPolyline) {
                polyline.remove();
                //Log.d("SHAKIL", "polyline removed no:"+polyline.getId());
            }
        }
    }

    private void cutPolylineOutsideCircle() {

        if(listPolyline.size() > 0) {
            for (Polyline polyline : listPolyline) {
                boolean inside = true;
                List<LatLng> listLatLng = new ArrayList<LatLng>();
                listLatLng = polyline.getPoints();
                for(LatLng latLng : listLatLng){
                    if(!isInsideCircle(circle.getCenter(), latLng)) {
                        inside = false;
                        break;
                    }
                }
                if(!inside)
                    polyline.remove();
                //polyline.remove();
                //Log.d("SHAKIL", "polyline removed no:"+polyline.getId());
            }
        }
    }
}

