package com.example.photofunpro;

import java.util.Locale;

import com.example.photofunpro.database.PhotoFunProDatabaseHelper;
import com.example.photofunpro.database.UsersTable;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends Fragment {

	ImageView profilePic, ivEditProfile;
	TextView tvName, tvEmail, tvDesignation, tvBio; 
	Button btnViewMap, btnViewImages;
	
	private Long userID;
	
	String uploader;
	
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
    public static Profile newInstance(int sectionNumber) {
    	Profile fragment = new Profile();    	
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_layout, container, false);
        
        //get the user's profile ID		
		sharedPref = getActivity().getSharedPreferences(AppSettings.MyPREFERENCES, Context.MODE_PRIVATE);
		editor = sharedPref.edit();

        //setup the activity's layout
        initializeLayout(rootView);
				
		String userName = "", email = "", designation = "", bio = "";
		
        //get the use's details from the DB
    	PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(getActivity());
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.setLocale(Locale.getDefault());
		
		userID = sharedPref.getLong(AppSettings.userKey, 0);
    	
		Cursor cur = db.query(UsersTable.TABLE_USERS, null, UsersTable.COLUMN_ID + " = " + userID, null, null, null, null, "1");
		cur.moveToFirst();

		while (cur.isAfterLast() == false) {
			email = cur.getString(1);
			userName = cur.getString(4);
			designation = cur.getString(5);
			bio = cur.getString(6);
			cur.moveToNext();
		}
		
        //CLOSE THE DATABASE
        db.close();
        dbHelper.close();
		uploader = sharedPref.getString(AppSettings.nameKey, userName);        
		
        tvName.setText(userName);
        tvEmail.setText(email);
        tvDesignation.setText(designation);
        tvBio.setText(bio);
        
        Bitmap proPicBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.pro_pic);
        Bitmap circularBitmap = getCircularBitmapFrom(proPicBitmap);
        
        profilePic.setImageBitmap(circularBitmap);
        
	    return rootView;
	}

	private void initializeLayout(View rootView) {
		profilePic = (ImageView) rootView.findViewById(R.id.ivProfilePic);
		tvName = (TextView) rootView.findViewById(R.id.tvName);
		tvEmail = (TextView) rootView.findViewById(R.id.tvEmail);
		tvDesignation = (TextView) rootView.findViewById(R.id.tvDesignation);
		tvBio = (TextView) rootView.findViewById(R.id.tvBio);
		ivEditProfile = (ImageView) rootView.findViewById(R.id.ivEditProfile);
		btnViewMap = (Button) rootView.findViewById(R.id.btnProfileMapView);
		btnViewImages = (Button) rootView.findViewById(R.id.btnProfileViewPhotos);
		
		btnViewImages.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
		        Bundle imagesBundleDetails = new Bundle();
		        imagesBundleDetails.putString("uploader", uploader);
		        
		        Intent openViewImages = new Intent("com.example.photofunpro.IMAGESBYUSER");
		        openViewImages.putExtras(imagesBundleDetails);
		        startActivity(openViewImages);				
			}
		});
		
		ivEditProfile.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(), "Edit Profile", Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
    
    public static Bitmap getCircularBitmapFrom(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        float radius = bitmap.getWidth() > bitmap.getHeight() ? ((float) bitmap
                .getHeight()) / 2f : ((float) bitmap.getWidth()) / 2f;
        Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP,
                TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        Canvas canvas = new Canvas(canvasBitmap);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                radius, paint);

        return canvasBitmap;
    }    
	
}
