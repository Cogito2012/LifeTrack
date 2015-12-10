package com.example.lifetrack.discovery;

import java.io.Serializable;

/**
 * @name Comment
 * @Descripation�������۵�ʵ����<br>
 *		1��<br>
 *		2��<br>
 *      3��<br>
 * @author ��۾�
 * @date 2014-10-23
 * @version 1.0
 */
public class Comment implements Serializable{
	private static final long serialVersionUID = 642225199212241050L;
	private String Content;
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	public UserInfo getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	private UserInfo userInfo;
	
	private boolean isLike;
	public boolean isLike() {
		return isLike;
	}
	public void setLike(boolean isLike) {
		this.isLike = isLike;
	}
	


}
