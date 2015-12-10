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
	 * �����ߵĿ��
	 */
	private int scaleWidth;
	/**
	 * �����ߵĸ߶�
	 */
	private int scaleHeight = 4;
	/**
	 * �����������������ɫ
	 */
	private int textColor = Color.BLACK;
	/**
	 * �������ϱߵ�����
	 */
	private String text = "0";
	/**
	 * �����С
	 */
	private int textSize = 16;
	/**
	 * �������������ľ���
	 */
	private int scaleSpaceText = 8;
	/**
	 * ���������߷�ĸֵ����
	 */
	private static final int[] SCALES = {20, 50, 100, 200, 500, 1000, 2000,
			5000, 10000, 20000, 25000, 50000, 100000, 200000, 500000, 1000000,
			2000000, 5000000, 10000000 };
	/**
	 * �����������������������
	 */
	private static final String[] SCALE_DESCS = { "20��", "50��", "100��", "200��",
			"500��", "1����", "2����", "5����", "10����", "20����", "25����", "50����",
			"100����", "200����", "500����", "1000����", "2000����" ,"5000����","1����"};
	
	private Map mMap;
	
	private double mMapWidth;

	/**
	 * ��MapView���ù���
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
	 * ������������ֺ�����ı����ߣ���Ϊ��������.9.png��������Ҫ����drawNinepath�������Ʊ�����
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
	 * �ֶ�����.9.pngͼƬ
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
	 * ����ScaleView�ķ�����
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSize = getWidthSize(widthMeasureSpec);
		int heightSize = getHeightSize(heightMeasureSpec);
		setMeasuredDimension(widthSize, heightSize);
	}
	
	/**
	 * ����ScaleView�Ŀ��
	 * @param widthMeasureSpec
	 * @return
	 */
	private int getWidthSize(int widthMeasureSpec){
		return MeasureSpec.getSize(widthMeasureSpec);
	}
	
	/**
	 * ����ScaleView�ĸ߶�
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
	 * �������ż��𣬵õ���Ӧ������
	 * 
	 * @param zoomLevel
	 * @return
	 */
	public static int getScale(int zoomLevel) {
		return SCALES[zoomLevel];
	}

	/**
	 *  �������ż��𣬵õ���Ӧ����������
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
	 * ���ݵ�ͼ��ǰ����λ�õ�γ�ȣ���ǰ�����ߣ��ó�������ͼ��Ӧ����ʾ�೤���������أ�
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
	 * ���ñ����ߵĿ��
	 * @param scaleWidth
	 */
	public  void setScaleWidth(int scaleWidth) {
		this.scaleWidth = scaleWidth;
	}

	/**
	 * ���ñ����ߵ������ text ���� 200����
	 * @param text
	 */
	private void setText(String text) {
		this.text = text;
	}

	/**
	 * ���������С
	 * @param textSize
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize;
		invalidate();
	}
	
	
	/**
	 * �������ż������ScaleView�������Լ������ߵĳ���
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
		else{ //����ͼ������ϵͳΪ��γ��ʱ����Ҫ������
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
