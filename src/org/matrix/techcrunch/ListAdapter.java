package org.matrix.techcrunch;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<UnityEvent> {
	
	final static int cacheSize = 4 * 1024 * 1024;
	private static LruCache<String, Bitmap> sCache = new LruCache<String, Bitmap>(cacheSize) {
		@SuppressLint("NewApi")
		protected int sizeOf(String key, Bitmap value) {
	           return value.getByteCount();
		}
	};
	
	  private final Context context;

	  private HandlerThread mThread;
	  private Looper mLooper;
	  private Handler mOffThreadHandler;
	  private Handler mUiHandler;
	  
	  public ListAdapter(Context context) {
	    super(context, 0);
	    this.context = context;
	    this.setNotifyOnChange(true);
	    mUiHandler = new Handler();
	    mThread = new HandlerThread("imageLoader");
	    mThread.start();
	    mLooper = mThread.getLooper();
	    mOffThreadHandler = new Handler(mLooper);
	  }
	  
	  public void setEvents(List<UnityEvent> events) {
		  Log.e("Adapter", "setEvents");
		  this.clear();
		  for (UnityEvent e : events) {
			  this.add(e);
		  }
		  this.notifyDataSetChanged();
	  }
	  
	  public static Bitmap getBitmapFromURL(String src) {
		    try {
		        URL url = new URL(src);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        return myBitmap;
		    } catch (IOException e) {
		        Log.e("BOO","Failed to get image: "+e);
		        return null;
		    }
		}

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.list_cell, parent, false);
	    final TextView textView = (TextView) rowView.findViewById(R.id.textCell);
	    final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageCell);
	    
	    final UnityEvent event = getItem(position);
	    
	    if (event.isAnimation()) {
	    	Bitmap bm = sCache.get(event.getImageUri());
	    	if (bm != null) {
	    		imageView.setImageBitmap(bm);
	    	}
	    	else {
		    	mOffThreadHandler.post(new Runnable() {
		    		@Override
		    		public void run() {
		    			Log.i("BOO", "Loading image "+event.getImageUri());
		    			try {
		    			    final Bitmap bitmap = getBitmapFromURL(event.getImageUri());
		    			    sCache.put(event.getImageUri(), bitmap);
		    			    mUiHandler.post(new Runnable() {
		    			    	@Override
		    			    	public void run() {
		    			    		imageView.setImageBitmap(bitmap);
		    			    	}
		    			    });
		    			    
		    			}
		    			catch (Exception e) {
		    			    Log.e("BOO","Ruh roh: "+e);
		    			}
		    		}
		    	});
	    	}
	    	
	    }
	    else {
	    	textView.setText(event.getText());
	    }
	    
	    

	    return rowView;
	  }

}