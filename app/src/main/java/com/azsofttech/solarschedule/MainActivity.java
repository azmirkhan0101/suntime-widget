package com.azsofttech.solarschedule;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.azsofttech.solarschedule.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<String> arrayList;
    ArrayList<SolarModel> solarModelArrayList;
    SharedPreferences spref;
    SharedPreferences.Editor editor;
    private static final String SPREF_NAME = "mypref";
    private static final String LOCATION_KEY = "location_key";
    private static final String LAST_UPDATE_KEY = "last_update_key";
    private static final String GSON_STRING_KEY = "gson_string_key";
    private static final String NARAYANPUR = "Narayanpur";
    private static final String MIZMIZI = "Mizmizi";
    private static final String FARMGATE = "Farmgate";
    public static String REFRESH;
    String location;
    public static MyViewModel myViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );

        initVarSetDrawer();
        showTimeList();
        loadDataAndSetInViews();

        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        myViewModel.mutableLiveData.observe(MainActivity.this, s -> {
            if ( s != null ){//WE HAVE DATA FROM RESPONSE
                getValuesSetViews( getSolarModel( s ) );
                showTimeList();
                setLastUpdate();
                Snackbar snackbar = Snackbar.make(binding.drawerlayout, "Widget Refreshed!", 2300);
                snackbar.setTextColor( Color.argb( 255, 0, 148, 44 ) )
                        .setAction("CLOSE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        })
                        .show();
            }else {//DATA IS NULL. SET FROM SPREF,IF SPREF IS NULL, SHOW NO DATA FOUND
                loadDataAndSetInViews();
            }
        });

        //REFRESH CLICK
        binding.llrefresh.setOnClickListener(v -> {
            //CHECK INTER CON
            updateWidget();
            //IT TRIGGERS ON UPDATE
            //ON UPDATE CALLS VOLLEY HELPER
            //VOLLEY HELPER LOADS JSON AND SAVES IN SPREF->TRIGGERS INTERFACE METHOD FOR ON UPDATE->
            // SETS DATA IN VIEW MODEL IF MAIN ACTIVITY IS ACTIVE( TRIGGERS OBSERVER )
        });

        binding.lldropdown.setOnClickListener(dropdown -> showDropDownPopup( dropdown ));
    }
    //ONCREATE END >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //////////////////////////////////////////////////////////
    //INIT VIEW SET DRAWER
    private void initVarSetDrawer(){
        spref = getSharedPreferences( SPREF_NAME, MODE_PRIVATE );
        editor = spref.edit();
        location = spref.getString( LOCATION_KEY, MIZMIZI );
        setLocation();
        setLastUpdate();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, binding.drawerlayout, binding.toolbar, R.string.OpenDrawer, R.string.CloseDrawer );
        binding.drawerlayout.addDrawerListener( toggle );
        toggle.syncState();
        REFRESH = "refresh";
    }
    //////////////////////////////////////////////////////////
    //UPDATE DATA
    private void updateWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
        ComponentName componentName = new ComponentName(MainActivity.this, SolarWidgetProvider.class);
        Intent intent = new Intent(MainActivity.this, SolarWidgetProvider.class);
        int[] ids = appWidgetManager.getAppWidgetIds( componentName );
        intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, ids );
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);
    }
    //////////////////////////////////////////////////////////
    //SHOW TIME LIST OF UPDATE IN LISTVIEW AND SHOW LAST UPDATE TIME IN ANOTHER TV
    @SuppressLint("SetTextI18n")
    private void showTimeList() {
        int keyNum = spref.getInt("keyNum", 0);
        arrayList = new ArrayList<>();
        if (keyNum != 0) {
            for (int i = 1; i <= keyNum; i++) {
                String timeKey = "timeKey" + i;
                String time = spref.getString(timeKey, "Error getting time!");
                arrayList.add(time);
                if (i == keyNum) {
                    if (time.equals("Error getting time!")) {
                        time = "Not updated";
                    }
                    binding.tvlastupdate.setText(time);
                }
            }
            binding.listView.setAdapter(new TimeListAdapter(this, arrayList));
        } else {
            binding.tvlastupdate.setText("Not updated");
            Toast.makeText(this, "No time data found!", Toast.LENGTH_SHORT).show();
        }
    }
    //////////////////////////////////////////////////////////
    //DROPDOWN OPTIONS FOR SELECTING LOCATION
    private void showDropDownPopup(View dropdown){
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, dropdown );
        popupMenu.getMenuInflater().inflate(R.menu.location_list_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            if ( item.getItemId() == R.id.narayanpur ){
                editor.putString( LOCATION_KEY, NARAYANPUR );
            }else if ( item.getItemId() == R.id.mizmizi ){
                editor.putString( LOCATION_KEY, MIZMIZI );
            }else {
                editor.putString( LOCATION_KEY, FARMGATE );
            }
            editor.apply();
            setLocation();
            updateWidget();//UPDATE EVERYTHING ON LOCATION SELECTION
            return true;
        });
    }
    //////////////////////////////////////////////////////////
    //SET SAVED LOCATION IN LOCATION DROPDOWN TEXT
    private void setLocation(){
        binding.tvlocation.setText( spref.getString( LOCATION_KEY, "Location" ));
    }
    //////////////////////////////////////////////////////////
    //SET LAST UPDATED TIME IN TV
    private void setLastUpdate(){
        binding.tvlastupdate.setText( spref.getString( LAST_UPDATE_KEY, "Not updated" ) );
    }
    //////////////////////////////////////////////////////////
    //GET SOLAR MODEL: MODEL CLASS FROM GSON STRING ( STRING -> ARRAYLIST -> MODEL ) USING GSON
    private SolarModel getSolarModel(String gsonData){
        Gson gson = new Gson();
        solarModelArrayList = new ArrayList<>();
        Type type = new TypeToken<ArrayList<SolarModel>>(){}.getType();
        solarModelArrayList = gson.fromJson( gsonData, type );

        return solarModelArrayList.get( 0 );
    }
    //////////////////////////////////////////////////////////
    //GET VALUES FROM MODEL CLASS AND SET IN VIEWS
    @SuppressLint("SetTextI18n")
    private void getValuesSetViews(SolarModel model) {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        String dateTime = simpleDateFormat.format(calendar.getTime());

        String dateAndLocation;
        if (location.equals(NARAYANPUR)) {
            dateAndLocation = dateTime + " (NP)";
        } else if (location.equals(MIZMIZI)) {
            dateAndLocation = dateTime + " (MZ)";
        } else {//FARMGATE
            dateAndLocation = dateTime + " (FG)";
        }
        binding.tvdate.setText(dateAndLocation);
        binding.tvrise.setText("  :  " + model.getSunrise());
        binding.tvset.setText("  :  " + model.getSunset());
        binding.tvlength.setText("  :  " + model.getDaylength());
        binding.tvnoon.setText("  :  " + model.getSolarnoon());
    }
    //////////////////////////////////////////////////////////
    //SET NO DATA TEXT IN TVS IF NO DATA FOUND FROM SPREF
    @SuppressLint("SetTextI18n")
    private void setNoDataText(){
        binding.tvdate.setText( "No data found" );
        binding.tvrise.setText( "  :  No data found" );
        binding.tvset.setText( "  :  No data found" );
        binding.tvlength.setText( "  :  No data found" );
        binding.tvnoon.setText( "  :  No data found" );
    }
    //////////////////////////////////////////////////////////
    //FINAL OUTPUT METHOD OF GET,SET DATA METHODS ABOVE
    //GETS DATA FROM SPREF AND SHOWS IN TVS ( EVEN IF DATA IS NOT FOUND )
    private void loadDataAndSetInViews(){
        String gsonData = spref.getString( GSON_STRING_KEY, null );
        if ( gsonData != null ){
            //get model and set model values in views
            getValuesSetViews( getSolarModel(gsonData) );
        }else{//SHOULD BE CALLED FIRST TIME ONLY
            setNoDataText();
        }
    }
    //RELEASE OBSERVER FROM VIEWMODEL ON DESTROY
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myViewModel.getMutableLiveData().removeObservers( this );
        REFRESH = null;
    }
}