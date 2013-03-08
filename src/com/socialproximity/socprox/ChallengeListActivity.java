package com.socialproximity.socprox;

import java.util.Calendar;

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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ChallengeListActivity extends Activity {
	private final static String DEBUG_TAG = "ChallengeListActivity";
	private final static boolean debug = true; // debug?
	
	private Calendar timeStamp;
	private final static int refreshInterval = 5; //time in minutes for refreshing content.
	private int textSizeMedium = 20;
	private int textSizeSmall = 16;
	ScrollView sv;
	ProgressDialog mProgressDialog;
	BluetoothAdapter mBtAdapter;
	JSONObject restJSONObject;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sv = new ScrollView(this);
    	setContentView(sv, new LinearLayout.LayoutParams(
    			LinearLayout.LayoutParams.FILL_PARENT,
    			LinearLayout.LayoutParams.FILL_PARENT));
        
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    	
    	timeStamp = Calendar.getInstance();	//get current time
    	
    	mProgressDialog = new ProgressDialog(ChallengeListActivity.this);
    	mProgressDialog.setMessage("Getting Challenge List");
    	mProgressDialog.setIndeterminate(true);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	
    	// execute this when the login must be fired
    	UILoaderAsyncTask uiLoaderAsyncTask = new UILoaderAsyncTask();
    	uiLoaderAsyncTask.execute();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	Calendar currentTime = Calendar.getInstance();
    	int h = currentTime.get(Calendar.HOUR_OF_DAY) - timeStamp.get(Calendar.HOUR_OF_DAY);
    	Log.d(DEBUG_TAG, "diff:"+h);
    	if((currentTime.get(Calendar.MINUTE)+h*60 - timeStamp.get(Calendar.MINUTE) > refreshInterval) || currentTime.get(Calendar.DAY_OF_MONTH)!=timeStamp.get(Calendar.DAY_OF_MONTH)) {
    		//if the page hasn't been refresh in the given interval, then refresh the UI.
    		// execute this when the login must be fired
        	UILoaderAsyncTask uiLoaderAsyncTask = new UILoaderAsyncTask();
        	uiLoaderAsyncTask.execute();
    	}
    }
    
    public void initUI(){
    	LinearLayout.LayoutParams barLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
    	
    	LinearLayout.LayoutParams opponentLayoutParams = new LinearLayout.LayoutParams(
    			0,
    			LinearLayout.LayoutParams.WRAP_CONTENT,
    			2);
    	
    	LinearLayout.LayoutParams statusLayoutParams = new LinearLayout.LayoutParams(
    			0,
    			LinearLayout.LayoutParams.WRAP_CONTENT,
    			1);
    	
    	LinearLayout.LayoutParams separatorLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				2);
    	
    	LinearLayout ll_challenge_list = new LinearLayout(this);
    	ll_challenge_list.setBackgroundResource(R.color.graygame);
    	ll_challenge_list.setOrientation(LinearLayout.VERTICAL);
    	
    	sv.removeAllViews();
    	
		try {
			boolean success = restJSONObject.getBoolean("success");
    		if(!success){
    			initEmptyUI();
    			Toast.makeText(this, restJSONObject.getString("message"), Toast.LENGTH_SHORT).show();
    			return;
    		}
			
    		JSONObject bodyJSONObject = restJSONObject.getJSONObject("body");
			JSONArray games = bodyJSONObject.getJSONArray("Games");
			
	    	for (int i = 0; i < games.length(); i++) {
				JSONObject currentGame = games.getJSONObject(i);
	    		
	    		TextView labelTitle = new TextView(this);
	        	labelTitle.setText(currentGame.getString("name"));
	    		labelTitle.setTextSize(textSizeMedium);
	    		//labelTitle.setBackgroundResource(R.drawable.game_title_back);
	    		labelTitle.setBackgroundResource(R.color.bluegame);
	    		ll_challenge_list.addView(labelTitle, barLayoutParams);
	    		
//    			View separator = new View(this);
//				separator.setBackgroundColor(Color.GRAY);
//				// Add a separator bar to the linear layout
//				ll_challenge_list.addView(separator, separatorLayoutParams);
			
				// Get the challenges for the current game
	    		JSONArray challenges = currentGame.getJSONArray("challenges");
	    		for (int j = 0; j < challenges.length(); j++) {
	    			final JSONObject challenge = challenges.getJSONObject(j);
	    			
	    			LinearLayout ll_challenge = new LinearLayout(this);
	    			ll_challenge.setOrientation(LinearLayout.VERTICAL);
	    			ll_challenge.setBackgroundResource(R.drawable.ll_click);
	    			ll_challenge.setClickable(true);
	    			ll_challenge.setOnClickListener(new OnClickListener() {
	    				public void onClick(View v) {
	    					if(ChallengeListActivity.debug) Log.d(ChallengeListActivity.DEBUG_TAG, challenge.toString());
	    					try{
		    					if(challenge.getString("m_strStatus").equals("pending")){
		    						if(challenge.getString("m_strUserAcceptance").equals("pending")){
		    							Intent intent = new Intent(
		    									ChallengeListActivity.this,
		    									ChallengeAcceptanceActivity.class);
		    							intent.putExtra("challenge_json", challenge.toString());
		    							startActivity(intent);
		    						}
		    						else if(challenge.getString("m_strUserAcceptance").equals("accepted")){
		    							Intent intent = new Intent(
		    									ChallengeListActivity.this,
		    									ChallengeViewActivity.class);
		    							intent.putExtra("challenge_json", challenge.toString());
		    							startActivity(intent);
		    						}
		    					}
		    					else if(challenge.getString("m_strStatus").equals("active")){
		    						Intent intent = new Intent(
	    									ChallengeListActivity.this,
	    									ChallengeViewActivity.class);
	    							intent.putExtra("challenge_json", challenge.toString());
	    							startActivity(intent);
		    					}
		    					else if(challenge.getString("m_strStatus").equals("completed")){
		    						//Toast.makeText(getApplicationContext(), "Oh yeah.", Toast.LENGTH_LONG).show();
		    					}
	    					} catch(JSONException ex){
	    						// unhandled!
	    					}
	    				}
	    			});
		    		
		    		TextView challengeTitle = new TextView(this);
		    		challengeTitle.setText(challenge.getJSONObject("m_oChallenge").getString("m_strName"));
		    		challengeTitle.setTextSize(textSizeSmall);
		    		ll_challenge.addView(challengeTitle, barLayoutParams);
		    		
		    		LinearLayout ll_status = new LinearLayout(this);
		    		ll_status.setOrientation(LinearLayout.HORIZONTAL);
		    		
		    		TextView opponentText = new TextView(this);
		    		opponentText.setText("Challenger: "+challenge.getJSONArray("m_aOpponents").getString(0));
		    		opponentText.setTextSize(textSizeSmall);
		    		ll_status.addView(opponentText, opponentLayoutParams);
		    			    		
		    		TextView statusText = new TextView(this);
		    		//statusText.setText(challenge.getString("m_strStatus") + "   ");
		    		statusText.setText(challenge.getString("m_strStatus"));
		    		statusText.setTextSize(textSizeSmall);
		    		statusText.setGravity(Gravity.RIGHT);
		    		statusText.setPadding(0,0,10,0);
		    		ll_status.addView(statusText, statusLayoutParams);
	
		    		ll_challenge.addView(ll_status, barLayoutParams);
		    		
		    		ll_challenge_list.addView(ll_challenge, barLayoutParams);
		    		
		    		View separator2 = new View(this);
					separator2.setBackgroundResource(R.color.bluegame);
					// Add a separator bar to the linear layout
					ll_challenge_list.addView(separator2, separatorLayoutParams);				
	    		}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
		timeStamp = Calendar.getInstance();	//get current time
    	sv.addView(ll_challenge_list, barLayoutParams);
    }
    
    private void initEmptyUI(){
    	timeStamp = Calendar.getInstance();	//get current time
    }
    
    private class UILoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... sVoid) {
        	boolean result = true;
            try {
            	String call = RESTCaller.getChallengeInstancesCall(mBtAdapter.getAddress(), "active,pending,completed");
                RESTCaller caller = new RESTCaller();
                restJSONObject = caller.execute(call);
            } catch (Exception e) {
            	result = false;
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
            if(result){ 
            	initUI();
            }
            else{
            	initEmptyUI();
            }
            mProgressDialog.dismiss();
        }
    }
}
