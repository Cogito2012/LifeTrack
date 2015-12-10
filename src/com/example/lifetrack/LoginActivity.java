package com.example.lifetrack;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.example.service.DatabaseHelper;
import com.example.service.UserService;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class LoginActivity extends SlidingFragmentActivity {

	private CanvasTransformer mTransformer;
	private EditText username;
	private EditText password;
	private Button login;
	private TextView register;
	private CheckBox pswRemembered;
	private SharedPreferences sp;
	private DatabaseHelper dbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,// 全屏
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.login);

		initAnimation();
		initSlidingMenu();

		// 初始化视图
		initView();

		sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		// 从文件中获取保存的数据
		String usernameContent = sp.getString("USER_NAME", "");
		String passwordContent = sp.getString("PASSWORD", "");
		if (usernameContent != null && !"".equals(usernameContent)) {
			username.setText(usernameContent);
		}
		if (passwordContent != null && !"".equals(passwordContent)) {
			password.setText(passwordContent);
		}
		/*
		 * //从数据库user.db查询username和password字段的值 dbHelper = new
		 * DatabaseHelper(getApplicationContext()); SQLiteDatabase sdb =
		 * dbHelper.getReadableDatabase(); String
		 * sql="select * from user where username=? and password=?"; Cursor
		 * cursor = sdb.rawQuery(sql, new String[]{"username","password"});
		 * if(cursor.moveToFirst()) { String usernameContent =
		 * cursor.getString(cursor.getColumnIndex("username")); String
		 * passwordContent =
		 * cursor.getString(cursor.getColumnIndex("password"));
		 * username.setText(usernameContent); password.setText(passwordContent);
		 * }
		 */
		pswRemembered.setOnCheckedChangeListener(pswRememberListener);
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

	private void initView() {
		username = (EditText) findViewById(R.id.username_edit);
		password = (EditText) findViewById(R.id.password_edit);
		login = (Button) findViewById(R.id.signin_button);
		register = (TextView) findViewById(R.id.logon_button);
		pswRemembered = (CheckBox) findViewById(R.id.savepasswordcb);

		login.setOnClickListener(loginClickListener);
		register.setOnClickListener(logonClickListener);
	}

	/**
	 * 登陆事件监听
	 */
	public OnClickListener loginClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final String name = username.getText().toString();
			final String pass = password.getText().toString();
			Log.i("TAG", name + "_" + pass);
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					UploadUsrInfo(name, pass);
				}
			}).start();
			UserService uService = new UserService(LoginActivity.this);
			boolean flag = uService.login(name, pass);
			if (flag) {

			} else {

			}
		}

	};

	public void UploadUsrInfo(String name, String password) {

		String urltext = getString(R.string.DataPath) + "url.txt";
		UserService us = new UserService(LoginActivity.this);
		String URL = us.readurl(urltext);
		if (!URL.equals(null)) {
			String nameSpace = "http://tempuri.org/";
			String methodName = "Login";

			String soapAction = nameSpace + methodName;
			SoapObject so = new SoapObject(nameSpace, methodName);
			so.addProperty("Name", name);
			so.addProperty("Password", password);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);

			envelope.bodyOut = so;
			envelope.dotNet = true;
			HttpTransportSE ht = new HttpTransportSE(URL);
			try {
				ht.call(soapAction, envelope);
				SoapObject object;
				if ((object = (SoapObject) envelope.bodyIn) != null) {
					String result = object.getProperty(0).toString();
					System.out.println(result);
					if (result.equals("success")) {
						Log.i("TAG", "登录成功");
						sp.edit().putString("USER_NAME", name).commit();
						sp.edit().putString("PASSWORD", password).commit();
						Intent intent = new Intent(LoginActivity.this,
								MainActivity.class);
						startActivity(intent);
						LoginActivity.this.finish();
					}
				} else {
					Log.i("TAG", "登录失败");
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	/**
	 * 注册事件监听
	 */
	public TextView.OnClickListener logonClickListener = new TextView.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(LoginActivity.this, LogonActivity.class);
			startActivity(intent);
			LoginActivity.this.finish();
		}

	};
	/**
	 * 记住密码监听
	 */
	public OnCheckedChangeListener pswRememberListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			if (pswRemembered.isChecked()) {
				System.out.println("记住密码已选中");
				sp.edit().putBoolean("ISCHECK", true).commit();
			} else {
				System.out.println("记住密码没有选中");
				sp.edit().putBoolean("ISCHECK", false).commit();
			}
		}

	};
}
