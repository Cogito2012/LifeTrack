package com.example.lifetrack;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifetrack.GalleryAdapter.ViewHolder;
import com.example.service.UserService;
import com.example.utils.AndroidTools;
import com.example.utils.Base64Coder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.supermap.data.Color;
import com.supermap.data.CoordSysTranslator;
import com.supermap.data.CursorType;
import com.supermap.data.Dataset;
import com.supermap.data.DatasetType;
import com.supermap.data.DatasetVector;
import com.supermap.data.DatasetVectorInfo;
import com.supermap.data.Datasource;
import com.supermap.data.DatasourceConnectionInfo;
import com.supermap.data.EncodeType;
import com.supermap.data.EngineType;
import com.supermap.data.Environment;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoPoint;
import com.supermap.data.GeoStyle;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Recordset;
import com.supermap.data.Rectangle2D;
import com.supermap.data.Size2D;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.mapping.CallOut;
import com.supermap.mapping.CalloutAlignment;
import com.supermap.mapping.Map;
import com.supermap.mapping.MapLoadedListener;
import com.supermap.mapping.MapParameterChangedListener;
import com.supermap.mapping.MapView;

//import android.graphics.Color;

public class MainActivity extends SlidingFragmentActivity implements
		MultiChoiceModeListener {
	// 辅助变量
	private CanvasTransformer mTransformer;

	// 基本地图变量
	Workspace m_workspace;
	MapView m_mapView;
	private Button btnZoomIn;
	private Button btnZoomOut;
	private Map mMap = null;
	private PrjCoordSys desPrjCoorSys;
	// 定位
	private Location lastLocation;
	AndroidTools at;

	public String provider = "N";// 定位方式
	private LocationManager gpsLocationManager;
	private static final long MINTIME = 2000;
	private static final float MININSTANCE = 2.0f;
	static final int REQUEST_LOCATION = 1;

	// 相册
	static final int REQUEST_TAKE_PHOTO = 1;
	String mCurrentPhotoPath;
	private ImageView mImageView;
	Bitmap bitmap;
	private final int REQUEST_IMAGE_CODE = 2;

	// 滚动图片
	private static GridView mGridView;
	private HorizontalScrollView mScrollView;
	private int cWidth = 600;
	Context context;
	private GalleryAdapter mAdapter;
	private static ArrayList<String> pathList;
	private static ArrayList<String> nameList;

	private TextView mActionText;
	private static final int MENU_SELECT_ALL = 0;
	private static HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	int photoH; // 临时存放图像原始宽高
	int photoW;

	// 上传数据
	String HOST;
	String url;
	int tag = 0;
	int count = 0;

	// 轨迹显示
	DatasourceConnectionInfo dsInfo;
	String serverPath;
	Datasource ds;
	Boolean hasOpen = false;
	Boolean hasCreateSource = false;
	private int trajID = 1;
	private String trajpath;
	private String imageFilePath;
	private String strTextFile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置系统路径
		Environment.setLicensePath(this.getResources().getString(
				R.string.LicensePath));
		Environment.setTemporaryPath(this.getResources().getString(
				R.string.TemporaryPath));
		Environment.setWebCacheDirectory(this.getResources().getString(
				R.string.WebCacheDirectory));
		// 组件功能初始化
		Environment.initialization(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去标题栏

		setContentView(R.layout.activity_main);
		at = (AndroidTools) getApplicationContext();

		// 初始化侧滑布局
		initAnimation();
		initSlidingMenu();

		if (!isGPSOpen(this)) {
			showDialog(0);
		}
		// 打开地图
		OpenMap();

		// 定位

		Location();

		// 滚动图片显示
		setGridView();

		// 上传数据
		UploadImage();

		// 轨迹显示
		showImagesTraj();

	}

	/**
	 * 初始化动画效果
	 */
	private void initAnimation() {
		mTransformer = new CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				float scale = (float) (percentOpen * 0.25 + 0.75);
				canvas.scale(scale, scale, canvas.getWidth() / 2,
						canvas.getHeight() / 2);
			}

		};
	}

	/**
	 * 初始化滑动菜单
	 */
	private void initSlidingMenu() {
		// 设置滑动菜单视图
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new MainListFragment()).commit();

		// 设置滑动菜单的属性
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		sm.setBehindScrollScale(0.0f);
		sm.setBehindCanvasTransformer(mTransformer);

		setSlidingActionBarEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 打开地图
	 */
	public void OpenMap() {
		// 打开工作空间
		m_workspace = new Workspace();
		WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
		info.setServer(getString(R.string.DataPath) + "SuperMapCloud.smwu");
		info.setType(WorkspaceType.SMWU);
		m_workspace.open(info);

		// 将地图显示控件和工作空间关联
		m_mapView = (MapView) findViewById(R.id.Map_view);
		m_mapView.getMapControl().getMap().setWorkspace(m_workspace);

		mMap = m_mapView.getMapControl().getMap();

		// 打开工作空间中的地图
		if (m_workspace.open(info)) {
			m_mapView.getMapControl().getMap().open("WuhanMap");

			mMap.setMapLoadedListener(new MapLoadedListener() {
				@Override
				public void onMapLoaded() {

				}
			});

		}

		desPrjCoorSys = new PrjCoordSys();
		desPrjCoorSys.setType(PrjCoordSysType.PCS_SPHERE_MERCATOR);

		btnZoomIn = (Button) findViewById(R.id.zoomin);
		btnZoomOut = (Button) findViewById(R.id.zoomout);
		m_mapView = (MapView) findViewById(R.id.Map_view);

		// 放大按钮
		btnZoomIn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				m_mapView.getMapControl().getMap().zoom(2);
				m_mapView.getMapControl().getMap().refresh();
			}

		});

		// 缩小按钮
		btnZoomOut.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				m_mapView.getMapControl().getMap().zoom(0.5);
				m_mapView.getMapControl().getMap().refresh();
			}

		});
		Point2D point2d = new Point2D(114.366592, 30.532217);
		double coordinate[] = at
				.LonLat2Mercator(point2d.getX(), point2d.getY());
		point2d.setX(coordinate[0] - 725);
		point2d.setY(coordinate[1] - 835);
		addCallOutBall(point2d, R.drawable.daohang);
		m_mapView.getMapControl().getMap().setCenter(point2d);

		m_mapView.getMapControl().getMap().refresh();
		m_mapView.getMapControl().setMapParamChangedListener(
				mapParameterChangedListener);
	}

	// /////////////////////////////////////////定位模块///////////////////////////////////////
	/**
	 * 对地图参数的改变进行监听
	 */
	private final MapParameterChangedListener mapParameterChangedListener = new MapParameterChangedListener() {

		@Override
		public void scaleChanged(double arg0) {
			updateLocation();

		}

		@Override
		public void boundsChanged(Point2D arg0) {
			updateLocation();
		}
	};

	/**
	 * 更新当前位置
	 */
	private void updateLocation() {
		Point2D curLocation = new Point2D();

		if (lastLocation == null) {
			return;
		}
		curLocation.setX(lastLocation.getLongitude());
		curLocation.setY(lastLocation.getLatitude());

		// 投影转换
		/*
		 * Point2Ds points = new Point2Ds(); points.add(curLocation);
		 * CoordSysTranslator.forward(points, desPrjCoorSys);
		 * //坐标点集经纬度——>Sphere_Mercator坐标
		 * curLocation.setX(points.getItem(0).getX()+800);
		 * curLocation.setY(points.getItem(0).getY()-400);
		 */
		double coordinate[] = at.LonLat2Mercator(curLocation.getX(),
				curLocation.getY());
		curLocation.setX(coordinate[0] + 800);
		curLocation.setY(coordinate[1] - 400);

		if (isLocInMap(curLocation)) {
			addCallOutBall(curLocation, R.drawable.daohang);
		}
	}

	/**
	 * 显示定位气泡
	 * 
	 * @param loction
	 */
	private void addCallOutBall(Point2D loction, int resid) {
		CallOut callout = new CallOut(MainActivity.this);
		callout.setStyle(CalloutAlignment.CENTER);
		callout.setCustomize(true);
		callout.setLocation(loction.getX(), loction.getY());
		ImageView image = new ImageView(MainActivity.this);
		image.setBackgroundResource(resid);
		callout.setContentView(image);
		m_mapView.addCallout(callout);

	}

	/**
	 * 判断定位点是否位于屏幕中
	 * 
	 * @param point2d
	 * @return
	 */
	private boolean isLocInMap(Point2D point2d) {
		Rectangle2D rcMap = m_mapView.getMapControl().getMap().getViewBounds();
		if (point2d.getX() < rcMap.getLeft()
				|| point2d.getX() > rcMap.getRight()) {
			return false;
		}
		if (point2d.getY() < rcMap.getBottom()
				|| point2d.getY() > rcMap.getTop()) {
			return false;
		}
		return true;
	}

	/**
	 * 定位功能主函数
	 */
	public void Location() {
		initLocationListener();
		LinearLayout buttonLocation = (LinearLayout) findViewById(R.id.button_location);
		buttonLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (lastLocation == null) {
					Toast.makeText(MainActivity.this, "正在定位",
							Toast.LENGTH_SHORT).show();
					// updateLocation();
					if (provider == "gps") {
						Toast.makeText(MainActivity.this, "请到室外实现定位",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					m_mapView.removeAllCallOut();
					Point2D point2d = new Point2D(lastLocation.getLongitude(),
							lastLocation.getLatitude());

					double coordinate[] = at.LonLat2Mercator(point2d.getX(),
							point2d.getY());
					point2d.setX(coordinate[0] - 725);
					point2d.setY(coordinate[1] - 835);

					addCallOutBall(point2d, R.drawable.daohang);

					m_mapView.getMapControl().getMap().setCenter(point2d);
					m_mapView.getMapControl().getMap().setScale(1.0 / 1.0);
					m_mapView.getMapControl().getMap().refresh();

				}
			}

		});

	}

	/**
	 * 对位置进行监听
	 */
	private void initLocationListener() {
		gpsLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// MainActivity.this.getBestProvider();// 获取最佳定位方式
		Toast.makeText(MainActivity.this, provider + " 定位", Toast.LENGTH_SHORT)
				.show();

		lastLocation = gpsLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);// LocationManager.GPS_PROVIDER
		gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MINTIME, MININSTANCE, locationListener);
	}

	/**
	 * 取得最佳位置服务
	 * 
	 * @return provider
	 */
	private String getBestProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗

		provider = gpsLocationManager.getBestProvider(criteria, true);
		if (provider == null) {
			Toast.makeText(MainActivity.this, "provider=null",
					Toast.LENGTH_SHORT).show();
			provider = "gps";
		}
		return provider;
	}

	/**
	 * 监听位置的变化
	 */
	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
			lastLocation = location;
			updateLocation();

		}
	};

	// //////////////////////////////////////////////滚动图片显示///////////////////////////////
	/**
	 * ' 设置滚动图片栏显示
	 */
	int itemIndex = 0;

	public void setGridView() {
		mGridView = (GridView) findViewById(R.id.mGridView);
		mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);// 设置为多选模式
		mScrollView = (HorizontalScrollView) findViewById(R.id.mScrollView);
		mScrollView.setHorizontalScrollBarEnabled(false);// 隐藏滚动条
		mScrollView.setBackgroundColor(android.graphics.Color.LTGRAY);
		mImageView = (ImageView) findViewById(R.id.mImage);

		mAdapter = new GalleryAdapter(MainActivity.this, mSelectMap);
		mGridView.setAdapter(mAdapter);
		LayoutParams params = new LayoutParams(mAdapter.getCount() * cWidth,
				LayoutParams.WRAP_CONTENT);
		mGridView.setLayoutParams(params);
		mGridView.setColumnWidth(cWidth);
		mGridView.setStretchMode(GridView.NO_STRETCH);
		mGridView.setNumColumns(mAdapter.getCount());
		mGridView.setMultiChoiceModeListener(this);// 设置多选模式监听
		pathList = new ArrayList<String>();
		nameList = new ArrayList<String>();

		// 图片单击响应监听

		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mGridView.getChildAt(position).getId() == view.getId()) {
					dispatchTakePictureIntent();
					itemIndex = position;
					Toast.makeText(MainActivity.this, "position: " + position,
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/**
	 * 调度拍照事件
	 */
	private void dispatchTakePictureIntent() {
		String[] items = { "相册", "相机" };
		new AlertDialog.Builder(this)
				.setTitle("选择照片来源")
				.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0: // 相册模式
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType("image/*");
							startActivityForResult(
									Intent.createChooser(intent, "选择图片"),
									REQUEST_IMAGE_CODE);

							break;
						case 1: // 相机模式
							Intent takePictureIntent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							if (takePictureIntent
									.resolveActivity(getPackageManager()) != null) {
								File photoFile = null;
								try {
									photoFile = createImageFile();
								} catch (IOException ex) {
								}
								if (photoFile != null) {
									takePictureIntent.putExtra(
											MediaStore.EXTRA_OUTPUT,
											Uri.fromFile(photoFile));
									startActivityForResult(takePictureIntent,
											REQUEST_TAKE_PHOTO);
								}
							}
							break;
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	/**
	 * 创建图像文件，并保存路径到全局变量mCurrentPhotoPath
	 * 
	 * @return image
	 * @throws IOException
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + ".jpg";

		File image = new File(imageFilePath + imageFileName);

		mCurrentPhotoPath = image.getAbsolutePath();
		pathList.add(mCurrentPhotoPath);
		nameList.add(imageFileName);

		return image;
	}

	/**
	 * 相机回调函数
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_TAKE_PHOTO // 相机模式
				&& resultCode == Activity.RESULT_OK) {
			// 解码位图
			bitmap = getBitmap(mCurrentPhotoPath, "autofit");
			// 显示在当前的itemIndex
			ViewHolder holder = new ViewHolder();
			View view = mGridView.getChildAt(itemIndex);
			holder.mImg = (ImageView) view.findViewById(R.id.mImage);
			holder.mImg.setImageBitmap(bitmap);
			view.setTag(holder);

		}
		if (requestCode == REQUEST_IMAGE_CODE // 相册模式
				&& resultCode == Activity.RESULT_OK) {
			ContentResolver resolver = getContentResolver();
			Uri originalUri = data.getData(); // 获得图片的uri
			try {
				bitmap = MediaStore.Images.Media.getBitmap(resolver,
						originalUri);
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(originalUri, proj, null, null,
						null);
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				mCurrentPhotoPath = cursor.getString(column_index);
				pathList.add(mCurrentPhotoPath);

				String imageFileName = null;
				int start = mCurrentPhotoPath.lastIndexOf("/");
				int end = mCurrentPhotoPath.lastIndexOf(".");
				if (start != -1 && end != -1) {
					imageFileName = mCurrentPhotoPath.substring(start + 1, end);
				} else {
				}
				nameList.add(imageFileName);
				// File imageFile = createImageFile();
				// data.putExtra(MediaStore.EXTRA_OUTPUT,
				// Uri.fromFile(imageFile));

				ViewHolder holder = new ViewHolder();
				View view = mGridView.getChildAt(itemIndex);
				holder.mImg = (ImageView) view.findViewById(R.id.mImage);
				holder.mImg.setImageBitmap(bitmap);
				view.setTag(holder);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 显得到bitmap图片

		}

	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// 得到布局文件的View
		View v = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,
				null);
		mActionText = (TextView) v.findViewById(R.id.action_text);
		// 设置显示内容为GridView选中的项目
		mActionText.setText(formatString(mGridView.getCheckedItemCount()));
		// 设置动作条的视图
		mode.setCustomView(v);
		// 得到菜单
		getMenuInflater().inflate(R.menu.action_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		//
		menu.getItem(MENU_SELECT_ALL).setEnabled(
				mGridView.getCheckedItemCount() != mGridView.getCount());
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		//
		switch (item.getItemId()) {
		case R.id.menu_select:
			for (int i = 0; i < mGridView.getCount(); i++) {
				mGridView.setItemChecked(i, true);
				mSelectMap.put(i, true);
			}
			break;
		case R.id.menu_unselect:
			for (int i = 0; i < mGridView.getCount(); i++) {
				mGridView.setItemChecked(i, false);
				mSelectMap.clear();
			}
			break;
		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		//
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		//
		mActionText.setText(formatString(mGridView.getCheckedItemCount()));
		mSelectMap.put(position, checked);/* 放入选中的集合中 */
		mode.invalidate();
	}

	private String formatString(int count) {
		return String.format(getString(R.string.selection), count);
	}

	// /////////////////////////////////////////上传模块///////////////////////////////////
	public void UploadImage() {

		LinearLayout btnUpload = (LinearLayout) findViewById(R.id.button_upload);
		btnUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// status = getURL();
				if (mSelectMap.size() != 0) {
					startUpload();
				} else {
					Toast.makeText(MainActivity.this, "请先选择图片！",
							Toast.LENGTH_LONG).show();
				}

			}

		});

	}

	private void startUpload() {
		Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage("确定要开始上传吗?");
		builder.setTitle("LifeTrack");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 获取用户名
				SharedPreferences sp = getSharedPreferences("userInfo",
						Context.MODE_PRIVATE);
				final String usrName = sp.getString("USER_NAME", "");
				if (usrName == null) {
					Toast.makeText(MainActivity.this, "请先登录！",
							Toast.LENGTH_LONG).show();
				} else {
					String urltext = getString(R.string.DataPath) + "url.txt";
					UserService us = new UserService(MainActivity.this);
					HOST = us.readurl(urltext);
					if (!HOST.equals(null)) {
						// 上传任务线程
						new Thread(new Runnable() {
							public void run() {
								uploadTask(usrName);
							}
						}).start();
					}
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	boolean status = false;

	private boolean getURL() {
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("设置服务器IP地址");
		View view = LayoutInflater.from(MainActivity.this).inflate(
				R.layout.editurl, null);
		builder.setView(view);
		final EditText url_part1 = (EditText) view.findViewById(R.id.editURL1);
		final EditText url_part2 = (EditText) view.findViewById(R.id.editURL2);
		final EditText url_part3 = (EditText) view.findViewById(R.id.editURL3);
		final EditText url_part4 = (EditText) view.findViewById(R.id.editURL4);
		final EditText url_port = (EditText) view.findViewById(R.id.etPort);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				final String url1 = url_part1.getText().toString();
				final String url2 = url_part2.getText().toString();
				final String url3 = url_part3.getText().toString();
				final String url4 = url_part4.getText().toString();
				String port = url_port.getText().toString();
				if ((url1 != "") && (url2 != "") && (url3 != "")
						&& (url4 != "") && port != "") {

					status = true;
					// 获取用户名
					SharedPreferences sp = getSharedPreferences("userInfo",
							Context.MODE_PRIVATE);
					final String usrName = sp.getString("USER_NAME", "");
					if (usrName == null) {
						Toast.makeText(MainActivity.this, "请先登录！",
								Toast.LENGTH_LONG).show();
					} else {
						String urltext = getString(R.string.DataPath)
								+ "url.txt";
						UserService us = new UserService(MainActivity.this);
						HOST = us.readurl(urltext);
						if (!HOST.equals(null)) {
							// 上传任务线程
							new Thread(new Runnable() {
								public void run() {
									uploadTask(usrName);
								}
							}).start();
						}
					}
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		builder.create().show();
		return status;
	}

	private void uploadTask(String usrName) {
		File txtfile = new File(strTextFile);
		if (txtfile.exists()) {
			txtfile.delete();
		}
		count = 0;
		String result = null;
		for (int i = 0; i < pathList.size(); i++) { // 遍历图册上加载的所有图片
			if (mSelectMap.containsKey(i)) { // 图片有被选中的历史
				if (mSelectMap.get(i) == true) { // 只上传当前选中的图，滤掉选中又取消的项
					// 获取要上传的图像
					String strPath = pathList.get(i);
					Bitmap bmp = getBitmap(strPath, "autofit"); // 如果传原图，改为"src"
					String strName = nameList.get(i);
					// 开始上传图片和名称
					// result = uploadimage(bmp,strName);
					// String lng = lastLocation.getLongitude() + "";
					// String lat = lastLocation.getLatitude() + "";
					String lng = 114.5 + "";
					String lat = 31.5 + "";
					result = uploadbyWS(bmp, strName, usrName, lng, lat);
					if (lastLocation != null) {// 写入轨迹
						WriteTraj(strTextFile, strName);
					}
					bmp.recycle(); // 上传结束释放
					if (result.equals("success")) {
						count++;
					}
				}
			}
		}

	}

	public String uploadbyWS(Bitmap Image, String Name, String usrName,
			String Lng, String Lat) {

		// Bitmap经过压缩、编码得到编码串codeImage
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Image.compress(Bitmap.CompressFormat.JPEG, 60, stream);
		byte[] bImage = stream.toByteArray();
		String codeImage = new String(Base64Coder.encodeLines(bImage));

		String nameSpace = "http://tempuri.org/";
		String methodName = "UploadFile";
		String soapAction = nameSpace + methodName;
		SoapObject so = new SoapObject(nameSpace, methodName);
		so.addProperty("userName", usrName);
		so.addProperty("fileImage", codeImage);
		so.addProperty("fileName", Name + ".jpg");
		so.addProperty("lng", Lng);
		so.addProperty("lat", Lat);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// SOAP协议版本，有三个
									// VER10、VER11、VER12如果项目出错可以修改这里先看一下
		envelope.bodyOut = so;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(HOST);
		try {
			ht.call(soapAction, envelope);
			SoapObject object;
			if ((object = (SoapObject) envelope.bodyIn) != null) {
				String result = object.getProperty(0).toString();
				System.out.println(result);// 打印返回结果
				return "success";
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return "failed";
	}

	/**
	 * 从图像文件路径中解码出位图，注意每次调用了本函数，原图宽photoW高photoH被覆盖更新
	 * 
	 * @param filepath
	 * @return Bitmap
	 */
	public Bitmap getBitmap(String filepath, String opts) {
		Bitmap bmp = null;
		mImageView = (ImageView) findViewById(R.id.mImage);
		// Get the dimensions of the View
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();
		try {
			// 获取原始图像宽高
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filepath, bmOptions);
			photoW = bmOptions.outWidth;
			photoH = bmOptions.outHeight;

			// 结合View大小，计算缩放比例
			int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

			// 设置缩放比例选项
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inPurgeable = true;
			bmOptions.inSampleSize = scaleFactor << 1;
			if (opts.equals("src")) {
				bmOptions.inSampleSize = 1;
			}

			// 图像解码（不可原图直接解码，否则图像过大OutOfMemory!!!
			bitmap = BitmapFactory.decodeFile(filepath, bmOptions);

			Matrix mtx = new Matrix();
			mtx.postRotate(0); // 不旋转
			// Rotating Bitmap
			bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), mtx, true);
		} catch (OutOfMemoryError e) {
			Toast.makeText(MainActivity.this, "OutOfMemoryError! ", 3000)
					.show();
		}
		return bmp;
	}

	/**
	 * 采用HttpClient第三方包实现上传
	 * 
	 * @param upbitmap
	 */
	public String uploadimage(Bitmap upbitmap, String imgName) {
		// Bitmap经过压缩、编码得到编码串file
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		upbitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
		byte[] bImage = stream.toByteArray();
		String file = new String(Base64Coder.encodeLines(bImage));
		// ImageName经过编码得到编码串name
		byte[] bName = null;
		try {
			bName = imgName.getBytes("UTF-8"); // 设置UTF-8字符格式防止中文乱码
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String name = new String(Base64Coder.encodeLines(bName));

		// 设置上传参数
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("imagefile", file));
		formparams.add(new BasicNameValuePair("imagename", name));

		// 利用Apache的HttpClient实现上传
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(HOST); // 设置服务器地host
		UrlEncodedFormEntity entity;
		try {
			// 将上传的数据封装到post
			entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			post.addHeader("Accept",
					"text/javascript, text/html, application/xml, text/xml");
			post.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			post.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			post.addHeader("Connection", "Keep-Alive");
			post.addHeader("Cache-Control", "no-cache");
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			post.setEntity(entity);
			// post方式上传执行
			HttpResponse response = client.execute(post);
			// 输出上传结果
			System.out.println(response.getStatusLine().getStatusCode());
			HttpEntity e = response.getEntity();
			System.out.println(EntityUtils.toString(e));
			if (200 == response.getStatusLine().getStatusCode()) {
				return "上传成功";
			}
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "上传失败";
	}

	/**
	 * 将图片的轨迹数据写入文本
	 * 
	 * @param imagepath
	 */
	private void WriteTraj(String fileName, String imagename) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(fileName, true); // 创建输出文本追加方式
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 写入格式：“图片名.jpg;经度;纬度”
		byte[] bytes = (imagename + ";"
				+ Double.toString(lastLocation.getLongitude()) + ";"
				+ Double.toString(lastLocation.getLatitude()) + "\r\n")
				.getBytes();
		try { // 流式写入文本
			fout.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fout.close(); // 关闭输出流
		} catch (IOException e) {
			//
			e.printStackTrace();
		}
	}

	// ///////////////////////////////////////轨迹显示///////////////////////////////////
	public void showImagesTraj() {
		// 创建轨迹数据根目录
		trajpath = getString(R.string.DataPath) + "TrajData/";
		File dir = new File(trajpath);
		if (!dir.exists()) { // 创建轨迹文件夹
			dir.mkdir();
		}
		// 创建当前轨迹的目录
		imageFilePath = trajpath + "traj_" + trajID + "/"; // 当前轨迹文件夹
		if (!(new File(imageFilePath)).exists()) {
			(new File(imageFilePath)).mkdir();
		}
		// 创建轨迹文本
		String textFileName = "TrajInfo.txt";
		strTextFile = imageFilePath + textFileName;

		final File trajText = new File(strTextFile);
		LinearLayout btnShowTraj = (LinearLayout) findViewById(R.id.button_traj);
		btnShowTraj.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 测试用：轨迹文本
				strTextFile = trajpath + "testTraj.txt";
				trajectoryshow(strTextFile, mMap);
			}
		});
	}

	private void trajectoryshow(String strTrajFile, Map mMap) {
		// ///读取轨迹文件，将坐标存放到ArrayList中
		ArrayList<Double> listX = new ArrayList<Double>(); // 经度
		ArrayList<Double> listY = new ArrayList<Double>(); // 纬度
		File trajFile = new File(strTrajFile);
		// 读取轨迹文本
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(trajFile);
			StringBuffer sBuffer = new StringBuffer();
			DataInputStream dataIO = new DataInputStream(fis);
			String strLine = null;
			while ((strLine = dataIO.readLine()) != null) {
				sBuffer.append(strLine + "\n");
				String[] strSplit = strLine.split(";");
				Double X = Double.parseDouble(strSplit[1]);
				Double Y = Double.parseDouble(strSplit[2]);
				listX.add(X);
				listY.add(Y);
			}
			dataIO.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dsInfo = new DatasourceConnectionInfo();
		serverPath = trajpath + "traj_" + trajID + ".udb";
		dsInfo.setServer(serverPath);
		dsInfo.setEngineType(EngineType.UDB);

		// 如果文件存在并且未被打开，则直接打开，否则创建
		if (hasCreateSource && !hasOpen) {
			ds = m_workspace.getDatasources().open(dsInfo);
			hasOpen = true;
		} else if (hasCreateSource && hasOpen) {
			// 跳过
		} else if (!hasCreateSource) {
			ds = m_workspace.getDatasources().create(dsInfo);
			hasCreateSource = true;
			if (ds != null) {
				Log.i("DataManage", "新建数据源成功。");
			}
			ds = m_workspace.getDatasources().open(dsInfo);
			hasOpen = true;
		}

		// 在数据源中创建两个数据层，point和line分别表示轨迹节点和路径，首先判断是否存在，
		Point2D centerP = new Point2D(); // 定义一个轨迹线图形中心点
		double X_sum = 0;
		double Y_sum = 0;
		if (ds != null) {
			DatasetVectorInfo dsvInfoPoint = new DatasetVectorInfo("point",
					DatasetType.POINT);
			DatasetVectorInfo dsvInfoLine = new DatasetVectorInfo("line",
					DatasetType.LINE);
			dsvInfoPoint.setEncodeType(EncodeType.NONE);
			dsvInfoLine.setEncodeType(EncodeType.NONE);
			Dataset dtPoint;// 点数据集
			Dataset dtLine;// 线数据集
			if (ds.getDatasets().contains("point")) {
				ds.getDatasets().delete("point");
			}
			dtPoint = ds.getDatasets().create(dsvInfoPoint);

			if (ds.getDatasets().contains("line")) {
				ds.getDatasets().delete("line");
			}
			dtLine = ds.getDatasets().create(dsvInfoLine);
			// ////将坐标加入数据集中
			if ((dtPoint != null) && (dtLine != null)) {
				Recordset recPoint = ((DatasetVector) dtPoint).getRecordset(
						false, CursorType.DYNAMIC);
				Recordset recLine = ((DatasetVector) dtLine).getRecordset(
						false, CursorType.DYNAMIC);
				int i = 0;
				Point2Ds points = new Point2Ds();
				for (i = 0; i < listX.size(); i++) {
					Point2D point = new Point2D(listX.get(i), listY.get(i));
					// WGS84坐标系经纬度转超图地图坐标

					// 将点坐标加入
					points.add(point);

					// 设置点符号风格
					GeoStyle geoStyle_P = new GeoStyle();
					geoStyle_P.setMarkerAngle(14.0);
					geoStyle_P.setMarkerSize(new Size2D(10, 10));
					geoStyle_P.setMarkerSymbolID(10);
					GeoPoint geoPoint = new GeoPoint(point);
					geoPoint.setStyle(geoStyle_P);

					boolean bAddP = recPoint.addNew(geoPoint);// ////添加点
					mMap.setCenter(point);

					if (bAddP) {
						recPoint.update();
					}
				}

				// 投影转换
				CoordSysTranslator.forward(points, desPrjCoorSys); // 坐标点集经纬度——>Sphere_Mercator坐标
				// 设置点符号
				for (i = 0; i < listX.size(); i++) {
					X_sum += points.getItem(i).getX();
					Y_sum += points.getItem(i).getY();
					addCallOutBall(points.getItem(i), R.drawable.location);
				}
				// 添加线
				GeoLine geoLine = new GeoLine(points);
				// 设置线型
				GeoStyle geoStyle_L = new GeoStyle();
				geoStyle_L.setLineSymbolID(22);
				geoStyle_L.setLineWidth(5.0);
				Color color = new Color(255, 255, 0);
				geoStyle_L.setLineColor(color);
				geoLine.setStyle(geoStyle_L);

				boolean bAddL = recLine.addNew(geoLine);
				if (bAddL) {
					recLine.update();
				}
				mMap.setWorkspace(m_workspace);
				mMap.getLayers().add(dtPoint, true);
				mMap.getLayers().add(dtLine, true);
				recPoint.dispose();
				recLine.dispose();
				// 计算中心点
				centerP.setX(X_sum / (listX.size()));
				centerP.setY(Y_sum / (listY.size()));

				mMap.setCenter(centerP);
				mMap.setScale(1.0 / 25000);
				mMap.refresh();
			}
		}

	}

	// ///////////////////////////////////////////其它/////////////////////////////////
	/**
	 * 判断GPS是否开启
	 * 
	 * @param context
	 * @return true 表示开启
	 */

	public boolean isGPSOpen(Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		boolean gps = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (gps) {
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		m_mapView.getMapControl().getMap().close();
		m_workspace.close();
		super.onDestroy();
		gpsLocationManager.removeUpdates(locationListener);
		m_mapView.getMapControl().removeMapParamChangedListener(
				mapParameterChangedListener);
		if (bitmap != null && !bitmap.isRecycled()) {
			// 回收并且置为null
			bitmap.recycle();
			bitmap = null;
		}
		System.gc();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// MINTIME, MININSTANCE, locationListener);//
		// LocationManager.GPS_PROVIDER,

	}

	@Override
	protected void onPause() {
		super.onPause();
		// 取消注册监听
		gpsLocationManager.removeUpdates(locationListener);
	}

	/**
	 * 菜单、返回键响应
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click(); // 调用双击退出函数
		}
		return false;
	}

	/**
	 * 双击退出函数
	 */
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			finish();
			System.exit(0);
		}
	}

}
