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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class LoloProvider extends AppWidgetProvider {
	private static final String TAG = LoloProvider.class.getSimpleName();
	public static final String FORCE_WIDGET_UPDATE = "com.codeskraps.lolo.FORCE_WIDGET_UPDATE";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate");
		updateWidget(context);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		final String action = intent.getAction();

		if (action.equals(FORCE_WIDGET_UPDATE))
			updateWidget(context);

		super.onReceive(context, intent);
	}

	public void updateWidget(Context context) {
		Log.d(TAG, "updateWidget");
		ComponentName thisWidget = new ComponentName(context, LoloProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		updateWidget(context, appWidgetManager, appWidgetIds);
	}

	private void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "updateWidget2");
		final int N = appWidgetIds.length;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		Intent serviceIntent = new Intent(context, UpdateWidgetService.class);
		serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

		Intent intent = null;
		PendingIntent pendingIntent = null;

		int onClick = Integer.parseInt(prefs.getString(PrefsActivity.ONCLICK, "0"));
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			switch (onClick) {
			case 0:
				intent = new Intent("com.codeskraps.lol.DO_NOTHING");
				pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
				break;
			case 1:
				pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
				break;
			case 2:
				intent = new Intent(context, PrefsActivity.class);
				pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
				break;
			case 3:
				intent = new Intent(Intent.ACTION_VIEW);
				String url = prefs.getString(PrefsActivity.EURL,
						context.getString(R.string.prefsURL_default));
				if (!url.startsWith("http://"))
					url = "http://" + url;
				intent.setData(Uri.parse(url));
				pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
				break;
			}
			remoteViews.setOnClickPendingIntent(R.id.imgLolo, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}

		// Update the widgets via the service
		context.startService(serviceIntent);
	}
}
