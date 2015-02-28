package com.example.photofunpro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.example.photofunpro.location.LocationUtils;
import com.example.photofunpro.volley.Const;
import com.example.photofunpro.volley.VolleySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

public class ImagesByUser extends Activity implements ConnectionCallbacks, OnConnectionFailedListener  {

    public String gpsLat, gpsLon, mapDistance, reqParameters, selectedUploader;
	
    private ArrayList<String> imagePath = new ArrayList<String>();
    private ArrayList<String> imageDescription = new ArrayList<String>();
    private ArrayList<String> imageUploader = new ArrayList<String>();
    private ArrayList<String> imageLat = new ArrayList<String>();
    private ArrayList<String> imageLon = new ArrayList<String>();
    private ArrayList<String> imageDate = new ArrayList<String>();
    
    private ProgressDialog pDialog;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;
    
    private GoogleApiClient mGoogleApiClient;
    
    TextView textLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_images_grid_layout);
		
		//back button in the menu
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		pDialog = new ProgressDialog(this);
		pDialog.setTitle("Photo Fun Pro");
		pDialog.setMessage("Loading images...");
		pDialog.setCancelable(true);	
		pDialog.show();
		
		Bundle imageExtras = getIntent().getExtras();
		selectedUploader = imageExtras.getString("uploader");
		
		ImagesByUser.this.setTitle("Photos by: " + selectedUploader);
		
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /** Set the update interval*/
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
		
		mapDistance = "1000";
		
		if (getLocation()) {
			reqParameters = "Latitude=" + gpsLat + "&Longitude=" + gpsLon + "&Distance=" + mapDistance;
		} else {
			reqParameters = "Latitude=-26.1555618041992&Longitude=28.1099967956543&Distance=" + mapDistance;			
		}
		
		//Log.d("images", "images -- before going into retrieve new images ");
		retrieveUserPhotos();		
		//Log.d("images", "images -- after going into retrieve new images ");		
    }
	
	/*
	 * get the adapter ready and start loading images
	 */
	public void getAdapterReady() {		
	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(new ViewImagesAdapter(this, imagePath));

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            //String imgDate = imageDate.get(position);
	            final String imgUploader = imageUploader.get(position);
	            final String imgPath = imagePath.get(position);
	            final String imgDescription = imageDescription.get(position);
	            final String imgLat = imageLat.get(position);
	            final String imgLon = imageLon.get(position);
	            
				// custom dialog
				final Dialog dialog = new Dialog(ImagesByUser.this);
				dialog.setContentView(R.layout.photo_detail_view);
				dialog.setTitle("Uploader: " + imgUploader);
	            
				// set the custom dialog components - text, image and button
				TextView text = (TextView) dialog.findViewById(R.id.tvImageDescription);
				text.setText("" + imgDescription);
				
				textLocation = (TextView) dialog.findViewById(R.id.tvPhotoLocation);        				
				textLocation.setText("GPS: " + imgLat + "," + imgLon);
				
				getAddress(imgLat, imgLon);
				
				ImageView imageView = (ImageView) dialog.findViewById(R.id.ivPhoto);
				
				Picasso.with(ImagesByUser.this)
				.load(imgPath)
				.placeholder(R.drawable.photoholder)
				.into(imageView);		
				        	 
				Button dialogButton = (Button) dialog.findViewById(R.id.btClose);
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
					        	 
				Button dialogShare = (Button) dialog.findViewById(R.id.btShare);
				dialogShare.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
				    	    	        
				        Bundle photoBundleDetails = new Bundle();
				        photoBundleDetails.putString("share_image_url", imgPath);
				        photoBundleDetails.putString("share_image_description", imgDescription);
				        
				        Intent openStreetView = new Intent("com.example.photofunpro.SOCIALSHAREMEDIA");
				        openStreetView.putExtras(photoBundleDetails);
				        startActivity(openStreetView);
						
					}
				});
	        	 
				Button dialogViewImageByUser = (Button) dialog.findViewById(R.id.btViewAllByUser);
				dialogViewImageByUser.setVisibility(View.INVISIBLE);
	 					            
				dialog.show();
	        }
	    });		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_camera) {
        	Intent openCamera = new Intent(this, Camera.class);
        	startActivity(openCamera);
			return true;
		} else {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}    
    
	public void retrieveUserPhotos() {
		//Log.d("images", "images -- in retrieve new images ");		
		Log.d("images", "images -- URL TO FETCH - " + Const.WS_URL + "" + reqParameters);		
		
		//For some reason there's no headers on the response and its returned as a String object hence use of StringReques instead of JsonObjectRequest -- Tshepo
		
		// Request a json object response from the provided URL.		
		StringRequest jsonObjReq = new StringRequest(Method.GET, Const.WS_URL + "" + reqParameters, 
			new Response.Listener<String>() {

				@Override
				public void onResponse(String responseObject) {
					
					try {
						
						JSONArray jsonArray = new JSONArray(responseObject);
						
						for (int i = 0; i < jsonArray.length(); i++) {
							
							JSONObject imageDetail = jsonArray.getJSONObject(i);
							
							String imgUploader = imageDetail.getString("Uploader");
							
							if (imgUploader.equals(selectedUploader)) {							
								imagePath.add(imageDetail.getString("ImagePath"));
								imageUploader.add(imgUploader);
								imageDescription.add(imageDetail.getString("PhotoDescription"));
								imageDate.add(imageDetail.getString("PhotoTimeStamp"));
								imageLat.add(imageDetail.getString("Latitude"));
								imageLon.add(imageDetail.getString("Longitude"));	
							}
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (pDialog.isShowing()) pDialog.dismiss();

					getAdapterReady();						
					
				}
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					//Log.d("images", "images error = " + error.getMessage());
					if (pDialog.isShowing()) pDialog.dismiss();
				}
			});
						
		//Access the RequestQueue through my singleton class.
		VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);				
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}


    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                //ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                //errorFragment.setDialog(dialog);
                //errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }
	

    /**
     * Calls getLastLocation() to get the current location
     */
    public boolean getLocation() {

        // Get the current location
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Display the current location in the UI
        String locationCoords = LocationUtils.getLatLng(this, currentLocation);
        //Log.d("photofunpro", "photofunpro - updated coordinates trying to update lat, lon");
        if (locationCoords.length() > 1) {
        	String[] parts = locationCoords.split(",");
        	gpsLat = parts[0];
        	gpsLon = parts[1];
            //Log.d("photofunpro", "photofunpro - updated coordinates lat = " + gpsLat + ", lon = " + gpsLon);
        } else {
        	return false;
        }
        return true;
    }
    
    public void getAddress(String lat, String lon) {

        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present
            return;
        }

        if (servicesConnected()) {
            // Start the background task
            (new GetAddressTask(this)).execute(lat, lon);
        }
    }

    /**
     * An AsyncTask that calls getFromLocation() in the background.
     * The class uses the following generic types:
     * Location - A {@link android.location.Location} object containing the current location,
     *            passed as the input parameter to doInBackground()
     * Void     - indicates that progress units are not used by this subclass
     * String   - An address passed to onPostExecute()
     */
    protected class GetAddressTask extends AsyncTask<String, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {

            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(String... params) {
            /*
             * Get a new geocoding service instance, set for localized addresses. This example uses
             * android.location.Geocoder, but other geocoders that conform to address standards
             * can also be used.
             */
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            // Get the current location from the input parameter list
            String lat = params[0];
            String lon = params[1];

            // Create a list to contain the result address
            List <Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                addresses = geocoder.getFromLocation(Double.parseDouble(lat),Double.parseDouble(lon), 1);

                // Catch network or other I/O problems.
                } catch (IOException exception1) {

                    // Log an error and return an error message
                    Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

                    // print the stack trace
                    exception1.printStackTrace();

                    // Return an error message
                    return (getString(R.string.IO_Exception_getFromLocation));

                // Catch incorrect latitude or longitude values
                } catch (IllegalArgumentException exception2) {

                    // Construct a message containing the invalid arguments
                    String errorString = getString(
                            R.string.illegal_argument_exception,lat,lon);
                    // Log the error and print the stack trace
                    Log.e(LocationUtils.APPTAG, errorString);
                    exception2.printStackTrace();

                    //
                    return errorString;
                }
                // If the reverse geocode returned an address
                if (addresses != null && addresses.size() > 0) {

                    // Get the first address
                    Address address = addresses.get(0);

                    // Format the first line of address
                    String addressText = getString(R.string.address_output_string,

                            // If there's a street address, add it
                            address.getMaxAddressLineIndex() > 0 ?
                                    address.getAddressLine(0) : "",

                            // Locality is usually a city
                            address.getLocality(),

                            // The country of the address
                            address.getCountryName()
                    );

                    // Return the text
                    return addressText;

                // If there aren't any addresses, post a message
                } else {
                  return getString(R.string.no_address_found);
                }
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {

            // Set the address in the UI
        	textLocation.setText("Location: " + address);
        }
    }
    
	
}
