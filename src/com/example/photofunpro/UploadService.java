package com.example.photofunpro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

import com.example.photofunpro.database.PhotoFunProDatabaseHelper;
import com.example.photofunpro.database.ImagesTable;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadService extends Fragment {

	TextView tvTotalImages, tvUploadedImages, tvLocalImages, tvWifiActive;
	Button btUploadAll;
	
	Integer localImages, uploadedImages;
	String uploader, localImagePath;
	
	File mediaStorageDir;
	
	ProgressBar pbUploadProgress, pbSpinner;
	
	SharedPreferences sharedPref;
	Editor editor;		
	
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static UploadService newInstance(int sectionNumber) {
    	UploadService fragment = new UploadService();    	
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.upload_service_layout, container, false);
		
		sharedPref = getActivity().getSharedPreferences(AppSettings.MyPREFERENCES, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		
		uploader = sharedPref.getString(AppSettings.nameKey, "photofunpro_GuestUser");
		
		initializeLayout(rootView);
		
	    mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "tshepo_photofun");
	    
		btUploadAll.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//start service to upload all images
				pbSpinner.setVisibility(View.VISIBLE);
				uploadAllImages();
			}
		});
	    
		return rootView;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

	private void initializeLayout(View rootView) {
		tvTotalImages = (TextView) rootView.findViewById(R.id.tvTotalImages);
		tvUploadedImages = (TextView) rootView.findViewById(R.id.tvUploadedImages);
		tvLocalImages = (TextView) rootView.findViewById(R.id.tvLocalImages);
		tvWifiActive = (TextView) rootView.findViewById(R.id.tvWifiActive);
		btUploadAll = (Button) rootView.findViewById(R.id.btUploadAll);
		pbUploadProgress = (ProgressBar) rootView.findViewById(R.id.pbUploadProgress);
		pbSpinner = (ProgressBar) rootView.findViewById(R.id.pbSpinner);
								
    	PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(getActivity());
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
		db = getActivity().openOrCreateDatabase(ImagesTable.DATABASE_FUNCAM, SQLiteDatabase.CREATE_IF_NECESSARY, null);
		db.setLocale(Locale.getDefault());

		Cursor curLocal = db.query(ImagesTable.TABLE_IMAGES, new String[] {ImagesTable.COLUMN_ID}, ImagesTable.COLUMN_UPLOADED + " = 0", null, null, null, null);
		localImages = curLocal.getCount();
		
		curLocal.close();

		Cursor curUploaded = db.query(ImagesTable.TABLE_IMAGES, new String[] {ImagesTable.COLUMN_ID},  ImagesTable.COLUMN_UPLOADED + " = 1", null, null, null, null);
		uploadedImages = curUploaded.getCount();
		
		Integer totalImages = localImages + uploadedImages;				
		curUploaded.close();
		
		db.close();
		dbHelper.close();
		
		pbUploadProgress.setMax(totalImages);
		pbUploadProgress.setProgress(uploadedImages);
		
		tvTotalImages.setText(totalImages.toString());
		tvUploadedImages.setText(uploadedImages.toString());
		tvLocalImages.setText(localImages.toString());

		if (isWifiActive()) {
			tvWifiActive.setText("Wifi Status: Active");			
		} else {
			tvWifiActive.setText("Wifi Status: Not Active");						
		}
		
		if ((localImages > 0) && (isWifiActive())) {
			btUploadAll.setEnabled(true);
		} else {
			btUploadAll.setEnabled(false);			
		}
	}
	
	public boolean isWifiActive() {
		getActivity();
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
		    return true;
		}
		return false;
	}
	
	public boolean uploadSinglePhoto(Integer photoID, String pathToImage) {		
		return false;
	}
	
	public void uploadAllImages() {
    	PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(getActivity());
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
		db = getActivity().openOrCreateDatabase(ImagesTable.DATABASE_FUNCAM, SQLiteDatabase.CREATE_IF_NECESSARY, null);
		db.setLocale(Locale.getDefault());

		Cursor cur = db.query(ImagesTable.TABLE_IMAGES, null, ImagesTable.COLUMN_UPLOADED + " = 0", null, null, null, "1");
		cur.moveToFirst();
		
		String myPhotoID = "", myPhotoDescription = "", myPhotoLat = "", myPhotoLon = "", myPhotoDate = "";

		while (cur.isAfterLast() == false) {
			myPhotoID = cur.getString(0);
			myPhotoDescription = cur.getString(1);
			myPhotoLat = cur.getString(2);
			myPhotoLon = cur.getString(3);
			myPhotoDate = cur.getString(4);

			localImagePath = mediaStorageDir.getPath() + File.separator + "IMG_"+ cur.getString(4) + ".jpg";			
			
			cur.moveToNext();
		}
		cur.close();
		db.close();
		dbHelper.close();
		
		if (myPhotoID.isEmpty()) {
			pbSpinner.setVisibility(View.INVISIBLE);
			//Log.d("photofunpro", "photofunpro uploadservice completed");
		} else{
			//Log.d("photofunpro", "photofunpro uploadservice start image upload");
			UploadPhoto photoUpload = new UploadPhoto(getActivity());				
			photoUpload.execute(localImagePath, myPhotoDescription, myPhotoDate, myPhotoLat, myPhotoLon, myPhotoID, uploader);			
		}
	}
		
	public class UploadPhoto extends AsyncTask<String, Void, Void> {
		 
	     //private final String NAMESPACE = "http://105.235.168.210/DevChallange2/";
	     private final String URL = "http://105.235.168.210/DevChallange2/PhotoFun.asmx";
	     private final String SOAP_ACTION = "http://105.235.168.210/DevChallange2/UploadPhoto";
	     //private final String METHOD_NAME = "/UploadPhoto";
	     private Context mContext;
	 
	     public UploadPhoto(Context baseContext) {
	          this.mContext = baseContext;
	     }
	 
	     protected void onPreExecute() {
	    	 Log.d("photofunpro", "photofunpro uploadservice preExe..");
	     }
	 
	     @Override
	     protected Void doInBackground(String... params) {
				Log.d("photofunpro", "photofunpro uploadservice doInBackground... ");

	          try {
	              String filePath = "file:" + params[0];
	              String fileDescription = params[1];
	              String fileTimeStamp = params[2];
	              String filePhotoLat = params[3];
	              String filePhotoLon = params[4];
	              String myPhotoID = params[5];
	              String uploader =  params[6];
	              
	              Bitmap selectedImage = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(filePath));
	              
				  Log.d("photofunpro", "photofunpro uploadservice doInBackground B4 resizing image... ");
	 
	              Bitmap resizedImage;             
	              //resizedImage = getResizedBitmap(selectedImage,500,(int)(selectedImage.getWidth()/(selectedImage.getHeight()/500.0)));

				  int width = selectedImage.getWidth();
				  int height = selectedImage.getHeight();
				  float scaleWidth = ((float) 200) / width;
				  float scaleHeight = ((float) 200) / height;
				  Matrix matrix = new Matrix();
				  //Log.d("photofunpro", "photofunpro uploadservice doInBackground B4 resizing image 2... width = " + width + " height = " + height);
				  matrix.postScale(scaleWidth, scaleHeight);		 
				  //Log.d("photofunpro", "photofunpro uploadservice doInBackground B4 resizing image 3... Swidth = " + scaleWidth + " Sheight = " + scaleHeight);
				  resizedImage = Bitmap.createBitmap(selectedImage, 0, 0, width, height, matrix, false);
				  
	              //Log.d("photofunpro", "photofunpro uploadservice doInBackground image resized ");
	             
	              ByteArrayOutputStream baos = new ByteArrayOutputStream();
	              resizedImage.compress(Bitmap.CompressFormat.JPEG, 70, baos);
	              byte[] byteArrayImage = baos.toByteArray();
	              String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
	 
	              HttpPost httppost = new HttpPost(URL);
	              StringEntity se;
	              
	              String SOAPRequestXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
	                        + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
	                        + "<soap:Body>"
	                        + "<UploadPhoto xmlns=\"http://105.235.168.210/DevChallange2/\">"
	                        + "<PhotoTimeStamp>" + fileTimeStamp + "</PhotoTimeStamp>"
	                        + "<PhotoDescription>" + fileDescription + "</PhotoDescription>"
	                        + "<Uploader>" + uploader + "</Uploader>"
	                        + "<Latitude>" + filePhotoLat + "</Latitude>"
	                        + "<Longitude>" + filePhotoLon + "</Longitude>"
	                        + "<file>"
	                        + encodedImage
	                        + "</file>"
	                        + "<fileExtention>jpg</fileExtention>"
	                        + "</UploadPhoto>"
	                        + "</soap:Body>" + "</soap:Envelope>";
	              
	              se = new StringEntity(SOAPRequestXML, HTTP.UTF_8);
	 
	              se.setContentType("text/xml");
	              httppost.setHeader("Content-Type", "text/xml;charset=UTF-8");
	              httppost.setHeader( "SOAPAction", SOAP_ACTION );
	 
	              httppost.setHeader("Accept", "text/xml,application/text+xml,application/soap+xml");
	              httppost.setEntity(se);
	 
	              HttpClient httpclient = new DefaultHttpClient();
	              BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient.execute(httppost);   
	              
	              
					PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(mContext);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					
					ContentValues valuesImg = new ContentValues();
					
					//create content values
					valuesImg.put(ImagesTable.COLUMN_UPLOADED, "1");
					
					//update database using a prepared statement
					db.updateWithOnConflict(ImagesTable.TABLE_IMAGES, valuesImg, ImagesTable.COLUMN_ID + "=" + myPhotoID, null, SQLiteDatabase.CONFLICT_IGNORE);
					
					//CLOSE THE DATABASE
					db.close();
					dbHelper.close();

	              Log.d("photofunpro", "photofunpro HTTP Response : " + httpResponse.getStatusLine().toString());
	          } catch (Exception ex) {
	              Log.d("photofunpro", "photofunpro exeption : " + ex.getMessage());
	          }
	          return null;
	     }
	 
	     @Override
	     protected void onPostExecute(Void result) {
	    	 if (btUploadAll.isEnabled()) {
	    		 btUploadAll.setEnabled(false);
	    	 }
	    	 //Log.d("photofunpro", "photofunpro uploadservice POST exe..");
	    	 
	    	 uploadedImages++;
			 tvUploadedImages.setText(uploadedImages.toString());
			 
			 localImages--;
			 tvLocalImages.setText(localImages.toString());

	    	 pbUploadProgress.setProgress(uploadedImages);
	    	 
	    	 uploadAllImages();
	     }
	    
	     public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	        int width = bm.getWidth();
	        int height = bm.getHeight();
	        float scaleWidth = ((float) newWidth) / width;
	        float scaleHeight = ((float) newHeight) / height;
	        Matrix matrix = new Matrix();
	 
	        matrix.postScale(scaleWidth, scaleHeight);
	 
	        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	        return resizedBitmap;
	     }
	 
	}	

}
