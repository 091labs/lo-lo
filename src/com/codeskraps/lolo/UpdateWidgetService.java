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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.codeskraps.lolo.RSSXmlParser.Entry;

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
		if (BuildConfig.DEBUG == true)
			Log.d(TAG, "onStartCommand");

		UpdateWidgetService.intent = intent;
		UpdateWidgetService.context = getApplicationContext();

		handler = new Handler();

		if (Utils.isNetworkAvailable(getApplicationContext())) {
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
			} else
				Log.d(TAG, "No widgets installed");

			downloadThread = new MyThread();
			downloadThread.start();

			// new DownloadXmlTask().execute();
		} else {
			Log.d(TAG, "No network connection");
		}

		stopSelf();

		return super.onStartCommand(intent, flags, startId);
	}

	static private class MyThread extends Thread {
		@Override
		public void run() {
			try {
				lolo = Utils.getLolo();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} finally {
				handler.post(new MyRunnable());
			}
		}
	}

	static private class MyRunnable implements Runnable {
		public void run() {
			ComponentName provider = new ComponentName(context, LoloProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgets = appWidgetManager.getAppWidgetIds(provider);

			if (appWidgets.length > 0) {
				for (int widgetId : appWidgets) {

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

					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(context);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(PrefsActivity.LAST_SYNC, DateFormat.getDateTimeInstance()
							.format(c.getTime()));
					editor.commit();

					boolean showSync = prefs.getBoolean(PrefsActivity.SHOW_SYNC, true);
					if (showSync) {
						boolean hour24 = prefs.getBoolean(PrefsActivity.HOUR24, true);
						String hours = new String();
						if (hour24) {
							hours = new String("");
							if (c.get(Calendar.HOUR_OF_DAY) < 10)
								hours += "0";
							hours += c.get(Calendar.HOUR_OF_DAY);
						} else {
							hours = new String("");
							if (c.get(Calendar.HOUR) < 10)
								hours += "0";
							hours += c.get(Calendar.HOUR);
						}

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

	// Implementation of AsyncTask used to download XML feed from
	// stackoverflow.com.
	private class DownloadXmlTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork("http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest");
			} catch (IOException e) {
				return e.getMessage();
			} catch (XmlPullParserException e) {
				return e.getMessage();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, result);
		}
	}

	// Uploads XML from stackoverflow.com, parses it, and combines it with
	// HTML markup. Returns HTML string.
	private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
		InputStream stream = null;
		RSSXmlParser rssXmlParser = new RSSXmlParser();
		List<Entry> entries = null;

		try {
			stream = downloadUrl(urlString);
			entries = rssXmlParser.parse(stream);
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		// Each Entry object represents a single post in the XML feed.
		// This section processes the entries list to combine each entry with
		// HTML markup.
		// Each entry is displayed in the UI as a link that optionally includes
		// a text summary.
		return entries.get(0).title;
	}

	// Given a string representation of a URL, sets up a connection and gets
	// an input stream.
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}
}