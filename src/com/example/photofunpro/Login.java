package com.example.photofunpro;

import java.util.Locale;

import com.example.photofunpro.database.PhotoFunProDatabaseHelper;
import com.example.photofunpro.database.UsersTable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends Activity {

	private EditText etEmail, etPassword;
	private TextView tvErrorMsg;
	private Button btSignIn;
	private String errorMsg;
	
	public static final String MyPREFERENCES = "MyPrefs" ;
	public static final String userKey = "user_key"; 
	
	SharedPreferences sharedPref;
	Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin_layout);
		initializeLayout();
	}
	
	private void initializeLayout() {
		etEmail = (EditText) findViewById(R.id.etEmail);
		etPassword = (EditText) findViewById(R.id.etPassword);
		
		tvErrorMsg = (TextView) findViewById(R.id.tvLoginErrorMsg);
		
		btSignIn = (Button) findViewById(R.id.btnSingIn);
		btSignIn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (validateLogin()) {
					//keep moving
					Intent openMain = new Intent(Login.this, MainActivity.class);
					startActivity(openMain);
				} else {
					//show invalid login message
					tvErrorMsg.setText(errorMsg);
				}
			}
		});
	}
	
	private boolean validateLogin() {
		String email, password;
		email = etEmail.getText().toString();
		password = etPassword.getText().toString();
		
		Long userID = (long) 0;
		
		if (email.length() < 6) {
			errorMsg = "Please enter a valid email address";
			return false;
		}		
		if (password.length() < 1) {
			errorMsg = "Please enter a password";
			return false;			
		}
		
    	PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(Login.this);
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.setLocale(Locale.getDefault());
    	
		Cursor cur = db.query(UsersTable.TABLE_USERS, null, UsersTable.COLUMN_EMAIL + " = '" + email + "' AND " + UsersTable.COLUMN_PASSWORD + " = '" + password + "'", null, null, null, null, "1");
		cur.moveToFirst();

		while (cur.isAfterLast() == false) {
			userID = cur.getLong(0);			
			cur.moveToNext();
		}
		
        //CLOSE THE DATABASE
        db.close();
        dbHelper.close();
		
        if (userID == 0) {
			errorMsg = "Invalid login details";
			return false;
        }
		
		//set the shared pref to the current user's unique ID
		sharedPref = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		
		editor.putLong(userKey, userID);
        
		//save changes
		editor.commit();
		
		return true;
	}
}
