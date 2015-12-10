package com.example.lifetrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.example.utils.MyListView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MessageActivity extends SlidingFragmentActivity {

	private CanvasTransformer mTransformer;
    private EditText info_edit;

    private ArrayList<Map<String, String[]>> mlist = new ArrayList<Map<String, String[]>>();
	private MessageAdapter adapter;
	private MyListView mListView; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去标题栏
		setContentView(R.layout.activity_message);
		
		initAnimation();
		initSlidingMenu();
		
		InitView();
		
		initData();
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//跳转
			}
		});
		
	}
	
	/**
	 * 初始化动画效果
	 */
	private void initAnimation(){
		mTransformer = new CanvasTransformer(){
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				float scale = (float) (percentOpen*0.25 + 0.75);
				canvas.scale(scale, scale, canvas.getWidth()/2, canvas.getHeight()/2);
			}
			
		};
	}
	
	/**
	 * 初始化滑动菜单
	 */
	private void initSlidingMenu(){
		// 设置滑动菜单视图
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new MainListFragment()).commit();

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
	

	private void InitView(){
		
	   info_edit=(EditText)findViewById(R.id.info_edit);
	   mListView=(MyListView)findViewById(R.id.info_list);
	}
	
	public void initData()
	{
		String[] strNames = getResources().getStringArray(R.array.nameList);
		String[] strMsgs = getResources().getStringArray(R.array.msgList);
		String[] strTimes = getResources().getStringArray(R.array.timeList);
		
		for (int i=0;i<15;i++){
			String[] unit = new String[5];
			unit[0] = "head"+String.valueOf(i);
			unit[1] = strNames[i];
			unit[2] = strMsgs[i];
			unit[3] = strTimes[i];
			unit[4] = String.valueOf(Math.random()*10);
			HashMap<String, String[]> map = new HashMap<String, String[]>();
			map.put("msg_id", unit);
			mlist.add(map);
		}
		adapter=new MessageAdapter(MessageActivity.this, mlist);	
	    mListView.setAdapter(adapter);
	}

}
