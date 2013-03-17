package com.codeskraps.lolo.home;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;

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

		Intent intent = new Intent(this, TwitterService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.RTC, 0, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
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
