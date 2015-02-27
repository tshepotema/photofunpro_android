package com.example.photofunpro;

import java.io.IOException;
import java.util.List;

import org.brickred.socialauth.Photo;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SocialShareMedia extends Activity {

	// SocialAuth Component
	SocialAuthAdapter adapter;
	Profile profileMap;
	List<Photo> photosList;

	// Android Components
	Button btUpdate;
	EditText edit;
	
	public String shareImageURL, shareImageDescription;
	
	public Bitmap photoBitmapImageURL;
	
	public Context applicationContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.social_share_layout);
		
		Intent shareIntent = getIntent();
		Bundle shareData = shareIntent.getExtras();
		shareImageURL = shareData.getString("share_image_url");
		shareImageDescription = shareData.getString("share_image_description");
		
		applicationContext = getApplicationContext();
		
		edit = (EditText) findViewById(R.id.editTxt);
		edit.setText(shareImageDescription);

		Button share = (Button) findViewById(R.id.sharebutton);
		share.setText("Share");
		share.setTextColor(Color.WHITE);
		share.setBackgroundResource(R.drawable.button_gradient);

		// Add it to Library
		adapter = new SocialAuthAdapter(new ResponseListener());

		// Add providers
		adapter.addProvider(Provider.FACEBOOK, R.drawable.facebook);
		adapter.addProvider(Provider.TWITTER, R.drawable.twitter);

		// Providers that require setting user call Back url
		adapter.addCallBack(Provider.TWITTER, "http://store.zetail.co.za/android/funcam/callback.php");

		// Enable Provider
		adapter.enable(share);

	}

	/**
	 * Listens Response from Library
	 */
	private final class ResponseListener implements DialogListener {
		
		@Override
		public void onComplete(Bundle values) {
			Log.d("funcam", "funcam socialshare Authentication Successful");
						
			// Get name of provider after authentication
			final String providerName = values.getString(SocialAuthAdapter.PROVIDER);
			Log.d("funcam", "funcam socialshare Provider Name = " + providerName);
			Toast.makeText(SocialShareMedia.this, providerName + " connected", Toast.LENGTH_LONG).show();

			btUpdate = (Button) findViewById(R.id.update);
			btUpdate.setEnabled(true);
			
			new getPhotoInBackground().execute(shareImageURL);
						
			btUpdate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Log.d("funcama", "funcam socia media TRYING to upload file for sharing");
						adapter.uploadImageAsync(edit.getText().toString(), "placeholder.png", photoBitmapImageURL, 20, new UploadImageListener());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d("funcama", "funcam socia media failed to upload file for sharing");
						e.printStackTrace();
					}
				}
			});
		}

		@Override
		public void onError(SocialAuthError error) {
			Log.d("funcam", "funcam socialshare Authentication Error: " + error.getMessage());
			btUpdate.setEnabled(false);
		}

		@Override
		public void onCancel() {
			Log.d("funcam", "funcam socialshare Authentication Cancelled");
		}

		@Override
		public void onBack() {
			Log.d("funcam", "funcam socialshare Dialog Closed by pressing Back Key");
		}

	}

    public class getPhotoInBackground extends AsyncTask<String, Void, Bitmap> {

        Bitmap bitmapPhoto;
        
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        @Override
        protected Bitmap doInBackground(String... url) {              
            try {
            	bitmapPhoto = Picasso.with(applicationContext).load(url[0]).resize(300, 300).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmapPhoto;
        }

        protected void onPostExecute(Bitmap imgFromURL) {
            try {
            	photoBitmapImageURL = imgFromURL;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }       		
	
	// To get status of image upload after authentication
	private final class UploadImageListener implements SocialAuthListener<Integer> {	 

		@Override
		public void onExecute(String arg0, Integer t) {
			Log.d("funcama", "funcam socia media onExecute - trying to upload media file");
			Integer status = t;
			Log.d("funcama", "funcam socia media onExecute - status = " + status.toString());
			Toast.makeText(SocialShareMedia.this, "Image Uploaded", Toast.LENGTH_SHORT).show();			
		}

		@Override
		public void onError(SocialAuthError arg0) {
			// TODO Auto-generated method stub
			Log.d("funcama", "funcam socia media onError - " + arg0.toString());			
		}
	}	

}