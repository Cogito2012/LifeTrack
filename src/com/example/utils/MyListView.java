package com.example.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
/**
 * 
 * @author Pelinter
 *重写ListView，解决ListView与ListView的冲突问题
 *由于ListView中嵌套了ListView，会造成ListView无法完全显示图片，所以要重写ListView
 */
public class MyListView extends ListView {

	public MyListView(Context context, AttributeSet attrs, int defStyle) { 
		super(context, attrs, defStyle); 
		} 
		public MyListView(Context context, AttributeSet attrs) { 
		super(context, attrs); 
		} 
		public MyListView(Context context) { 
		super(context); 
		} 
		/** 
		* 设置不滚动 
		*/ 
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, 
		MeasureSpec.AT_MOST); 
		super.onMeasure(widthMeasureSpec, expandSpec); 
		} 
	

}
