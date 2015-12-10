package com.example.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

import com.example.lifetrack.MainActivity;

/**
 * 自定义进度条消息对话框类
 * @author wtbao
 *
 */
public class DialogUtil {
	public static ProgressDialog MyDialog;
	public static boolean yesorno=false;
	// 定义�?��显示消息的对话框
    public static void showDialog(final Context ctx
        , String msg , boolean closeSelf)
    {
        // 创建�?��AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
            .setMessage(msg).setCancelable(false);
        if(closeSelf)
        {
            builder.setPositiveButton("确定", new OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    // 结束当前Activity
                    ((Activity)ctx).finish();
                }
            });        
        }
        else
        {
            builder.setPositiveButton("好哒", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub;
					DialogUtil.yesorno=true;
					showPrDialog(ctx,"稍等片刻就好");
					
	//				MainActivity.uploadall(ctx);
					
				}});
            builder.setNegativeButton("不要", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					DialogUtil.yesorno=false;
					
				}});
        }
        builder.create().show();
    }    
    // 定义�?��显示指定组件的对话框
    public static void showDialog(Context ctx , View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
            .setView(view).setCancelable(false)
            .setPositiveButton("确定", null);
        builder.create()
            .show();
    }   
    // 定义进度�?
    public static void showPrDialog(Context ctx , String msg)
    {
    	MyDialog = ProgressDialog.show(ctx, "(｡^_^�?ﾉ♥" , msg, true);
    }
}
