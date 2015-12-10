package com.example.lifetrack;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 自定义适配器类
 * @author wtbao
 *
 */
public class GalleryAdapter extends BaseAdapter {

	Context mContext;
    LayoutInflater mInflater;
    HashMap<Integer, Boolean> mSelectMap;
    public GalleryAdapter(Context c, HashMap<Integer, Boolean> SelectMap) {
        mContext = c;
        mInflater = LayoutInflater.from(mContext);
        mSelectMap = SelectMap;
    }
    
	static class ViewHolder {
        ImageView mImg;
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 15;//先一次性设置15个image格子
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
//		ViewHolder holder;
		GridItem item;
        if (convertView == null) {
//            holder = new ViewHolder();
//            convertView = mInflater.inflate(R.layout.gridview_item, null);
//            holder.mImg = (ImageView) convertView.findViewById(R.id.mImage);

            item = new GridItem(mContext);
            item.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
        } else {
//            holder = (ViewHolder) convertView.getTag();
            item = (GridItem) convertView;
        }
//        convertView.setTag(holder);
        
 //       item.setImgResId((Integer) getItem(position));
		item.setChecked(mSelectMap.get(position) == null ? false
				: mSelectMap.get(position));
		return item;
//        return convertView;
	}
}
