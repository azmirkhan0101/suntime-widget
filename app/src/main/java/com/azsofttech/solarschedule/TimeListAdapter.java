package com.azsofttech.solarschedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TimeListAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> arrayList;
    TextView timetv;

    public TimeListAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        @SuppressLint("ViewHolder") View view = LayoutInflater.from(context).inflate( R.layout.time_list_item, parent, false );

        timetv = view.findViewById(R.id.timetv);
        int serial = position + 1;
        int descPosition = getCount() - 1 - position;
        timetv.setText( serial+". "+arrayList.get(descPosition) );

        return view;
    }
}
