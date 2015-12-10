package com.example.lifetrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class GuidePage extends Activity{
	
	@Override 
	protected void onCreate(Bundle savedInstanceState){
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,// ȫ��
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.guidepage);
		
		ImageView guideimage = (ImageView)findViewById(R.id.guideimage);
		
		AlphaAnimation animation = new AlphaAnimation(0.1f,1.0f);
		animation.setDuration(1000);
		guideimage.setAnimation(animation);
		
		//����Ч��
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				//��������
				startActivity(new Intent(GuidePage.this, MainActivity.class));
				GuidePage.this.finish();

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}
		});
	}

}
