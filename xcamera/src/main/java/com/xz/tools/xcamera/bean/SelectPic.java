package com.xz.tools.xcamera.bean;

import java.io.File;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/14
 */
public class SelectPic {
	private File file;
	private int position;

	public SelectPic(File file, int position) {
		this.file = file;
		this.position = position;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
