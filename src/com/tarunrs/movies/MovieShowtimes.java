package com.tarunrs.movies;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MovieShowtimes extends ListActivity implements OnSharedPreferenceChangeListener {
	private Activity _activeActivity;
	public void setActiveActivity(Activity activity) {
	    _activeActivity = activity;
	}
	public Activity getActiveActivity() {
	    return _activeActivity;
	}
	String city = "Bangalore";
	private ProgressDialog m_ProgressDialog = null; 
    private ArrayAdapter<String> m_adapter;
    private Runnable loadMovies;
    GlobalData globalData = null;
    String err_msg = "asd";
    boolean loaded = false;
    /** Called when the activity is first created. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:  
            	showSettings();
                break;
            case R.id.refresh:
            	refreshList();
            	break;
            case R.id.exit:
            	finish();     	
                break;            
        }
        return true;
    }
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        refreshList();
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
  
	  globalData = new GlobalData();
	  globalData.MOVIES = new ArrayList<String>() ;
	  globalData.tts = new ArrayList<JSONArray>() ;
	  m_adapter = new ArrayAdapter<String>(this, R.layout.list_item, globalData.MOVIES);
	  setListAdapter(m_adapter);
	  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	  prefs.registerOnSharedPreferenceChangeListener(this);
	  if(!loaded)
	  {
		  refreshList();
		  loaded =true;
	  }
	 
	}
	private void refreshList(){
		 getCity();
		 loadMovies = new Runnable(){
	          public void run() {
	              getMovies();
	          };
		  };
		Thread thread =  new Thread(null, loadMovies, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);
        
        Toast.makeText(getApplicationContext(), city, Toast.LENGTH_SHORT).show();	
	}
    private Runnable returnRes = new Runnable() {

        public void run() {
        	m_adapter.clear();
            if( globalData.MOVIES != null &&  globalData.MOVIES.size()> 0){   
                m_adapter.notifyDataSetChanged();
                for(int i=0;i<globalData.MOVIES.size();i++)
                {
                	m_adapter.add(URLDecoder.decode(globalData.MOVIES.get(i)));
                }
          	  ListView lv = getListView();
        	  lv.setTextFilterEnabled(true);
        	  lv.setOnItemClickListener(new OnItemClickListener() {
        		  	public void onItemClick(AdapterView<?> parent, View view,
        		        int position, long id) {
        		  		handleButtonClick(position);
        		    }
        		  });
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
			setTitle(globalData.loc);
        }
    };
	
	private void getMovies() {
		 try{
			 populateArray();
		 	} catch (Exception e) {

		 	}
           runOnUiThread(returnRes);
       }
	
	private void getCity(){

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            city = prefs.getString("city","Bangalore");
            city = city.trim();
	}
	
	private void showSettings(){
		Intent i = new Intent(this, EditPreferences.class);
    	startActivity(i);
	} 	
	
	private void handleButtonClick(int pos)
    {
		Intent i = new Intent(this, SecondScreen.class);
		String tts = globalData.tts.get(pos).toString();
		i.putExtra("theaters", tts);
		i.putExtra("title", globalData.loc);
    	startActivity(i);
    }
	
	private void populateArray() throws MalformedURLException, IOException, JSONException 
	{
	   	HttpClient httpClient = new DefaultHttpClient();
    	HttpContext localContext = new BasicHttpContext();
    	//String url = "http://www.jammm.in/tarunrs/movies?city="+city;
    	String encCity = URLEncoder.encode(city);
    	String url = "http://10.0.2.2:4567/tarunrs/movies?city="+encCity;
    	
    	//String url = "http://tarun.appspot.com/movies?city="+encCity;
    	HttpGet httpGet = new HttpGet(url);
    	HttpResponse response1 = null;
		try {
			response1 = httpClient.execute(httpGet, localContext);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String res = HttpHelper.request(response1);

		JSONArray movies = null;
		JSONObject response ;
		try {
			response = new JSONObject(res);
			JSONObject result = response.getJSONObject("result");
			globalData.loc = result.getString("location");
			Log.d("asd",globalData.loc);
			movies = result.getJSONArray("listing");
			Log.d("asd",movies.toString());
			saveToFile(res);
		}
		catch(JSONException e) {
			e.printStackTrace();
			populateFromFile(res);
			response = new JSONObject(res);
			movies = response.getJSONArray("listing");
		}
		
		if(movies.length() > 0)
		{
			globalData.tts = new ArrayList<JSONArray>(); 
			globalData.MOVIES = new ArrayList<String>();
			
			for(int i = 0; i < movies.length(); i++){
				JSONObject movie = movies.getJSONObject(i);
				JSONArray theaters = movie.getJSONArray("theaters");
				globalData.tts.add(theaters);
				globalData.MOVIES.add(movie.getString("name")) ;
			}
		}
	}
	
	public void populateFromFile(String content){
		String FILENAME = "listing_file";
		StringBuffer strContent = new StringBuffer("");
		int ch;
		FileInputStream fin = null;
		try {
			fin = openFileInput(FILENAME);
			while( (ch = fin.read()) != -1)
				strContent.append((char)ch);
			content = strContent.toString();
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	return;	
	}
	
	public void saveToFile(String content) {
		String FILENAME = "listing_file";
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			fos.write(content.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}