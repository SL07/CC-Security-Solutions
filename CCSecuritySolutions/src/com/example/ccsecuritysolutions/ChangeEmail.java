package com.example.ccsecuritysolutions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeEmail extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_email);

		Button save_pass = (Button) findViewById(R.id.save);
		final TextView email1TextView = (TextView) findViewById(R.id.new_email1);
		final TextView email2TextView = (TextView) findViewById(R.id.new_email2);
		final TextView emailpassword1TextView = (TextView) findViewById(R.id.newEmailPassword1);
		final TextView emailpassword2TextView = (TextView) findViewById(R.id.newEmailPassword2);
		
		save_pass.setOnClickListener(new OnClickListener() {
			@SuppressLint("ShowToast")
            public void onClick(View view0) {
            	String email1  = email1TextView.getText().toString();
            	String email2  = email2TextView.getText().toString();
            	String pw1	   = emailpassword1TextView.getText().toString();
            	String pw2	   = emailpassword2TextView.getText().toString();
            	
            	Context baseContext = getBaseContext();
            	
            	//check if the new password was input correctly
            	if(email1.equals(email2) && pw1.equals(pw2)){
            		PreferenceObject.editEmail(baseContext,email1,pw1);
            		Intent intent = new Intent(getBaseContext(), Settings.class);
            		startActivity(intent);
            		return;
            		
            	}
            	else {	//if old password was wrong, maketoast
            		Toast.makeText(baseContext, "Something doesnt match", Toast.LENGTH_SHORT).show();
            		return;
            	}
            }
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.change_email, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
