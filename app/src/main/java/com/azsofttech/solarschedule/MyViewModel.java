package com.azsofttech.solarschedule;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyViewModel extends ViewModel {

    MutableLiveData<String> mutableLiveData;
    public MyViewModel() {
        getMutableLiveData().setValue( null );
    }

    public MutableLiveData<String> getMutableLiveData(){
        if( mutableLiveData == null ){
            mutableLiveData = new MutableLiveData<>();
        }
        return mutableLiveData;
    }

    public void setMyData( String myString ){
        getMutableLiveData().setValue( myString );
    }
}
