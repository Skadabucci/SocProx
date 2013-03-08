package com.socialproximity.socprox;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserStatsActivity extends Activity {
	private final static String DEBUG_TAG = "UserStatsActivity";
	private final static boolean debug = true;
	
	private Calendar timeStamp;
	private final static int refreshInterval = 5; //time in minutes for refreshing content.
	private int textSizeMedium = 20;
	private int textSizeSmall = 16;
	private JSONObject restJSONObject;
	private ProgressDialog mProgressDialog;
	private BluetoothAdapter mBtAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.user_stats);
        
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        timeStamp = Calendar.getInstance();	//get current time
        
        // async dialog settings
     	mProgressDialog = new ProgressDialog(UserStatsActivity.this);
     	mProgressDialog.setMessage("Loading User Stats");
     	mProgressDialog.setIndeterminate(true);
     	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        
     	UILoaderAsyncTask task = new UILoaderAsyncTask();
     	task.execute();
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
    
    private void initEmptyUI(){
    	timeStamp = Calendar.getInstance();	//get current time
    }
    
    private void initUI(){
    	LinearLayout.LayoutParams separatorLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				2);
    	
    	try {
    		boolean success = restJSONObject.getBoolean("success");
    		if(!success){
    			initEmptyUI();
    			Toast.makeText(this, restJSONObject.getString("message"), Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		JSONObject userStats = restJSONObject.getJSONObject("body");
    		
        	LinearLayout ll = (LinearLayout)findViewById(R.id.ll_container);
        	ll.removeAllViews();
        	ll.setBackgroundResource(R.color.graygame);
        	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        		     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			JSONArray gameStats = userStats.getJSONArray("m_aUserGameStats");
			
			// Add the number of challenges			
			TextView labelChallenges = new TextView(this);
			labelChallenges.setText("Challenges Completed: " + userStats.getString("m_iChallengesCompleted"));
			labelChallenges.setTextSize(textSizeMedium);
			ll.addView(labelChallenges, layoutParams);
			
			View separator = new View(this);
			separator.setBackgroundResource(R.color.bluegame);
			// Add a separator bar to the linear layout
			ll.addView(separator, separatorLayoutParams);
			
			for (int i = 0; i < gameStats.length(); i++) {
				JSONObject currentGame = gameStats.getJSONObject(i);
				
				// Add the game title
				TextView labelTitle = new TextView(this);
				labelTitle.setText(currentGame.getString("m_strGameName"));
				labelTitle.setTextSize(textSizeMedium);
				labelTitle.setBackgroundResource(R.color.bluegame);
				ll.addView(labelTitle, layoutParams);
				
				View separator2 = new View(this);
				separator2.setBackgroundColor(Color.GRAY);
				// Add a separator bar to the linear layout
				ll.addView(separator2, separatorLayoutParams);
				
				// Add the description
				TextView labelDesc = new TextView(this);
				labelDesc.setText("Description: " + currentGame.getString("m_strGameDescription"));
				labelDesc.setTextSize(textSizeSmall);
				ll.addView(labelDesc, layoutParams);
				
				// Add the points
				TextView labelPoints = new TextView(this);
				labelPoints.setText("Total Points: " + currentGame.getString("m_iTotalPoints"));
				labelPoints.setTextSize(textSizeSmall);
				ll.addView(labelPoints, layoutParams);
				
				View separator3 = new View(this);
				separator3.setBackgroundColor(Color.GRAY);
				// Add a separator bar to the linear layout
				ll.addView(separator3, separatorLayoutParams);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	timeStamp = Calendar.getInstance();	//get current time
    }
    
    private class UILoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... sVoid) {
        	boolean result = true;
            try {
            	if(debug) Log.d(DEBUG_TAG, "Starting to execute REST call!");
            	String call = RESTCaller.userStatsCall(mBtAdapter.getAddress());
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
            if(debug) Log.d(DEBUG_TAG, "Starting UserStatsActivity UILoaderAsyncTask");
            mProgressDialog.show();
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(debug) Log.d(DEBUG_TAG, "Finished executing UILoaderAsyncTask");
            if(result){ 
            	initUI();
            }
            else{
            	initEmptyUI();
            }
            mProgressDialog.dismiss();
            Intent intent = new Intent(UserStatsActivity.this, ProximityService.class);
            startService(intent);
        }
    }
}