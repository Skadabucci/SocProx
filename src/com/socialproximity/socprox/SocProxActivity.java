package com.socialproximity.socprox;

import org.json.JSONException;
import org.json.JSONObject;

import com.socialproximity.socprox.RESTCaller.Website;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class SocProxActivity extends Activity {
	private final static String DEBUG_TAG = "SocProxActivity";
	private final static boolean d = true; // debug?
	private static String socproxUsername;
	
	/* Twttier4J Variables */
	Twitter twitter;
	RequestToken requestToken;
	public static final String TWITTER_CONSUMERKEY = "MsEHvrFP2yVRqQzn78zvMw";
	public static final String TWITTER_CONSUMERSECRET = "xw0XpcfXMJP22k2Qbsf13VYbwmJ65CkFiTqr4Xgc";
	//Callback URL that tells the WebView to load this activity when it finishes with twitter.com. (see manifest)
	private final String CALLBACKURL = "socprox-app://socprox";	//this will need to be whatever View we went to switch to once the person logs in.
	
	/* Facebook Variables */
	private static final String FACEBOOK_APPID = "192154080895161";
	private static final String FACEBOOK_PERMISSION = "publish_stream";
	private final Handler mFacebookHandler = new Handler();
	private static String facebook_username = "";
	private FacebookConnector facebookConnector;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.facebookConnector = new FacebookConnector(FACEBOOK_APPID, this, getApplicationContext(), new String[] {FACEBOOK_PERMISSION});
    }  
    
    //Button listeners
    public void onFBLoginButtonClicked(View v){
    	facebookLogin();
    }
    
    public void onTwitterLoginButtonClicked(View v){
    	twitterLogin();
    }
    
    public void onLoginButtonClicked(View v){
    	this.startActivity(new Intent(SocProxActivity.this, LoginActivity.class));
    }
    
    public void onCreateAccountButtonClicked(View v){
    	this.startActivity(new Intent(SocProxActivity.this, SignupActivity.class));
    }
    
    //==============================================================================
  	//	Twitter4J methods
  	//==============================================================================
  	
  	/*
  	 * - Creates object of Twitter and sets consumerKey and consumerSecret
  	 * - Prepares the URL accordingly and opens the WebView for the user to provide sign-in details
  	 * - When user finishes signing-in, WebView opens your activity back
  	 */
  	void twitterLogin() {
  		try {
  			twitter = new TwitterFactory().getInstance();
  			twitter.setOAuthConsumer(TWITTER_CONSUMERKEY, TWITTER_CONSUMERSECRET);
  			requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
  			String authUrl = requestToken.getAuthenticationURL();
  			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
  					.parse(authUrl)));
  		} catch (TwitterException ex) {
  			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
  			Log.e("in TwitterLoginActivity.OAuthLogin", ex.getMessage());
  		}
  	}

  	
  	/*
  	 * - Called when WebView calls your activity back.(This happens when the user has finished signing in)
  	 * - Extracts the verifier from the URI received
  	 * - Extracts the token and secret from the URL 
  	 */
  	@Override
  	protected void onNewIntent(Intent intent) {
  		super.onNewIntent(intent);
  		
  		//Checks if the user is already logged in, if they are redirect them to the GameActivity class
  		try {
	        if(User.getInstance()!=null) {
	        	this.startActivity(new Intent(SocProxActivity.this, GameActivity.class));
	        }
        } catch(NullPointerException ex) {
        	
        }
  		
  		Uri uri = intent.getData();
  		try {
  			String verifier = uri.getQueryParameter("oauth_verifier");
  			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,
  					verifier);
  			if(null != accessToken) {
  				twitter.setOAuthAccessToken(accessToken);
  				String call = RESTCaller.loginCall(Website.TWITTER, twitter.getScreenName());
  				executeREST(call);
  		    	
  			} else {
  				Toast.makeText(this, "Not Verified", Toast.LENGTH_LONG).show();
  			}

  		} catch (TwitterException ex) {
  			Log.e("SocProxActivity.onNewIntent", "" + ex.getMessage());
  		} catch (NullPointerException ex) {
  			//This happens on a normal newIntent. Meaning it wasn't waiting for Twitter to return.
  			Log.e("SocProxActivity.onNewIntent", "" + ex.getMessage());
  		}
  	}
  	
  	//==============================================================================
  	//	END Twitter4J methods
  	//==============================================================================
  	
  	//==============================================================================
  	//	Facebook methods
  	//==============================================================================
  	
  	void facebookLogin() {
    	if (!facebookConnector.getFacebook().isSessionValid()) {
			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {

				public void onAuthSucceed() {
					Bundle params = new Bundle();
			        params.putString("fields", "name, picture, username");
					facebookConnector.makeRequest(params, new UserRequestListener());
				}

				public void onAuthFail(String error) {
					Toast.makeText(getBaseContext(), "Login failed.", Toast.LENGTH_LONG).show();
				}

			};
			SessionEvents.addAuthListener(listener);
			facebookConnector.login();
		}
  	}
  	
  	@Override
  	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  		this.facebookConnector.getFacebook().authorizeCallback(requestCode, resultCode, data);
  	}
  	
  	//Having this here makes it so we can do something with the Facebook data as soon as it returns.
  	public class UserRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);

                final String username = jsonObject.getString("username");

                mFacebookHandler.post(new Runnable() {
                    public void run() {
                    	facebook_username = username;
                    	
          				String call = RESTCaller.loginCall(Website.FACEBOOK, facebook_username);
          		    	executeREST(call);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
  	
  	//==============================================================================
  	//	END Facebook methods
  	//==============================================================================
  	
  	//==============================================================================
  	//	Helper methods
  	//==============================================================================
  	
  	private void executeREST(String call) {
		RESTCaller caller = new RESTCaller();
		JSONObject jsonObject = caller.execute(call);
		boolean error = false;
      
		try {
			socproxUsername = jsonObject.getString("m_strUsername");

			//Login successful
			JSONObject jObj = new JSONObject();
			jObj.put("username", socproxUsername);
			jObj.put("userMac", jsonObject.getString("m_strMac"));
			jObj.put("realName", jsonObject.getString("m_strName"));
			jObj.put("email", jsonObject.getString("m_strFacebook"));
			User.getInstance(jObj, SocProxActivity.this);
			
			Intent intent = new Intent(SocProxActivity.this, GameActivity.class);
    		startActivity(intent);
		} catch (JSONException ex) {
			//If there is no m_strUsername field then there was an error (user not in database).
			error=true;
			ex.printStackTrace();
		}
      
        if(error) {
        	try {
	        	//Login error
	          	String errorMessage = jsonObject.getString("desc");
	          	Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
	        } catch (JSONException e) {
	    		e.printStackTrace();
	    	}
        }
  	}
  	
  	//==============================================================================
  	//	END Helper methods
  	//==============================================================================
  	
}