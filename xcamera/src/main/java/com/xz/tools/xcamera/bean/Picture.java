package com.xz.tools.xcamera.bean;

import android.net.Uri;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/11
 */
public class Picture {
	private int id;//媒体库对应文件的id
	private Uri uri;
	private String path;
	private Long updateDate;

	public Picture() {
	}

	public Picture(int id, Uri uri, String path, Long updateDate) {
		this.id = id;
		this.uri = uri;
		this.path = path;
		this.updateDate = updateDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}
}
