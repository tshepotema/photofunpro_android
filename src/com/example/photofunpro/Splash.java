package com.example.photofunpro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Splash extends Activity implements OnClickListener {

	private Button btnSignIn, btnRegister;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_layout);
		initializeLayout();
	}
	
	private void initializeLayout() {
		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		
		btnSignIn.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnSignIn:
			Intent signIn = new Intent(Splash.this, Login.class);
			startActivity(signIn);
			break;
		case R.id.btnRegister:
			Intent register = new Intent(Splash.this, Register.class);
			startActivity(register);
			break;		
		}
	}	
}
