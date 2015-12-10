package com.example.lifetrack;

import java.util.Timer;
import java.util.TimerTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.service.GetSmsContent;
import com.example.service.UserService;
import com.example.utils.UserUtils;

public class LogonActivity extends Activity {

	public EditText telephone;
	public EditText username;
	private EditText password;
	private EditText pincode;
	private Button btn_getPIN;
	private Button show_password;
	private CheckBox chek_protocol;
	private Button register_commit;

	private boolean showPassword = true;
	String strPIN = null;
	private int time = 60;
	private Timer timer = new Timer();
	TimerTask task;
	GetSmsContent content;

	private String URL = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.logon);

		initView();

		content = new GetSmsContent(LogonActivity.this, new Handler(), pincode);
		// 注册短信变化监听
		this.getContentResolver().registerContentObserver(
				Uri.parse("content://sms/"), true, content);

	}

	private void initView() {
		telephone = (EditText) findViewById(R.id.edit_phone_user);
		username = (EditText) findViewById(R.id.logon_edit_username);
		password = (EditText) findViewById(R.id.edit_phone_pwd);
		pincode = (EditText) findViewById(R.id.edit_pincode);
		chek_protocol = (CheckBox) findViewById(R.id.register_check_protocol);

		show_password = (Button) findViewById(R.id.btn_reister_show_password);
		show_password.setOnClickListener(showPasswordListener);

		btn_getPIN = (Button) findViewById(R.id.btn_reister_getphone_pin);
		btn_getPIN.setOnClickListener(getpinListener);

		register_commit = (Button) findViewById(R.id.register_phone);
		register_commit.setOnClickListener(registerListener);
	}

	/**
	 * 显示密码事件监听
	 */
	public OnClickListener showPasswordListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (showPassword) {// 显示密码
				showPassword = !showPassword;
				show_password.setText("隐藏");
				password.setTransformationMethod(HideReturnsTransformationMethod
						.getInstance());
				password.setSelection(password.getText().toString().length());
			} else {// 隐藏密码
				showPassword = !showPassword;
				show_password.setText("显示");
				password.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
				password.setSelection(password.getText().toString().length());
			}
		}

	};
	/**
	 * 获取验证码事件监听
	 */
	public OnClickListener getpinListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (checkPhone()) {
				new Send_pinMessage().execute();
			}
		}
	};
	/**
	 * 注册账号事件监听
	 */
	public OnClickListener registerListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Boolean agree = chek_protocol.isChecked();
			if (checkPhone() && checkUsername() && checkPassword() && agree) {
				// 界面跳转
				final String name = username.getText().toString().trim();
				final String pass = password.getText().toString().trim();
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						UploadUsrInfo(name, pass);
					}
				}).start();
			}
		}
	};

	/**
	 * 手机号格式检查
	 * 
	 * @return
	 */
	private Boolean checkPhone() {
		String phone = telephone.getText().toString();
		if (null == phone || "".equals(phone) || "".equals(phone.trim())) {
			Toast.makeText(getApplicationContext(), "请输入手机号码",
					Toast.LENGTH_SHORT).show();
			return false;
		} else if (phone.length() != 11 || !phone.startsWith("1")) {
			Toast.makeText(getApplicationContext(), "手机号码格式不对",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * 用户名检查
	 * 
	 * @return
	 */
	private boolean checkUsername() {
		String usn = username.getText().toString();
		if (null == usn || "".equals(usn) || "".equals(usn.trim())) {
			Toast.makeText(getApplicationContext(), "请输入用户名",
					Toast.LENGTH_SHORT).show();
			return false;
		} else if (usn.contains(" ")) {
			Toast.makeText(getApplicationContext(), "用户名不能包含空格",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * 密码格式检查
	 * 
	 * @return
	 */
	private boolean checkPassword() {
		String psw = password.getText().toString();
		if (null == psw || "".equals(psw) || "".equals(psw.trim())) {
			Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT)
					.show();
			return false;
		} else if (psw.contains(" ")) {
			Toast.makeText(getApplicationContext(), "密码不能包含空格",
					Toast.LENGTH_SHORT).show();
			return false;
		} else if (psw.length() < 6 || psw.length() > 20) {
			Toast.makeText(getApplicationContext(), "密码长度为6-20字符",
					Toast.LENGTH_SHORT).show();
			return false;
		} else if (username.getText().toString().equals(psw.trim())) {
			Toast.makeText(getApplicationContext(), "密码不能跟账号一致,请重新输入",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * 验证码发送任务
	 * 
	 * @author wtbao
	 * 
	 */
	class Send_pinMessage extends AsyncTask<Integer, Integer, Integer> {
		private String message;

		@Override
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			final String ADDRESS = "address";
			final String DATE = "date";
			final String READ = "read";
			final String STATUS = "status";
			final String TYPE = "type";
			final String BODY = "body";
			int MESSAGE_TYPE_INBOX = 1;
			int MESSAGE_TYPE_SENT = 2;
			ContentValues values = new ContentValues();
			/* 手机号 */
			values.put(ADDRESS, "400888666");// 发送者
			/* 时间 */
			values.put(DATE, "1281403142857");
			values.put(READ, 0);// 未读
			values.put(STATUS, -1);
			/* 类型1为收件箱，2为发件箱 */
			values.put(TYPE, 1);
			/* 短信体内容 */
			values.put(BODY, "您本次操作的验证码是：234456,请勿将验证码告知他人[Life Track]");
			/* 插入数据库操作 */
			Uri inserted = getContentResolver().insert(
					Uri.parse("content://sms"), values);

			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Toast.makeText(getApplicationContext(), "验证短信已发送给您,请注意查收",
					Toast.LENGTH_SHORT).show();
			btn_getPIN.setEnabled(false);
			btn_getPIN.setBackgroundResource(R.drawable.background_timer);

			runTimerTask();

		}

	}

	private void runTimerTask() {
		task = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() { // UI thread
					@Override
					public void run() {
						if (time <= 0) {
							// 当倒计时小余=0时记得还原图片，可以点击
							btn_getPIN.setEnabled(true);
							btn_getPIN
									.setBackgroundResource(R.drawable.btn_pin_selector);
							btn_getPIN
									.setTextColor(Color.parseColor("#454545"));
							btn_getPIN.setText("获取验证码");
							task.cancel();
						} else {
							btn_getPIN.setText(time + "秒后重试");
							btn_getPIN.setTextColor(Color.rgb(125, 125, 125));
						}
						time--;
					}
				});
			}
		};
		time = 60;
		timer.schedule(task, 0, 1000);
	}

	public void UploadUsrInfo(String name, String password) {

		String urltext = getString(R.string.DataPath) + "url.txt";
		UserService us = new UserService(LogonActivity.this);
		String URL = us.readurl(urltext);
		if (!URL.equals(null)) {
			String nameSpace = "http://tempuri.org/";
			String methodName = "Register";

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
						UserService uService = new UserService(
								getApplicationContext());
						UserUtils user = new UserUtils();
						user.setUsername(name);
						user.setPassword(password);
						uService.register(user);
						Intent intent = new Intent(LogonActivity.this,
								LoginActivity.class);
						startActivity(intent);
						LogonActivity.this.finish();
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	boolean status = false;
	/*
	 * private boolean getURL() { Builder builder = new
	 * AlertDialog.Builder(this); builder.setTitle("设置服务器IP地址"); View view =
	 * LayoutInflater.from(MainActivity.this).inflate( R.layout.editurl, null);
	 * builder.setView(view); final EditText url_part1 = (EditText)
	 * view.findViewById(R.id.editURL1); final EditText url_part2 = (EditText)
	 * view.findViewById(R.id.editURL2); final EditText url_part3 = (EditText)
	 * view.findViewById(R.id.editURL3); final EditText url_part4 = (EditText)
	 * view.findViewById(R.id.editURL4); final EditText url_port = (EditText)
	 * view.findViewById(R.id.etPort); builder.setPositiveButton("确定", new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) { //
	 * TODO Auto-generated method stub final String url1 =
	 * url_part1.getText().toString(); final String url2 =
	 * url_part2.getText().toString(); final String url3 =
	 * url_part3.getText().toString(); final String url4 =
	 * url_part4.getText().toString(); final String port =
	 * url_port.getText().toString(); if ((url1 != "") && (url2 != "") && (url3
	 * != "") && (url4 != "") && port != "") { String url = url1 + "." + url2 +
	 * "." + url3 + "." + url4; URL = "http://" + url +
	 * "/WebSite1/WebService.asmx"; Toast.makeText(MainActivity.this, HOST,
	 * Toast.LENGTH_LONG) .show(); status = true;
	 * 
	 * } } }); builder.setNegativeButton("取消", new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * 
	 * dialog.dismiss(); } }); builder.create().show(); return status; }
	 */
	// public class UploadTask extends AsyncTask<Integer, Integer, String>{
	//
	//
	// @Override
	// protected String doInBackground(Integer... params) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	// }
}
