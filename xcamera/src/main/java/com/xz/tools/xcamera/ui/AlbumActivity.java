package com.xz.tools.xcamera.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.xz.tools.xcamera.R;
import com.xz.tools.xcamera.bean.AlbumConfig;
import com.xz.tools.xcamera.bean.Picture;
import com.xz.tools.xcamera.bean.SelectPic;
import com.xz.tools.xcamera.utils.MediaStoreUtils;
import com.xz.tools.xcamera.utils.PermissionsUtils;
import com.xz.tools.xcamera.utils.SpacesItemDecorationUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

public class AlbumActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {
	public static final String TAG = AlbumActivity.class.getName();
	public static final String EXTRA_CONFIG = "ALBUM_CONFIG";
	private AlbumConfig config;
	private Context mContext;
	/**
	 * 本实例任务
	 */
	private Set<AsyncTask> taskList;
	private RecyclerView picRecyclerView;
	private PictureAdapter picAdapter;
	private MenuItem mSelectAllItem;
	private MenuItem mSelectDoneItem;
	private MenuItem mSelectDeleteItem;
	private volatile int totalPic = 0;//照片总数（显示的）
	private boolean mSelectMode = false;    //Item 选择模式
	private Vector<File> picFiles;//图片文件地址
	private Set<Integer> mSelectItemIndex = new TreeSet<>(); //已选的item的索引


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_album);
		Intent intent = getIntent();
		if (intent != null) {
			config = (AlbumConfig) intent.getSerializableExtra(EXTRA_CONFIG);
		}
		if (config == null) {
			//如果没有收到传进来的配置，那就使用默认配置
			config = new AlbumConfig();
		}
		initView();
		ReadPicTask readTask = new ReadPicTask();
		taskList = new HashSet<>();
		taskList.add(readTask);//加入进任务组

		String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		PermissionsUtils.getInstance().chekPermissions(this, permissions, new PermissionsUtils.IPermissionsResult() {
			@Override
			public void passPermissons() {
				readTask.execute(config.getAlbumPath());
			}

			@Override
			public void forbitPermissons() {
				Log.e(TAG, "缺少读写权限");
			}
		});


		//todo 查询媒体库测试 ，待删
		//List<MediaStoreUtils.FileItem> allPhoto = MediaStoreUtils.getAllPhoto(mContext);
		//Log.i(TAG, "-------------媒体库------------: ");
		//for (MediaStoreUtils.FileItem item : allPhoto) {
		//	Log.i(TAG, item.getFilePath());
		//}
		//Log.i(TAG, "-------------媒体库------------: ");
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	protected void onDestroy() {
		//移除和取消未执行的任务
		if (taskList.size() > 0) {
			for (AsyncTask task : taskList) {
				if (task.getStatus() == AsyncTask.Status.RUNNING ||
						task.getStatus() == AsyncTask.Status.PENDING) {
					task.cancel(true);
				}
				taskList.remove(task);
			}
		}
		super.onDestroy();

	}

	@Override
	public void onBackPressed() {
		if (mSelectMode) {
			selectMode(false);
		} else {
			super.onBackPressed();
		}
	}

	private void initView() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		//显示相册名
		if (config.getAlbumName() == null) {
			try {
				File f = new File(config.getAlbumPath());
				toolbar.setTitle(f.getName());
			} catch (Exception e) {
				Log.e(TAG, "相册路径异常" + e.getMessage());
				toolbar.setTitle("相册");
			}
		} else {
			toolbar.setTitle(config.getAlbumName());
		}

		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectMode(false);
			}
		});

		//Menu菜单控制
		Menu menu = toolbar.getMenu();
		mSelectAllItem = menu.findItem(R.id.select_all);
		mSelectAllItem.setVisible(false);
		mSelectAllItem.setOnMenuItemClickListener(this);

		mSelectDoneItem = menu.findItem(R.id.select_done);
		mSelectDoneItem.setVisible(false);
		mSelectDoneItem.setOnMenuItemClickListener(this);

		mSelectDeleteItem = menu.findItem(R.id.select_delete);
		mSelectDeleteItem.setVisible(false);
		mSelectDeleteItem.setOnMenuItemClickListener(this);


	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.select_all) {
			selectAll();
		} else if (itemId == R.id.select_done) {
			Toast.makeText(mContext, "提交", Toast.LENGTH_SHORT).show();
		} else if (itemId == R.id.select_delete) {
			selectDelete();
		}
		return true;
	}


	/**
	 * 删除选中的
	 */
	private void selectDelete() {
		if (mSelectItemIndex.size() == 0) {
			Toast.makeText(mContext, "未选择任何图片", Toast.LENGTH_SHORT).show();
			return;
		}
		DeletePicTask delPicTask = new DeletePicTask();
		//只能同时进行一个删除任务，如果添加成功就，开始任务
		if (taskList.add(delPicTask)) {
			//安全可变参数
			delPicTask.execute();
		}

	}

	/**
	 * 选择所有
	 */
	private void selectAll() {
		try {
			//如果当前选中的item数量不等于总照片数，那就使用全选状态
			if (mSelectItemIndex.size() != totalPic) {
				//全选照片
				mSelectItemIndex.clear();
				for (int i = 0; i < totalPic; i++) {
					mSelectItemIndex.add(i);
				}
			} else {
				//否者清除全选状态
				mSelectItemIndex.clear();
			}
			picAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			Toast.makeText(mContext, "全选异常", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 选择模式
	 *
	 * @param off 关闭或开启
	 */
	private void selectMode(boolean off) {

		switch (config.getStartMode()) {
			case AlbumConfig.START_ALBUM:
				//图库模式
				mSelectAllItem.setVisible(off);
				mSelectDeleteItem.setVisible(off);
				break;
			case AlbumConfig.START_SINGLE:
				//单选模式
				mSelectDoneItem.setVisible(off);
				break;
			case AlbumConfig.START_MULTIPLE:
				//多选模式
				mSelectDoneItem.setVisible(off);
				break;
		}
		mSelectMode = off;
		mSelectItemIndex.clear();
		picAdapter.notifyDataSetChanged();

	}

	private class PictureAdapter extends RecyclerView.Adapter<PictureViewHolder> {
		private LayoutInflater inflater;
		private List<Picture> mList;

		PictureAdapter() {
			mList = new ArrayList<>();
		}

		public void add(Picture path) {
			mList.add(path);
			notifyItemChanged(mList.size());
		}

		public void notifyAndRemove(int position) {
			mList.remove(position);
			notifyItemRemoved(position);
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
			if (mSelectMode) {
				holder.itemView.setScaleX(0.95f);
				holder.itemView.setScaleY(0.95f);
				//如果选中集合中有当前的item，那就显示选中状态
				if (mSelectItemIndex.contains(position)) {
					holder.selectView.setVisibility(View.VISIBLE);
				} else {
					holder.selectView.setVisibility(View.GONE);
				}
			} else {
				holder.itemView.setScaleX(1f);
				holder.itemView.setScaleY(1f);
				holder.selectView.setVisibility(View.GONE);

			}
		}


		@Override
		public int getItemCount() {
			return mList.size();
		}
	}

	private class PictureViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;
		View selectView;

		PictureViewHolder(@NonNull View itemView) {
			super(itemView);
			selectView = itemView.findViewById(R.id.select_view);
			imageView = itemView.findViewById(R.id.image);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mSelectMode) {
						int position = getLayoutPosition();
						//多选模式
						boolean isAdd = mSelectItemIndex.add(position);
						if (!isAdd) {
							//如果添加失败，那就把item改为取消选择状态
							mSelectItemIndex.remove(position);
						}
						picAdapter.notifyItemChanged(position);

					}

				}
			});
			itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (!mSelectMode) {
						selectMode(true);
					}
					return true;
				}
			});
		}
	}


	/**
	 * 删除图片耗时任务
	 */
	private class DeletePicTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected final Integer doInBackground(Void... voids) {
			/*得到待删除图片的文件对象，使用Stack是因为它后进先出，我们需要先删除后面的item才行，不然item刷新有问题*/
			Stack<SelectPic> delFileStack = new Stack<>();
			for (Integer index : mSelectItemIndex) {
				delFileStack.push(new SelectPic(picFiles.get(index), index));
			}
			boolean b;
			SelectPic temp;
			while (!delFileStack.empty()) {
				temp = delFileStack.pop();
				b = MediaStoreUtils.deleteImgStore(mContext, temp.getFile());
				if (b) {
					//UI进度显示
					publishProgress(temp.getPosition());
					//移出选中列表
					mSelectItemIndex.remove(temp.getPosition());
					//移出图片列表
					picFiles.remove(temp.getFile());
				} else {
					Log.e(TAG, "删除失败：" + temp.getFile().getAbsolutePath());
				}
				SystemClock.sleep(500);
			}


			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... position) {
			//刷新item
			picAdapter.notifyAndRemove(position[0]);
		}

		@Override
		protected void onPostExecute(Integer integer) {


			taskList.remove(this);
		}

		@Override
		protected void onCancelled() {
			taskList.remove(this);
		}

		/*
		 * 保证对象唯一性
		 */
		@Override
		public int hashCode() {
			return 'b';
		}

		/*
		 * 保证对象唯一性
		 */
		@Override
		public boolean equals(@Nullable Object obj) {
			if (obj != null) {
				return hashCode() == obj.hashCode();
			}
			return false;
		}
	}


	/**
	 * 读取图片耗时任务
	 */
	private class ReadPicTask extends AsyncTask<String, Picture, Integer> {
		// 方法1：onPreExecute（）
		// 作用：执行 线程任务前的操作
		@Override
		protected void onPreExecute() {
			totalPic = 0;
			picRecyclerView = findViewById(R.id.pic_recycler);
			GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
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
			File[] album = new File(strings[0]).listFiles();
			if (album == null) {
				return 0;
			}
			/*
				构造方法：new   vector(int   a,int   b)
				（a是容量，b是一旦容量超过，以b为单位自动扩大容量。）
			 */
			picFiles = new Vector<>(album.length, 1);
			picFiles.addAll(Arrays.asList(album));
			sort(picFiles);
			Picture picture;
			for (File f : picFiles) {
				if (f.isFile()) {
					picture = MediaStoreUtils.queryImgStore(mContext, f);
					if (picture != null) {
						publishProgress(picture);
					}
				}
			}

			return picFiles.size();
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
			totalPic = integer;
			taskList.remove(this);//执行完就移除任务列表
		}

		// 作用：将异步任务设置为：取消状态
		@Override
		protected void onCancelled() {
			taskList.remove(this);//移除任务列表
		}

		/**
		 * 排序文件列表,按照最后修改时间排序
		 * 采用冒泡排序算法
		 * 递增排序
		 *
		 * @param files
		 * @return
		 */
		private void sort(Vector<File> files) {
			//冒泡
			for (int i = 0; i < files.size(); i++) {
				//外层循环，遍历次数
				for (int j = 0; j < files.size() - i - 1; j++) {
					//内层循环，升序（如果前一个值比后一个值大，则交换）
					//内层循环一次，获取一个最大值
					if (files.get(j).lastModified() > files.get(j + 1).lastModified()) {
						File temp = files.get(j + 1);
						files.set(j + 1, files.get(j));
						files.set(j, temp);
					}
				}
			}
			//因为是递增排序，所以这边反转以下
			Collections.reverse(files);

		}

		/*
		 * 保证对象唯一性
		 */
		@Override
		public int hashCode() {
			return 'a';
		}

		/*
		 * 保证对象唯一性
		 */
		@Override
		public boolean equals(@Nullable Object obj) {
			if (obj != null) {
				return hashCode() == obj.hashCode();
			}
			return false;
		}
	}

}
