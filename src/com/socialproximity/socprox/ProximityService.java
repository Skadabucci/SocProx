package com.socialproximity.socprox;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class ProximityService extends Service {
	private final static int CONSTANT_DISCOVERABILITY = 0;
	private final static int HOUR_DISCOVERABILITY = 3600;
	
	private final static String DEBUG_TAG = "ProximityService";
	private final static boolean debug = true; // debug?

	private static final int DEVICE_NONEXISTENT = 0;
	private static final int DEVICE_VALID_EXISTS = 1;
	private static final int DEVICE_INVALID_EXISTS = 2;
	
	protected BluetoothAdapter mBtAdapter;
	//protected BluetoothDeviceBuffer mBtDeviceBuffer;
	protected NotificationManager notificationManager;
	protected static int scanningIteration = 0;
	private Vector<String> foundValidAddresses;
    private Vector<String> foundInvalidAddresses;

    /* REST stuff */
    String URL = "http://cjcornell.com/bluegame/REST/pushChallenge/";
    String result = "";
    String deviceId = "xxxxx";
    
    
	@Override
	public void onCreate(){
		super.onCreate();
		
		if(debug) Log.d(DEBUG_TAG, "Started service");
		
		// Initialize device vectors
		foundValidAddresses = new Vector<String>();
		foundInvalidAddresses = new Vector<String>();
		
		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		//mBtDeviceBuffer = new BluetoothDeviceBuffer();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		// Set discoverability periodicity
		setBluetoothDiscoveryDuration(CONSTANT_DISCOVERABILITY);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		doDiscovery();
		
		return START_STICKY;
	}
	
    @Override
	public void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
//	private void doNotification(/*String address*/BluetoothDevice btDevice){
//		if(debug) Log.d(DEBUG_TAG, "Creating notification");
//		
//		notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
//		
//		// Create Notifcation
//		Notification notification = new Notification(R.drawable.icon, "A new notification", System.currentTimeMillis());
//
//		// Cancel the notification after its selected
//		notification.flags |= Notification.FLAG_AUTO_CANCEL;
//		notification.number += 1;
//
//		// Specify the called Activity
//		Intent intent = new Intent(this, ChallengeAcceptanceActivity.class);
//		intent.putExtra("bt_address", btDevice.getAddress());
//		
//		PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
//		notification.setLatestEventInfo(this, "This is the title",
//			"This is the text", activity);
//		notificationManager.notify(0, notification);
//		
//		if(debug) Log.d(DEBUG_TAG, "Notification created");
//    }
//	
//	private void doNotification(JSONObject challengeJSON){
//		if(debug) Log.d(DEBUG_TAG, "Creating notification");
//		
//		notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
//		
//		// Create Notifcation
//		Notification notification = new Notification(R.drawable.icon, "A new notification", System.currentTimeMillis());
//
//		// Cancel the notification after its selected
//		notification.flags |= Notification.FLAG_AUTO_CANCEL;
//		notification.number += 1;
//
//		Intent intent = new Intent(this, ChallengeAcceptanceActivity.class);
//		intent.putExtra("challenge_json", challengeJSON.toString());
//		
//		
//		PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
//		notification.setLatestEventInfo(this, "This is the title",
//			"This is the text", activity);
//		notificationManager.notify(0, notification);
//		
//		if(debug) Log.d(DEBUG_TAG, "Notification created");
//    }
	
	
	
	/*
	 * start discovery process
	 */
	private void doDiscovery(){
		if (debug) Log.d(DEBUG_TAG, "Starting discovery, iteration #" + scanningIteration++);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
	}
	
	

	/*
	 * value in seconds, 0 sets it to constantly discoverable
	 */
	private void setBluetoothDiscoveryDuration(int value){
		if(debug) Log.d(DEBUG_TAG, "Setting discoverability to [" + value + "]");
		Intent discoverableIntent = new
		Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, value);
		discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(discoverableIntent);
	}
	
	
	private static Vector<String> attemptedAddresses;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            	
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add to the buffer and notify the client
                if(debug) Log.d(DEBUG_TAG, "Device found: Name=" + device.getName() + " Address=" + device.getAddress());
                
                boolean listContainsAddress = false;
                if(attemptedAddresses == null){
                	attemptedAddresses = new Vector<String>();
                }
                for(int i = 0; i < attemptedAddresses.size(); i++){
                	if(attemptedAddresses.get(i).equals(device.getAddress())){
                		listContainsAddress = true;
                		break;
                	}
                }
                if(!listContainsAddress){
                	Log.d(DEBUG_TAG, "Adding " + device.getAddress() + " to list and pushing for challenges!");
                	attemptedAddresses.add(device.getAddress());
	                String challengeResult = callWebServiceGetChallenge(
	                		mBtAdapter.getAddress(), // This user's Bluetooth MAC address
	                		device.getAddress()); // Received MAC address
                
                }
                else{
                	Log.d(DEBUG_TAG, "List contains " + device.getAddress());
                }
            // When discovery is finished, start discovery again
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	// TODO: Set wait amount and start timer
            	if(debug) Log.d(DEBUG_TAG, "Bluetooth scanning complete");
            	doDiscovery();
            }
        }
    };
    
    /** Method for calling the web service and returning string value of the result */
    public String callWebServiceGetChallenge(String userAddress, String discoveredAddress){
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(URL + userAddress + '/' + discoveredAddress);
        request.addHeader("deviceId", deviceId);
        ResponseHandler<String> handler = new BasicResponseHandler();
        try {
            result = httpclient.execute(request, handler);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpclient.getConnectionManager().shutdown();
        Log.d(DEBUG_TAG, result);
        
        return result;
    } // end callWebService()
    
    public boolean isErrorObject(JSONObject json){
    	if(json.has("error")){
    		try{
	    		if(debug){
	    			Log.d(DEBUG_TAG, "Server returned " + json.getString("code") + " error: " + json.getString("message"));
	    		}
    		} catch(JSONException e){ Log.d(DEBUG_TAG, e.toString()); }
    		
    		return true;
    	}
    	return false;
    }
    
}
