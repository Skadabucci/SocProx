package com.socialproximity.socprox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ChallengeAcceptanceActivity extends Activity {
	private final static String DEBUG_TAG = "ChallengeAcceptanceActivity";
	private final static boolean d = true; // debug?
	private final static String STATUS_ACCEPTED = "accepted";
	private final static String STATUS_DENIED = "denied";
	
	private JSONObject challengeJson;
	private ProgressDialog mProgressDialog;
	private String challengeStatus;
	private String macAddress;
	private int challengeInstanceId;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge_acceptance);
        
        macAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        
        mProgressDialog = new ProgressDialog(ChallengeAcceptanceActivity.this);
    	mProgressDialog.setIndeterminate(true);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        
        try{
        	String returnedChallengeString = getIntent().getStringExtra("challenge_json");
        	JSONObject returnedChallengeJson = new JSONObject(returnedChallengeString);
        	challengeInstanceId = Integer.parseInt(returnedChallengeJson.getString("m_iID"));
        	String challengeJsonString = returnedChallengeJson.getString("m_oChallenge");
        	challengeJson = new JSONObject(challengeJsonString);
        	
        	//challengeInstanceId = Integer.parseInt(challengeJson.getString("m_iID"));
        	
        	TextView tv_name = (TextView)findViewById(R.id.tv_challenge_acc_name);
        	tv_name.setText(challengeJson.getString("m_strName"));
        	
        	TextView tv_description = (TextView)findViewById(R.id.tv_challenge_acc_description);
        	tv_description.setText(challengeJson.getString("m_strDesc"));
        	
        	if(d) Log.d(DEBUG_TAG, "Attempting to set challenger text.");
        	TextView tv_challenger = (TextView)findViewById(R.id.tv_challenge_acc_challenger);
        	JSONArray opponentArray = returnedChallengeJson.getJSONArray("m_aOpponents");
        	String opponents = "";
        	for(int i = 0; i < opponentArray.length(); i++){
        		opponents += opponentArray.get(i).toString();
        		if(i != 0) opponents += ", ";
        	}
        	tv_challenger.setText(opponents);
        	if(d) Log.d(DEBUG_TAG, "Setting challenger text to: " + opponents);
        } catch (Exception ex){
        	// TODO: handle this exception?
        	// To handle this, cause the acceptance activity to display an error or close
        	Log.e(DEBUG_TAG, ex.toString());
        }
        
        if(d) Log.d(DEBUG_TAG, getIntent().toString());
    }
    
    /**  */
    @Override
    protected void onNewIntent(Intent intent){
    	String text = intent.getData().toString();
    	if(d) Log.d(DEBUG_TAG, text);
    	
    	/*challengeDevice = intent.getStringExtra("bt_address");
    	
    	TextView tv = (TextView) findViewById(R.id.tv_challenge_dialog);
    	tv.setText("Challenging device: " + challengeDevice);*/
    }
    
    public void onChallengeAcceptButtonClicked(View v){
    	mProgressDialog.setMessage("Accepting Challenge");
    	challengeStatus = STATUS_ACCEPTED;
    	
    	UpdateChallengeAsyncTask task = new UpdateChallengeAsyncTask();
    	task.execute();
    }
    
    public void onChallengeDenyButtonClicked(View v){
    	mProgressDialog.setMessage("Denying Challenge");
    	challengeStatus = STATUS_DENIED;
    	
    	UpdateChallengeAsyncTask task = new UpdateChallengeAsyncTask();
    	task.execute();
    }
    
    private boolean executeREST(String call) {
		RESTCaller caller = new RESTCaller();
		JSONObject jsonObject = caller.execute(call);
		boolean success = false;
		try {
			success = jsonObject.getBoolean("success");
		} catch (JSONException ex) {
			// plain and simple...return whether an error occurs
			success = false;
			if (d) {
				Log.d(DEBUG_TAG, ex.getMessage());
			}
		}
		return success;
	}
    
    private class UpdateChallengeAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... sUrl) {
        	boolean result = false;
            try {
            	String call = RESTCaller.updateChallengeCall(macAddress, challengeInstanceId, challengeStatus);
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
            	if(challengeStatus.equals(STATUS_ACCEPTED)){
            		Intent intent = new Intent(ChallengeAcceptanceActivity.this, ChallengeViewActivity.class);
                	intent.putExtra("challenge_json", getIntent().getStringExtra("challenge_json"));
                	startActivity(intent);
            	}
            	else{
            		ChallengeAcceptanceActivity.this.finish();
            	}
            }
            else{
            	Toast.makeText(ChallengeAcceptanceActivity.this, "Update failed! Please visit your challenge list to try again.", Toast.LENGTH_SHORT).show();
            	ChallengeAcceptanceActivity.this.finish();
            }
        }
    }
}
