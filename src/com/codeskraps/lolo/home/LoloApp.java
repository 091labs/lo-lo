package com.codeskraps.lolo.home;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import com.codeskraps.lolo.twitter.TwitterService;

import android.app.Application;
import android.content.Intent;

@ReportsCrashes(formKey = "dC1lU3BVVGh4ejlQSTJta05pNXBPQVE6MQ")
public class LoloApp extends Application {

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();

		startService(new Intent(this, TwitterService.class));
	}
}
