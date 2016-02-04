package com.example.ccsecuritysolutions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceObject {
	
	private static SharedPreferences sharedpreferences;
	private static final String MyPREFERENCES = "CCSSPreference" ;
	private static final String Name = "Name";
	private static final String Password = "Password";
	private static final String Email = "Email";
	private static final String EmailPassword = "EmailPassword";
	
	//checks if the application user has set up a account yet
	public static boolean constructPreference(Context baseContext){
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		//if there is no user account then return false, otherwise return true
		if(!sharedpreferences.contains(Name)){
			return false;
		}
		else{
			return true;
		}
	}
	
	//method used to set the password of the preference
	public static void setPassword(Context baseContext, String password) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = sharedpreferences.edit();
        editor.putString(Password, password);
        editor.commit(); 
	}
	
	//method used to set the name of the preference
	public static void setName(Context baseContext, String name) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = sharedpreferences.edit();
        editor.putString(Name, name);
        editor.commit(); 
	}
	
	public static boolean checkPassword(Context baseContext, String testPassword) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		//check if the given password matches the preference
		if (testPassword.equals(sharedpreferences.getString(Password, "")))
		{
    		return true;
		}
		else{
			return false;
		}
	}
	
	public static String getName(Context baseContext) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		return sharedpreferences.getString(Name, "");
	}
	
	public static String getPassword(Context baseContext) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		return sharedpreferences.getString(Password, "");
	}
	
	public static void editEmail(Context baseContext, String emailAddress, String emailPassword) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = sharedpreferences.edit();
        editor.putString(Email, emailAddress);
        editor.putString(EmailPassword, emailPassword);
        editor.commit(); 
	}
	
	public static String getEmail(Context baseContext) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		return sharedpreferences.getString(Email, "");
	}
	
	public static String getEmailPassword(Context baseContext) {
		sharedpreferences = baseContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		return sharedpreferences.getString(EmailPassword, "");
	}

}
