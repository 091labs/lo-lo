/*
 * lo-lo Copyright (C) 091 Labs 2012 info@091labs.com
 *
 * lo-lo is free software: you can
 * redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later
 * version.

 * lo-lo is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU
 * General Public License along with this program.
 * If not, see http://www.gnu.org/licenses.
 */

package com.codeskraps.lolo.home;

import com.codeskraps.lolo.BuildConfig;
import com.codeskraps.lolo.R;
import com.codeskraps.lolo.misc.Constants;
import com.codeskraps.lolo.misc.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class LoloProvider extends AppWidgetProvider {
	private static final String TAG = LoloProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onUpdate");
		updateWidget(context, appWidgetManager, appWidgetIds);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");
		final String action = intent.getAction();
		Log.d(TAG, "Action: " + action);
		if (action.equals(Constants.FORCE_WIDGET_UPDATE)) {
			ComponentName thisWidget = new ComponentName(context, LoloProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

			updateWidget(context, appWidgetManager, appWidgetIds);
		}
		super.onReceive(context, intent);
	}

	private void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		if (BuildConfig.DEBUG) Log.d(TAG, "updateWidget");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		
		if (appWidgetIds.length > 0) {
			for (int appWidgetId : appWidgetIds) {
				remoteViews.setOnClickPendingIntent(R.id.imgLolo,
						Utils.getOnTouchIntent(context));
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		}

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(Constants.BROADCAST_RECEIVER);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);

		String intervalString = prefs.getString(Constants.INTERVAL, "1");
		int intervalEntry = Integer.parseInt(intervalString);

//		@formatter:off
		int interval = 0;
		switch(intervalEntry){
		case 0: interval = 5; break;
		case 1: interval = 15; break;
		case 2: interval = 30; break;
		case 3: interval = 60; break;
		}
//		@formatter:on

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000 * 60
				* interval, 1000 * 60 * interval, pi);

		context.sendBroadcast(broadcastIntent);
	}
}
