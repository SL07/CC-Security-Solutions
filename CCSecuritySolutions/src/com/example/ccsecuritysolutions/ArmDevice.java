package com.example.ccsecuritysolutions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ArmDevice extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arm_device);
		
		Button arm_device = (Button) findViewById(R.id.arm_device);
		final TextView passwordTextView = (TextView) findViewById(R.id.editText1);
		
		arm_device.setOnClickListener(new OnClickListener() {
            @SuppressLint("ShowToast")
			public void onClick(View view0) {
            	String password  = passwordTextView.getText().toString();
            	final Context currentContext = getBaseContext();
            	
            	if(PreferenceObject.checkPassword(currentContext,password) == true){
            		Log.d("Armdevicetag", "correct password");
            		Toast.makeText(currentContext, "Device Armed", Toast.LENGTH_SHORT).show();
            		
            		//chunk of code for nick to modify
            		//makes a new email with the users credentials and sends the user a msg with a photo
            		final GmailSender sender = new GmailSender(PreferenceObject.getEmail(currentContext), PreferenceObject.getEmailPassword(currentContext));
                	new AsyncTask<Void, Void, Void>() {
                        @Override public Void doInBackground(Void... arg) {
                            try {   
                            	//attach a photo
                            	sender.addAttachment("/sdcard/DCIM/CCSecuritySolutionsBuffer/IMG_1.jpg");
                            	//make the body of the program and send it to the user
                            	sender.sendMail("We have detected an intruder in your home!", "Alert from CCSecurity Solutions",   PreferenceObject.getEmail(currentContext),  PreferenceObject.getEmail(currentContext));   
                            } catch (Exception e) {   
                                Log.e("SendMail", e.getMessage(), e);   
                            }
    						return null; 
                        }
                    }.execute();
            	}
            	else{
            		Log.d("Armdevicetag", "incorrect password");
            		Toast.makeText(currentContext, "Incorrect Password, Please Try Again", Toast.LENGTH_SHORT).show();
            	}
            }
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
	}

}
