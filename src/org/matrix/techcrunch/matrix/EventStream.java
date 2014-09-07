package org.matrix.techcrunch.matrix;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class EventStream {
	public EventStream(String host, String access_token, EventStreamCallback callback) {
		this.callback = callback;
		this.end_token = null;
		this.host = host;
		this.access_token = access_token;
		this.base_url = host + MatrixUrls.PREFIX + MatrixUrls.EVENTS + "?access_token=" + access_token + "&timeout=30000";
	}
	
	public void start_stream() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					doInitialSync();
					do_stream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "EventStream").start();
	}
	
	private void doInitialSync() throws IOException, JSONException {
		String str_url = host + MatrixUrls.PREFIX + "/initialSync" + "?access_token=" + access_token;
		Log.i("ES", "Initial sync to "+str_url);
		URL url = new URL(str_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		final int code;
		final String resp;
		try {
			code = conn.getResponseCode();
			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8")); 
		    StringBuilder responseStrBuilder = new StringBuilder();
		    String inputStr;
		    while ((inputStr = streamReader.readLine()) != null)
		        responseStrBuilder.append(inputStr);
		    resp = responseStrBuilder.toString();
		} finally {
			conn.disconnect();
		}
		
		if (code == 200) {
			Log.i("test", "got response " + resp);
			JSONObject json = new JSONObject(resp);
			JSONArray rooms = json.getJSONArray("rooms");
			for (int j = 0; j < rooms.length(); ++j) {
				JSONArray chunks = rooms.getJSONObject(j).getJSONObject("messages").getJSONArray("chunk");
				for (int i = 0; i < chunks.length(); ++i) {
					try {
						Event ev = Event.from_json(chunks.getJSONObject(i));
						this.callback.onEvent(ev);
					} catch(Exception e) {}
				}
			}
			this.end_token = json.getString("end");
		} else {
			Log.e("test", "got response code: " + code);
		}
	}
	
	private void do_stream() throws IOException, JSONException {
		String str_url = this.base_url;
		if (this.end_token != null) str_url += "&from=" + end_token;
		
		Log.i("test", "Starting stream: " + str_url);
		
		URL url = new URL(str_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		final int code;
		final String resp;
		try {
			code = conn.getResponseCode();
			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8")); 
		    StringBuilder responseStrBuilder = new StringBuilder();
		    String inputStr;
		    while ((inputStr = streamReader.readLine()) != null)
		        responseStrBuilder.append(inputStr);
		    resp = responseStrBuilder.toString();
		} finally {
			conn.disconnect();
		}
		
		if (code == 200) {
			Log.i("test", "got response " + resp);
			JSONObject json = new JSONObject(resp);
			JSONArray chunks = json.getJSONArray("chunk");
			for (int i = 0; i < chunks.length(); ++i) {
				try {
					Event ev = Event.from_json(chunks.getJSONObject(i));
					this.callback.onEvent(ev);
				} catch(Exception e) {}
			}
			this.end_token = json.getString("end");
		} else {
			Log.e("test", "got response code: " + code);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		do_stream();
	}
	
	private String end_token; 
	private final EventStreamCallback callback;
	private final String base_url;
	private final String host;
	private final String access_token;
}
