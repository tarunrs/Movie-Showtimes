package com.tarunrs.movies;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class SecondScreen extends ListActivity {
	String[] TIMINGS = null;
	String[] THEATERS = null;

	/** Called when the activity is first created. */
	
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  
	  Bundle extras = getIntent().getExtras();
	  JSONArray tts = null;
	  String loc = "Movie Showtimes";
	  if(extras !=null)
	  {
		  String value = extras.getString("theaters");
		  loc = extras.getString("title");
		  try {
			tts = new JSONArray(value);
		  } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	  }
	  try {
		populateArray(tts);
	  } catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	  
	  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, THEATERS));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);
	  lv.setOnItemClickListener(new OnItemClickListener() 
	  	{
		  public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
		  	{		
			  Toast.makeText(getApplicationContext(), TIMINGS[position],Toast.LENGTH_SHORT).show();
			}
		 });
		setTitle(loc);
	  
	}

	private void populateArray(JSONArray theaters) throws JSONException 
	{
		THEATERS = new String[theaters.length()];
		TIMINGS= new String[theaters.length()];
		for(int j = 0; j< theaters.length();j++)
		{
			THEATERS[j] = theaters.getJSONObject(j).getString("name");
			TIMINGS[j] = theaters.getJSONObject(j).getString("times");
		}
	}
}