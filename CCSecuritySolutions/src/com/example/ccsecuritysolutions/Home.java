package com.example.ccsecuritysolutions;

//import com.example.actionbar.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends ActionBarActivity {

	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arm_device);

		//check if there is a an existing preference account, if not then make one
		Context currentContext = getBaseContext();
		if(PreferenceObject.constructPreference(currentContext) == false){
			Intent intent = new Intent(currentContext, NewAccount.class);
			startActivity(intent);
		}
		else{
			Toast.makeText(currentContext, "Welcome " + PreferenceObject.getName(currentContext) + "!", Toast.LENGTH_SHORT).show();
		}

		Button arm_device = (Button) findViewById(R.id.arm_device);
		final TextView passwordTextView = (TextView) findViewById(R.id.editText1);

		arm_device.setOnClickListener(new OnClickListener() {
			public void onClick(View view0) {
				String password  = passwordTextView.getText().toString();
				final Context currentContext = getBaseContext();

				if(PreferenceObject.checkPassword(currentContext,password) == true){
					Log.d("Armdevicetag", "correct password");
					Toast.makeText(currentContext, "Device Armed", Toast.LENGTH_SHORT).show();
					Toast.makeText(getApplicationContext(), "Opening Camera...", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(currentContext, OpenCamera.class);
					startActivity(intent);
					
				}
				else{
					Log.d("Armdevicetag", "incorrect password");
					Toast.makeText(currentContext, "Incorrect Password, Please Try Again", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			openSettings(item);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void openSettings(MenuItem item) {
		// Do something in response to button
		Intent intent = new Intent(this, Settings.class);
		startActivity(intent);
	}

}
