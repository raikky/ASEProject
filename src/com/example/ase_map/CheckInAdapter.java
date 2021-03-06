package com.example.ase_map;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CheckInAdapter extends ArrayAdapter<CheckIn>
{
	private Activity activity; 
    ArrayList<CheckIn> data;
   
    public CheckInAdapter(Activity activity, int layoutResourceId,ArrayList<CheckIn> checkInList) 
    {
        super(activity, layoutResourceId, checkInList);
        this.activity = activity;
        this.data = checkInList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        View v = convertView;
       
    	LayoutInflater vi =(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.checkin_list_item,null);
           
        ImageView iv = (ImageView)v.findViewById(R.id.CheckInIcon);
        TextView rn = (TextView)v.findViewById(R.id.CheckInName);
        TextView rt = (TextView)v.findViewById(R.id.CheckInText);
       
        CheckIn checkIn = data.get(position);
        
        rn.setText(checkIn.getUsername());
        rt.setText("Checked in at "+checkIn.getTimeDate()+"!");
        iv.setImageResource(R.drawable.android96);

        return v;
    }
}
