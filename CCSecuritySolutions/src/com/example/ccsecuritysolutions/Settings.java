package com.example.ccsecuritysolutions;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Settings extends Activity {
	
	SharedPreferences sharedpreferences;
	public static final String MyPREFERENCES = "MyPrefs" ;
	public static final String Name = "nameKey"; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Button change_name = (Button) findViewById(R.id.change_name);
		Button change_alarm_code = (Button) findViewById(R.id.change_alarm_code);
		Button edit_email = (Button) findViewById(R.id.edit_email);

		change_name.setOnClickListener(new OnClickListener() {
            public void onClick(View view0) {
            	Intent intent = new Intent(getBaseContext(), ChangeName.class);
        		startActivity(intent);
            }
        });
		
		change_alarm_code.setOnClickListener(new OnClickListener() {
            public void onClick(View view0) {
            	Intent intent = new Intent(getBaseContext(), ChangePassword.class);
        		startActivity(intent);
            }
        });
		
		edit_email.setOnClickListener(new OnClickListener() {
            public void onClick(View view0) {
            	Intent intent = new Intent(getBaseContext(), ChangeEmail.class);
        		startActivity(intent);
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
