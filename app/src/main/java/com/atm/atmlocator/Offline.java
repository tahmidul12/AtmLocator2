package com.atm.atmlocator;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
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
    private List<String> sbankinCb;
    private TextView textv_noSearch;
    private ListView listOfBank;
    private RelativeLayout rlmenu;
    private CheckBox rb_dbbl, rb_brac, rb_exim, rb_ific, rb_all, rb_prime;
    private int pageNo, backpressDo = 0, listViewPos = 0;
    private ArrayAdapterBank arrayAdapterBank;
    private ArrayAdapter<BankModel> adapter;

    private String[] spinnerItems = new String[]{"All", "DBBL", "Brac Bank", "AB Bank"
                                                 , "City Bank", "EBL", "Exim Bank", "HSBC"
                                                 , "IFIC Bank", "One Bank", "Premier Bank", "Prime Bank"
                                                 , "SCB", "UCBL", "South-East Bank"};

    private Cursor cursor;
    private View bankPopUpView;
    MenuItem banklists;
    private String selected_bank = "All";
    private boolean checked = false;
    /*
    experimental
     */
    Rect rlmenu_rect;
    private RelativeLayout content_offline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(R.style.FadeOffline);
        setContentView(R.layout.activity_offline);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AtmLocator");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Offline.this, Online.class);
                startActivity(intent);
                finish();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //.setAction("Action", null).show();
            }
        });

        //init vars
        rlmenu_rect = new Rect();
        sbankinCb = new ArrayList<String>();
        rb_all =  (CheckBox) findViewById(R.id.rb_all);
        rb_brac = (CheckBox) findViewById(R.id.rb_brac);
        rb_dbbl = (CheckBox) findViewById(R.id.rb_dbbl);
        rb_prime = (CheckBox) findViewById(R.id.rb_primes);
        rb_ific = (CheckBox) findViewById(R.id.rb_ific);
        rb_exim = (CheckBox) findViewById(R.id.rb_exim);

        rb_all.setOnCheckedChangeListener(new RadioButtonCheckListener());
        rb_brac.setOnCheckedChangeListener(new RadioButtonCheckListener());
        rb_dbbl.setOnCheckedChangeListener(new RadioButtonCheckListener());
        rb_ific.setOnCheckedChangeListener(new RadioButtonCheckListener());
        rb_exim.setOnCheckedChangeListener(new RadioButtonCheckListener());
        rb_prime.setOnCheckedChangeListener(new RadioButtonCheckListener());

        //experimental
        content_offline = (RelativeLayout) findViewById(R.id.content_offline);
        content_offline.setOnTouchListener(new RlTouchListener());
        rlmenu = (RelativeLayout) findViewById(R.id.rlmenu);
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

    /*
    experimental added for touch event hasWindowFocus
     */
    private class RlTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(view.getId() == R.id.rlmenu)
                Log.d("SHAKIL", "yap rlmenu clicked");
            if(rlmenu_rect != null) {
                if (rlmenu_rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())){
                }
                else {
                    if(rlmenu.getVisibility() == View.VISIBLE){
                        rlmenu.setVisibility(View.GONE);
                        setBanktoListasCb();
                    }

                }
            }
            return false;
        }
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
        banklists = banklist;
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

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rlmenu.getVisibility() == View.VISIBLE){
                    rlmenu.setVisibility(View.GONE);
                    //setBanktoListasCb();
                }

                //Log.d("SHAKIL", "yap clicked....................");
            }
        });

        searchView.setOnQueryTextListener(new SearchViewListener());
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_bank)
        {
            if(rlmenu.getVisibility() == View.VISIBLE) {
                rlmenu.setVisibility(View.GONE);
                //setBanktoListasCb();
                //if(rlmenu_rect != null)
                     rlmenu.getGlobalVisibleRect(rlmenu_rect);
            }else{
                rlmenu.setVisibility(View.VISIBLE);
            }
            View view = findViewById(R.id.action_bank);
            PopupMenu popup = new PopupMenu(Offline.this, view);
            popup.getMenuInflater()
                    .inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    selected_bank = item.getTitle().toString();
                    if(selected_bank.matches("All")) {
                        addDatatoList();
                        banklists.setTitle("All");
                    }

                    else {
                        banklists.setTitle(item.getTitle());
                        item.setChecked(true);
                        //setBanktoList(selected_bank);
                    }
                    return false;
                }
            });
            //popup.show();
        }
           return super.onOptionsItemSelected(item);
    }
    /*private void sbClick(View v){
        Log.d("SHAKIL", "yap search view is clicked 4m sbClick");
    }*/

    private class RadioButtonCheckListener implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
           switch (compoundButton.getId()) {
               case R.id.rb_all:
                   if (b) {
                       sbankinCb.clear();
                       setrb_others();
                       setBanktoListasCb();
                   }
                   else{
                       if(sbankinCb.size() == 0)
                           rb_all.setChecked(true);
                   }
                   Log.d("SHAKIL", "now listbank size = " + sbankinCb.size() + " added bank=" + rb_all.getText());
                   break;
               case R.id.rb_brac:
                   if (b) {
                       sbankinCb.add((String) rb_brac.getText());
                       setrb_all();
                       if(sbankinCb.size() < 4)
                          setBanktoListasCb();
                       else
                           rb_all.setChecked(true);
                   }else{
                       sbankinCb.remove((String) rb_brac.getText());
                       if(sbankinCb.size() == 0 && !rb_all.isChecked())
                           rb_all.setChecked(true);
                       setBanktoListasCb();
                   }
                   Log.d("SHAKIL", "now listbank size = " + sbankinCb.size() + " added bank=" + rb_brac.getText());
                   break;
               case R.id.rb_dbbl:
                   if (b) {
                       sbankinCb.add((String) rb_dbbl.getText());
                       setrb_all();
                       setBanktoListasCb();
                   }else{
                       sbankinCb.remove((String) rb_dbbl.getText());
                       if(sbankinCb.size() == 0 && !rb_all.isChecked())
                           rb_all.setChecked(true);
                       setBanktoListasCb();
                   }
                   Log.d("SHAKIL", "now listbank size = " + sbankinCb.size() + " added bank=" + rb_dbbl.getText());
                   break;
               case R.id.rb_exim:
                   if (b) {
                       sbankinCb.add((String) rb_exim.getText());
                       setrb_all();
                       setBanktoListasCb();
                   }else{
                       sbankinCb.remove((String) rb_exim.getText());
                       if(sbankinCb.size() == 0 && !rb_all.isChecked())
                           rb_all.setChecked(true);
                       setBanktoListasCb();
                   }
                   Log.d("SHAKIL", "now listbank size = " + sbankinCb.size() + " added bank=" + rb_exim.getText());
                   break;
               case R.id.rb_ific:
                   if (b){
                       sbankinCb.add((String) rb_ific.getText());
                       setrb_all();
                       setBanktoListasCb();
                   }else{
                       sbankinCb.remove((String) rb_ific.getText());
                       if(sbankinCb.size() == 0 && !rb_all.isChecked())
                           rb_all.setChecked(true);
                       setBanktoListasCb();
                   }
                   Log.d("SHAKIL", "now listbank size = "+sbankinCb.size()+" added bank="+rb_ific.getText());
                   break;
               case R.id.rb_primes:
                   if (b){
                       sbankinCb.add((String) rb_prime.getText());
                       setrb_all();
                       setBanktoListasCb();
                   }else{
                       sbankinCb.remove((String) rb_prime.getText());
                       if(sbankinCb.size() == 0 && !rb_all.isChecked())
                           rb_all.setChecked(true);
                       setBanktoListasCb();
                   }
                   Log.d("SHAKIL", "now listbank size = "+sbankinCb.size()+" added bank="+rb_prime.getText());
                   break;
               default:
                   break;
           }

        }
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


    public void setBanktoListasCb() {
        Log.d("SHAKIL", "listbank size 4m as cb= "+listBank.size());
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
                if(sbankinCb.size()!= 0) {
                    if (sbankinCb.contains(bname)) {
                        Log.d("SHAKIL", "yap bame contains and bname = " + bname);
                        listBank.add(new BankModel(bname, batmname, lat, longi, baddress, city, state, null));
                    }
                }else{
                    Log.d("SHAKIL", "yap all selcted in cb");
                    listBank.add(new BankModel(bname, batmname, lat, longi, baddress, city, state, null));
                }
                adapter.notifyDataSetChanged();
            } while (cursor.moveToNext());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                int playerPos = data.getIntExtra("listViewPos", 0);
                listViewPos = playerPos;
            }
        }
    }

    private void setrb_all() {
        if(rb_all.isChecked())
            rb_all.setChecked(false);
    }
    private void setrb_others() {
        if(rb_brac.isChecked())
            rb_brac.setChecked(false);
        if(rb_dbbl.isChecked())
            rb_dbbl.setChecked(false);
        if(rb_exim.isChecked())
            rb_exim.setChecked(false);
        if(rb_ific.isChecked())
            rb_ific.setChecked(false);
        if(rb_prime.isChecked())
            rb_prime.setChecked(false);
        /*if(rb_brac.isChecked())
            rb_brac.setChecked(false);
        if(rb_brac.isChecked())
            rb_brac.setChecked(false);
        if(rb_brac.isChecked())
            rb_brac.setChecked(false);
        */
    }

    private void cb_filter(String bname){
        for(int i = 0; i<listBank.size(); i++){
            if(!listBank.get(i).getBname().equalsIgnoreCase(bname))
                listBank.remove(i);
        }
        adapter.notifyDataSetChanged();
    }
}
