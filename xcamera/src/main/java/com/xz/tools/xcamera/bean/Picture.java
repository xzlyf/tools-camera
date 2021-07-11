package com.xz.tools.xcamera.bean;

import android.net.Uri;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/11
 */
public class Picture {
	private Uri uri;
	private String path;

	public Picture(Uri uri, String path) {
		this.uri = uri;
		this.path = path;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
