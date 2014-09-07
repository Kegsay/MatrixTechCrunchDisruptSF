package org.matrix.techcrunch.matrix;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MatrixClient {
	
	public MatrixClient(String host, String access_token) {
		super();
		this.host = host;
		this.access_token = access_token;
		this.thread_pool = Executors.newCachedThreadPool();
	}
	
	public void sendContent(final String room_id, final String type, final JSONObject content, final SendEventCallback callback) {
		this.thread_pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					doSendContent(room_id, type, content, callback);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (callback != null) callback.onException(e);
				}
			}
		});
	}
	
	private void doSendContent(String room_id, String type, JSONObject content, final SendEventCallback callback) throws IOException {
		String str_url = host + MatrixUrls.PREFIX + "/rooms/" + room_id + "/send/" + type + "?access_token=" + access_token;
		URL url = new URL(str_url);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		final int code;
		final String resp;
		try {
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			
			OutputStream out = new BufferedOutputStream(conn.getOutputStream());
		    out.write(content.toString().getBytes("UTF-8"));
		    out.close();
		  
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
		
		if (code != 200) {
			Log.e("test", "Got a non 200 response code: " + code + " resp: " + resp);
		}
		
		try {
			if (callback != null) callback.onResponse(code, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void upload(final InputStream input, final String contentType, final UploadCallback callback) {
		this.thread_pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					doUpload(input, contentType, callback);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (callback != null) callback.onException(e);
				}
			}
		});
	}
	
	public void doUpload(InputStream input, String contentType, UploadCallback callback) throws JSONException, IOException {
		String str_url = host + MatrixUrls.PREFIX + "/content?access_token=" + access_token;
		URL url = new URL(str_url);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		final int code;
		final String resp;
		try {
			conn.setDoOutput(true);
			conn.setChunkedStreamingMode(0);
			conn.setRequestProperty("Content-Type", "application/json");
			
			OutputStream out = new BufferedOutputStream(conn.getOutputStream());
			byte[] buffer = new byte[1024]; // Adjust if you want
		    int bytesRead;
		    while ((bytesRead = input.read(buffer)) > -1) {
		        out.write(buffer, 0, bytesRead);
		    }
		    out.close();
		  
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
		
		if (code != 200) {
			Log.e("test", "Got a non 200 response code: " + code + " resp: " + resp);
			if (callback != null) callback.onException(new Exception("Failed to uploade: " + code));
		}
		
		JSONObject json = new JSONObject(resp);
		
		try {
			if (callback != null) {
				callback.onFinished(json.getString("content_token"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private final String host;
	private final String access_token;
	private final ExecutorService thread_pool;
}
