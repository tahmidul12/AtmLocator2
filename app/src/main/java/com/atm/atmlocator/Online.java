package com.atm.atmlocator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import apiconstant.ApiSearch;
import apiconstant.Constant;

/*
   using google map we have to implement the OnMapReadyCallback so when the map will be ready then we can add necessary attribute
   ex: circle, marker to the map
 */
public class Online extends AppCompatActivity implements OnMapReadyCallback , LoaderManager.LoaderCallbacks<Cursor>{

    GoogleMap mMap;
    private boolean mapReady = false;
    private TextView textv_seekOnline;
    private SeekBar seekBarOnline;
    private Circle circle;
    private List<Polygon> listPoly;
    private List<Marker> listMarker;

    // for search menu on toolbar
    private ArrayList<String> stringArrayList;
    private ArrayAdapter<String> adapter;

    //as array adapter will not work
    private static final String[] SUGGESTIONS = {
            "Bauru", "Sao Paulo", "Rio de Janeiro",
            "Bahia", "Mato Grosso", "Minas Gerais",
            "Tocantins", "Rio Grande do Sul"
    };
    private SimpleCursorAdapter mAdapter;
    MatrixCursor c;
    public Cursor cursor;
    Button button;
    Marker marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialize layout components
        button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContentResolver().delete(AtmProvider.CONTENT_URI, null, null);
            }
        });
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
                        cu = CameraUpdateFactory.newLatLngBounds(bound, 10);
                        for(Polygon poly : listPoly) {
                            //poly.remove();
                            //Log.d("SHAKIL", "removed no:");
                        }
                        mMap.animateCamera(cu, 2000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                //setPolygon();
                                removeMarker();
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
        //init variables
        listPoly = new ArrayList<Polygon>();
        listMarker = new ArrayList<Marker>();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("SHAKIL", "yap map ready");
        mapReady = true;
        mMap = googleMap;
        LatLng dhaka = new LatLng(23.7917399,90.4041357);
        LatLng mOffice = new LatLng(23.7936268,90.4005859);
        //creating and adding a circle
        final CircleOptions circleOptions = new CircleOptions().center(mOffice).radius(Constant.CIRCLE_RADIUS_MIN)
                .strokeColor(Color.BLUE).fillColor(0x5500ff00)
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
                if(marker != null)
                    marker.remove();
                //Log.d("SHAKIL", "click position = "+c.getString(position) );
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String feedName = cursor.getString(1);
                String lati = cursor.getString(2);
                String loti = cursor.getString(3);
                String bankName = cursor.getString(4);
                LatLng location = new LatLng(Double.parseDouble(lati), Double.parseDouble(loti));
                //adding a marker
                MarkerOptions myOffice = new MarkerOptions().position(location).title(bankName)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.markers));
                marker = mMap.addMarker(myOffice);
                searchView.setQuery(feedName, false);
                searchView.clearFocus();
                CameraPosition target = CameraPosition.builder().target(location).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 2000, null);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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
    private void filter(String query){
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
                LatLng from = circle.getCenter();
                LatLng to = new LatLng(lat, longi);
                if(isInsideCircle(from, to)){
                    //adding a marker
                    MarkerOptions myOffice = new MarkerOptions().position(to).title(bankName)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.markers));
                    Marker marker1 = mMap.addMarker(myOffice);
                    listMarker.add(marker1);
                }
            } while (cursor.moveToNext());
        }
    }
    private void removeMarker(){
        LatLng from = circle.getCenter();
        for (Marker marker : listMarker){
            LatLng to = marker.getPosition();

            if(!isInsideCircle(from, to))
                marker.remove();
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
        Toast.makeText(getApplicationContext(), "cursor row no : "+data.getCount(), Toast.LENGTH_SHORT).show();
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
                        ContentValues values = new ContentValues();

                        values.put(AtmProvider.BANK, bank);
                        values.put(AtmProvider.ATM_NAME, atm_name);
                        values.put(AtmProvider.LAT, lat);
                        values.put(AtmProvider.LONGI, longi);
                        values.put(AtmProvider.ADDRESS, address);
                        values.put(AtmProvider.CITY, city);
                        values.put(AtmProvider.STATE, state);
                        values.put(AtmProvider.COUNTRY, country);


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
}

