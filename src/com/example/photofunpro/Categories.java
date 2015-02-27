package com.example.photofunpro;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;


public class Categories extends Fragment {
	
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static Categories newInstance(int sectionNumber) {
    	Categories fragment = new Categories();    	
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.categories_layout, container, false);
	
	    GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(getActivity()));
	
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	
	        	String[] imgCategories = {"Sport", "Nature", "People", "Wildlife", "Architecture", "Technology", "Sport", "Nature", "People", "Wildlife", "Architecture", "Technology"}; 
	            String selectedCategory = imgCategories[position];
	        	
	            Bundle intentExtras = new Bundle();
	            intentExtras.putString("selected_category", selectedCategory);
	            
	            Intent viewImagesIntent = new Intent(getActivity(), ViewImages.class);
	            viewImagesIntent.putExtras(intentExtras);
	            startActivity(viewImagesIntent);
	            
	        }
	    });
	return rootView;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
}
