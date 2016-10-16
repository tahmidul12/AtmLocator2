package com.atm.atmlocator;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Offlinedtl extends AppCompatActivity {

    private TextView textv_atmName, textv_bankName, textv_address, textv_city, textv_state;
    private ImageView imgv_atm, imgv_backButton;

    String bname, batmname, baddress, blat, blongi, bstate, bcity;
    int bpos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offlinedtl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle("AtmLocator");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init layout components
        textv_atmName = (TextView) findViewById(R.id.textv_atmName);
        textv_bankName = (TextView) findViewById(R.id.textv_bankName);
        textv_address = (TextView) findViewById(R.id.textv_address);
        textv_city = (TextView) findViewById(R.id.textv_city);
        textv_state = (TextView) findViewById(R.id.textv_state);
        imgv_backButton = (ImageView) findViewById(R.id.imagev_backButton);
        imgv_backButton.setOnClickListener(new OnclickListener());

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        if(getIntent()!= null){
            Intent intent = getIntent();
            bpos = intent.getIntExtra("bpos", 0);
            bname = intent.getStringExtra("bname");
            batmname = intent.getStringExtra("batmname");
            baddress = intent.getStringExtra("baddress");
            bcity = intent.getStringExtra("bcity");
            bstate = intent.getStringExtra("bstate");

        }

        setDatatoLayout();
    }

    private void setDatatoLayout() {
        textv_atmName.setText(batmname);
        textv_bankName.setText(bname);
        textv_address.setText(baddress);
        textv_city.setText(bcity);
        textv_state.setText(bstate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.menu.menu_home){
            Log.d("SHAKIL", "yap back button clicked");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.d("SHAKIL", "BOTAO HOME");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private class OnclickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.putExtra("listViewPos", bpos);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
