package com.codeskraps.lolo.home;

public class TweetItem {

	private String id;
	private String created;
	private String ImgUser;
	private String text;
	private String txtUser;
	private String txtScreenName;

	public TweetItem(String created, String ImgUser, String text, String txtUser,
			String txtScreenName) {
		this.created = created;
		this.ImgUser = ImgUser;
		this.text = text;
		this.txtUser = txtUser;
		this.txtScreenName = txtScreenName;
	}

	public TweetItem(String id, String created, String ImgUser, String text, String txtUser,
			String txtScreenName) {
		this.created = created;
		this.id = id;
		this.ImgUser = ImgUser;
		this.text = text;
		this.txtUser = txtUser;
		this.txtScreenName = txtScreenName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getImgUser() {
		return ImgUser;
	}

	public void setImgUser(String imgUser) {
		ImgUser = imgUser;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTxtUser() {
		return txtUser;
	}

	public void setTxtUser(String txtUser) {
		this.txtUser = txtUser;
	}

	public String getTxtScreenName() {
		return txtScreenName;
	}

	public void setTxtScreenName(String txtScreenName) {
		this.txtScreenName = txtScreenName;
	}
}
