package org.matrix.techcrunch;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UnityEvent {

	private JSONObject mJson;
	private String mThumbnail = null;
	
	public UnityEvent(String json) {
		try {
			mJson = new JSONObject(json);
			mThumbnail = mJson.optString("thumbnail", null);
		}
		catch (JSONException e) {
			Log.e("TC", "Bad json string: "+json);
		}
	}
	
	public String getText() {
		return mJson.optString("body", "");
	}
	
	public boolean isAnimation() {
		return mThumbnail != null;
	}
	
	public String getImageUri() {
		return mJson.optString("thumbnail", null);
	}
}
