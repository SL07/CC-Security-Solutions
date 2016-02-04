package com.example.ccsecuritysolutions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NewAccount extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_account);
		
		Button save = (Button) findViewById(R.id.save);
		final TextView personNameTextView = (TextView) findViewById(R.id.entered_name);
		final TextView personPasswordTextView = (TextView) findViewById(R.id.entered_password);
		final TextView personEmailTextView = (TextView) findViewById(R.id.entered_email);
		final TextView personEmailPasswordTextView = (TextView) findViewById(R.id.entered_emailPassword);
		
		save.setOnClickListener(new OnClickListener() {
            @SuppressLint("ShowToast")
			public void onClick(View view0) {
            	String personName  = personNameTextView.getText().toString();
            	String personPassword  = personPasswordTextView.getText().toString();
            	String personEmail  = personEmailTextView.getText().toString();
            	String personEmailPassword  = personEmailPasswordTextView.getText().toString();
            	
            	Context currentContext = getBaseContext();
            	
            	PreferenceObject.setName(currentContext,personName);
            	PreferenceObject.setPassword(currentContext,personPassword);
            	PreferenceObject.editEmail(currentContext,personEmail,personEmailPassword);
            	
            	Intent intent = new Intent(currentContext, Home.class);
        		startActivity(intent);
            }
		});
	}

}
