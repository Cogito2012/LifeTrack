package com.example.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.utils.UserUtils;

public class UserService {
	private DatabaseHelper dbHelper;

	public UserService(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	// 登录用
	public boolean login(String username, String password) {
		SQLiteDatabase sdb = dbHelper.getReadableDatabase();
		String sql = "select * from user where username=? and password=?";
		Cursor cursor = sdb.rawQuery(sql, new String[] { username, password });
		if (cursor.moveToFirst() == true) {
			cursor.close();
			return true;
		}
		return false;
	}

	// 注册用
	public boolean register(UserUtils user) {
		SQLiteDatabase sdb = dbHelper.getReadableDatabase();
		String sql = "insert into user(username,password) values(?,?)";
		Object obj[] = { user.getUsername(), user.getPassword() };
		sdb.execSQL(sql, obj);
		return true;
	}

	// 获取服务器地址
	public String readurl(String fileName) {
		String URL = null;
		String ip = null;
		String port = null;
		File urlfile = new File(fileName);
		if (urlfile.exists() == false) {
			return null;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(urlfile);
			DataInputStream dataIO = new DataInputStream(fis);
			StringBuffer sBuffer = new StringBuffer();
			String strLine = null;
			while ((strLine = dataIO.readLine()) != null) {
				sBuffer.append(strLine + "\n");
				String[] strSplit = strLine.split(":");
				ip = strSplit[0];
				port = strSplit[1];
			}
			dataIO.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!(ip.equals(null)) && !(port.equals(null))) {
			URL = "http://" + ip + ":" + port + "/WebSite1/WebService.asmx";
			return URL;
		} else {
			return null;
		}
	}
}
