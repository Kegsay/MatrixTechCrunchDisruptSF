package org.matrix.techcrunch;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.matrix.techcrunch.matrix.Event;
import org.matrix.techcrunch.matrix.EventStream;
import org.matrix.techcrunch.matrix.EventStreamCallback;
import org.matrix.techcrunch.matrix.MatrixClient;
import org.matrix.techcrunch.matrix.SendEventCallback;
import org.matrix.techcrunch.matrix.UploadCallback;

import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.app.NativeActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

public class UnityActivity extends NativeActivity {
	private static final String TAG = "UnityActivity";
	public static final int REQ_CODE_ANIMATION = 2;
	protected UnityPlayer mUnityPlayer;		// don't change the name of this variable; referenced from native code
	private Handler mHandler;
	private ListAdapter mAdapter;
	private MatrixClient mClient;
	public static final String USER_ID = "@tc:matrix.org";
	private String mRoomId = "!hmGOAhRgWDatZmxzWr:matrix.org";
	
	// list of all events
	private List<Event> mEvents = new ArrayList<Event>();
	
	private EventStreamCallback mCallback = new EventStreamCallback() {

		@Override
		public void onEvent(Event event) {
			if (event.type.equals("m.room.message") || event.type.equals("org.matrix.demo.models.unity.stickman")) {
				if (event.room_id.equals(mRoomId)) {
					Log.i(TAG, "onEvent "+event);
					addEvent(event);
				}
			}
		}
		
	};

	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		mHandler = new Handler();
		getWindow().takeSurface(null);
		getWindow().setFormat(PixelFormat.RGB_565);
		

		loadUnity();
		// showUnity();
		loadList();
		
		String host = "http://matrix.org";
		String access_token = "QHRjOm1hdHJpeC5vcmc..enDGPyJfutxYykiszs";
		mClient = new MatrixClient(host, access_token);
		EventStream stream = new EventStream(host, access_token, mCallback);
		stream.start_stream();
	}
	
	public void showUnity() {
		setContentView(R.layout.unity_send);
		findViewById(R.id.sendMessage).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("AndroidTC", "Requesting unity event...");
				UnityPlayer.UnitySendMessage("Robot", "getState", "");
			}
			
		});
		
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideUnity();
				loadList();
			}
			
		});
		
		FrameLayout layout = (FrameLayout) findViewById(R.id.frameLayout);    
        LayoutParams lp = new LayoutParams (LayoutParams.FILL_PARENT, 700);
        layout.addView(mUnityPlayer.getView(), 0, lp);
		mUnityPlayer.requestFocus();
	}
	
	public void hideUnity() {
		((ViewGroup)findViewById(R.id.frameLayout)).removeAllViews();
	}
	
	public void loadUnity() {
		mUnityPlayer = new UnityPlayer(this);
        
	}
	
	public void addEvent(final Event event) {
		mEvents.add(event);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAdapter.add(event);
				mAdapter.notifyDataSetChanged();
			}
			
		});
		
	}
	
	public void loadList() {
		setContentView(R.layout.main);
		
		findViewById(R.id.sendAnimation).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showUnity();
			}
			
		});
		
		findViewById(R.id.sendText).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String text = ((EditText)findViewById(R.id.editText)).getText().toString();
				Log.i(TAG,"Sending "+text);
				
				JSONObject j = new JSONObject();
				try {
					j.put("body", text);
					j.put("msgtype", "m.text");
				}
				catch (JSONException e) {
					Log.e(TAG, "Can't set body json: "+e);
				}
				mClient.sendContent(mRoomId, "m.room.message", j, new SendEventCallback() {

					@Override
					public void onResponse(int code, String response) {
						Log.d(TAG, "onResponse "+code+" "+response);
					}

					@Override
					public void onException(Exception e) {
						// TODO Auto-generated method stub
						
					}
					
				});
				
				
				((EditText)findViewById(R.id.editText)).setText("");
			}
			
		});
		
		ListView list = (ListView)findViewById(R.id.listView);
		mAdapter = new ListAdapter(this);
		for (Event e : mEvents) {
			mAdapter.add(e);
		}
		list.setAdapter(mAdapter);
		
	}
	
	// Called by unity
	public void onReceiveUnityJson(String json) {
		Log.d(TAG, "onReceiveUnityJson "+json);
		try {	
			JSONObject j = new JSONObject(json);
			j.putOpt("thumbnail", "http://matrix.org:8080/_matrix/content/QGtlZ2FuOm1hdHJpeC5vcmcvNupjfhmFhjxDPquSZGaGlYj.aW1hZ2UvcG5n.png");
			sendUnityJson(j);
		}
		catch (JSONException e) {
			Log.e(TAG, "Matthew screwed up the json: "+e);
		}
	}
	
	private void sendUnityJson(JSONObject json) {
		mClient.sendContent(mRoomId, "org.matrix.demo.models.unity.stickman", json, new SendEventCallback() {

			@Override
			public void onResponse(int code, String response) {
				Log.d(TAG, "onResponse "+code+" "+response);
			}

			@Override
			public void onException(Exception e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	// Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();
		super.onDestroy();
	}

	// Pause Unity
	@Override protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
	}
	
	// This ensures the layout will be correct.
	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}

	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)     { 
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return super.onKeyUp(keyCode, event);
		}
		return mUnityPlayer.injectEvent(event); 
	}
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)   { 
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		}
		return mUnityPlayer.injectEvent(event); 
	}
	@Override public boolean onTouchEvent(MotionEvent event) { 
		if (!mUnityPlayer.injectEvent(event)) {
			return super.onTouchEvent(event);
		}
		return true;
	} 
	public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); } 
}
