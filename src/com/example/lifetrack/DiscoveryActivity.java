package com.example.lifetrack;

import java.util.ArrayList;

import com.example.lifetrack.discovery.DiscoveryFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class DiscoveryActivity extends SlidingFragmentActivity implements
OnCheckedChangeListener{

	private CanvasTransformer mTransformer;
	private TextView title;
	private Animation loadAnimation;
	private DiscoveryFragment jingXuanFragment;
	private ArrayList<Fragment> fragments;
	private RadioGroup group;
	private RadioButton imageView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_discovery);
		
		initAnimation();
		initSlidingMenu();
		
		jingXuanFragment = new DiscoveryFragment();
		title = (TextView) findViewById(R.id.main_title);
		
		fragments = new ArrayList<Fragment>();
		fragments.add(jingXuanFragment);
		if (getIntent().getIntExtra("FragmentType",0)==3) {
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.main_framelayout,fragments.get(3));
			transaction.commit();
		}else {
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.main_framelayout, fragments.get(0));
			transaction.commit();
		}
	}

	/**
	 * ��ʼ������Ч��
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
	 * ��ʼ�������˵�
	 */
	private void initSlidingMenu(){
		// ���û����˵���ͼ
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new MainListFragment()).commit();

		// ���û����˵�������
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
	public void onCheckedChanged(RadioGroup view, int checkId) {
		int childCount = group.getChildCount();
		int checkedIndex = 0;
		RadioButton btnButton = null;
		for (int i = 0; i < childCount; i++) {
			btnButton = (RadioButton) group.getChildAt(i);
			if (btnButton.isChecked()) {
				checkedIndex = i;
				break;
			}
		}

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		Fragment fragment = null;
		switch (checkedIndex) {
		case 0:
			fragment = fragments.get(0);
			transaction.replace(R.id.main_framelayout, fragment);
			transaction.commit();
			title.setText("��ѡ");
			break;
		case 1:
			fragment = fragments.get(1);
			transaction.replace(R.id.main_framelayout, fragment);
			transaction.commit();
			title.setText("ר��");
			break;
		case 2:
			
			break;
		case 3:
			fragment = fragments.get(2);
			transaction.replace(R.id.main_framelayout, fragment);
			transaction.commit();
			title.setText("�");
			break;
		case 4:
			fragment = fragments.get(3);
			transaction.replace(R.id.main_framelayout, fragment);
			transaction.commit();
			title.setText("��¼");
			break;

		default:
			break;
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.gc();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
