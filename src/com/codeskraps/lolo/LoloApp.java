package com.codeskraps.lolo;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "dC1lU3BVVGh4ejlQSTJta05pNXBPQVE6MQ")
public class LoloApp extends Application {

	@Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
    }
}
