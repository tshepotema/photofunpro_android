package com.example.photofunpro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photofunpro.Camera.ImageCaptureFragment.ErrorDialogFragment;
import com.example.photofunpro.location.LocationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
    
public class MapSectionFragment extends android.app.Fragment
	implements OnMarkerClickListener, OnInfoWindowClickListener,
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, ConnectionCallbacks, OnConnectionFailedListener {    	

    // Stores the current instantiation of the location client in this object
    //private LocationClient mLocationClient;
    private GoogleApiClient mGoogleApiClient;
	
    public String gpsLat, gpsLon, mapDistance;
    
	SharedPreferences sharedPref;
	Editor editor;		    
	
	ProgressDialog mProgressDialog;
	
	Marker customMarker;
    float rotation;
    boolean flat;
    Integer markerSize;
	
    ImageView mapWindowPhoto;
    
    Map<String, String> mapWindowMap = new HashMap<String, String>();
    
	public CameraPosition PREV; 	
	public CameraPosition NEXT; 
	
	Integer photoPosition;
	ArrayList<String> photoPositionCoords = new ArrayList<String>();
	
	private GoogleMap mMap;
	
    private List<Marker> mMarkersListPhotos = new ArrayList<Marker>();
    
	private Button bPrev, bNext, bZoomIn, bZoomOut, bTiltMore, bTiltLess;
	
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MapSectionFragment newInstance(int sectionNumber) {
    	MapSectionFragment fragment = new MapSectionFragment();    	
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /** Demonstrates customizing the info window and/or its contents. */
    @SuppressLint("InflateParams")
	class CustomInfoWindowAdapter implements InfoWindowAdapter {

        // These a both viewgroups containing an ImageView with id "badge" and two TextViews with id "title" and "snippet".
        private final View mWindow;
        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {
            
            String photoID = marker.getTitle();
        	
    		String title = mapWindowMap.get(photoID + "_uploader");    		
            String mapPhotoURL = mapWindowMap.get(photoID + "_url");
    		String mapPhotoDate = mapWindowMap.get(photoID + "_date");    		

            mapWindowPhoto = (ImageView) view.findViewById(R.id.badge);
            
			Picasso.with(getActivity())
			.load(mapPhotoURL)
			.resize(200, 200)
			.placeholder(R.drawable.photoholder)
			.into(mapWindowPhoto);	
            
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.GREEN), 0, titleText.length(), 0);
            titleUi.setText(titleText);

            String snippet = marker.getSnippet();
            snippet += "\nDate : " + mapPhotoDate + "\n"; 
            //snippet += "\nLat : " + mapPhotoLat + " Lon : " + mapPhotoLon; 
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            
            SpannableString snippetText = new SpannableString(snippet);
            snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, snippet.length(), 0);
            snippetUi.setText(snippetText);
        }
    }
    
    //
    // Marker related listeners.
    //
    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator
                        .getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String photoID = marker.getTitle();
    	
		//String title = mapWindowMap.get(photoID + "_uploader");    		
        //String mapPhotoURL = mapWindowMap.get(photoID + "_url");
        String mapPhotoLat = mapWindowMap.get(photoID + "_lat");
        String mapPhotoLon = mapWindowMap.get(photoID + "_lon");
		//String mapPhotoDate = mapWindowMap.get(photoID + "_date");    		
    	    	        
        Bundle mapBundleDetails = new Bundle();
        mapBundleDetails.putDouble("gps_lat", Double.parseDouble(mapPhotoLat));
        mapBundleDetails.putDouble("gps_lon", Double.parseDouble(mapPhotoLon));
        
        Intent openStreetView = new Intent("com.example.photofunpro.STREETVIEW");
        openStreetView.putExtras(mapBundleDetails);
        startActivity(openStreetView);
        
    }
    
    public MapSectionFragment() {
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_map, container, false);
		sharedPref = getActivity().getSharedPreferences(AppSettings.MyPREFERENCES, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		
		mapDistance = sharedPref.getString(AppSettings.distanceKey, "50");
		markerSize = sharedPref.getInt(AppSettings.markerKey, 50);

        rotation = 10;	
        flat = false;	
        photoPosition = 0;
				
        //mLocationClient = new LocationClient(getActivity(), this, this);        
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
        
        bNext = (Button) rootView.findViewById(R.id.next);
        bPrev = (Button) rootView.findViewById(R.id.prev);
        bTiltMore = (Button) rootView.findViewById(R.id.tilt_more);
        bTiltLess = (Button) rootView.findViewById(R.id.tilt_less);
        bZoomIn = (Button) rootView.findViewById(R.id.zoom_in);
        bZoomOut = (Button) rootView.findViewById(R.id.zoom_out);
        
        bPrev.setEnabled(false);
        bPrev.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				onGoToPrev(v);					
			}
		});
        
        bNext.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				onGoToNext(v);					
			}
		});
        
        bTiltMore.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onTiltMore(v);				
			}
		});
        
        bTiltLess.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onTiltLess(v);				
			}
		});
        
        bZoomIn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onZoomIn(v);				
			}
		});
        
        bZoomOut.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onZoomOut(v);				
			}
		});

        setUpMapIfNeeded();
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
	

    /* Called when the Activity is restarted, even before it becomes visible.*/
    @Override
    public void onStart() {
        super.onStart();
        /* Connect the client. Don't re-start any requests here; instead, wait for onResume() */
        //mLocationClient.connect();
        mGoogleApiClient.connect();
    }

	public void connectionNotAvailable() {
        // Create a progress dialog
        mProgressDialog = new ProgressDialog(getActivity());
        // Set progress dialog title
        mProgressDialog.setTitle("Funcam");
        // Set progress dialog message
        mProgressDialog.setMessage("Location services not avalable");
        mProgressDialog.setIndeterminate(true);
        // Show progress dialog
        mProgressDialog.show();            
	}

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
		// Get the current location
		//Location currentLocation = mLocationClient.getLastLocation();
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    	
		
		// Display the current location in the UI
        String locationCoords = LocationUtils.getLatLng(getActivity(), currentLocation);                		
        //Log.d("camerafun", "MapPhotos updated coordinates trying to update lat, lon");		
        if (locationCoords.length() > 1) {
        	String[] parts = locationCoords.split(",");
    		gpsLat = parts[0];
    		gpsLon = parts[1];
    		Double dGPSlat = Double.parseDouble(gpsLat);
    		Double dGPSlon = Double.parseDouble(gpsLon);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dGPSlat, dGPSlon), 10));
            FetchPhotosInMapArea();
        } else {
        	connectionNotAvailable();
        }
		
    }

    /*Called by Location Services if the connection to the location client drops because of an error.*/
    @Override
    public void onDisconnected() {
		//connection dialog
		connectionNotAvailable();
    }

    /* Called by Location Services if the attempt to Location Services fails.*/
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	//Log.d("cameraResult", "funcam cameraResult -- connection failed for some reason - in method onConnectionFailed ");
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {
        		//connection dialog
        		connectionNotAvailable();

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }	

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            getActivity(),
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            //errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
            //errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
        }
    }
    

	@Override
	public void onLocationChanged(Location location) {
		//do nothing here for now		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//do nothing here for now		
	}

	@Override
	public void onProviderEnabled(String provider) {
		//do nothing here for now		
	}

	@Override
	public void onProviderDisabled(String provider) {
		//do nothing here for now		
	}
			

    private void setUpMapIfNeeded() {
    	Log.d("map", "map setUpMapIfNeeded");
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
            	Log.d("map", "map map is null about to call setupmap");
                setUpMap();
            }
        }
    }

    private void setUpMap() {
    	Log.d("map", "map in setUpMap");
        // We will provide our own zoom controls.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Setting an info window adapter allows us to change the both the contents and look of the info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        
    }
    
    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
     * all entry points that call methods on the Google Maps API.
     */
    private boolean checkReady() {
        if (mMap == null) {
        	Log.d("map", "map map is ready? no");
            Toast.makeText(getActivity(), R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
    	Log.d("map", "map map is ready? yes");
        return true;
    }

    /**
     * Called when the Go To Prev button is clicked.
     */
    public void onGoToPrev(View view) {
        if (!checkReady()) {
            return;
        }
        
        if (photoPosition > 0) {
        	String positionCoords = photoPositionCoords.get(photoPosition);
        	String[] parts = positionCoords.split(",");
    		String positionLat = parts[0];
    		String positionLon = parts[1];
	        PREV = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(positionLat), Double.parseDouble(positionLon)))
	            .zoom(15.5f)
	            .bearing(300)
	            .tilt(50)
	            .build();
    		
    		changeCamera(CameraUpdateFactory.newCameraPosition(PREV));
    		photoPosition--;
    		if (!bNext.isEnabled()) {
    			bNext.setEnabled(true);
    		}
        }
        if (photoPosition <= 0) {
        	bPrev.setEnabled(false);
        }
    }

    /**
     * Called when the Animate To Next button is clicked.
     */
    public void onGoToNext(View view) {
        if (!checkReady()) {
            return;
        }
        
        if (photoPosition+1 < photoPositionCoords.size()) {
        	String positionCoords = photoPositionCoords.get(photoPosition);
        	String[] parts = positionCoords.split(",");
    		String positionLat = parts[0];
    		String positionLon = parts[1];
	        NEXT = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(positionLat), Double.parseDouble(positionLon)))
	            .zoom(15.5f)
	            .bearing(0)
	            .tilt(25)
	            .build();
	        changeCamera(CameraUpdateFactory.newCameraPosition(NEXT));
	        photoPosition++;
	        if (!bPrev.isEnabled()) {
	        	bPrev.setEnabled(true);
	        }
        }
        
        if (photoPosition+1 >= photoPositionCoords.size()) {
        	bNext.setEnabled(false);
        }
    }

    /**
     * Called when the zoom in button is clicked.
     */
    public void onZoomIn(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.zoomIn());
    }

    /**
     * Called when the zoom out button is clicked.
     */
    public void onZoomOut(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.zoomOut());
    }

    /**
     * Called when the tilt more button is clicked.
     */
    public void onTiltMore(View view) {
        CameraPosition currentCameraPosition = mMap.getCameraPosition();
        float currentTilt = currentCameraPosition.tilt;
        float newTilt = currentTilt + 10;

        newTilt = (newTilt > 90) ? 90 : newTilt;

        CameraPosition cameraPosition = new CameraPosition.Builder(currentCameraPosition)
                .tilt(newTilt).build();

        changeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Called when the tilt less button is clicked.
     */
    public void onTiltLess(View view) {
        CameraPosition currentCameraPosition = mMap.getCameraPosition();

        float currentTilt = currentCameraPosition.tilt;

        float newTilt = currentTilt - 10;
        newTilt = (newTilt > 0) ? newTilt : 0;

        CameraPosition cameraPosition = new CameraPosition.Builder(currentCameraPosition)
                .tilt(newTilt).build();

        changeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }

    /**
     * Change the camera position by moving or animating the camera depending on the state of the
     * animate toggle button.
     */
    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
        mMap.animateCamera(update, callback);
    }
    	
	public void FetchPhotosInMapArea() {		
		// get the posted photos
		new HttpAsyncTask().execute("http://105.235.168.210/DevChallange2/PhotoFun.asmx/FetchPhotosInArea", gpsLat, gpsLon, mapDistance);				
	}
	
    public static String GET(String url, String Lat, String Lon, String mapDistance){
        InputStream inputStream;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            
            HttpPost post = new HttpPost(url);

            JSONObject jsonRequest = new JSONObject();
            
            jsonRequest.put("Latitude", Lat);
            jsonRequest.put("Longitude", Lon);
            jsonRequest.put("Distance", mapDistance);
            
            StringEntity reqEntity = new StringEntity(jsonRequest.toString());
            reqEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(reqEntity);
            
            // make POST request to the url
            HttpResponse httpResponse = httpclient.execute(post);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Tshepo - Error connecting to remote resource";

        } catch (Exception e) {
        	e.printStackTrace();
            //Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        	inputStream.close();
        return result;

    }
		
    public class HttpAsyncTask extends AsyncTask<String, Void, String> {    	
    	
    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0], urls[1], urls[2], urls[3]);
        }

        // onPostExecute process the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {                
                JSONArray response = new JSONArray(result);
                
                for (int i = 0; i < response.length(); i++) {
                    JSONObject webPhotosObj = response.getJSONObject(i);

                    String photoID = webPhotosObj.getString("PhotoID");
                    String uploader = webPhotosObj.getString("Uploader");
                    String description = webPhotosObj.getString("PhotoDescription");
                    String webPhotoURL = webPhotosObj.getString("ImagePath");
                    String webPhotoLat = webPhotosObj.getString("Latitude");
                    String webPhotoLon = webPhotosObj.getString("Longitude");
                    String PhotoTimeStamp = webPhotosObj.getString("PhotoTimeStamp");

            		
            		long timestamp = Long.parseLong(PhotoTimeStamp) * 1000;		
            		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            	    cal.setTimeInMillis(timestamp);
            	    String dateCaptured = DateFormat.format("dd-MMM-yyyy", cal).toString();		
            	    
                    mapWindowMap.put(photoID + "_uploader", uploader);
                    mapWindowMap.put(photoID + "_url", webPhotoURL);
                    mapWindowMap.put(photoID + "_lat", webPhotoLat);
                    mapWindowMap.put(photoID + "_lon", webPhotoLon);
                    mapWindowMap.put(photoID + "_date", dateCaptured);

                    new CustomAddMarker().execute(webPhotoURL, uploader, description, webPhotoLat, webPhotoLon, dateCaptured, photoID);
                    
                    String photoCoords = webPhotoLat + "," + webPhotoLon;
                    
                    photoPositionCoords.add(photoCoords);
                    
                }
                                    
            } catch (JSONException e) {
                e.printStackTrace();
            }                            
        }

    }
    
    public class CustomAddMarker extends AsyncTask<String, Void, BitmapDescriptor> {

        BitmapDescriptor bitmapMarker1;
        
        String uploader, description, webPhotoLat, webPhotoLon, dateCaptured, photoID; 
        
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        @Override
        protected BitmapDescriptor doInBackground(String... url) {  
            String imgUrl = url[0];
            uploader = url[1];
            description = url[2];
            webPhotoLat = url[3];
            webPhotoLon = url[4];
            dateCaptured = url[5];
            photoID = url[6];
            
            try {
                bitmapMarker1 = BitmapDescriptorFactory.fromBitmap(Picasso.with(getActivity()).load(imgUrl).resize(markerSize, markerSize+15).get());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmapMarker1;
        }

        protected void onPostExecute(BitmapDescriptor iconFromURL) {
            try {
            	
            	Double dGPSlat = Double.parseDouble(webPhotoLat);
            	Double dGPSlon = Double.parseDouble(webPhotoLon);
            	
    			customMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(dGPSlat, dGPSlon))
                .title(photoID)
                .snippet(description)
                .icon(iconFromURL)
                .flat(flat)
                .rotation(rotation));        			
    			
    			mMarkersListPhotos.add(customMarker);                                                            
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }       
	
    
    
    public class getSingleImage extends AsyncTask<String, Void, Bitmap> {
        Bitmap bitMapPhoto;
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        @Override
        protected Bitmap doInBackground(String... url) {  
            String imgUrl = url[0];
            
            try {
            	bitMapPhoto = Picasso.with(getActivity()).load(imgUrl).resize(markerSize, markerSize+15).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitMapPhoto;
        }

        protected void onPostExecute(Bitmap imageFromURL) {
            try {
            	
    			mapWindowPhoto.setImageBitmap(imageFromURL);		
    			
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}       
    
        
}