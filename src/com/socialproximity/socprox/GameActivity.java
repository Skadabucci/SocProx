package com.socialproximity.socprox;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class GameActivity extends TabActivity {
	private final static String DEBUG_TAG = "GameActivity";
	private final static boolean d = true; // debug?
	
	private static TabHost tabHost;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        
        tabHost = getTabHost();
        
        // Tab for Stats
        TabSpec statsspec = tabHost.newTabSpec("Stats");
        // setting Title and Icon for the Tab
        statsspec.setIndicator("Stats", getResources().getDrawable(R.drawable.user));
        Intent statsIntent = new Intent(GameActivity.this, UserStatsActivity.class);
        statsspec.setContent(statsIntent);
 
        // Tab for Community
        TabSpec communityspec = tabHost.newTabSpec("Community");
        communityspec.setIndicator("Community", getResources().getDrawable(R.drawable.community));
        Intent communityIntent = new Intent(GameActivity.this, CommunityStatsActivity.class);
        communityspec.setContent(communityIntent);
 
        // Tab for Challenge
        TabSpec challengespec = tabHost.newTabSpec("Challenge");
        challengespec.setIndicator("Challenge", getResources().getDrawable(R.drawable.challenge));
        Intent videosIntent = new Intent(this, ChallengeListActivity.class);
        challengespec.setContent(videosIntent);
 
        // Adding all TabSpec to TabHost
        tabHost.addTab(statsspec); // Adding stats tab
        tabHost.addTab(communityspec); // Adding community tab
        tabHost.addTab(challengespec); // Adding challenge tab
        
//        Intent intent = new Intent(GameActivity.this, ProximityService.class);
//        startService(intent);
    }
    
    //==============================================================================
  	//	Menu methods
  	//==============================================================================
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.edit_profile_option:
            	this.startActivity(new Intent(GameActivity.this, EditProfileActivity.class));
                return true;
            case R.id.logout_option:
            	//logout method
            	User.logout();
            	this.startActivity(new Intent(GameActivity.this, SocProxActivity.class));
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    //==============================================================================
  	//	END Menu methods
  	//==============================================================================
}
