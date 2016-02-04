package com.example.ccsecuritysolutions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ChangeName extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_name);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Button save_name = (Button) findViewById(R.id.save_name);
		final TextView newNameTextView = (TextView) findViewById(R.id.newName);
		
		save_name.setOnClickListener(new OnClickListener() {
			@SuppressLint("ShowToast")
            public void onClick(View view0) {
            	String newName  = newNameTextView.getText().toString();
            	
            	Context baseContext = getBaseContext();
            	
            	PreferenceObject.setName(baseContext, newName);
            	
            	Intent intent = new Intent(getBaseContext(), Settings.class);
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
