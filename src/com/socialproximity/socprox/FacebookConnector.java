package com.socialproximity.socprox;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.socialproximity.socprox.Facebook.DialogListener;
import com.socialproximity.socprox.SessionEvents.AuthListener;
import com.socialproximity.socprox.SessionEvents.LogoutListener;

public class FacebookConnector {

	private Facebook facebook = null;
	private Context context;
	private String[] permissions;
	private Handler mHandler;
	private Activity activity;
	private SessionListener mSessionListener = new SessionListener();
	private AsyncFacebookRunner mAsyncRunner;

	public FacebookConnector(String appId,Activity activity,Context context,String[] permissions) {
		this.facebook = new Facebook(appId);
		this.mAsyncRunner = new AsyncFacebookRunner(this.facebook);

		SessionStore.restore(this.facebook, context);
        SessionEvents.addAuthListener(new FbAPIsAuthListener());
        SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
        
		this.context = context;
		this.permissions = permissions;
		this.mHandler = new Handler();
		this.activity = activity;
	}

	public void login() {
        if (!facebook.isSessionValid()) {
            facebook.authorize(this.activity, this.permissions, Facebook.FORCE_DIALOG_AUTH, new LoginDialogListener());
        }
    }

	public void logout() {
        SessionEvents.onLogoutBegin();
        AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
        asyncRunner.logout(this.context, new LogoutRequestListener());
	}

	public void postMessageOnWall(String msg) {
		if (facebook.isSessionValid()) {
		    Bundle parameters = new Bundle();
		    parameters.putString("message", msg);
		    try {
				String response = facebook.request("me/feed", parameters,"POST");
				System.out.println(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			login();
		}
	}	
	
	
	public void makeRequest(Bundle params, BaseRequestListener listener) {
		this.mAsyncRunner.request("me", params, listener);
	}
	
	//Added for use in SocProxActivity
	public void requestUserData() {
        Bundle params = new Bundle();
        params.putString("fields", "name, picture");
        this.mAsyncRunner.request("me", params, new UserRequestListener());
    }
	
	private String fbid = "null";
	private String fbname = "null";
	
	public String getFacebookID() {
		
		return fbid;
	}
	
	public String getFacebookName() {
		return fbname;
	}
	
	/*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);

                final String name = jsonObject.getString("name");
                final String id = jsonObject.getString("id");
                //final String username = jsonObject.getString("username");

                mHandler.post(new Runnable() {
                    public void run() {
                    	fbname = name;
                    	fbid = id;
                    	//fbusername = username;
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    
    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
            SessionEvents.onLoginError(error.getMessage());
        }
        
        public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }
    
    public class LogoutRequestListener extends BaseRequestListener {
        public void onComplete(String response, final Object state) {
            // callback should be run in the original thread, 
            // not the background thread
            mHandler.post(new Runnable() {
                public void run() {
                    SessionEvents.onLogoutFinish();
                }
            });
        }
    }
    
    private class SessionListener implements AuthListener, LogoutListener {
        
        public void onAuthSucceed() {
            SessionStore.save(facebook, context);
        }

        public void onAuthFail(String error) {
        }
        
        public void onLogoutBegin() {           
        }
        
        public void onLogoutFinish() {
            SessionStore.clear(context);
        }
    }
    
    public class FbAPIsAuthListener implements AuthListener {

        public void onAuthSucceed() {
            requestUserData();
        }

        public void onAuthFail(String error) {
        }
    }

    /*
     * The Callback for notifying the application when log out starts and
     * finishes.
     */
    public class FbAPIsLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
        }

        public void onLogoutFinish() {
        }
    }

	public Facebook getFacebook() {
		return this.facebook;
	}
}