package com.example.ase_map;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.provider.SyncStateContract.Constants;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MainActivity extends MapActivity implements LocationListener 
{
	private MapView mapView;
	private MapController mapController;
	private LocationManager locationManager;
	
	private String provider;
	private String posStatus;
	
	double latitude;
    double longitude;
    
    GooglePlaces googlePlaces = new GooglePlaces();
    PlacesList nearPlaces;
    PlaceDetails placeDetails;
    
    ImageButton logOutButton;
    ImageButton showNearLocButton;
    
    TextView date;
    ListView placesListView;
    LinearLayout placeLayoutDetails;
    
    Date dNow;
    Handler reviewThread;
    Handler checkInThread;
	
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Set loose policy.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        
        //Tab View Start
        TabHost tabHost=(TabHost)findViewById(R.id.tabhost);
        tabHost.setup();

        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Check Ins");

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Reviews");
        spec2.setContent(R.id.tab2);

//        TabSpec spec3=tabHost.newTabSpec("Tab 3");
//        spec3.setIndicator("Tab 3");
//        spec3.setContent(R.id.tab3);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
//        tabHost.addTab(spec3);
        //Tab View End
        
        //Retrieve and show date.
        dNow = new Date( );
        SimpleDateFormat days = new SimpleDateFormat ("dd.MM.yy");
	    date = (TextView) findViewById(R.id.textView1);
	    date.setText(days.format(dNow));
	    
        if (isOnline())
        {
            System.out.println("INTERNET's FINE"); 
        }
        else 
        { 
            try 
            {
            	new AlertDialog.Builder(getBaseContext()).setTitle("Info").setMessage("No internet connection."+"\n"
            		+ "Please check your internet settings!").setIcon(R.drawable.warning).setNeutralButton("Ok", null).show();
            }
            catch(Exception e) 
            {
            	Log.d(Constants.ACCOUNT_NAME, "Show Dialog: "+e.getMessage());
            }
        }
                       
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        
        // Check if enabled and if not send user to the GSP settings
        if (!enabled) 
        {
        	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        	startActivity(intent);
        } 
        
        // Define the criteria how to select the location in provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        // Initialize the location fields
        if (location != null) 
        {
          System.out.println("Provider " + provider + " has been selected.");
          //onLocationChanged(location);
        } else 
        {
          //latituteField.setText("Location not available");
          //longitudeField.setText("Location not available");
        }
        
        // GPS Location
        GeoPoint point = new GeoPoint((int)(latitude * 1E6), (int)(longitude *1E6));
        
        // Dispalay an itemizedoverlay on the map.
        List<Overlay> mapOverlays = mapView.getOverlays();	
        Drawable drawable = this.getResources().getDrawable(R.drawable.bmarker);
        CustomItemizedOverlay itemizedoverlay = new CustomItemizedOverlay(drawable, this,placeLayoutDetails,placeDetails, nearPlaces, googlePlaces);
        OverlayItem overlayitem = new OverlayItem(point," "," ");
        itemizedoverlay.addOverlay(overlayitem);
        mapOverlays.add(itemizedoverlay);
        
     // Getting listview
        placesListView = (ListView) findViewById(R.id.listView1);
        
        placeLayoutDetails = (LinearLayout) findViewById(R.id.LinearLayout2);
        placeLayoutDetails.setVisibility(View.INVISIBLE);
        
        placesListView.setOnItemClickListener(new OnItemClickListener()
        {
        	public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
        	{
	        		String placeName = ((TextView) view).getText().toString();
	        		placeDetails = Utils.getPlaceDetails(placeName, nearPlaces, googlePlaces);
        			
	        		if(placeLayoutDetails.getVisibility()==View.INVISIBLE)
	        		{
		        		TranslateAnimation anim = new TranslateAnimation(300,0,0,0);
		            	anim.setDuration(1000);
		            	anim.setFillAfter(true);
		            	placeLayoutDetails.startAnimation(anim);
	        		}
	        		placeLayoutDetails.setVisibility(View.VISIBLE);
	        		String location = Utils.createPlaceInfo(placeLayoutDetails,placeDetails,MainActivity.this);
	        		if(reviewThread!=null)
	        		{
	        			reviewThread.removeCallbacksAndMessages(null);
	        			reviewThread = Utils.getReviews(location,MainActivity.this);
	        		}
	        		else
	        		{
	        			reviewThread = Utils.getReviews(location,MainActivity.this);
	        		}
	        		
	        		if(checkInThread!=null)
	        		{
	        			checkInThread.removeCallbacksAndMessages(null);
	        			checkInThread = Utils.getCheckIns(location,MainActivity.this);
	        		}
	        		else
	        		{
	        			checkInThread = Utils.getCheckIns(location,MainActivity.this);
	        		}
             }
        });
        
        posStatus = "Show";
        showNearLocButton = (ImageButton) findViewById(R.id.imageButton1);
        showNearLocButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {              	
            	if(posStatus.equals("Show")||posStatus.equals("Update"))
            	{	
	            	try 
	            	{
						nearPlaces = googlePlaces.search(latitude, longitude,500,null);
						posStatus="Hide";
					} 
	            	catch (Exception e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                ArrayList<String> placesNames = new ArrayList<String>();
	                
	                List<Overlay> mapOverlays = mapView.getOverlays();
	                CustomItemizedOverlay itemizedoverlay = (CustomItemizedOverlay) mapOverlays.get(0);
	                
	            	for(Place place : nearPlaces.results)
	            	{
	            		placesNames.add(place.name);
	            		GeoPoint geoPoint = new GeoPoint((int) (place.geometry.location.lat * 1E6),(int) (place.geometry.location.lng * 1E6));
	            		
	            		OverlayItem overlayitem = new OverlayItem(geoPoint, place.name,place.vicinity);
	            		itemizedoverlay.addOverlay(overlayitem);
	            	}
	            	itemizedoverlay.setPlaceDetails(placeDetails);
	            	itemizedoverlay.setNearPlaces(nearPlaces);
	            	mapOverlays.set(0,itemizedoverlay);
	            	 	
	            	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),R.layout.list_item, placesNames);
	            	// Assign adapter to ListView
	            	placesListView.setAdapter(adapter); 

	            	TranslateAnimation anim = new TranslateAnimation(0,0,-1000,0);
	            	anim.setDuration(placesListView.getAdapter().getCount()*100);
	            	anim.setFillAfter(true);
	            	placesListView.startAnimation(anim);
            	}
            	else if(posStatus.equals("Hide"))
            	{   
            		if(placeLayoutDetails.getVisibility()==View.VISIBLE)
	        		{
	            		TranslateAnimation anim2 = new TranslateAnimation(0,-1000,0,0);
	            		anim2.setDuration(1000);
		            	anim2.setFillAfter(true);
		            	placeLayoutDetails.startAnimation(anim2);
	        		}
	            	placeLayoutDetails.setVisibility(View.INVISIBLE);
            		
            		TranslateAnimation anim1 = new TranslateAnimation(0,1000,0,0);
            		anim1.setDuration(1000);
	            	anim1.setFillAfter(true);
	            	anim1.setAnimationListener(new AnimationListener() 
	            	{
	            	    public void onAnimationEnd(Animation animation) 
	            	    {
	            	    	posStatus="Show";
	    	            	ArrayList<String> placesNames = new ArrayList<String>();
	    	            	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),R.layout.list_item, placesNames);
	    	            	placesListView.setAdapter(adapter); 
	            	    }
	            	    public void onAnimationStart(Animation animation){}
						public void onAnimationRepeat(Animation animation){}
	            	});
	            	placesListView.startAnimation(anim1);
	            }
            }
        });
        
        logOutButton = (ImageButton) findViewById(R.id.imageButton2);
        logOutButton.setOnClickListener(new View.OnClickListener() 
        {
			public void onClick(View v) 
			{
				 // Create new entry for the database and when the location is changed put those values in the db.
		        LocationStuff locEntry = new LocationStuff(MainActivity.this);
		        Bundle extras = getIntent().getExtras();
		        String strvalue= extras.getString("username");        
		        locEntry.open();
		        locEntry.createEntry(strvalue, longitude, latitude);
		        //System.out.println("Locs coming from user: " +strvalue);
		        locEntry.clearDb();
		        System.out.println("Local db on logOut: " +locEntry.getData());
		        locEntry.close();    	

				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction("com.package.ACTION_LOGOUT");
				sendBroadcast(broadcastIntent);
				
				Intent logInIntent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(logInIntent);
			}
		});
   
        // Initialize map fields.
        mapController = mapView.getController();
        mapController.animateTo(point);
        mapController.setZoom(17);
        mapView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected boolean isRouteDisplayed() 
    {
        return false;
    }
    
    /* Request updates at startup */
    @Override
    protected void onResume() 
    {
      super.onResume();
      boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
      if (!enabled) 
      {
      	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
      	startActivity(intent);
      } 
      locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() 
    {
      super.onPause();
      locationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location location) 
    {   	
    	latitude = location.getLatitude();
    	longitude = location.getLongitude();    	
    	GeoPoint point = new GeoPoint((int)(latitude * 1E6), (int)(longitude *1E6));    	    	
    	
    	// Update itemizedoverlay when location changes.
    	List<Overlay> mapOverlays = mapView.getOverlays();
        CustomItemizedOverlay itemizedoverlay = (CustomItemizedOverlay) mapOverlays.get(0);
    	
        // Create new entry for the database and when the location is changed put those values in the local db.
        LocationStuff locEntry = new LocationStuff(MainActivity.this);
        Bundle extras = getIntent().getExtras();
        String strvalue= extras.getString("username");        
        locEntry.open();
        locEntry.createLocalEntry(strvalue, longitude, latitude);
        System.out.println("Local db on change: " + locEntry.getData());
        locEntry.close();    	
    	
        OverlayItem oldOverlayitem = itemizedoverlay.getItem(0);
        OverlayItem newOverlayitem = new OverlayItem(point," "," ");
        itemizedoverlay.setOverlay(oldOverlayitem,newOverlayitem);
        mapOverlays.set(0,itemizedoverlay);
        
        // set updated flag.
        if(placesListView.getAdapter()!=null)
        {
        	if(!placesListView.getAdapter().isEmpty())
            {
        		posStatus="Update";
            }
        }
    	
    	mapController.animateTo(point);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) 
    {
    	// TODO Auto-generated method stub
    }

    public void onProviderEnabled(String provider) 
    {
    	Toast.makeText(this, "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();
    }

    public void onProviderDisabled(String provider) 
    {
    	Toast.makeText(this, "Disabled provider " + provider,Toast.LENGTH_SHORT).show();
    }
    
    public boolean isOnline() 
    {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable())
        {
            Toast.makeText(getBaseContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
            return false;
        }
    return true; 
    }
    
    public String getUsernameFromLogin() {
    	Bundle extras = getIntent().getExtras();
        String strvalue= extras.getString("username");
        return strvalue;
    }
    
    @Override
    public void onBackPressed() 
    {
		new AlertDialog.Builder(MainActivity.this).setTitle(" ").setMessage("Please use the Log Out button at the top to logout first!").setIcon(R.drawable.warning).setNeutralButton("Close", null).show();  			        

//		Dialog d = new Dialog(MainActivity.this);
//		d.setTitle(":(");
//		TextView tv = new TextView(MainActivity.this);
//		tv.setText("Please use the Log Out button at the top to logout!");
//		d.setContentView(tv);
//		d.show();
    }

}
