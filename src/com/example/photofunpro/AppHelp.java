package com.example.photofunpro;

import android.app.Activity;
import android.os.Bundle;

public class AppHelp extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_help);
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
}
