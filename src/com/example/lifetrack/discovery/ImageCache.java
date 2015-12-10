package com.example.lifetrack.discovery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.example.utils.BitMapUtils;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class ImageCache {
	/**
	 * ������
	 * */
	private static Context context;
	/**
	 * ��Ҫ���ص�ImageView
	 * */
	private ImageView imageView;
	/**
	 * ͼƬ��URL
	 * */

	private String path;
	private static LruCache<String, Bitmap> lruCache;
	/**
	 * ͼƬѹ����Ŀ����
	 * */
	private int width;
	/**
	 * ͼƬѹ����Ŀ��߶�
	 * */
	private int height;
	// ʹ��Ӳ�̻���
	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	/**
	 * ϣ���洢��Ŀ���ļ���
	 * */
	private static String FolderName;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (imageView.getTag()!= null && imageView.getTag().equals(path)) {
				imageView.setImageBitmap((Bitmap) msg.obj);
			}
		}

	};

	public ImageCache(Context context, LruCache<String, Bitmap> lruCache,
			ImageView imageView, String path, String FolderName, int width,
			int height) {
		this.context = context;
		this.imageView = imageView;
		this.path = path;
		this.FolderName = FolderName;
		this.width = width;
		this.height = height;
		this.lruCache = lruCache;
		// ���û���ռ��С
		// final int max = (int) Runtime.getRuntime().maxMemory();

		File cacheFile = getDiskCacheDir(context, ImageCache.FolderName);
		new InitDiskCacheTask().execute(cacheFile);
		loadBitmap(path, imageView);// ����ͼƬ����
	}

	/**
	 * MD5�㷨
	 * */
	public String GetMD5(String url) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] data = digest.digest(url.getBytes());
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < data.length; i++) {
				int iv = (int) (data[i] & 0x0ff);
				if (iv <= 0x0f) {
					stringBuilder.append('0');
				}
				stringBuilder.append(Integer.toHexString(iv));
			}
			String fileName = stringBuilder.toString();
			return fileName;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
		@Override
		protected Void doInBackground(File... params) {
			// TODO Auto-generated method stub
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				mDiskLruCache = DiskLruCache.openCache(context, cacheDir,
						DISK_CACHE_SIZE);
				mDiskCacheStarting = false;
				mDiskCacheLock.notifyAll();// �������еĶ���
			}
			return null;
		}

	}

	/**
	 * ��ô��̻����Ŀ¼
	 * 
	 * @param context
	 * @param uniqueName
	 * @return
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) || !isExternalStorageRemovable() ? getExternalCacheDir(
				context).getPath()
				: context.getCacheDir().getPath();
		return new File(cachePath + File.separator + uniqueName);
	}

	public static File getExternalCacheDir(Context context) {
		return context.getExternalCacheDir();
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isExternalStorageRemovable() {
		return Environment.MEDIA_REMOVED.equals(Environment
				.getExternalStorageState());
	}

	public void loadBitmap(String key, ImageView imageView) {
		Bitmap bitmap = null;// �ȴӻ����л�ȡͼƬ
		/*
		 * if (bitmap != null) { if (imageView.getTag() != null &&
		 * imageView.getTag().equals(path)) { imageView.setImageBitmap(bitmap);
		 * } } else if (bitmap == null) { bitmap = getBitmapFromDiskCache(key);
		 * if (bitmap != null) { if (imageView.getTag() != null &&
		 * imageView.getTag().equals(path)) { imageView.setImageBitmap(bitmap);
		 * 
		 * } } } else if(bitmap==null){ new MyTask().execute(path);
		 * Log.i("","---------------+++++++++++++"); }
		 */

		bitmap = getBitmapFromMemCache(key);
		if (bitmap == null) {
			// ��SDCard�ļ��л�ȡ�ļ�
			bitmap = getBitmapFromDiskCache(key);
			if (bitmap == null) {
				// ��������������Դ
				downLoadImage(key);
			} else {
				imageView.setImageBitmap(bitmap);
			}

		} else {
			imageView.setImageBitmap(bitmap);
		}
	}

	/**
	 * �������д��ͼƬ
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			lruCache.put(key, bitmap);
		}
	}

	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			Log.i("", "-------------------*********���뻺��************");
			lruCache.put(key, bitmap);
		}
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null && mDiskLruCache.get(key) == null) {
				mDiskLruCache.put(key, bitmap);
				Log.i("", "-------------------*********����SDKA************");
			}
		}
	}

	/**
	 * �ӻ����л�ȡͼƬ
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromMemCache(String key) {
		// ���жϴӻ����л�ȡͼƬ���ݣ�����Ҳ��������sdcard���л�ȡ��Ϣ
		// ���齨��һ��ͼƬ��ά����url ����ͼƬ��·��
		return lruCache.get(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromDiskCache(String key) {
		synchronized (mDiskCacheLock) {
			// Wait while disk cache is started from background thread
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (mDiskLruCache != null) {

				return mDiskLruCache.get(key);
			} else {

				Log.i("", "-------------------����ʧЧ--------------->");
			}
		}
		return null;
	}

	/**
	 * �����첽��������ͼƬ
	 * 
	 * @author jack
	 * 
	 */
	public static LruCache<String, Bitmap> GetLruCache(Context context) {

		final int max_size = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = 1024 * 1024 * max_size / 8;
		LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(
				cacheSize) {
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
				// return value.getRowBytes() * value.getHeight();
			}
		};
		;
		return lruCache;
	}

	public void downLoadImage(final String path) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpClient httpClient = new DefaultHttpClient();
				HttpParams httpParams = httpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
				HttpConnectionParams.setSoTimeout(httpParams, 3000);
				HttpGet httpget = new HttpGet(path);
				HttpResponse response = null;

				try {
					response = httpClient.execute(httpget);
					if (response.getStatusLine().getStatusCode() == 200) {
						byte[] data = EntityUtils.toByteArray(response
								.getEntity());
						Bitmap bitmap = BitMapUtils.decodeBitmap(data, width,
								height);

						if (bitmap != null) {
							// addBitmapToMemoryCache(path, bitmap);
							// ������سɹ� �ȷŵ�������
							addBitmapToCache(path, bitmap);
							Message msgMessage = Message.obtain();
							msgMessage.obj = bitmap;
							handler.sendMessage(msgMessage);
						}

					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {

					if (httpClient != null)
						httpClient.getConnectionManager().shutdown();
				}

			}
		}).start();

	}

}
