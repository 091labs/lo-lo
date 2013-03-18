package com.codeskraps.lolo.home;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.codeskraps.lolo.misc.Constants;
import com.codeskraps.lolo.twitter.TwitterService;

@ReportsCrashes(formKey = "dC1lU3BVVGh4ejlQSTJta05pNXBPQVE6MQ")
public class LoloApp extends Application {

	private DataBase data = null;

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();

		data = new DataBase(this);

		setTwitterAlarm();
	}

	public void setTwitterAlarm() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String intervalTwitter = prefs.getString(Constants.TWITTER_INTERVAL, "2");
		int intTwitter = Integer.parseInt(intervalTwitter);

		long interval = 0;
		// @formatter:off
		switch(intTwitter){
		case 0: interval = AlarmManager.INTERVAL_HALF_HOUR / 2; break;
		case 1: interval = AlarmManager.INTERVAL_HALF_HOUR; break;
		case 2: interval = AlarmManager.INTERVAL_HOUR; break;
		case 3: interval = AlarmManager.INTERVAL_HOUR * 2; break;
		}
		// @formatter:on

		Intent intent = new Intent(this, TwitterService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.RTC, 0, interval, pendingIntent);
	}

	public DataBase getDataBase() {
		return data;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		data.close();
	}
}
