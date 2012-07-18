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
	private static final String URL = "http://091labs.com";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		updateWidget(context);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(FORCE_WIDGET_UPDATE))
			updateWidget(context);

		super.onReceive(context, intent);
	}
	
	public void updateWidget(Context context) {
        ComponentName thisWidget = new ComponentName(context, LoloProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        updateWidget(context, appWidgetManager, appWidgetIds);
    }

	private void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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
				intent.setData(Uri.parse(URL));
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
