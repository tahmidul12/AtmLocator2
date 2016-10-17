package com.atm.atmlocator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import apiconstant.Constant;

public class Home extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    Timer timer, timer2;
    private Cursor cursor;
    private boolean dataAvailable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();


        /*try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //goOnline();
        dataAvailable = false;
        getSupportLoaderManager().initLoader(1, null, this);
    }

    private void goOnline(){
        Intent intent = new Intent(Home.this, Online.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if(manager.getActiveNetworkInfo() == null) {
            if(dataAvailable) {
                Intent intent = new Intent(Home.this, Offline.class);
                startActivity(intent);
                finish();
                Log.d("SHAKIL", "yap data found");
            }else{
                Log.d("SHAKIL", "timr2 is starting as no data found");
                timer2 = new Timer();
                timer2.schedule(new RemindTask2(), Constant.WAIT_TIME + 200);
                //userLoginFailed();
            }
        }else{
            timer = new Timer();
            timer.schedule(new RemindTask(), Constant.WAIT_TIME);
        }
    }
    public void userLoginFailed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("Ops!!!");
        builder.setMessage("No internet connection detected. Please check your connection.");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent dialogIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
                /*Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);*/
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String URL = "content://com.atmlocator.Bank/atms";
        Uri atmsUri = Uri.parse(URL);
        return new android.support.v4.content.CursorLoader(this, atmsUri, null, null, null, "bank");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      if(data.getCount() >0){
          dataAvailable = true;
          Log.d("SHAKIL", "yap data available");
      }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class RemindTask extends TimerTask {
        public void run(){
            timer.cancel();
            Intent intent = new Intent(Home.this, Online.class);
            startActivity(intent);
            //check
            finish();
        }
    }

    private class RemindTask2 extends TimerTask {
        public void run(){
            if(dataAvailable){
                Log.d("SHAKIL", "yap data found and going for offline");
            timer2.cancel();
            Intent intent = new Intent(Home.this, Offline.class);
            startActivity(intent);
            //check
            finish();
            }else{
                timer2.cancel();
                userLoginFailed();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
