package com.socialproximity.socprox;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class User implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static String username;
	public static String userMac;
	public static String realName;
	public static String email;
	
	private static User ref;
	private static Context context;
	private static final String FILENAME = "socprox_file";
	
	private User(JSONObject json, Context con){
		try {
			username = json.get("username").toString();
			userMac = json.get("userMac").toString();
			realName = json.get("realName").toString();
			email = json.get("email").toString();
			context = con;
		} catch(JSONException ex) {
			ex.printStackTrace();
		}
	}
	
	public static User getInstance() {
		if(null == ref) {
			throw new NullPointerException();
		}
		return ref;
	}
	
	public static User getInstance(JSONObject json, Context con){
		if(null == ref){
			ref = new User(json, con);
		}
		return ref;
	}
	
	public static void logout() {
		ref = null;
	}
	
	private static String toJsonString() {
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("username", username);
			jObj.put("userMac", userMac);
			jObj.put("realName", realName);
			jObj.put("email", email);
		
		} catch(JSONException ex) {
			ex.printStackTrace();
		}
		return jObj.toString();
	}
	
	public static boolean writeToLocalStorage(){

		FileOutputStream fos;
		try {
			fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			User.getInstance();
			fos.write(User.toJsonString().getBytes());
			fos.close();
			
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean readFromLocalStorage(){
		return false;
	}
}
