package com.example.lifetrack;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lifetrack.R;
import com.example.utils.CircularImage;

public class MessageAdapter extends BaseAdapter{


	private Context mContext;
	private ArrayList<Map<String,String[]>> mList;


	
	public MessageAdapter(Context context,ArrayList<Map<String, String[]>> mlistHashMaps) {

		this.mContext = context;
		this.mList = mlistHashMaps;
		
		
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public View getView(final int arg0, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		 final ViewHolder holder;
		if (convertView !=null) {
			holder = (ViewHolder) convertView.getTag();
		} else {
			
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, arg2, false);
			
			holder.iv = (CircularImage) convertView.findViewById(R.id.info_head);
			holder.name_tv=(TextView)convertView.findViewById(R.id.info_name);
		    holder.time_tv=(TextView)convertView.findViewById(R.id.info_time);
		    holder.tall_tv=(TextView)convertView.findViewById(R.id.info_last);
		    holder.num_tv=(TextView)convertView.findViewById(R.id.info_num_tv);
			convertView.setTag(holder);
		}
		
		String[] strItem = mList.get(arg0).get("msg_id");
		int resID = mContext.getResources().getIdentifier(strItem[0], "drawable", "com.example.lifetrack");
		holder.iv.setImageResource(resID);
		holder.name_tv.setText(strItem[1]);
		holder.tall_tv.setText(strItem[2]);
		holder.time_tv.setText(strItem[3]);
		holder.num_tv.setText(strItem[4]);
		return convertView;
	}

	public static class ViewHolder {

		 ImageView iv;				
		 TextView name_tv;
         TextView time_tv;
         TextView tall_tv;
         TextView num_tv;
	}
	
	// 重新加载数据
	public void refreshData(ArrayList<Map<String, String[]>> listItems) {
		this.mList = listItems;
		notifyDataSetChanged();
	}
	
	// 重新加载数据
	public void norefreshData(String loginid) {
		
		if (this.mList!=null) {
			this.mList.clear();
		}
		notifyDataSetChanged();
	}
		
}