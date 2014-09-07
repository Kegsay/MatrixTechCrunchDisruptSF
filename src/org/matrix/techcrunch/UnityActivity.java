package org.matrix.techcrunch;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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
	
	// list of all events
	private List<UnityEvent> mEvents = new ArrayList<UnityEvent>();
	
	private Runnable mAddEventLoop = new Runnable() {

		@Override
		public void run() {
			addEvent(new UnityEvent("{\"thumbnail\":\"http://9pixs.com/wp-content/uploads/2014/06/dog-pics_1404159465.jpg\"}"));
			mHandler.postDelayed(this, 5000);
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
		loadList();
		
		// TODO listen on /events and dump the json into mEvents then call mAdapter.notifyDatasetChanged()
		mHandler.postDelayed(mAddEventLoop, 2000);
	}
	
	public void showUnity() {
		setContentView(R.layout.unity_send);
		findViewById(R.id.sendMessage).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("AndroidTC", "Requesting unity event...");
				UnityPlayer.UnitySendMessage("gameEventMaker", "getEvent", "someArg");
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
	
	public void addEvent(UnityEvent event) {
		mEvents.add(event);
		mAdapter.add(event);
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
				
				// TODO send
				JSONObject j = new JSONObject();
				try {
					j.put("body", text);
				}
				catch (JSONException e) {
					Log.e(TAG, "Can't set body json: "+e);
				}
				
				((EditText)findViewById(R.id.editText)).setText("");
				addEvent(new UnityEvent(j.toString()));
			}
			
		});
		
		ListView list = (ListView)findViewById(R.id.listView);
		mAdapter = new ListAdapter(this);
		for (UnityEvent e : mEvents) {
			mAdapter.add(e);
		}
		list.setAdapter(mAdapter);
		
	}
	
	public void updateListIfVisible(UnityEvent event) {
		if (mAdapter != null) {
			mAdapter.add(event);
		}
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
