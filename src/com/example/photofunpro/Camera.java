package com.example.photofunpro;

import java.io.File;

import com.example.photofunpro.database.PhotoFunProDatabaseHelper;
import com.example.photofunpro.database.ImagesTable;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photofunpro.location.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

public class Camera extends FragmentActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    
    /**
     * The {@link ViewPager} that will display the primary sections of the app, one at a time.
     */
    ViewPager mViewPager;
    
	public Integer imgLocalID;
	    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main);
        
        // Create the adapter that will return a fragment for each of the primary sections of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is hierarchical parent.
        actionBar.setHomeButtonEnabled(true);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, 
        	// as the listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera_menu, menu);
		return true;
	}    

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_app_main:
				Intent openMain = new Intent(getApplication(), MainActivity.class);
				startActivity(openMain);
				finish();
                return true;
    		case R.id.action_app_help:
    			Intent openHelp = new Intent(getApplication(), AppHelp.class);
    			startActivity(openHelp);
    			return true;                
    		case R.id.action_sync:
    			Intent syncPhotos = new Intent(getApplication(), UploadService.class);
    			startActivity(syncPhotos);
    			finish();
    			return true;                
        }
        return super.onOptionsItemSelected(item);
    }    
    
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
        
        private static String[] tabTitles = {"Take Photo", "Photo Settings"};
        Context pagerContext;
        
        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                	//simply take photos
                    return new ImageCaptureFragment();
                case 1:
                	//show page to update settings
                    return new PhotoSettings();
                default:
                	//show page to update settings
                    return new PhotoSettings();
            }
        }

        @Override
        public int getCount() {
        	return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
	

    /**
     * photo settings section
     */
    public static class PhotoSettings extends Fragment {
    	EditText etDefaultDescription;
    	Button btSave, btClose;
    	CheckBox cbMultiUploads;
    	RadioButton rbCameraFront, rbCameraBack, rbCameraBoth;
    	
    	SharedPreferences sharedPref;
    	Editor editor;
    	
    	private static final String MyPREFERENCES = "MyPrefs" ;
    	private static final String descriptionKey = "description_key"; 
    	private static final String multiuploadsKey = "multiuploads_key"; 
    	private static final String cameraKey = "camera_key";
    	
    	Boolean multiuploads;
    	Integer cameralens;    	

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.camera_settings_fragment, container, false);
            
            initializeFragLayout(rootView);
    		
    		sharedPref = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    		editor = sharedPref.edit();

    		etDefaultDescription.setText(sharedPref.getString(descriptionKey, getResources().getString(R.string.settings_default_description)));
    		multiuploads = sharedPref.getBoolean(multiuploadsKey, true);
    		cameralens = sharedPref.getInt(cameraKey, 2);
    	      	      
    		if (multiuploads) {
				cbMultiUploads.setChecked(true);
    		} else {
				cbMultiUploads.setChecked(false);
    		}
    	      
			switch (cameralens) {
				case 0:
					rbCameraBack.setChecked(true);
					break;
				case 1:
					rbCameraFront.setChecked(false);
					break;
				case 2:
					rbCameraBoth.setChecked(false);
					break;
			}
    		
			btSave.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					// update the shared preferences
					String defaultPhotoDes = etDefaultDescription.getText().toString();
					
					editor.putString(descriptionKey, defaultPhotoDes);
					editor.putBoolean(multiuploadsKey, cbMultiUploads.isChecked());
					
					if (rbCameraBack.isChecked()) {
						editor.putInt(cameraKey, 0);
					} else if (rbCameraFront.isChecked()) {
						editor.putInt(cameraKey, 1);
					} else if (rbCameraBoth.isChecked()) {
						editor.putInt(cameraKey, 2);						
					}
					
					//save changes in SharedPreferences
					editor.commit();

					Intent openMain = new Intent(getActivity(), MainActivity.class);
					startActivity(openMain);					
					getActivity().finish();
				}
			});											
			
            return rootView;
        }
        
        private void initializeFragLayout(View rootView) {
        	etDefaultDescription = (EditText) rootView.findViewById(R.id.etDefaultDescription);
        	cbMultiUploads = (CheckBox) rootView.findViewById(R.id.cbMultiUploads);
        	rbCameraBack = (RadioButton) rootView.findViewById(R.id.rbCameraBack);
        	rbCameraFront = (RadioButton) rootView.findViewById(R.id.rbCameraFront);
        	rbCameraBoth = (RadioButton) rootView.findViewById(R.id.rbCameraBoth);
        	btSave = (Button) rootView.findViewById(R.id.btSave);
        	btClose = (Button) rootView.findViewById(R.id.btClose);
        	btClose.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Intent openMain = new Intent(getActivity(), MainActivity.class);
					startActivity(openMain);					
					getActivity().finish();					
				}
			});
        }
    }
        
	public static class ImageCaptureFragment extends Fragment 
		implements LocationListener,
	    GooglePlayServicesClient.ConnectionCallbacks,
	    GooglePlayServicesClient.OnConnectionFailedListener, ConnectionCallbacks, OnConnectionFailedListener {	

	    // A request to connect to Location Services
	    private LocationRequest mLocationRequest;

	    // Stores the current instantiation of the location client in this object
	    //private LocationClient mLocationClient;
	    private GoogleApiClient mGoogleApiClient;

	    // Handles to UI widgets
	    private TextView mLatLng;
	    
		private ImageView ivPhoto;
		private EditText etDescription;
		private Button btSave, btNext, btCancel;
		
		ProgressDialog mProgressDialog;
		
		private Long imageID;
		private String imageLat, imageLon;
		
		private static final int MEDIA_TYPE_IMAGE = 1;
		private static final int MEDIA_TYPE_VIDEO = 2;
		
		private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
		
		private boolean photoTaken = false;
		
		public Uri fileUri;
		public Intent shootPhoto;
		public String imgTimeStamp;
		
		private Context camFragContext;
					
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View cameraView = inflater.inflate(R.layout.camera, container, false);
			
			ivPhoto = (ImageView) cameraView.findViewById(R.id.ivPhoto);			
			etDescription = (EditText) cameraView.findViewById(R.id.etDescription);
			btSave = (Button) cameraView.findViewById(R.id.btSave);
			btNext = (Button) cameraView.findViewById(R.id.btNextPhoto);
			btCancel = (Button) cameraView.findViewById(R.id.btCancel);

	        mLatLng = (TextView) cameraView.findViewById(R.id.lat_lng);
			
	        camFragContext = getActivity();
			
	        // Create a new global location parameters object
	        mLocationRequest = LocationRequest.create();

	        /** Set the update interval*/
	        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

	        // Use high accuracy
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	        // Set the interval ceiling to one minute
	        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
	        
	        //mLocationClient = new LocationClient(getActivity(), this, this);
	        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
	            .addApi(LocationServices.API)
	            .addConnectionCallbacks(this)
	            .addOnConnectionFailedListener(this)
	            .build();
	        
			fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); 		// create a file to save the image
			shootPhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			shootPhoto.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name					
			
			btSave.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					//update the saved image
					updateImageRecord();
				}
			});
			
			btNext.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); 		// create a file to save the image
					shootPhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					shootPhoto.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name					
					Log.d("funcam", "funcam NEW FILE URI = " + fileUri.toString());
					photoTaken = false;
					etDescription.setText("");
					if (getLocation()) {
						Log.d("funcam", "funcam next photo - location is available");
					}
				}
			});
			
			btCancel.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Intent openMain = new Intent(getActivity(), MainActivity.class);
					startActivity(openMain);
					getActivity().finish();
				}
			});
			
			return cameraView;
		}
		
		public void connectionIsAvailable() {
    		btSave.setEnabled(true);
    		btCancel.setEnabled(true);
    		btNext.setEnabled(true);
    		
    		etDescription.setVisibility(View.VISIBLE);
    		mLatLng.setVisibility(View.VISIBLE);
    		
    		if (photoTaken == false) {
    			startActivityForResult(shootPhoto, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    		}
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
    		
    		btSave.setEnabled(false);
    		btCancel.setEnabled(false);
    		btNext.setEnabled(false);
    		
    		etDescription.setVisibility(View.INVISIBLE);
    		mLatLng.setVisibility(View.INVISIBLE);            			
		}
			
		/** Create a file Uri for saving an image or video */
		private Uri getOutputMediaFileUri(int type){
		      return Uri.fromFile(getOutputMediaFile(type));
		}
		
		/** Create a File for saving an image or video */
		private File getOutputMediaFile(int type){
			Log.d("funcam", "funcam -- In getOutputMediaFile");
		    // To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this.
		
		    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
		              Environment.DIRECTORY_PICTURES), "tshepo_photofun");
		    // This location works best if you want the created images to be shared
		    // between applications and persist after your app has been uninstalled.
		
		    // Create the storage directory if it does not exist
		    if (! mediaStorageDir.exists()){
		        if (! mediaStorageDir.mkdirs()){
		            Log.d("funcam", "funcam -- failed to create directory");
		            return null;
		        }
		    }
		
		    // Create a media file name
		    Long tsLong = System.currentTimeMillis()/1000;
		    imgTimeStamp = tsLong.toString();	    
		    File mediaFile;
		    
		    Log.d("funcam", "funcam -- timestamp = " + imgTimeStamp);
		    
		    if (type == MEDIA_TYPE_IMAGE){
			    Log.d("funcam", "funcam -- file = " + File.separator + "IMG_"+ imgTimeStamp + ".jpg");
		        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ imgTimeStamp + ".jpg");
		    } else if(type == MEDIA_TYPE_VIDEO) {
		        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ imgTimeStamp + ".mp4");
		    } else {
		        return null;
		    }	
		    return mediaFile;
		}
						
		public void saveImageRecord(String timeStamp) {
			Log.d("funcam", "funcam save image with lat = " + imageLat + " lon = " + imageLon);
			if (imageLat.equals("null") || imageLon.equals("null")) {
	            String locationCoords = mLatLng.getText().toString();	            
	            if (locationCoords.length() > 1) {
	            	String[] parts = locationCoords.split(",");
	        		imageLat = parts[0];
	        		imageLon = parts[1];
	            }
			}
			
			if (imageLat.equals("null") || imageLon.equals("null")) {
				//TODO: not important right now
				Log.d("funcam", "funcam IMAGE HAS NO lat = " + imageLat + " OR lon = " + imageLon);
			} else {
		    	PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(getActivity());
		    	SQLiteDatabase db = dbHelper.getWritableDatabase();
		    	
		    	ContentValues valuesImg = new ContentValues();
		    			    	
			    Long tsLong = System.currentTimeMillis()/1000;			    
		    	String dateStamp = tsLong.toString();
		    	
		        //create content values
		    	valuesImg.put(ImagesTable.COLUMN_GPS_LAT, imageLat);
		    	valuesImg.put(ImagesTable.COLUMN_GPS_LON, imageLon);
		    	valuesImg.put(ImagesTable.COLUMN_IMG_PATH, timeStamp);
		    	valuesImg.put(ImagesTable.COLUMN_IMG_DATE, dateStamp);
		        
		        //insert the data into the database using a prepared statement
		    	imageID = db.insertWithOnConflict(ImagesTable.TABLE_IMAGES, null, valuesImg, SQLiteDatabase.CONFLICT_REPLACE);
		
		        //CLOSE THE DATABASE
		        db.close();
		        dbHelper.close();
		        Log.d("funcam", "funcam SAVED IMAGE ON ID = " + imageID);		        
	        }
		}		
		
		public void updateImageRecord() {
			if (imageID > 0) {
				if (imageLat.equals("null") || imageLon.equals("null")) {
		            String locationCoords = mLatLng.getText().toString();	            
		            if (locationCoords.length() > 1) {
		            	String[] parts = locationCoords.split(",");
		        		imageLat = parts[0];
		        		imageLon = parts[1];
		            }
				}
				if (imageLat.equals("null") || imageLon.equals("null")) {
					//TODO: not important right now
				} else {
					String description = etDescription.getText().toString();
					//Log.d("funcam", "funcam update image with id = " + imageID + " description = " + description);
					PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(getActivity());
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					
					ContentValues valuesImg = new ContentValues();
					
					//create content values
					valuesImg.put(ImagesTable.COLUMN_TITLE, description);
					
					//update database using a prepared statement
					db.updateWithOnConflict(ImagesTable.TABLE_IMAGES, valuesImg, ImagesTable.COLUMN_ID + "=" + imageID, null, SQLiteDatabase.CONFLICT_IGNORE);
					
					//CLOSE THE DATABASE
					db.close();
					dbHelper.close();
					
					Toast.makeText(getActivity(), "Image Description Updated!", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
				
	    /* Called when the Activity is no longer visible at all. Stop updates and disconnect.*/
	    @Override
	    public void onStop() {
	        // After disconnect() is called, the client is considered "dead".
	    	mGoogleApiClient.disconnect();
	        super.onStop();
	    }
	    
	    /* Called when the Activity is going into the background. Parts of the UI may be visible, but the Activity is inactive.*/
	    @Override
	    public void onPause() {
	        super.onPause();
	    }

	    /* Called when the Activity is restarted, even before it becomes visible.*/
	    @Override
	    public void onStart() {
	        super.onStart();
	        /* Connect the client. Don't re-start any requests here; instead, wait for onResume() */
	        mGoogleApiClient.connect();
	    }
	    
	    /*Called when the system detects that this Activity is now visible.*/
	    @Override
	    public void onResume() {
	        super.onResume();
	    }

	    /*
	     * Handle results returned to this Activity by other Activities started with
	     * startActivityForResult(). In particular, the method onConnectionFailed() in
	     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
	     * start an Activity that handles Google Play services problems. The result of this
	     * call returns here, to onActivityResult.
	     */
	    @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
			Log.d("cameraResult", "funcam cameraResult -- rque code = [" + requestCode + "] :: cam req code = [" + CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE + "]");
			if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
				photoTaken = true;
		        if (resultCode == Activity.RESULT_OK) {
		            // Image captured and saved to fileUri specified in the Intent
		    		Log.d("cameraResult", "funcam cameraResult -- image captured successfully to [" + fileUri.toString() + "]");
		            //Toast.makeText(camContext, "Image saved to:\n" + fileUri.toString(), Toast.LENGTH_LONG).show();
		            
		    		//ivPhoto.setImageURI(fileUri);
					
					//Uri imageUri = Uri.fromFile(new File(fileUri));						
					Picasso.with(getActivity())
					.load("file://" + fileUri)
					.resize(300, 300)
					.placeholder(R.drawable.photoholder)
					.into(ivPhoto);		
		    		
		            Log.d("funcam", "funcam pre save - uri = " + fileUri + " :: ts = " + imgTimeStamp);
		            try {
		            	saveImageRecord(imgTimeStamp);
			            Log.d("funcam", "funcam successfully saved image record to DB tmstmp = " + imgTimeStamp);
		            } catch (Exception e) {
			            Log.d("funcam", "funcam FAILED TO SAVE IMAGE tmstmp = " + imgTimeStamp);
		            	e.printStackTrace();
		            	btSave.setEnabled(false);
		            }
		        } else if (resultCode == Activity.RESULT_CANCELED) {
		            // User cancelled the image capture
		    		Log.d("cameraResult", "funcam cameraResult -- user cancelled camera");
		            //Toast.makeText(camContext, "User cancelled image capture", Toast.LENGTH_LONG).show();
	            	btSave.setEnabled(false);
		        } else {
		            // Image capture failed
		    		Log.d("cameraResult", "funcam cameraResult -- camera activity failed");
	            	btSave.setEnabled(false);
		        }
		    }		
	    	
			Log.d("cameraResult", "funcam cameraResult -- connection request code");
	        // Choose what to do based on the request code
	        switch (requestCode) {
	        	
	            // If the request code matches the code sent in onConnectionFailed
	            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

	                switch (resultCode) {
	                    // If Google Play services resolved the problem
	                    case Activity.RESULT_OK:
	            			Log.d("cameraResult", "funcam cameraResult -- connection request code = RESULT_OK");
	                        // Log the result
	                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
	                    break;
	                    // If any other result was returned by Google Play services
	                    default:
	            			Log.d("cameraResult", "funcam cameraResult -- connection request code = " + getString(R.string.no_resolution));
	                        // Log the result
	                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
	                    break;
	                }

	            // If any other request code was received
	            default:
	            	Log.d("cameraResult", "funcam cameraResult -- connection request code unknown error");
	               // Report that this Activity received an unknown requestCode
	               Log.d(LocationUtils.APPTAG,
	                       getString(R.string.unknown_activity_request_code, requestCode));
	               break;
	        }
	    }

	    /**
	     * Verify that Google Play services is available before making a request.
	     *
	     * @return true if Google Play services is available, otherwise false
	     */
	    private boolean servicesConnected() {

	        // Check that Google Play services is available
	        int resultCode =
	                GooglePlayServicesUtil.isGooglePlayServicesAvailable(camFragContext);

	        // If Google Play services is available
	        if (ConnectionResult.SUCCESS == resultCode) {
	            // In debug mode, log the status
	            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

	            // Continue
	            return true;
	        // Google Play services was not available for some reason
	        } else {
	            // Display an error dialog
	            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
	            if (dialog != null) {
	                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
	                errorFragment.setDialog(dialog);
	                errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
	            }
	            return false;
	        }
	    }

	    /**
	     * Calls getLastLocation() to get the current location
	     */
	    public boolean getLocation() {

	        // If Google Play Services is available
	        if (servicesConnected()) {

	            // Get the current location
	            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

	            // Display the current location in the UI
	            String locationCoords = LocationUtils.getLatLng(getActivity(), currentLocation);
	            mLatLng.setText(locationCoords);
	            Log.d("camerafun", "updated coordinates trying to update lat, lon");
	            if (locationCoords.length() > 1) {
	            	String[] parts = locationCoords.split(",");
	        		imageLat = parts[0];
	        		imageLon = parts[1];
		            Log.d("camerafun", "updated coordinates lat = " + imageLat + ", lon = " + imageLon);
	            	connectionIsAvailable();
	            } else {
	            	connectionNotAvailable();
	            	return false;
	            }
	        }
	        return true;
	    }

	    /*
	     * Called by Location Services when the request to connect the
	     * client finishes successfully. At this point, you can
	     * request the current location or start periodic updates
	     */
	    @Override
	    public void onConnected(Bundle bundle) {
	        //mConnectionStatus.setText(R.string.connected);
	    	Log.d("cameraResult", "funcam cameraResult -- onConnected !!!");
    		// Get the current location
    		Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    		
    		// Display the current location in the UI
            String locationCoords = LocationUtils.getLatLng(getActivity(), currentLocation);                		
    		mLatLng.setText(locationCoords);
            Log.d("camerafun", "updated coordinates trying to update lat, lon");
    		
            if (locationCoords.length() > 1) {
            	String[] parts = locationCoords.split(",");
        		imageLat = parts[0];
        		imageLon = parts[1];
                Log.d("camerafun", "updated coordinates lat = " + imageLat + ", lon = " + imageLon);
            	connectionIsAvailable();
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
	    	Log.d("cameraResult", "funcam cameraResult -- connection failed for some reason - in method onConnectionFailed ");
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
	     * Report location updates to the UI.
	     *
	     * @param location The updated location.
	     */
	    @Override
	    public void onLocationChanged(Location location) {
	        // In the UI, set the latitude and longitude to the value received
	    	String locationCoords = LocationUtils.getLatLng(getActivity(), location);
	    	
	        mLatLng.setText(locationCoords);
            Log.d("camerafun", "updated coordinates trying to update lat, lon");
	        	        
            if (locationCoords.length() > 1) {
            	String[] parts = locationCoords.split(",");
        		imageLat = parts[0];
        		imageLon = parts[1];
                Log.d("camerafun", "updated coordinates lat = " + imageLat + ", lon = " + imageLon);
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
	            errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
	        }
	    }

	    /**
	     * Define a DialogFragment to display the error dialog generated in
	     * showErrorDialog.
	     */
	    public static class ErrorDialogFragment extends DialogFragment {

	        // Global field to contain the error dialog
	        private Dialog mDialog;

	        /**
	         * Default constructor. Sets the dialog field to null
	         */
	        public ErrorDialogFragment() {
	            super();
	            mDialog = null;
	        }

	        /**
	         * Set the dialog to display
	         * @param dialog An error dialog
	         */
	        public void setDialog(Dialog dialog) {
	            mDialog = dialog;
	        }

	        /*This method must return a Dialog to the DialogFragment.*/
	        @Override
	        public Dialog onCreateDialog(Bundle savedInstanceState) {
	            return mDialog;
	        }
	    }

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub			
		}

		@Override
		public void onConnectionSuspended(int arg0) {
			// TODO Auto-generated method stub
			
		}						
		
	}	//end class ImageCapture	
}