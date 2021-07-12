package com.xz.tools.xcamera.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.xz.tools.xcamera.R;
import com.xz.tools.xcamera.bean.Picture;
import com.xz.tools.xcamera.utils.MediaStoreUtils;
import com.xz.tools.xcamera.utils.PermissionsUtils;
import com.xz.tools.xcamera.utils.SpacesItemDecorationUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {
	public static final String TAG = AlbumActivity.class.getName();
	public static final String EXTRA_PATH = "ALBUM_PATH";
	public static final String DEFAULT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "camera";
	private Context mContext;
	private String albumPath;//相册路径
	private ReadPicTask readTask;
	private RecyclerView picRecyclerView;
	private PictureAdapter picAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_album);
		Intent intent = getIntent();
		if (intent != null) {
			albumPath = intent.getStringExtra(EXTRA_PATH);
		}
		if (albumPath == null) {
			albumPath = DEFAULT_PATH;
		}
		initView();
		readTask = new ReadPicTask();

		String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		PermissionsUtils.getInstance().chekPermissions(this, permissions, new PermissionsUtils.IPermissionsResult() {
			@Override
			public void passPermissons() {
				readTask.execute(albumPath);
			}

			@Override
			public void forbitPermissons() {
				Log.e(TAG, "缺少读写权限");
			}
		});


		//todo 查询媒体库测试 ，待删
		List<MediaStoreUtils.FileItem> allPhoto = MediaStoreUtils.getAllPhoto(mContext);
		for (MediaStoreUtils.FileItem item : allPhoto) {
			Log.d(TAG, "-------------媒体库------------: " + item.getFilePath());
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	protected void onDestroy() {
		if (readTask != null) {
			if (readTask.getStatus() == AsyncTask.Status.RUNNING ||
					readTask.getStatus() == AsyncTask.Status.PENDING) {
				readTask.cancel(true);
			}
			readTask = null;
		}
		super.onDestroy();

	}

	private void initView() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		//显示相册名
		if (albumPath.equals(DEFAULT_PATH)) {
			toolbar.setTitle("相册");
		} else {
			try {
				File f = new File(albumPath);
				toolbar.setTitle(f.getName());
			} catch (Exception e) {
				Log.e(TAG, "相册路径异常" + e.getMessage());
				toolbar.setTitle("相册");
			}
		}
	}


	private class ReadPicTask extends AsyncTask<String, Picture, Integer> {
		// 方法1：onPreExecute（）
		// 作用：执行 线程任务前的操作
		@Override
		protected void onPreExecute() {
			picRecyclerView = findViewById(R.id.pic_recycler);
			GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
			picRecyclerView.setLayoutManager(gridLayoutManager);
			picRecyclerView.addItemDecoration(new SpacesItemDecorationUtil.SpacesItemDecorationVH(2));
			SimpleItemAnimator itemAnimator = (SimpleItemAnimator) picRecyclerView.getItemAnimator();
			if (itemAnimator != null) {
				//解决Glide 加载闪烁
				itemAnimator.setSupportsChangeAnimations(false);
			}
			picAdapter = new PictureAdapter();
			picRecyclerView.setAdapter(picAdapter);
		}

		// 方法2：doInBackground（）
		// 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
		@Override
		protected Integer doInBackground(String... strings) {
			File album = new File(strings[0]);
			File[] picFile = album.listFiles();
			if (picFile == null) {
				return 0;
			}
			sort(picFile);
			Uri uri;
			for (File f : picFile) {
				if (f.isFile()) {
					Log.i(TAG, "doInBackground: " + f.lastModified());
					uri = MediaStoreUtils.getImgStoreUri(mContext, f);
					if (uri != null) {
						publishProgress(new Picture(uri, f.getAbsolutePath(), f.lastModified()));
					}
				}
			}

			return null;
		}

		// 方法3：onProgressUpdate（）
		// 作用：在主线程
		@Override
		protected void onProgressUpdate(Picture... values) {
			picAdapter.add(values[0]);
		}

		// 方法4：onPostExecute（）
		// 作用：接收线程任务执行结果、将执行结果显示到UI组件
		@Override
		protected void onPostExecute(Integer integer) {
		}

		// 作用：将异步任务设置为：取消状态
		@Override
		protected void onCancelled() {

		}

		/**
		 * 排序文件列表
		 * 采用冒泡排序算法
		 * 递增排序
		 *
		 * @param files
		 * @return
		 */
		private void sort(File[] files) {
			//冒泡
			for (int i = 0; i < files.length; i++) {
				//外层循环，遍历次数
				for (int j = 0; j < files.length - i - 1; j++) {
					//内层循环，升序（如果前一个值比后一个值大，则交换）
					//内层循环一次，获取一个最大值
					if (files[j].lastModified() > files[j + 1].lastModified()) {
						File temp = files[j + 1];
						files[j + 1] = files[j];
						files[j] = temp;
					}
				}
			}
			//因为是递增排序，所以这边反转以下
			reverseArray(files);

		}

		/**
		 * 反转数组
		 *
		 * @param f
		 */
		private void reverseArray(File[] f) {
			ArrayList<File> list = new ArrayList<>();
			for (int i = 0; i < f.length; i++) {
				list.add(f[f.length - i - 1]);
			}
			list.toArray(f);
		}


	}

	private class PictureAdapter extends RecyclerView.Adapter<PictureViewHolder> {
		private LayoutInflater inflater;
		private List<Picture> mList;

		PictureAdapter() {
			mList = new ArrayList<>();
		}

		public void add(Picture path) {
			mList.add(path);
			notifyDataSetChanged();
		}


		@NonNull
		@Override
		public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			inflater = LayoutInflater.from(mContext);
			return new PictureViewHolder(inflater.inflate(R.layout.item_picture, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
			Picture picture = mList.get(position);
			//生成缩略图
			//Bitmap bitmap = ImageUtils.getImageThumbnail(picture.getPath(), 200, 200);
			//直接显示太卡....
			//holder.imageView.setImageBitmap(bitmap);
			//使用Glide来处理，生成缩略图
			Glide.with(mContext)
					.load(picture.getPath())
					.crossFade()
					.override(200, 200)
					.into(holder.imageView);

		}


		@Override
		public int getItemCount() {
			return mList.size();
		}
	}

	private class PictureViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;

		PictureViewHolder(@NonNull View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.image);
		}
	}
}
