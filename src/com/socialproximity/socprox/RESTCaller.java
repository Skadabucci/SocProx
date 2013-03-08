package com.socialproximity.socprox;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class RESTCaller {
	
	public static final String DEBUG_TAG = "RESTCaller";
	public static final boolean debug = true;
	
	/* EXAMPLE USAGE:
	 * String call = RESTCaller.loginCall("socprox", "ebeerman", "beerman");
    	RESTCaller caller = new RESTCaller();
    	String result = caller.execute(call);
	 */
	
	static final String BASEURL = "http://www.cjcornell.com/bluegame/REST/";
	
	public JSONObject execute(String url) {
		String finalURL = BASEURL + url;
		
		// Setup the http variables for the call
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(finalURL);
		String result;
		JSONObject jsonObject = null;
		try {
			if(debug) Log.d(DEBUG_TAG, "REST Call being attempted: " + finalURL);
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			result = getASCIIContentFromEntity(entity);
			jsonObject = new JSONObject(result);
			if(debug) Log.d(DEBUG_TAG, jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	public JSONArray executeToArray(String url) {
		String finalURL = BASEURL + url;
		
		// Setup the http variables for the call
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(finalURL);
		String result;
		JSONArray jsonArray = null;
		try {
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			result = getASCIIContentFromEntity(entity);
			jsonArray = new JSONArray(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}
	
	protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
		InputStream in = entity.getContent();
		StringBuffer out = new StringBuffer();
		int n = 1;
		while (n > 0) {
			byte[] b = new byte[4096];
			n = in.read(b);
			if (n > 0) out.append(new String(b, 0, n));
		}
		return out.toString();
	}
	
	public static String challengeStatusCall(String mac1, int id) {
		return "challengeStatus/" + mac1 + "/" + id;
	}
	
	public static String getAllGamesCall() {
		return "getAllGames";
	}
	
	public static String getChallengeCall(String mac1, String mac2) {
		return "getChallenge/" + mac1 + "/" + mac2;
	}
	
	public static String getChallengeInstancesCall(String mac, String statuses) {
		return "getChallengeInstances/" + mac + "/" + statuses;
	}
	
	public static String getChallengeInstanceCall(String mac, int challengeId) {
		return "getChallengeInstance/" + mac + "/" + challengeId;
	}
	
	public static String getStandings(String mac, int limit, String gameName) {
		String result = "getStandings/";
		if (!mac.equals("")) {
			result += "mac/" + mac + "/";
		}
		if (limit != -1) {
			result += "limit/" + limit + "/";
		}
		if (!gameName.equals("")) {
			result += "gamename/" + gameName;
		}
		return result;
	}
	
	public static String listChallengesCall(String mac1, String mac2) {
		return "listChallenges/" + mac1 + "/" + mac2;
	}
	
	public static String loginCall(Website website, String username) {
		switch(website) {
			case FACEBOOK: return "login/facebook/" + username;
			case TWITTER: return "login/twitter/" + username;
			default: return "";
		
		}
	}
	
	public static String loginCall(Website website, String macAddress, String username, String password) {
		switch(website) {
			case FACEBOOK: return "login/" + macAddress + "/facebook/" + username;
			case TWITTER: return "login/" + macAddress + "/twitter/" + macAddress + username;
			case SOCPROX: return "login/" + macAddress + "/socprox/" + username + "/" + password;
			default: return "";
		
		}
	}
	
	public static String registerCall(String macAddress, String username, String password) {
		
		return "register/" + macAddress + "/" + username + "/" + password;
	}
	
	public static String registerAliasCall(Website website, String username, String socproxUsername) {
		switch(website) {
			case FACEBOOK: return "registerAlias/facebook/" + username + "/" + socproxUsername;
			case TWITTER: return "registerAlias/twitter/" + username + "/" + socproxUsername;
			case SOCPROX: return "registerAlias/soxprox/" + username + "/" + socproxUsername;
			default: return "";
		}
	}
	
	public static String updateProfile(String socproxUsername, String firstName, String lastName, String email) {
		String firstPart = "updateProfile/" + socproxUsername + "/";
		String secondPart = "/";
		boolean mark = false;
		if(!firstName.equals("") && !lastName.equals("")) {
			firstPart += "Name";
			secondPart += firstName + "%20" + lastName;
			mark=true;
		}
		if(!email.equals("")) {
			if(mark) {
				firstPart += ",";
				secondPart += ",";
			}
			firstPart += "Facebook_Email";
			secondPart += email;
			mark = true;
		}
		
		if(!mark) {
			return "";
		}
		return (firstPart + secondPart);
	}
	
	public static String updateChallengeCall(String macID, int id, String input) {
		return "updateChallenge/" + macID + "/" + id + "/" + input;
	}
	
	public static String userStatsCall(String macID) {
		return "userStats/" + macID;
	}
	
	//Types of website for REST calls.
	public enum Website {
		FACEBOOK, TWITTER, SOCPROX
	}
}