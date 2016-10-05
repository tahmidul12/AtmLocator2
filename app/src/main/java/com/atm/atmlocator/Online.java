package com.atm.atmlocator;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import apiconstant.Constant;

/*
   using google map we have to implement the OnMapReadyCallback so when the map will be ready then we can add necessary attribute
   ex: circle, marker to the map
 */
public class Online extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    private boolean mapReady = false;
    private TextView textv_seekOnline;
    private SeekBar seekBarOnline;
    private Circle circle;
    private List<Polygon> listPoly;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialize layout components
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
                        cu = CameraUpdateFactory.newLatLngBounds(bound, 10);
                        for(Polygon poly : listPoly) {
                            poly.remove();
                            Log.d("SHAKIL", "removed no:");
                        }
                        mMap.animateCamera(cu, 2000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                setPolygon();
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
                        cu = CameraUpdateFactory.newLatLngBounds(bound, 10);
                        for(Polygon poly : listPoly) {
                            poly.remove();
                            Log.d("SHAKIL", "removed no:");
                        }
                        mMap.animateCamera(cu, 2000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                setPolygon();
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
                    inprogress = seekBar.getProgress();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //Log.d("SHAKIL", "on stop tracking touch");
                }
            });
        }
        listPoly = new ArrayList<Polygon>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("SHAKIL", "yap map ready");
        mapReady = true;
        mMap = googleMap;
        LatLng dhaka = new LatLng(23.7917399,90.4041357);
        LatLng mOffice = new LatLng(23.7936268,90.4005859);
        //creating and adding a circle
        final CircleOptions circleOptions = new CircleOptions().center(mOffice).radius(Constant.CIRCLE_RADIUS_MIN)
                .strokeColor(Color.BLUE).fillColor(getResources().getColor(R.color.clrs))
                .strokeWidth(3);
        circle = mMap.addCircle(circleOptions);
        //setting the camera with the specified location and animate in map
        CameraPosition target = CameraPosition.builder().target(mOffice).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 3000, null);
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

}

