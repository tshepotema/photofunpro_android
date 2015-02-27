package com.example.photofunpro;

import com.example.photofunpro.database.PhotoFunProDatabaseHelper;
import com.example.photofunpro.database.UsersTable;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Register extends Activity {

	private EditText etEmail, etPassword, etPasswordConfirm;
	private TextView tvErrorMsg;
	private Button btRegister;
	private String errorMsg;
	
	public static final String MyPREFERENCES = "MyPrefs" ;
	public static final String userKey = "user_key"; 
	
	SharedPreferences sharedPref;
	Editor editor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_layout);
		initializeLayout();
	}
	
	private void initializeLayout() {
		etEmail = (EditText) findViewById(R.id.etEmail);
		etPassword = (EditText) findViewById(R.id.etPassword);
		etPasswordConfirm = (EditText) findViewById(R.id.etPasswordConfirm);
		
		tvErrorMsg = (TextView) findViewById(R.id.tvErrorMsg);
		
		btRegister = (Button) findViewById(R.id.btnRegister);
		btRegister.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (validateRegister()) {
					//keep moving
					Intent openMain = new Intent(Register.this, MainActivity.class);
					startActivity(openMain);
				} else {
					//show invalid registration details message
					tvErrorMsg.setText(errorMsg);
				}
			}
		});
	}
	
	private boolean validateRegister() {
		String email, password, passwordConfirm;
		
		email = etEmail.getText().toString();
		password = etPassword.getText().toString();
		passwordConfirm = etPasswordConfirm.getText().toString();
		
		if (email.length() < 6) {
			errorMsg = "Please enter a valid email address";
			return false;
		}		
		if (password.length() < 1) {
			errorMsg = "Please enter a password";
			return false;			
		}		
		if (!password.equals(passwordConfirm)) {
			errorMsg = "Password does not match the confirmation password";
			return false;			
		}
		
		addUser(email, password);
		return true;
	}
	
	private void addUser(String email, String password) {
		Long userID;
		
		//add user to the DB
    	PhotoFunProDatabaseHelper dbHelper = new PhotoFunProDatabaseHelper(Register.this);
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	
    	ContentValues valuesUser = new ContentValues();
    	
        //create content values
    	valuesUser.put(UsersTable.COLUMN_EMAIL, email);
    	valuesUser.put(UsersTable.COLUMN_PASSWORD, password);
    	valuesUser.put(UsersTable.COLUMN_NAME, "Guest");
    	valuesUser.put(UsersTable.COLUMN_DESIGNATION, "e.g Software Tester");
    	valuesUser.put(UsersTable.COLUMN_BIO, "short bio...");
        
        //insert the data into the database using a prepared statement
    	userID = db.insertWithOnConflict(UsersTable.TABLE_USERS, null, valuesUser, SQLiteDatabase.CONFLICT_REPLACE);

        //CLOSE THE DATABASE
        db.close();
        dbHelper.close();

		
		//set the shared pref to the current user's unique ID
		sharedPref = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
                
		editor.putLong(userKey, userID);
        
		//save changes
		editor.commit();
	}

}
