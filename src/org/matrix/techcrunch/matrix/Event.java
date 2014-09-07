package org.matrix.techcrunch.matrix;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
	public String id;
	public String room_id;
	public String type;
	public String state_key;
	public int ts;
	public String user_id;
	public JSONObject content;
	
	public Event(String id, String room_id, String type, String state_key, int ts,
			String user_id, JSONObject content) {
		this.id = id;
		this.room_id = room_id;
		this.type = type;
		this.state_key = state_key;
		this.ts = ts;
		this.user_id = user_id;
		this.content = content;
	}

	public static Event from_json(JSONObject json) throws JSONException {
		String id = json.getString("event_id");
		String room_id = json.getString("room_id");
		String type = json.getString("type");
		String state_key = null;
		if (json.has("state_key")) {
			state_key = json.getString("state_key");
		}
		int ts = json.getInt("ts");
		String user_id = json.getString("user_id");
		JSONObject content = json.getJSONObject("content");
		
		return new Event(id, room_id, type, state_key, ts, user_id, content);
	}
}
