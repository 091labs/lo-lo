package com.codeskraps.lolo.home;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Calendar;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.codeskraps.lolo.BuildConfig;
import com.codeskraps.lolo.R;
import com.codeskraps.lolo.misc.Constants;
import com.codeskraps.lolo.misc.Constants.LOLO;
import com.codeskraps.lolo.misc.Utils;

public class UpdateWidgetReceiver extends BroadcastReceiver {
	private static final String TAG = UpdateWidgetReceiver.class.getSimpleName();

	private static Context context = null;
	private static Handler handler = null;
	private static LOLO lolo;
	private static String[] loloRSS;
	private Thread downloadThread = null;
	private static SharedPreferences prefs = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onStartCommand");

		UpdateWidgetReceiver.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		lolo = LOLO.NULL;
		loloRSS = null;
		handler = new Handler();

		if (Utils.isNetworkAvailable(context)) {
			ComponentName provider = new ComponentName(context, LoloProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgets = appWidgetManager.getAppWidgetIds(provider);

			if (appWidgets.length > 0) {
				for (int widgetId : appWidgets) {
					RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
							R.layout.widget);
					remoteViews.setViewVisibility(R.id.prgBar, View.VISIBLE);
					appWidgetManager.updateAppWidget(widgetId, remoteViews);
				}
			} else if (BuildConfig.DEBUG) Log.d(TAG, "No widgets installed");

			downloadThread = new MyThread();
			downloadThread.start();

			// new DownloadXmlTask().execute();
		} else {
			if (BuildConfig.DEBUG) Log.d(TAG, "No network connection");
			new MyRunnable();
		}
	}

	static private class MyThread extends Thread {
		@Override
		public void run() {
			try {
				lolo = Utils.getLolo();
			} catch (Exception e) {
				Log.i(TAG, "Handled:" + e, e);
			} finally {
				handler.post(new MyRunnable());
			}
		}
	}

	static private class MyRunnable implements Runnable {
		public void run() {
			ComponentName provider = new ComponentName(context, LoloProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);

			if (appWidgetIds.length > 0) {
				for (int appWidgetId : appWidgetIds) {

					switch (lolo) {
					case ON:
						remoteViews.setImageViewResource(R.id.imgLolo, R.drawable.open);
						Log.d(TAG, "The labs is open");
						break;
					case OFF:
						remoteViews.setImageViewResource(R.id.imgLolo, R.drawable.closed);
						Log.d(TAG, "The labs is close");
						break;
					default:
						remoteViews.setImageViewResource(R.id.imgLolo, R.drawable._null);
						Log.d(TAG, "The labs is null");
					}

					Calendar c = Calendar.getInstance();

					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(Constants.LAST_SYNC,
							DateFormat.getDateTimeInstance().format(c.getTime()));
					editor.commit();

					boolean showSync = prefs.getBoolean(Constants.SHOW_SYNC, true);
					if (showSync) {
						boolean hour24 = prefs.getBoolean(Constants.HOUR24, true);
						String hours = new String("");
						if (hour24) {
							if (c.get(Calendar.HOUR_OF_DAY) < 10) hours += "0";
							hours += c.get(Calendar.HOUR_OF_DAY);
						} else {
							if (c.get(Calendar.HOUR) < 10) hours += "0";
							hours += c.get(Calendar.HOUR);
						}

						String minutes = new String("");
						if (c.get(Calendar.MINUTE) < 10) minutes += "0";
						minutes += c.get(Calendar.MINUTE);

						String lastSync = String.format("%s:%s", hours, minutes);
						Log.d(TAG, "lastSnync:" + lastSync);
						remoteViews.setTextViewText(R.id.txtSync, lastSync);
						remoteViews.setViewVisibility(R.id.txtSync, View.VISIBLE);

					} else remoteViews.setViewVisibility(R.id.txtSync, View.GONE);

					if (loloRSS != null) remoteViews.setTextViewText(R.id.txtRSS, loloRSS[1]);

					remoteViews.setViewVisibility(R.id.prgBar, View.GONE);
					remoteViews.setOnClickPendingIntent(R.id.imgLolo,
							Utils.getOnTouchIntent(context));
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
				}
			} else if (BuildConfig.DEBUG) Log.d(TAG, "No widgets installed");
		}
	}
}