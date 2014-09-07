package org.matrix.techcrunch.matrix;

public interface UploadCallback {
	public void onFinished(String url);
	public void onException(Exception e);
}
