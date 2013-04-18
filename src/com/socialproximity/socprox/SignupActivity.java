package com.socialproximity.socprox;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class SignupActivity extends Activity {
	private final static String DEBUG_TAG = "SignupActivity";
	private final static boolean debug = true;
	private static String socproxUsername;
	private static final int REQUEST_ENABLE_BT = 100;

	private String username;
	private String password;
	private String confirmPassword;
	private String name, email, regId, socproxID;
	private SignupAsyncTask mRegisterTask;
	private BluetoothAdapter mBluetoothAdapter;
	private ProgressDialog mProgressDialog;
	AlertDialogManager alert = new AlertDialogManager();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Button signupButton = (Button) findViewById(R.id.btn_signup);
		// if device does not support Bluetooth
		if (mBluetoothAdapter == null) {
			Toast.makeText(getBaseContext(),
					"This device does not support Bluetooth.",
					Toast.LENGTH_LONG).show();
			Log.d(DEBUG_TAG, "Device does not support Bluetooth.");
			signupButton.setEnabled(false);
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

		// async dialog settings
		mProgressDialog = new ProgressDialog(SignupActivity.this);
		mProgressDialog.setMessage("Creating account");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				// do nothing, bt enabled
			} else {
				Button signupButton = (Button) findViewById(R.id.btn_signup);
				signupButton.setEnabled(false);
				Toast.makeText(getBaseContext(), "Bluetooth must be enabled.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void onSignupButtonClicked(View v) {
		username = ((EditText) findViewById(R.id.et_username)).getText()
				.toString();
		socproxID = username;
		name = username;
		email = username;
		password = ((EditText) findViewById(R.id.et_password1)).getText()
				.toString();
		confirmPassword = ((EditText) findViewById(R.id.et_password2))
				.getText().toString();

		// Password verification
		if (password.equals(confirmPassword)) {
			// execute this when the signup must be fired

			/***************************************************************************/
			ConnectionDetector cd;
			cd = new ConnectionDetector(getApplicationContext());

			// Check if Internet present
			if (!cd.isConnectingToInternet()) {
				// Internet Connection is not present
				alert.showAlertDialog(SignupActivity.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				// stop executing code by return
				return;
			}

			// Make sure the device has the proper dependencies.
			GCMRegistrar.checkDevice(this);

			// Make sure the manifest was properly set - comment out this line
			// while developing the app, then uncomment it when it's ready.
			GCMRegistrar.checkManifest(this);

			registerReceiver(mHandleMessageReceiver, new IntentFilter(
					CommonUtilities.DISPLAY_MESSAGE_ACTION));

			// Get GCM registration id
			regId = GCMRegistrar.getRegistrationId(this);

			// Check if regid already presents
			if (regId.equals("")) {
//				// Registration is not present, register now with GCM
				GCMRegistrar.register(this, CommonUtilities.SENDER_ID);
				Log.d("SignupActivity", "Starting to register 1.");
			} else {
				// Device is already registered on GCM
				if (GCMRegistrar.isRegisteredOnServer(this)) {
					// Skips registration.
					Toast.makeText(getApplicationContext(),
							"Already registered with GCM", Toast.LENGTH_LONG)
							.show();
					// TODO: Remove unregister...it's for testing purposes!
					GCMRegistrar.unregister(this);
					
					/*Log.d("SignupActivity", "Had to unregister 2.");
				} else {
					Log.d("SignupActivity", "Starting to register.");*/
					// Try to register again, but not in the UI thread.
					// It's also necessary to cancel the thread onDestroy(),
					// hence the use of AsyncTask instead of a raw thread.
					GCMRegistrar.register(this, CommonUtilities.SENDER_ID);
				}
			}
			/***************************************************************************/
		} else {
			Toast.makeText(getBaseContext(), "Passwords must match.",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(CommonUtilities.EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			/**
			 * Take appropriate action on this message depending upon your app
			 * requirement For now i am just displaying it on the screen
			 * */
			Log.d("QQQQQ", intent.getAction());
			if(newMessage != null){
				if(newMessage.equals(getString(R.string.gcm_registered))){
					regId = GCMRegistrar.getRegistrationId(SignupActivity.this);
					mRegisterTask = new SignupAsyncTask();
					mRegisterTask.execute();
				}
			}

			// Releasing wake lock
			WakeLocker.release();
		}
	};

	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	// ==============================================================================
	// Helper methods
	// ==============================================================================

	private class SignupAsyncTask extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(String... sUrl) {
			boolean result = false;
			try {
				// register user in our DB
				String call = RESTCaller.registerCall(
						mBluetoothAdapter.getAddress(), username, password);
				Log.d("ASDFADSF", "call: " + call);
				result = executeREST(call);
				
				// register user's GCM id to db
				ServerUtilities.register(SignupActivity.this, name, email, regId, socproxID);
				Log.d("SignupActivity", "name: " + name);
				Log.d("SignupActivity", "email: " + email);
				Log.d("SignupActivity", "regId: " + regId);
				Log.d("SignupActivity", "socproxID: " + socproxID);
			} catch (Exception e) {
				Log.d("SignupAsyncTask", "Failed to register!");
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
			if (result) {
				Intent intent = new Intent(SignupActivity.this,
						RegisterAliasActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} else {
				Toast.makeText(SignupActivity.this, "Account creation failed.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private boolean executeREST(String call) {
		RESTCaller caller = new RESTCaller();
		JSONObject jsonObject = caller.execute(call);
		boolean error = false;

		try {
			boolean success = jsonObject.getBoolean("success");
			if (!success) error = true;
			else {
				JSONObject body = jsonObject.getJSONObject("body");
				socproxUsername = body.getString("m_strUsername"); // make
																			// sure
																			// there
																			// is a
																			// username
	
				// Signup successful
				JSONObject jObj = new JSONObject();
				jObj.put("username", socproxUsername);
				jObj.put("userMac", body.getString("m_strMac"));
				jObj.put("realName", body.getString("m_strName"));
				jObj.put("email", body.getString("m_strFacebook"));
				User.getInstance(jObj, SignupActivity.this);
			}
		} catch (JSONException ex) {
			// If there is no m_strUsername field then there was an error (user
			// not in database).
			error = true;
			if (debug) {
				Log.d(DEBUG_TAG, ex.getMessage());
			}
		}

		return !error;

	}

	// ==============================================================================
	// END Helper methods
	// ==============================================================================
}
