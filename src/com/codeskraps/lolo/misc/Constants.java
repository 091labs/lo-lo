package com.codeskraps.lolo.misc;

public class Constants {

	private Constants() {}

	public static final String LOLO_URL = "http://scruffy.091labs.com/lolo/json/status.json";
	public static final String LOLO_RSS = "http://091labs.com/feed/";

	public static final String FORCE_WIDGET_UPDATE = "com.codeskraps.lolo.FORCE_WIDGET_UPDATE";
	public static final String BROADCAST_RECEIVER = "com.codeskraps.lolo.BROADCAST_RECEIVER";
	public static final String CONFIGURE_ACTION = "android.appwidget.action.APPWIDGET_CONFIGURE";

	public static final String ONCLICK = "lstOnClick";
	public static final String EURL = "eURL";
	public static final String LAST_SYNC = "sync";
	public static final String SHOW_SYNC = "chkSync";
	public static final String HOUR24 = "chk24";
	public static final String INTERVAL = "lstInterval";
	public static final String WORDPRESS_ACOUNT = "prefWordPressAccount";
	public static final String TWITTER_ACCOUNT = "prefTwitterAccount";
	public static final String ABOUT = "prefAbout";
	public static final String FIRST_LAUNCH = "firstLaunch";
	public static final String RSS_TITLE = "rsstitle";
	public static final String ACCESS_TOKEN = "accestoken";
	public static final String ACCESS_SECRET = "accesssecret";

	public enum LOLO {
		ON, OFF, NULL;

		public String toString() {
			switch (this) {
			case ON:
				return "on";
			case OFF:
				return "off";
			default:
				return "no signal";
			}
		}
	}
}
