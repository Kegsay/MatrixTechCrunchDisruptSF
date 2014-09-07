package org.matrix.techcrunch.matrix;

public interface SendEventCallback {
	public void onResponse(int code, String response);
	public void onException(Exception e);
}
