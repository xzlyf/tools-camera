package com.xz.tools.xcamera.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体库工具类
 *
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/11
 */
public class MediaStoreUtils {
	private static final String TAG = MediaStoreUtils.class.getName();

	/**
	 * 查询文件是否在媒体库，如果在，返回Uri，如果不在则把文件插入到媒体库方便下次查询,同时返回uri
	 * 兼容Android 10
	 */
	public static Uri getImgStoreUri(Context context, File f) {
		Cursor cursor = null;
		try {
			//查询媒体库 ,根据修改日期（ImageColumns.DATE_MODIFIED）降序排列
			cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
					new String[]{f.getAbsolutePath()}, null);

			if (cursor != null && cursor.moveToFirst()) {
				int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
				Uri baseUri = Uri.parse("content://media/external/images/media");
				return Uri.withAppendedPath(baseUri, "" + id);
			} else {
				// 如果图片不在手机的媒体库，就先把它插入。
				if (f.exists()) {
					ContentValues values = new ContentValues();
					values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
					return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, MediaStoreUtils.class.getName() + ":" + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return null;
	}


	/**
	 * 查询当前媒体库所有图片信息
	 */
	public static List<FileItem> getAllPhoto(Context context) {
		List<FileItem> photos = new ArrayList<>();
		String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME};
		//asc 按升序排列
		//    desc 按降序排列
		//projection 是定义返回的数据，selection 通常的sql 语句，例如  selection=MediaStore.Images.ImageColumns.MIME_TYPE+"=? " 那么 selectionArgs=new String[]{"jpg"};
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc");
			String imageId = null;
			String fileName;
			String filePath;
			while (cursor != null && cursor.moveToNext()) {
				imageId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
				fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
				filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
				FileItem fileItem = new FileItem(imageId, filePath, fileName);
				photos.add(fileItem);


			}
		} catch (Exception e) {
			Log.e(TAG, MediaStoreUtils.class.getName() + ":" + e.getMessage());

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return photos;

	}

	public static List<FileItem> getAllText(Context context) {

		List<FileItem> texts = new ArrayList<>();
		String[] projection = new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.MIME_TYPE};
		//相当于我们常用sql where 后面的写法
		String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
				+ " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
				+ " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
				+ " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
				+ " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";

		String[] selectionArgs = new String[]{"text/plain", "application/msword", "application/pdf", "application/vnd.ms-powerpoint", "application/vnd.ms-excel"};
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");
			String fileId;
			String fileName;
			String filePath;
			while (cursor != null && cursor.moveToNext()) {
				fileId = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
				fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
				filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
				FileItem fileItem = new FileItem(fileId, filePath, fileName);
				texts.add(fileItem);
			}
		} catch (Exception e) {

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return texts;
	}


	public static class FileItem {
		private String imageId, filePath, fileName;

		public FileItem(String imageId, String filePath, String fileName) {
			this.imageId = imageId;
			this.filePath = filePath;
			this.fileName = fileName;
		}

		public String getImageId() {
			return imageId;
		}

		public void setImageId(String imageId) {
			this.imageId = imageId;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
	}
}
