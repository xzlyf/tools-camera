package com.xz.tools.xcamera.bean;

import java.io.Serializable;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/12
 */
public class AlbumConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 启动模式-普通相册模式（默认）
	 * 可查看大图，批量选择删除，不会返回选择对象
	 */
	public static final int START_ALBUM = 0;

	/**
	 * 启动模式-图片单选模式
	 * 只能选择一个图片，返回选择对象。
	 * 不可多选，不可操作，不可删除，不可进入大图模式
	 */
	public static final int START_SINGLE = 1;

	/**
	 * 启动模式-图片多选模式
	 * 可以选择一个或一组图片，并返回对象
	 * 通过 {@link #setMaxSelect(int)} 或 {@link #setMinSelect(int)} 来限制最小和最大的选择数量
	 * 不可操作，不可删除，不可进入大图模式
	 */
	public static final int START_MULTIPLE = 2;


	private int startMode = START_ALBUM;
	private int selectMax = 9;
	private int selectMin = 1;
	private boolean navigationVisible = false;


	/**
	 * 设置启动模式
	 *
	 * @param mode {@link #START_ALBUM}、{@link #START_SINGLE}
	 * @return
	 */
	public void setStartMode(int mode) {
		this.startMode = mode;
	}

	/**
	 * 最大选择数量，需要结合配置{@link #START_MULTIPLE}才能生效
	 * 默认多选模式最大选择数量是 9
	 *
	 * @param max 最大选择数量
	 */
	public void setMaxSelect(int max) {
		this.selectMax = max;
	}

	/**
	 * 最小选择数量，需要结合配置{@link #START_MULTIPLE}才能生效
	 * 默认多选模式最小选择数量是 1
	 *
	 * @param min 最小选择数量
	 */
	public void setMinSelect(int min) {
		this.selectMin = min;
	}


	/**
	 * 返回按钮是否可见
	 * 默认 false-不可见
	 *
	 * @param off true可见 false不可见
	 */
	public void setReturnVisible(boolean off) {
		this.navigationVisible = off;
	}


}
