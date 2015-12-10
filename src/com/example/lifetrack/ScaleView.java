package com.example.lifetrack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.supermap.data.GeoSpheroid;
import com.supermap.data.Geometrist;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Rectangle2D;
import com.supermap.mapping.Map;

public class ScaleView extends View {
	private Paint mPaint;
	/**
	 * 比例尺的宽度
	 */
	private int scaleWidth;
	/**
	 * 比例尺的高度
	 */
	private int scaleHeight = 4;
	/**
	 * 比例尺上面字体的颜色
	 */
	private int textColor = Color.BLACK;
	/**
	 * 比例尺上边的字体
	 */
	private String text = "0";
	/**
	 * 字体大小
	 */
	private int textSize = 16;
	/**
	 * 比例尺与字体间的距离
	 */
	private int scaleSpaceText = 8;
	/**
	 * 各级比例尺分母值数组
	 */
	private static final int[] SCALES = {20, 50, 100, 200, 500, 1000, 2000,
			5000, 10000, 20000, 25000, 50000, 100000, 200000, 500000, 1000000,
			2000000, 5000000, 10000000 };
	/**
	 * 各级比例尺上面的文字数组
	 */
	private static final String[] SCALE_DESCS = { "20米", "50米", "100米", "200米",
			"500米", "1公里", "2公里", "5公里", "10公里", "20公里", "25公里", "50公里",
			"100公里", "200公里", "500公里", "1000公里", "2000公里" ,"5000公里","1万公里"};
	
	private Map mMap;
	
	private double mMapWidth;

	/**
	 * 与MapView设置关联
	 * @param mapView
	 */
	public void setMap(Map map) {
		this.mMap = map;
	}

	public ScaleView(Context context) {
		this(context, null);
	}
	
	public ScaleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScaleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}

	
	/**
	 * 绘制上面的文字和下面的比例尺，因为比例尺是.9.png，我们需要利用drawNinepath方法绘制比例尺
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int width = scaleWidth;
		
		mPaint.setColor(textColor);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(textSize);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		float textWidth = mPaint.measureText(text);
		
		canvas.drawText(text, (width - textWidth) / 2, textSize, mPaint);
		
		Rect scaleRect = new Rect(0, textSize + scaleSpaceText, scaleWidth, textSize + scaleSpaceText + scaleHeight);
		drawNinepath(canvas, R.drawable.icon_scale, scaleRect);
	}
	
	/**
	 * 手动绘制.9.png图片
	 * @param canvas
	 * @param resId
	 * @param rect
	 */
	private void drawNinepath(Canvas canvas, int resId, Rect rect){  
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), resId);  
        NinePatch patch = new NinePatch(bmp, bmp.getNinePatchChunk(), null);  
        patch.draw(canvas, rect);  
    }


	/**
	 * 测量ScaleView的方法，
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSize = getWidthSize(widthMeasureSpec);
		int heightSize = getHeightSize(heightMeasureSpec);
		setMeasuredDimension(widthSize, heightSize);
	}
	
	/**
	 * 测量ScaleView的宽度
	 * @param widthMeasureSpec
	 * @return
	 */
	private int getWidthSize(int widthMeasureSpec){
		return MeasureSpec.getSize(widthMeasureSpec);
	}
	
	/**
	 * 测量ScaleView的高度
	 * @param widthMeasureSpec
	 * @return
	 */
	private int getHeightSize(int heightMeasureSpec){
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		int height = 0;
		switch (mode) {
		case MeasureSpec.AT_MOST:
			height = textSize + scaleSpaceText + scaleHeight;
			break;
		case MeasureSpec.EXACTLY:{
			height = MeasureSpec.getSize(heightMeasureSpec);
			break;
		}
		case MeasureSpec.UNSPECIFIED:{
			height = Math.max(textSize + scaleSpaceText + scaleHeight, MeasureSpec.getSize(heightMeasureSpec));
			break;
		}
		}
		
		return height;
	}

	/**
	 * 根据缩放级别，得到对应比例尺
	 * 
	 * @param zoomLevel
	 * @return
	 */
	public static int getScale(int zoomLevel) {
		return SCALES[zoomLevel];
	}

	/**
	 *  根据缩放级别，得到对应比例尺文字
	 * @param zoomLevel
	 * @return
	 */
	public static String getScaleDesc(int zoomLevel) {
		return SCALE_DESCS[zoomLevel];
	}

	private int getLevel(double dWidth) {
		dWidth /= 3;
		int nlevel = SCALES.length - 1;
		for (int i = 0; i < nlevel; i++) {
			if (dWidth > SCALES[i] && dWidth < SCALES[i+1] ) {
				nlevel = i;
				break;
			}
		}
		return nlevel;
	}
	
	/**
	 * 根据地图当前中心位置的纬度，当前比例尺，得出比例尺图标应该显示多长（多少像素）
	 * @param map
	 * @param scale
	 * @return
	 */
	public int meterToPixels(int scale) {
		int mapImageSize = mMap.getImageSizeWidth();
		int svPixels =(int)(mapImageSize * scale/mMapWidth);
		return svPixels;
	}

	/**
	 * 设置比例尺的宽度
	 * @param scaleWidth
	 */
	public  void setScaleWidth(int scaleWidth) {
		this.scaleWidth = scaleWidth;
	}

	/**
	 * 设置比例尺的上面的 text 例如 200公里
	 * @param text
	 */
	private void setText(String text) {
		this.text = text;
	}

	/**
	 * 设置字体大小
	 * @param textSize
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize;
		invalidate();
	}
	
	
	/**
	 * 根据缩放级别更新ScaleView的文字以及比例尺的长度
	 * @param level
	 */
	public void refreshScaleView() {
		if(mMap == null){
			throw new NullPointerException("you can call setMap(Map map) at first");
		}		
		Rectangle2D rectMap = mMap.getViewBounds();
		
		PrjCoordSys prjCoordSys = mMap.getPrjCoordSys();
		
		mMapWidth = 0;
		
		if(prjCoordSys.getType() != PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE){
			
			mMapWidth = rectMap.getWidth();			
		}
		else{ //当地图的坐标系统为经纬度时，需要计算宽度
			Point2Ds pnts  = new Point2Ds();
			pnts.add(new Point2D(rectMap.getLeft(),rectMap.getBottom()));
			pnts.add(new Point2D(rectMap.getRight(),rectMap.getBottom()));
			
			GeoSpheroid geoSpheroid = prjCoordSys.getGeoCoordSys().getGeoDatum().getGeoSpheroid();
			mMapWidth = Geometrist.computeGeodesicDistance(pnts, geoSpheroid.getAxis(),geoSpheroid.getFlatten());
		}
		
		if (mMapWidth > 0) {
			int level = getLevel(mMapWidth);
			
			setText(getScaleDesc(level));
			
			setScaleWidth(meterToPixels(getScale(level)));
			
			invalidate();
		}
	}
}
