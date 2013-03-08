package com.socialproximity.socprox;

import java.util.Calendar;

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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class CommunityStatsActivity extends Activity {
	
	private final static String DEBUG_TAG = "CommunityStatsActivity";
	private final static boolean d = true; // debug?
	
	private Calendar timeStamp;
	private final static int refreshInterval = 5; //time in minutes for refreshing content.
	private int textSizeMedium = 20;
	private int textSizeSmall = 16;
	ScrollView sv;
	ProgressDialog mProgressDialog;
	JSONObject restJSONObject;
	BluetoothAdapter mBtAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	sv = new ScrollView(this);
    	setContentView(sv, new LinearLayout.LayoutParams(
    			LinearLayout.LayoutParams.FILL_PARENT,
    			LinearLayout.LayoutParams.FILL_PARENT));
    	sv.setBackgroundResource(R.color.graygame);
    	
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    	
    	timeStamp = Calendar.getInstance();	//get current time
    	
		mProgressDialog = new ProgressDialog(CommunityStatsActivity.this);
    	mProgressDialog.setMessage("Getting Community Stats");
    	mProgressDialog.setIndeterminate(true);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		// execute this when the stats retrieval must be fired
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
    
    private void initUI(){
    	LinearLayout.LayoutParams barLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
    	LinearLayout.LayoutParams separatorLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				2);
		LinearLayout.LayoutParams userLayoutParams = new LinearLayout.LayoutParams(
				0,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				1);
		LinearLayout.LayoutParams scoreLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
    	
    	
    	LinearLayout ll = new LinearLayout(this);
    	ll.setOrientation(LinearLayout.VERTICAL);
    	sv.removeAllViews();
    	sv.addView(ll, barLayoutParams);
		
        try {
        	boolean success = restJSONObject.getBoolean("success");
    		if(!success){
    			initEmptyUI();
    			Toast.makeText(this, restJSONObject.getString("message"), Toast.LENGTH_SHORT).show();
    			return;
    		}
        	
        	JSONArray standings = restJSONObject.getJSONArray("body");
        	
        	// Iterate through all the games' standings and add views to the screen
			for (int i = 0; i < standings.length(); i++) {
				JSONObject currentGame = standings.getJSONObject(i);
				
				// Add the game title bar
				TextView labelTitle = new TextView(this);
				labelTitle.setText(currentGame.getString("m_strName"));
				labelTitle.setTextSize(textSizeMedium);
				labelTitle.setBackgroundResource(R.color.bluegame);
				ll.addView(labelTitle, barLayoutParams);
				
				View separatorBar = new View(this);
				separatorBar.setBackgroundResource(R.color.bluegame);
				
				// Add a separator bar to the linear layout
				ll.addView(separatorBar, separatorLayoutParams);
				
				JSONArray gameStandings = currentGame.getJSONArray("m_aStandings");
				
				// Add each ranking/user/score triplet to the screen
				for (int j = 0; j < gameStandings.length(); j++) {
					JSONObject currentPlace = gameStandings.getJSONObject(j);

					LinearLayout userBar = new LinearLayout(this);
					userBar.setOrientation(LinearLayout.HORIZONTAL);
					
					// Add the standing
					TextView placeText = new TextView(this);
					TextView userText = new TextView(this);
					TextView scoreText = new TextView(this);
					
					String username = currentPlace.getString("username");
					String points = currentPlace.getString("points");
					int place = j + 1;
					placeText.setText(place+".");
					placeText.setTextSize(textSizeSmall);
					userText.setText(username);
					userText.setTextSize(textSizeSmall);
					scoreText.setText(points+"p");
					scoreText.setTextSize(textSizeSmall);
					scoreText.setPadding(0,0,10,0);
					
					// Add place, username and score to the user bar
					userBar.addView(placeText, scoreLayoutParams);
					userBar.addView(userText, userLayoutParams);
					userBar.addView(scoreText, scoreLayoutParams);
					
					View separator = new View(this);
					separator.setBackgroundResource(R.color.bluegame);
					
					// Add the user bar to the linear layout
					ll.addView(userBar, barLayoutParams);
					// Add a separator bar to the linear layout
					ll.addView(separator, separatorLayoutParams);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        timeStamp = Calendar.getInstance();	//get current time
    }
    
    public void initEmptyUI(){
    	LinearLayout ll = new LinearLayout(this);
    	ll.setOrientation(LinearLayout.VERTICAL);
    	TextView tv = new TextView(this);
    	
    	tv.setText("No games found!");
    	
    	ll.addView(tv, new LinearLayout.LayoutParams(
    			LinearLayout.LayoutParams.FILL_PARENT,
    			LinearLayout.LayoutParams.WRAP_CONTENT));
    	
    	sv.removeAllViews();
    	sv.addView(ll, new LinearLayout.LayoutParams(
    			LinearLayout.LayoutParams.FILL_PARENT,
    			LinearLayout.LayoutParams.WRAP_CONTENT));
    	
    	timeStamp = Calendar.getInstance();	//get current time
    }

    private class UILoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... sVoid) {
        	boolean result = true;
            try {
            	String call = RESTCaller.getStandings(mBtAdapter.getAddress(), -1, "");
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
