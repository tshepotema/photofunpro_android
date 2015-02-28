package com.example.photofunpro;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;

public class AppSettings extends Fragment {
	EditText etUploaderName, etDistanceKM;
	CheckBox cbWifiOn;
	SeekBar sbMarkerSize;
	Button btSave;
	Integer wifiOn;

	public static final String MyPREFERENCES = "MyPrefs" ;
	public static final String nameKey = "name_key"; 
	public static final String userKey = "user_key"; 
	public static final String distanceKey = "distance_key"; 
	public static final String wifiKey = "wifi_key"; 
	public static final String markerKey = "marker_key"; 
	
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
    public static AppSettings newInstance(int sectionNumber) {
    	AppSettings fragment = new AppSettings();    	
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_settings, container, false);
		
		initializeLayout(rootView);
		
		sharedPref = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		editor = sharedPref.edit();

		etUploaderName.setText(sharedPref.getString(nameKey, "Tshepo"));
		etDistanceKM.setText(sharedPref.getString(distanceKey, "100"));
		sbMarkerSize.setProgress(sharedPref.getInt(markerKey, 50));		
		wifiOn = sharedPref.getInt(wifiKey, 1);
	      	      
		switch (wifiOn) {
			case 1:
				cbWifiOn.setChecked(true);
				break;
			case 2:
				cbWifiOn.setChecked(false);
				break;
		}		
		
		btSave.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// update the shared preferences
				String uploaderName = etUploaderName.getText().toString();
				String distanceKM = etDistanceKM.getText().toString();
				Integer markerSize = sbMarkerSize.getProgress() + 25;
				
				editor.putString(nameKey, uploaderName);
				editor.putString(distanceKey, distanceKM);				
				editor.putInt(markerKey, markerSize);
				if (cbWifiOn.isChecked()) {
					editor.putInt(wifiKey, 1);
				} else {
					editor.putInt(wifiKey, 2);					
				}
				
				//save changes in SharedPreferences
				editor.commit();
				
				onPause();
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
		etUploaderName = (EditText) rootView.findViewById(R.id.etUploaderName);
		etDistanceKM = (EditText) rootView.findViewById(R.id.etDefaultMapDistance);
		cbWifiOn = (CheckBox) rootView.findViewById(R.id.cbUploadWifi);
		sbMarkerSize = (SeekBar) rootView.findViewById(R.id.sbMarkerSize);
		btSave = (Button) rootView.findViewById(R.id.btSave);
	}
}