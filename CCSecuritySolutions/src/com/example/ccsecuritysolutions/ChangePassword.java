package com.example.ccsecuritysolutions;

import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePassword extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);

		Button save_pass = (Button) findViewById(R.id.button1);
		final TextView oldPasswordTextView = (TextView) findViewById(R.id.oldPassword);
		final TextView passwordTextView = (TextView) findViewById(R.id.newPassword);
		final TextView passwordTextView2 = (TextView) findViewById(R.id.newPassword2);
		
		save_pass.setOnClickListener(new OnClickListener() {
			@SuppressLint("ShowToast")
            public void onClick(View view0) {
            	String oldPass  = oldPasswordTextView.getText().toString();
            	String newPass  = passwordTextView.getText().toString();
            	String newPass2  = passwordTextView2.getText().toString();
            	
            	Context baseContext = getBaseContext();
            	
            	//check if the new password was input correctly
            	if(oldPass.equals(PreferenceObject.getPassword(baseContext)) && newPass.equals(newPass2)){
            		PreferenceObject.setPassword(baseContext,newPass);
            		Toast.makeText(baseContext, "Password Has Been Changed", Toast.LENGTH_SHORT).show();
            		try {
            		    TimeUnit.SECONDS.sleep(2);
            		} catch (Exception e) {
            			Intent intent = new Intent(getBaseContext(), Settings.class);
                		startActivity(intent);
                		return;
            		}
            		Intent intent = new Intent(getBaseContext(), Settings.class);
            		startActivity(intent);
            		return;
            		
            	}
            	else if(!oldPass.equals(PreferenceObject.getPassword(baseContext))){	//if old password was wrong, maketoast
            		Toast.makeText(baseContext, "That is not your password.", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	else if(!newPass.equals(newPass2)){	//if new password was input incorrectly, maketoast
            		Toast.makeText(baseContext, "You Did not repear your password correctly", Toast.LENGTH_SHORT).show();
            		return;
            	}
            }
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
	}



}
