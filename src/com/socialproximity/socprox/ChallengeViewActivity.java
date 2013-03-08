package com.socialproximity.socprox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ChallengeViewActivity extends Activity {
	private final static String DEBUG_TAG = "ChallengeViewActivity";
	private final static boolean debug = true; // debug?
	private final static String STATUS_SUCCESS = "success";
	private final static String STATUS_FAIL = "fail";
	
	private int challengeInstanceId;
	private JSONObject challengeJson;
	private String challengeStatus;
	private ProgressDialog mProgressDialog;
	private String macAddress;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge_view);
        
        macAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        
        mProgressDialog = new ProgressDialog(ChallengeViewActivity.this);
    	mProgressDialog.setIndeterminate(true);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        
        initUI();
    }
    
    private void initUI(){
    	//String challengeString = getIntent().getStringExtra("challenge_json");
    	
    	try{
    		((TextView)findViewById(R.id.tv_challenge_dialog)).setText("Open\nChallenge");
    		
        	String returnedChallengeString = getIntent().getStringExtra("challenge_json");
        	JSONObject returnedChallengeJson = new JSONObject(returnedChallengeString);
        	challengeInstanceId = Integer.parseInt(returnedChallengeJson.getString("m_iID"));
        	String challengeJsonString = returnedChallengeJson.getString("m_oChallenge");
        	challengeJson = new JSONObject(challengeJsonString);
        	
        	//challengeInstanceId = Integer.parseInt(challengeJson.getString("m_iID"));
        	
        	TextView tv_name = (TextView)findViewById(R.id.tv_challenge_suc_name);
        	tv_name.setText(challengeJson.getString("m_strName"));
        	
        	TextView tv_description = (TextView)findViewById(R.id.tv_challenge_suc_description);
        	tv_description.setText(challengeJson.getString("m_strDesc"));
        	
        	if(debug) Log.d(DEBUG_TAG, "Attempting to set challenger text.");
        	TextView tv_challenger = (TextView)findViewById(R.id.tv_challenge_suc_challenger);
        	JSONArray opponentArray = returnedChallengeJson.getJSONArray("m_aOpponents");
        	String opponents = "";
        	for(int i = 0; i < opponentArray.length(); i++){
        		opponents += opponentArray.get(i).toString();
        		if(i != 0) opponents += ", ";
        	}
        	tv_challenger.setText(opponents);
        	if(debug) Log.d(DEBUG_TAG, "Setting challenger text to: " + opponents);
    	} catch(JSONException e) {
    		if(debug) {
    			Log.d(DEBUG_TAG, e.toString());
    		}
		}
    }
    
    public void onChallengeSuccessButtonClicked(View v){
    	mProgressDialog.setMessage("Submitting Challenge");
    	challengeStatus = STATUS_SUCCESS;
    	
    	UpdateChallengeAsyncTask task = new UpdateChallengeAsyncTask();
    	task.execute();	
    }
    
    public void onChallengeFailButtonClicked(View v){
    	mProgressDialog.setMessage("Submitting Challenge");
    	challengeStatus = STATUS_FAIL;
    	
    	UpdateChallengeAsyncTask task = new UpdateChallengeAsyncTask();
    	task.execute();
    }
    
    //==============================================================================
  	//	Helper methods
  	//==============================================================================
  	
    private boolean executeREST(String call) {
		RESTCaller caller = new RESTCaller();
		JSONObject jsonObject = caller.execute(call);
		boolean success = false;
		try {
			success = jsonObject.getBoolean("success");
		} catch (JSONException ex) {
			// plain and simple...return whether an error occurs
			success = false;
			if (debug) {
				Log.d(DEBUG_TAG, ex.getMessage());
			}
		}
		return success;
	}
  	
  	//==============================================================================
  	//	END Helper methods
  	//==============================================================================
  	
  	private class UpdateChallengeAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... sUrl) {
        	boolean result = false;
            try {
            	String call = RESTCaller.updateChallengeCall(macAddress, challengeInstanceId, challengeStatus);
            	result = executeREST(call);
            } catch (Exception e) {
            	if(debug) {
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
            if(!result){
            	Toast.makeText(ChallengeViewActivity.this, "Update failed! Please visit your challenge list to try again.", Toast.LENGTH_SHORT).show();
            }
            ChallengeViewActivity.this.finish();
        }
    }
}
