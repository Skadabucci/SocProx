package com.socialproximity.socprox;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.socialproximity.socprox.RESTCaller.Website;

public class LoginActivity extends Activity {
	private final static String DEBUG_TAG = "LoginActivity";
	private final static boolean d = true; // debug?
	private static String socproxUsername;
    private String username;
    private String password;
    private BluetoothAdapter mBluetoothAdapter;
    
    private static final int REQUEST_ENABLE_BT = 100;
	
	private ProgressDialog mProgressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Button loginButton = (Button) findViewById(R.id.btn_login);
		// if device does not support Bluetooth
		if (mBluetoothAdapter == null) {
			Toast.makeText(this,
					"This device does not support Bluetooth.",
					Toast.LENGTH_LONG).show();
			Log.d(DEBUG_TAG, "Device does not support Bluetooth.");
			loginButton.setEnabled(false);
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				// prompt user to enable bluetooth
			    boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
                if (firstrun){
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    
                    // Save the state
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstrun", false)
                    .commit();
                }    
			}
		}
        mProgressDialog = new ProgressDialog(LoginActivity.this);
    	mProgressDialog.setMessage("Logging In");
    	mProgressDialog.setIndeterminate(true);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    } 
    
    //Button listener (set in xml)
    public void onLoginButtonClicked(View v){
    	username = ((EditText)findViewById(R.id.et_username)).getText().toString().trim();
    	password = ((EditText)findViewById(R.id.et_password)).getText().toString().trim();

    	// execute this when the login must be fired
    	LoginAsyncTask loginAsyncTask = new LoginAsyncTask();
    	loginAsyncTask.execute();
    }
    
    private boolean executeREST(String call) {
		RESTCaller caller = new RESTCaller();
		JSONObject jsonObject = caller.execute(call);
		boolean error = false;
      
		try {
			JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
			socproxUsername = jsonObjectBody.getString("m_strUsername");
			
			//Login successful
			JSONObject jObj = new JSONObject();
			jObj.put("username", socproxUsername);
			jObj.put("userMac", jsonObjectBody.getString("m_strMac"));
			jObj.put("realName", jsonObjectBody.getString("m_strName"));
			jObj.put("email", jsonObjectBody.getString("m_strFacebook"));
			User.getInstance(jObj, LoginActivity.this);
		} catch (JSONException ex) {
			//If there is no m_strUsername field then there was an error (user not in database).
			error=true;
			ex.printStackTrace();
			if(d) {
				Log.d(DEBUG_TAG, "Error on REST return.");
			}
		}
      
        if(error) {
        	try {
	        	//Login error
	          	String errorMessage = jsonObject.getString("message");
	          	Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
	        } catch (JSONException e) {
	    		e.printStackTrace();
	    		if(d) {
					Log.d(DEBUG_TAG, "No error specified by REST.");
				}
	    	}
        }
        
        if(d) {
			Log.d(DEBUG_TAG, "Boolean error = " + error);
		}
        return !error;
  	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				// do nothing, bt enabled
			} else {
				Button loginButton = (Button) findViewById(R.id.btn_login);
				loginButton.setEnabled(false);
				Toast.makeText(getBaseContext(), "Bluetooth must be enabled.",
						Toast.LENGTH_LONG).show();
			}
		}
	}
    
    private class LoginAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... sUrl) {
        	boolean result = false;
            try {
            	String call = RESTCaller.loginCall(Website.SOCPROX, mBluetoothAdapter.getAddress(), username, password);
            	result = executeREST(call);
            } catch (Exception e) {
            	if(d) {
    				Log.d(DEBUG_TAG, "Error on REST execution.");
    			}
            }
            return result;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            if(result){
            	Intent intent = new Intent(LoginActivity.this, GameActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(intent);
            }
            else{
            	Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
