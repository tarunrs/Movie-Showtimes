package com.tarunrs.movies;

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
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MovieShowtimes extends TabActivity implements android.view.View.OnClickListener, OnSharedPreferenceChangeListener{
    /** Called when the activity is first created. */
	String city = "Bangalore";
	String[] Times = null;
	String[] TheaterTimes = null;
	int whence =0;
	
	private ProgressDialog m_ProgressDialog = null; 
    private ArrayAdapter<String> m_adapter = null, t_adapter =null;
    private ArrayList<String> MovieListItems = new ArrayList<String>();
    private ArrayList<String> TheaterListItems = new ArrayList<String>();
    private Runnable loadListing;
    GlobalData globalListing = null;
	
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
    private void refreshList(){
    	Button back;
		 getCity();
		 loadListing = new Runnable(){
	          public void run() {
	              getListing();
	          };
		  };
		  Thread thread =  new Thread(null, loadListing, "ListingRetrieveBackground");
		  thread.start();
		  m_ProgressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);  
		  Toast.makeText(getApplicationContext(), city, Toast.LENGTH_SHORT).show();
		  back = (Button) findViewById(R.id.back);
		  back.setOnClickListener(null);
	}
    private Runnable returnRes = new Runnable() {
        	public void run() {
        	//	if(m_adapter != null)
        		loadMovies();
        	}
        };
    private Runnable displayToast= new Runnable() {
        	public void run() {
        	//	if(m_adapter != null)
        		Toast.makeText(getApplicationContext(), "Showtimes not available for the requested date",Toast.LENGTH_SHORT).show();
        	}
        };
        
	private void loadMovies(){
		m_adapter.clear();
        if( globalListing.movies != null && globalListing.movies.names != null &&  globalListing.movies.names.size()> 0){
        	if(m_adapter != null)
        		m_adapter.notifyDataSetChanged();
            for(int i=0;i<globalListing.movies.names.size();i++)
            {
            	m_adapter.add(URLDecoder.decode(globalListing.movies.names.get(i)));
            }
            ListView lv = (ListView)findViewById(R.id.movies);
      	    lv.setTextFilterEnabled(true);
      	    lv.setOnItemClickListener(new OnItemClickListener() {
      		  	public void onItemClick(AdapterView<?> parent, View view,
      		        int position, long id) {
      		  		try {
						handleMovieButtonClick(position);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
      		    }
      		  });
        }
        t_adapter.clear();
        if( globalListing.theaters!= null && globalListing.theaters.names != null &&  globalListing.theaters.names.size()> 0){
        	if(t_adapter != null)
        		t_adapter.notifyDataSetChanged();
            for(int i=0;i<globalListing.theaters.names.size();i++)
            {
            	t_adapter.add(URLDecoder.decode(globalListing.theaters.names.get(i)));
            }
            ListView lv = (ListView)findViewById(R.id.theaters);
      	    lv.setTextFilterEnabled(true);
      	    lv.setOnItemClickListener(new OnItemClickListener() {
      		  	public void onItemClick(AdapterView<?> parent, View view,
      		        int position, long id) {
      		  		try {
						handleTheaterButtonClick(position);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
      		    }
      		  });
            
        }
        m_ProgressDialog.dismiss();
        setTitle(globalListing.location);
        TextView tv = (TextView) findViewById(R.id.whence);
        tv.setText(globalListing.currentDate);
	}
   
	private void getListing() {
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
	
	private void populateArray() throws MalformedURLException, IOException, JSONException 
	{
    	String encCity = URLEncoder.encode(city);
    	//String url = "http://tarun.appspot.com/movielistings?city="+encCity;
    	
    	String url = "http://109.169.62.221/tarunrs/movies?city="+encCity+"&date="+whence;
    	//String url = "http://10.0.2.2:4567/tarunrs/movies?city="+encCity+"&date="+whence;	
    	JSONArray movies = getHttpResult(url);
    	if(movies == null) return;
		if(movies.length() > 0)
		{
			globalListing.movies.theaters.clear();
			globalListing.movies.names.clear();
			for(int i = 0; i < movies.length(); i++){
				JSONObject movie = movies.getJSONObject(i);
				JSONArray theaters = movie.getJSONArray("theaters");
				globalListing.movies.theaters.add(theaters);
				globalListing.movies.names.add(movie.getString("name")) ;
			}
		}
		// get the listing sorted by theaters
		url = "http://109.169.62.221/tarunrs/theaters?city="+encCity+"&date="+whence;	
    	movies = getHttpResult(url);
		if(movies.length() > 0)
		{
			globalListing.theaters.movies.clear();
			globalListing.theaters.names.clear();
			for(int i = 0; i < movies.length(); i++){
				JSONObject movie = movies.getJSONObject(i);
				JSONArray theaters = movie.getJSONArray("movies");
				globalListing.theaters.movies.add(theaters);
				globalListing.theaters.names.add(movie.getString("name")) ;
			}
		}
		
	}
	
	private JSONArray getHttpResult(String url){
	 	HttpClient httpClient = new DefaultHttpClient();
    	HttpContext localContext = new BasicHttpContext();
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

		JSONArray webResult = null;
		JSONObject response ;
		try {
			response = new JSONObject(res);
			JSONObject result = response.getJSONObject("result");
			webResult = result.getJSONArray("listing");
			if(result.getString("date") == "-" || webResult.length() == 0){
				whence--;
				runOnUiThread(displayToast);
				return null;
			}			
			globalListing.location = result.getString("location");
			globalListing.currentDate= result.getString("date");
			
			return webResult; 
		}
		catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	} 
    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView lvm, lvt;
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Button butn = null;
  	  	prefs.registerOnSharedPreferenceChangeListener(this);
        // Create an Intent to launch an Activity for the tab (to be reused)
         // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("movies").setIndicator("Movies",
                          res.getDrawable(R.drawable.ic_tab_movie))
                      .setContent(R.id.movies);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        spec = tabHost.newTabSpec("theaters").setIndicator("Theaters",
                          res.getDrawable(R.drawable.ic_tab_theaters))
                      .setContent(R.id.theaters);
        tabHost.addTab(spec);
        tabHost.setCurrentTab(0);
        lvm = (ListView)findViewById(R.id.movies);
        lvt = (ListView)findViewById(R.id.theaters);
        
        globalListing = new GlobalData();
        globalListing.movies.theaters = new ArrayList<JSONArray>(); 
		globalListing.movies.names = new ArrayList<String>();
		globalListing.theaters.movies = new ArrayList<JSONArray>(); 
		globalListing.theaters.names = new ArrayList<String>();
		
        m_adapter = new ArrayAdapter<String>(this, R.layout.list_item, MovieListItems);
        t_adapter = new ArrayAdapter<String>(this, R.layout.list_item, TheaterListItems);
        lvm.setAdapter(m_adapter);
        lvt.setAdapter(t_adapter);
        butn = (Button) findViewById(R.id.next);
        butn.setOnClickListener(this);
        butn.setText(" > ");
        butn = (Button) findViewById(R.id.previous);
        butn.setOnClickListener(this);
        butn.setText(" < ");
        butn = (Button) findViewById(R.id.first);
        butn.setOnClickListener(this);
        butn.setText(" << ");
        
        refreshList();
    }
    
    
    private void handleMovieButtonClick(int pos) throws JSONException
    {
    	Button back =null;
    	back = (Button) findViewById(R.id.back);
        back.setOnClickListener(this);
    	if(m_adapter != null)
			m_adapter.clear();
    	
    	JSONArray theaters = globalListing.movies.theaters.get(pos);
    	Times = new String[theaters.length()];
    	
        if( globalListing.movies != null && globalListing.movies.theaters != null){
        	if(m_adapter != null)
        		m_adapter.notifyDataSetChanged();
        	for(int j = 0; j< theaters.length();j++)
            {
            	m_adapter.add(URLDecoder.decode(theaters.getJSONObject(j).getString("name")));
            	Times[j] = theaters.getJSONObject(j).getString("times");
            }
            ListView lv = (ListView)findViewById(R.id.movies);
      	    lv.setTextFilterEnabled(true);
      	    lv.setOnItemClickListener(new OnItemClickListener(){
      		  public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
  		  		{		
      			  Toast.makeText(getApplicationContext(), Times[position],Toast.LENGTH_SHORT).show();
  		  		}
      	  	});
        }		
    }
    
    private void handleTheaterButtonClick(int pos) throws JSONException
    {
    	Button back =null;
    	back = (Button) findViewById(R.id.back);
        back.setOnClickListener(this);
    	if(t_adapter != null)
			t_adapter.clear();
    	
    	JSONArray movies  = globalListing.theaters.movies.get(pos);
    	TheaterTimes = new String[movies.length()];
    	
        if( globalListing.theaters != null && globalListing.theaters.movies != null){
        	if(t_adapter != null)
        		t_adapter.notifyDataSetChanged();
        	for(int j = 0; j< movies.length();j++)
            	{
        		t_adapter.add(URLDecoder.decode(movies.getJSONObject(j).getString("name")));
        		TheaterTimes[j] = movies.getJSONObject(j).getString("times");
            	}
            ListView lv = (ListView)findViewById(R.id.theaters);
      	    lv.setTextFilterEnabled(true);
      	    lv.setOnItemClickListener(new OnItemClickListener(){
      		  public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
  		  		{		
      			  Toast.makeText(getApplicationContext(), TheaterTimes[position],Toast.LENGTH_SHORT).show();
  		  		}
      	  	});
        }		
    }
    
    
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.back:
				loadMovies();
				break;
			case R.id.first:
				whence = 0;
				refreshList();
				break;
			case R.id.next:
				whence++;
				refreshList();
				break;
			case R.id.previous:
				if(whence ==0 )
				{
	      			  Toast.makeText(getApplicationContext(), "Showtimes not available for the requested date",Toast.LENGTH_SHORT).show();
	      			  break;			
				}
				whence--;
				refreshList();
				break;
			default: break;
		}
	}
}