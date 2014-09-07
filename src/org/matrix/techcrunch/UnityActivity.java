package org.matrix.techcrunch;

import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.app.NativeActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class UnityActivity extends NativeActivity {
	private static final String TAG = "UnityActivity";
	public static final int REQ_CODE_ANIMATION = 2;
	protected UnityPlayer mUnityPlayer;		// don't change the name of this variable; referenced from native code

	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		getWindow().takeSurface(null);
		getWindow().setFormat(PixelFormat.RGB_565);

		loadUnity();
		loadList();
		
		
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
	
	public void loadList() {
		setContentView(R.layout.main);
		
		findViewById(R.id.sendAnimation).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showUnity();
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
