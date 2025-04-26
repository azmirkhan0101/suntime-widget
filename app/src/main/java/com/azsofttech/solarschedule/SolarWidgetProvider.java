package com.azsofttech.solarschedule;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SolarWidgetProvider extends AppWidgetProvider
{
	ArrayList<SolarModel> solarModelArrayList;
	RemoteViews views;
	SharedPreferences spref;
	SharedPreferences.Editor editor;
	private static final String SPREF_NAME = "mypref";
	private static final String GSON_STRING_KEY = "gson_string_key";
	private static final String PREVIOUS_DATE_KEY = "previous_date_key";
	private static final String LOCATION_KEY = "location_key";
	private static final String NARAYANPUR = "Narayanpur";
	private static final String MIZMIZI = "Mizmizi";
	String location;
	String dateTime;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		spref = context.getSharedPreferences( SPREF_NAME, context.MODE_PRIVATE );
		editor = spref.edit();

		String previuosdateTime = spref.getString( PREVIOUS_DATE_KEY, "No_date" );

		Calendar calendar = Calendar.getInstance();
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd MMM yyyy" );
		dateTime = simpleDateFormat.format(calendar.getTime());

		String refresh = "";
		String rString = MainActivity.REFRESH;
		if ( rString != null ) refresh = rString;

		if ( !dateTime.equals( previuosdateTime ) || refresh.equals( "refresh" ) ){
			editor.putString( PREVIOUS_DATE_KEY, dateTime );
			editor.apply();

			location = spref.getString( LOCATION_KEY, MIZMIZI );

			for ( int appWidgetId : appWidgetIds ){
				solarModelArrayList = new ArrayList<>();
				views = new RemoteViews( context.getPackageName(), R.layout.solar_widget_layout);

				Intent goMainIntent = new Intent( context, MainActivity.class );
				PendingIntent goMainPi = PendingIntent.getActivity( context, 0, goMainIntent, PendingIntent.FLAG_IMMUTABLE );
				views.setOnClickPendingIntent( R.id.tvrise, goMainPi );

				VolleyHelper volleyHelper = new VolleyHelper(context);
				volleyHelper.sendRequest(mesg -> {
					String gsonData = spref.getString(GSON_STRING_KEY, null);
					if (gsonData != null) {
						setDataInViews( views, getSolarModel(gsonData) );
						appWidgetManager.updateAppWidget(appWidgetId, views);
					}
				});
			}
		}
	}

	private SolarModel getSolarModel(String gsonData){
		Gson gson = new Gson();
		solarModelArrayList = new ArrayList<>();
		Type type = new TypeToken<ArrayList<SolarModel>>(){}.getType();
		solarModelArrayList = gson.fromJson( gsonData, type );

		return solarModelArrayList.get( 0 );
	}

	private void setDataInViews( RemoteViews views, SolarModel model ){

		String dateAndLocation;
		if ( location.equals(NARAYANPUR) ){
			dateAndLocation = dateTime+" (NP)";
		}else if ( location.equals(MIZMIZI) ){
			dateAndLocation = dateTime+" (MZ)";
		}else {//FARMGATE
			dateAndLocation = dateTime+" (FG)";
		}
		views.setCharSequence( R.id.tvdate, "setText", dateAndLocation );
		views.setCharSequence( R.id.tvrise, "setText", "  :  "+model.getSunrise() );
		views.setCharSequence( R.id.tvset, "setText", "  :  "+model.getSunset() );
		views.setCharSequence( R.id.tvlength, "setText", "  :  "+model.getDaylength() );
		views.setCharSequence( R.id.tvnoon, "setText", "  :  "+model.getSolarnoon() );
	}
}
