package com.azsofttech.solarschedule;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MySingleTon {

    private static MySingleTon instance;

    private Context context;
    private RequestQueue requestQueue;

    private MySingleTon(Context context) {
        this.context = context;
        this.requestQueue = getRequestQueue();
    }

    public static synchronized MySingleTon getInstance(Context context){

        if ( instance == null ){
            instance = new MySingleTon( context );
        }
        return instance;
    }

    public RequestQueue getRequestQueue(){
        if ( requestQueue == null ){
            requestQueue = Volley.newRequestQueue( context.getApplicationContext() );
            requestQueue.getCache().clear();
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req){
        getRequestQueue().add( req );
    }
}
