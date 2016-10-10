package com.atm.atmlocator;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import adapterClasses.ArrayAdapterBank;
import modelClasses.BankModel;

public class Offline extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static List<BankModel> listBank;
    private TextView textv_noSearch;
    private ListView listOfBank;
    private int pageNo, backpressDo = 0, listViewPos = 0;
    private ArrayAdapterBank arrayAdapterBank;
    private ArrayAdapter<BankModel> adapter;

    private String[] spinnerItems = new String[]{"All", "DBBL", "Brac Bank", "AB Bank"
                                                 , "City Bank", "EBL", "Exim Bank", "HSBC"
                                                 , "IFIC Bank", "One Bank", "Premier Bank", "Prime Bank"
                                                 , "SCB", "UCBL", "South-East Bank"};

    private Cursor cursor;
    private View bankPopUpView;

    private String selected_bank = "All";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AtmLocator");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //init vars
        textv_noSearch = (TextView) findViewById(R.id.textv_noSearch);
        listOfBank = (ListView) findViewById(R.id.listv_bank);
        listOfBank.setDivider(new ColorDrawable(getResources().getColor(R.color.dividerColor)));
        listOfBank.setDividerHeight(12);
        if(listBank == null || listBank.size() ==0)
            listBank = new ArrayList<BankModel>();
        adapter = new ArrayAdapterBank(this, listBank);
        listOfBank.setAdapter(adapter);

        getSupportLoaderManager().initLoader(1, null, this);

    }

    /*
    when coming back to the offline bank detail user should scroll down to the same banklist item from where he go for detail
    so he is set to the same position
     */
    @Override
    protected void onResume() {
        listOfBank.setSelection(listViewPos);
        super.onResume();
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
        if(textv_noSearch.getVisibility() == View.VISIBLE)
            textv_noSearch.setVisibility(View.GONE);
        if(listBank.size()>0)
            listBank.clear();
        String bname, batmname, baddress, lat, longi, state, city;
        if (cursor.moveToFirst()) {
            do{
                bname = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                batmname = cursor.getString(cursor.getColumnIndex(AtmProvider.ATM_NAME));
                lat = cursor.getString(cursor.getColumnIndex(AtmProvider.LAT));
                longi = cursor.getString(cursor.getColumnIndex(AtmProvider.LONGI));
                baddress = cursor.getString(cursor.getColumnIndex(AtmProvider.ADDRESS));
                city = cursor.getString(cursor.getColumnIndex(AtmProvider.CITY));
                state = cursor.getString(cursor.getColumnIndex(AtmProvider.STATE));

                listBank.add(new BankModel(bname, batmname, lat, longi, baddress, city, state, null));
                adapter.notifyDataSetChanged();
            } while (cursor.moveToNext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_offline, menu);
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        MenuItem spinneritem = menu.findItem(R.id.spinner);
        MenuItem banklist = menu.findItem(R.id.action_bank);
        bankPopUpView = banklist.getActionView();
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(spinneritem);
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(this,
                R.array.allBankName, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapterSpinner);
        spinner.setOnItemSelectedListener(new SpinnerItemSelectedListener());
        View actionView = myActionMenuItem.getActionView();
        //AutoCompleteTextView searchView = (AutoCompleteTextView) actionView.findViewById(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchViewListener());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_bank)
        {
            View view = findViewById(R.id.action_bank);
            PopupMenu popup = new PopupMenu(Offline.this, view);
            popup.getMenuInflater()
                    .inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    selected_bank = item.getTitle().toString();
                    setBanktoList(selected_bank);
                    /*switch (item.getItemId()){
                        case R.id.menu_alls:
                            Toast.makeText(getApplicationContext(), "yap menu all checked", Toast.LENGTH_SHORT).show();
                            addDatatoList();
                            break;
                        case R.id.menu_dbbls:
                            selected_bank = item.getTitle().toString();
                            setBanktoList(selected_bank);
                            break;
                        case R.id.menu_bracks:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_abs:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_citys:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_ebls:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_hsbcs:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_scbs:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_primes:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_premiers:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_exims:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_ones:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_sebls:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;
                        case R.id.menu_ifics:
                            Toast.makeText(getApplicationContext(), "yap brac", Toast.LENGTH_SHORT).show();
                            setBanktoList(item.getTitle().toString());
                            break;

                    }*/

                    return false;
                }
            });
            popup.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private class SearchViewListener implements SearchView.OnQueryTextListener{

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if(newText.isEmpty() || newText.matches("") || newText.length() == 0)
                addDatatoList();
            filterData(newText);
            return false;
        }
    }
    private class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Toast.makeText(getApplicationContext(), "selected bank="+spinnerItems[i], Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private void filterData(String query){
        if(listBank != null)
            listBank.clear();
        String bname, batmname, baddress, lat, longi, state, city;
        if (cursor.moveToFirst()) {
            do{
                bname = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                batmname = cursor.getString(cursor.getColumnIndex(AtmProvider.ATM_NAME));
                lat = cursor.getString(cursor.getColumnIndex(AtmProvider.LAT));
                longi = cursor.getString(cursor.getColumnIndex(AtmProvider.LONGI));
                baddress = cursor.getString(cursor.getColumnIndex(AtmProvider.ADDRESS));
                city = cursor.getString(cursor.getColumnIndex(AtmProvider.CITY));
                state = cursor.getString(cursor.getColumnIndex(AtmProvider.STATE));
                if(selected_bank.matches("All")) {
                    if (baddress.toLowerCase().contains(query.toLowerCase()) || city.toLowerCase().contains(query.toLowerCase())
                            || state.toLowerCase().contains(query.toLowerCase()))
                        listBank.add(new BankModel(bname, batmname, lat, longi, baddress, city, state, null));

                }else{
                    if ((baddress.toLowerCase().contains(query.toLowerCase()) || city.toLowerCase().contains(query.toLowerCase())
                            || state.toLowerCase().contains(query.toLowerCase())) && bname.matches(selected_bank))
                        listBank.add(new BankModel(bname, batmname, lat, longi, baddress, city, state, null));
                }
                adapter.notifyDataSetChanged();
            } while (cursor.moveToNext());
        }
        if(listBank.size() == 0)
            textv_noSearch.setVisibility(View.VISIBLE);
        else
            textv_noSearch.setVisibility(View.GONE);
    }

    private void setBanktoList(String bankName) {
        Log.d("SHAKIL", "listbank size = "+listBank.size());
        if(listBank != null)
            listBank.clear();
        String bname, batmname, baddress, lat, longi, state, city;
        if (cursor.moveToFirst()) {
            do{
                bname = cursor.getString(cursor.getColumnIndex(AtmProvider.BANK));
                batmname = cursor.getString(cursor.getColumnIndex(AtmProvider.ATM_NAME));
                lat = cursor.getString(cursor.getColumnIndex(AtmProvider.LAT));
                longi = cursor.getString(cursor.getColumnIndex(AtmProvider.LONGI));
                baddress = cursor.getString(cursor.getColumnIndex(AtmProvider.ADDRESS));
                city = cursor.getString(cursor.getColumnIndex(AtmProvider.CITY));
                state = cursor.getString(cursor.getColumnIndex(AtmProvider.STATE));
                if(bname.equalsIgnoreCase(bankName))
                    listBank.add(new BankModel(bname, batmname, lat, longi, baddress, city, state, null));
                adapter.notifyDataSetChanged();
            } while (cursor.moveToNext());
        }
        /*if(listBank!= null && listBank.size()>0) {

            for(int i = 0; i<listBank.size(); i++) {
                BankModel bank = listBank.get(i);
                Log.d("SHAKIL", "bankname = "+bank.getBname());
                if(!bank.getBname().equalsIgnoreCase(bankName)) {
                    listBank.remove(i);

                    Log.d("SHAKIL", "matched and listBank size="+listBank.size());
                    //adapter.remove(adapter.getItem(i));
                    //listOfBank.setAdapter(adapter);
                    //adapter.notifyDataSetChanged();
                    //listOfBank.invalidateViews();
                }
            }
            Log.d("SHAKIL", "after removing size of the listbank is:"+listBank.size());
            //adapter.clear();
            adapter = new ArrayAdapterBank(this, listBank);
            listOfBank.setAdapter(adapter);
            //listOfBank.invalidateViews();
        }*/
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){

                int playerPos = data.getIntExtra("listViewPos", 0);
                listViewPos = playerPos;
                //if (listOfPlayer.getFirstVisiblePosition() > playerPos || listOfPlayer.getLastVisiblePosition() < playerPos)
                // listOfPlayer.setItemChecked(playerPos, true);
                //listOfPlayer.smoothScrollToPosition(playerPos);
            }
        }
    }
}
