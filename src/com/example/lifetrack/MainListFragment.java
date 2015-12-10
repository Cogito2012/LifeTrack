package com.example.lifetrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.utils.CircularImage;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * @author yangyu
 *	功能描述：列表Fragment，用来显示滑动菜单打开后的内容
 */
public class MainListFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.menu_left_fragment, container, false);
		CircularImage headimg = (CircularImage) view.findViewById(R.id.btn_head_avater);
		headimg.setImageResource(R.drawable.teamhead);
		btnLogin = (TextView)view.findViewById(R.id.btn_head_login);
		btnLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(),LoginActivity.class);
				startActivity(intent);
			}
		});
		btnExit = (TextView)view.findViewById(R.id.tv_exit);
		btnExit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().finish();
				System.exit(0);
			}
		});
		return view;
	}

	private ListView listView;
	private TextView btnLogin;
	private TextView btnExit;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView = getListView();
		SimpleAdapter adapter = new SimpleAdapter(
				getActivity(), 
				this.getData(),
				R.layout.menu_left_list,
				new String[]{"image","text"},
				new int[]{R.id.menu_list_image, R.id.menu_list_text});
		listView.setAdapter(adapter);
		listView.setDivider(null);
		listView.setDividerHeight(0);
		listView.setOnItemClickListener(new MyItemClickListener());
		
		
		SharedPreferences sp = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		String name = sp.getString("USER_NAME", "");
		if (!name.equals("")){
			btnLogin.setText(name);
		}
		
		
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int[] images = {
				R.drawable.menu_home,
				R.drawable.menu_trace,
				R.drawable.menu_collect,
				R.drawable.menu_message,
				R.drawable.menu_setting
				};
		String[] texts = {
				getResources().getString(R.string.primary_page),
				getResources().getString(R.string.discovery_page),
				getResources().getString(R.string.collection_page),
				getResources().getString(R.string.message_page),
				getResources().getString(R.string.more_page)
		};
		
		
		
		for (int i = 0; i < texts.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("image", images[i]);
			map.put("text", texts[i]);
			list.add(map);
		}
		return list;
	}
	
	public View getViewByPosition(int pos, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition ) {
		    return listView.getAdapter().getView(pos, null, listView);
		} else {
		    final int childIndex = pos - firstListItemPosition;
		    return listView.getChildAt(childIndex);
		}
	} 
	
	private class MyItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			Fragment fragment = null;
			switch (position) {  //左侧侧边栏向各个页面的跳转
			case 0:
				startActivity(new Intent(getActivity(), MainActivity.class));   //首页
				break;
			case 1:
				startActivity(new Intent(getActivity(), DiscoveryActivity.class));  //发现
				break;
			case 2:
				startActivity(new Intent(getActivity(), CollectionActivity.class));  //收藏
				break;
			case 3:
				startActivity(new Intent(getActivity(), MessageActivity.class));  //消息
				break;
			case 4:
				startActivity(new Intent(getActivity(), MoreActivity.class));  //更多
				break;
			default:
				break;
			}
		}
	}
}
