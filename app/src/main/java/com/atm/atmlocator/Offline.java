package com.atm.atmlocator;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import adapterClasses.ArrayAdapterBank;
import modelClasses.BankModel;

public class Offline extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static List<BankModel> listBank;
    private ListView listOfBank;
    private int pageNo, backpressDo = 0, listViewPos;
    private ArrayAdapterBank arrayAdapterBank;
    private ArrayAdapter<BankModel> adapter;

    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
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

        //init vars
        listOfBank = (ListView) findViewById(R.id.listv_bank);
        listOfBank.setDivider(new ColorDrawable(getResources().getColor(R.color.dividerColor)));
        listOfBank.setDividerHeight(12);
        if(listBank == null || listBank.size() ==0)
            listBank = new ArrayList<BankModel>();
        adapter = new ArrayAdapterBank(this, listBank);
        listOfBank.setAdapter(adapter);

        getSupportLoaderManager().initLoader(1, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("SHAKIL", "4m onCreateLoader");
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
        String bname, batmname, baddress;
        if (cursor.moveToFirst()) {
            do{
                bname = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                batmname = cursor.getString(cursor.getColumnIndex(AtmProvider.ATM_NAME));
                baddress = cursor.getString(cursor.getColumnIndex(AtmProvider.ADDRESS));

                listBank.add(new BankModel(bname, batmname, null, null, baddress, null, null, null));
                adapter.notifyDataSetChanged();
            } while (cursor.moveToNext());
        }
    }
}
