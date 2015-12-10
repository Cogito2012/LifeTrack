package com.example.lifetrack.discovery;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @name ClientApi
 * @Descripation ����һ�����������������<br>
 *               1��<br>
 *               2��<br>
 *               3��<br>
 * @author ��۾�
 * @date 2014-10-22
 * @version 1.0
 */
public class ClientApi {
	private static String startId;

	public ClientApi() {
		// TODO Auto-generated constructor stub
	}

	public static JSONObject ParseJson(final String path, final String encode) {
		// TODO Auto-generated method stub
		HttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		// HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		// HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpPost httpPost = new HttpPost(path);
		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(httpResponse.getEntity(),
						encode);
				JSONObject jsonObject = new JSONObject(result);
				return jsonObject;
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return null;

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return null;

	}

	/**
	 * @param Url
	 *            ���ص�Url
	 * @return
	 */
	public static ArrayList<DiscoveryData> getJingXuanData(String Url) {
		ArrayList<DiscoveryData> list = new ArrayList<DiscoveryData>();
		JSONObject json = ParseJson(Url, "utf-8");

		if (json == null) {
			return null;
		} else {
			try {

				JSONArray Data = json.getJSONObject("obj").getJSONArray("list");
				
				for (int i = 0; i < Data.length(); i++) {
					System.out.println("------->"+i);
					JSONObject data = Data.getJSONObject(i);
					DiscoveryData jingXuanData = new DiscoveryData();
					jingXuanData.setId(data.optString("id"));
					JSONObject element = data.getJSONObject("tour");
					jingXuanData.setTitle(element.optString("title"));
					jingXuanData.setPubdate(element.optString("startdate"));
					jingXuanData.setPictureCount(element.optString("cntP"));
					jingXuanData.setImage(element.optString("coverpic"));
					jingXuanData.setViewCount(element.optString("pcolor"));
					jingXuanData.setFavoriteCount(element.getString("likeCnt"));
					jingXuanData.setViewCount(element.optString("viewCnt"));
					jingXuanData.setForeword(element.optString("foreword"));
					UserInfo userInfo = new UserInfo();
					JSONObject owner = element.optJSONObject("owner");
					userInfo.setUsername(owner.optString("username"));
					userInfo.setNickname(owner.optString("nickname"));
					userInfo.setUserId(owner.optString("userid"));
					userInfo.setAvatar(owner.getString("avatar"));
					jingXuanData.setUserInfo(userInfo);
					JSONArray dispcitys = element.getJSONArray("dispCities");
					String[] citys = new String[dispcitys.length()];
					for (int j = 0; j < dispcitys.length(); j++) {

						citys[j] = dispcitys.optString(j);
					}
					jingXuanData.setDispCities(citys);
					jingXuanData.setCmtCount(element.getString("cntcmt"));
					// System.out.println("----->"+jingXuanData.getDispCities().length);
					/*JSONArray cmt = element.optJSONArray("cmt");
					Comment[] comments = new Comment[cmt.length()];
					if (cmt!=null) {
						
						for (int j = 0; j < cmt.length(); j++) {
							JSONObject cmtdata = cmt.getJSONObject(i);
							Comment comment = new Comment();
							UserInfo uInfo = new UserInfo();
							JSONObject user = cmtdata.getJSONObject("user");
							uInfo.setAvatar(user.optString("avatar"));
							uInfo.setNickname(user.optString("username"));
							uInfo.setNickname(user.optString("nickname"));
							comment.setUserInfo(uInfo);
							comment.setContent(cmtdata.optString("words"));
							comment.setLike(cmtdata.optBoolean("isLiked"));
							comments[j] = comment;
						}
					}
					jingXuanData.setComments(comments);*/
					jingXuanData.setTourId(element.optString("id"));
					list.add(jingXuanData);
					if (i == Data.length() - 1) {
						startId = jingXuanData.getId();
					}

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("------->"+list.size());
			return list;

		}

	}

	public static String getStartId() {

		return startId;
	}

	
	public static ArrayList<DiscoveryData> getTourZhuanti(String searchId){
		ArrayList<DiscoveryData> list = new ArrayList<DiscoveryData>();
		String Url = "http://app.117go.com/demo27/php/searchAction.php?submit=getSearchTours&searchid="+searchId+"&startId=0&length=20&fetchNewer=1&vc=wandoujia&vd=80f117eb4244b778&v=a5.0.6";
		JSONObject json = ParseJson(Url, "utf-8");
		if (json == null) {
			return null;
		} else {
			try {

				JSONArray Data = json.getJSONObject("obj").getJSONArray("items");
				
				for (int i = 0; i < Data.length(); i++) {
					System.out.println("------->"+i);
					JSONObject data = Data.getJSONObject(i);
					DiscoveryData jingXuanData = new DiscoveryData();
					jingXuanData.setId(data.optString("id"));
					JSONObject element = data.getJSONObject("tour");
					jingXuanData.setTitle(element.optString("title"));
					jingXuanData.setPubdate(element.optString("startdate"));
					jingXuanData.setPictureCount(element.optString("cntP"));
					jingXuanData.setImage(element.optString("coverpic"));
					jingXuanData.setViewCount(element.optString("pcolor"));
					jingXuanData.setFavoriteCount(element.getString("likeCnt"));
					jingXuanData.setViewCount(element.optString("viewCnt"));
					jingXuanData.setForeword(element.optString("foreword"));
					UserInfo userInfo = new UserInfo();
					JSONObject owner = element.optJSONObject("owner");
					userInfo.setUsername(owner.optString("username"));
					userInfo.setNickname(owner.optString("nickname"));
					userInfo.setUserId(owner.optString("userid"));
					userInfo.setAvatar(owner.getString("avatar"));
					jingXuanData.setUserInfo(userInfo);
					JSONArray dispcitys = element.getJSONArray("dispCities");
					String[] citys = new String[dispcitys.length()];
					for (int j = 0; j < dispcitys.length(); j++) {

						citys[j] = dispcitys.optString(j);
					}
					jingXuanData.setDispCities(citys);
					jingXuanData.setCmtCount(element.getString("cntcmt"));
					// System.out.println("----->"+jingXuanData.getDispCities().length);
					/*JSONArray cmt = element.optJSONArray("cmt");
					Comment[] comments = new Comment[cmt.length()];
					if (cmt!=null) {
						
						for (int j = 0; j < cmt.length(); j++) {
							JSONObject cmtdata = cmt.getJSONObject(i);
							Comment comment = new Comment();
							UserInfo uInfo = new UserInfo();
							JSONObject user = cmtdata.getJSONObject("user");
							uInfo.setAvatar(user.optString("avatar"));
							uInfo.setNickname(user.optString("username"));
							uInfo.setNickname(user.optString("nickname"));
							comment.setUserInfo(uInfo);
							comment.setContent(cmtdata.optString("words"));
							comment.setLike(cmtdata.optBoolean("isLiked"));
							comments[j] = comment;
						}
					}
					jingXuanData.setComments(comments);*/
					jingXuanData.setTourId(element.optString("id"));
					list.add(jingXuanData);
					if (i == Data.length() - 1) {
						startId = jingXuanData.getId();
					}

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("------->"+list.size());
			return list;

		}

		
	}
	
}
