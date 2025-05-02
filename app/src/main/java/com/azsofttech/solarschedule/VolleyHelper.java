package com.azsofttech.solarschedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class VolleyHelper {
    Context context;
    ArrayList<SolarModel> solarModelArrayList;
    SharedPreferences spref;
    SharedPreferences.Editor editor;
    private static final String SPREF_NAME = "mypref";
    private static final String GSON_STRING_KEY = "gson_string_key";
    private static final String LAST_UPDATE_KEY = "last_update_key";
    private static final String LOCATION_KEY = "location_key";
    private static final String NARAYANPUR = "Narayanpur";
    private static final String MIZMIZI = "Mizmizi";
    Location location;
    public VolleyHelper(Context context, Location location) {
        this.context = context;
        this.location = location;
    }

    public void sendRequest( DataSavedCallback dataSavedCallback ){
        spref = context.getSharedPreferences( SPREF_NAME, context.MODE_PRIVATE );
        editor = spref.edit();

        //DEFAULT LOCATION IS MIZMIZI
        //TODO: parse location from SOLARWIDGETPROVIDER CLASS AND USE FOR VOLLEY REQUEST
        //location = spref.getString( LOCATION_KEY, MIZMIZI );
//        if ( location.equals(NARAYANPUR) ){
//            latitude = "23.8621";
//            longitude = "90.9763";
//        }else if ( location.equals(MIZMIZI) ){
//            latitude = "23.6847";
//            longitude = "90.4949";
//        }else {//FARMGATE
//            latitude = "23.7605";
//            longitude = "90.3901";
//        }

        DecimalFormat df = new DecimalFormat("#.###");
        String url = "https://api.sunrisesunset.io/json?lat="+df.format(location.getLatitude())+"&lng="+df.format(location.getLongitude());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                parsedObject -> {
                    try {
                        JSONObject resultObject = parsedObject.getJSONObject("results");

                            SolarModel solarModel = new SolarModel();
                            solarModel.setSunrise( resultObject.getString( "sunrise" ) );
                            solarModel.setSunset( resultObject.getString( "sunset" ) );
                            solarModel.setDaylength( resultObject.getString( "day_length" ) );
                            solarModel.setSolarnoon( resultObject.getString( "solar_noon" ) );
                            solarModelArrayList = new ArrayList<>();
                            solarModelArrayList.add( solarModel );
                            Gson gson = new Gson();
                            String gsonData = gson.toJson( solarModelArrayList );

                            editor.putString( GSON_STRING_KEY, gsonData );
                            editor.apply();

                            Calendar calendar = Calendar.getInstance();
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd LLL yyyy kk:mm:ss aaa" );
                            String dateTime = simpleDateFormat.format(calendar.getTime());

                            int keyNum = spref.getInt( "keyNum", 0 ) + 1;
                            String timeKey = "timeKey"+keyNum;
                            editor.putString( timeKey, dateTime);
                            editor.apply();
                            editor.putInt( "keyNum", keyNum );
                            editor.apply();
                            editor.putString( LAST_UPDATE_KEY, dateTime);
                            editor.apply();

                        setDataToViewModel( gsonData );
                        dataSavedCallback.onComplete( "Data is saved" );

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }, error -> {
                });
        jsonObjectRequest.setShouldCache( false );
        MySingleTon.getInstance(context).getRequestQueue().add( jsonObjectRequest );
    }

    public void setDataToViewModel(String dataString ){
        MyViewModel myViewModel = MainActivity.myViewModel;
        if ( myViewModel != null ){
            myViewModel.setMyData( dataString );
        }
    }

}
