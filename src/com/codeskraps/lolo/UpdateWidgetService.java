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

package com.codeskraps.lolo;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	private static final String TAG = UpdateWidgetService.class.getSimpleName();

	private static Context context = null;
	private static Intent intent = null;
	private static Handler handler;
	private static boolean lolo;
	private Thread downloadThread;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		UpdateWidgetService.context = getApplicationContext();
		UpdateWidgetService.intent = intent;
		handler = new Handler();

		if (Utils.isNetworkAvailable(getApplicationContext())) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context
					.getApplicationContext());

			int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

			if (appWidgetIds.length > 0) {
				for (int widgetId : appWidgetIds) {
					RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
							R.layout.widget);
					remoteViews.setViewVisibility(R.id.prgBar, View.VISIBLE);
					appWidgetManager.updateAppWidget(widgetId, remoteViews);
				}
			} else {
				Log.d(TAG, "No widgets installed");
			}

			downloadThread = new MyThread();
			downloadThread.start();
		} else {
			Log.d(TAG, "No network connection");
		}

		stopSelf();

		return super.onStartCommand(intent, flags, startId);
	}

	static public class MyThread extends Thread {
		@Override
		public void run() {
			try {
				lolo = Utils.getLolo();
				handler.post(new MyRunnable());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

	static private class MyRunnable implements Runnable {
		public void run() {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context
					.getApplicationContext());

			int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

			if (appWidgetIds.length > 0) {
				for (int widgetId : appWidgetIds) {

					RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
							R.layout.widget);

					if (lolo) {
						remoteViews.setImageViewResource(R.id.imgLolo, R.drawable.open);
						Log.d(TAG, "The labs is open");
					} else {
						remoteViews.setImageViewResource(R.id.imgLolo, R.drawable.closed);
						Log.d(TAG, "The labs is close");
					}

					Calendar c = Calendar.getInstance();
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(PrefsActivity.LAST_SYNC, DateFormat.getDateTimeInstance()
							.format(c.getTime()));
					editor.commit();

					boolean showSync = prefs.getBoolean(PrefsActivity.SHOW_SYNC, true);
					if (showSync) {
						String hours = new String("");
						if (c.get(Calendar.HOUR_OF_DAY) < 10)
							hours += "0";
						hours += c.get(Calendar.HOUR_OF_DAY);

						String minutes = new String("");
						if (c.get(Calendar.MINUTE) < 10)
							minutes += "0";
						minutes += c.get(Calendar.MINUTE);

						String lastSync = String.format("%s:%s\n", hours, minutes);
						Log.d(TAG, "lastSnync:" + lastSync);
						remoteViews.setTextViewText(R.id.txtSync, lastSync);
						remoteViews.setViewVisibility(R.id.txtSync, View.VISIBLE);
					} else {
						remoteViews.setViewVisibility(R.id.txtSync, View.GONE);
					}
					
					remoteViews.setViewVisibility(R.id.prgBar, View.GONE);
					
					appWidgetManager.updateAppWidget(widgetId, remoteViews);
				}
			} else {
				Log.d(TAG, "No widgets installed");
			}
		}
	}
}