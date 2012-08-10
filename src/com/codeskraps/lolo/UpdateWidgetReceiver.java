package com.codeskraps.lolo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

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

import com.codeskraps.lolo.RSSXmlParser.Entry;

public class UpdateWidgetReceiver extends BroadcastReceiver {
	private static final String TAG = UpdateWidgetReceiver.class.getSimpleName();

	private static Context context = null;
	private static Handler handler;
	private static boolean lolo;
	private Thread downloadThread;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onStartCommand");

		UpdateWidgetReceiver.context = context;

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
			} else Log.d(TAG, "No widgets installed");

			downloadThread = new MyThread();
			downloadThread.start();

			// new DownloadXmlTask().execute();
		} else Log.d(TAG, "No network connection");
	}

	static private class MyThread extends Thread {
		@Override
		public void run() {
			try {
				lolo = Utils.getLolo();
			} catch (UnsupportedEncodingException e) {
				if (e != null) Log.e(TAG, e.getMessage());
			} catch (ClientProtocolException e) {
				if (e != null) Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				if (e != null) Log.e(TAG, e.getMessage());
			} catch (IllegalArgumentException e) {
				if (e != null) Log.e(TAG, e.getMessage());
			} catch (NullPointerException e) {
				if (e != null) Log.e(TAG, e.getMessage());
			} catch (JSONException e) {
				if (e != null) Log.e(TAG, e.getMessage());
			} finally {
				handler.post(new MyRunnable());
			}
		}
	}

	static private class MyRunnable implements Runnable {
		public void run() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			ComponentName provider = new ComponentName(context, LoloProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);

			if (appWidgetIds.length > 0) {
				for (int appWidgetId : appWidgetIds) {

					if (lolo) {
						remoteViews.setImageViewResource(R.id.imgLolo, R.drawable.open);
						Log.d(TAG, "The labs is open");
					} else {
						remoteViews.setImageViewResource(R.id.imgLolo, R.drawable.closed);
						Log.d(TAG, "The labs is close");
					}

					Calendar c = Calendar.getInstance();

					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(Constants.LAST_SYNC,
							DateFormat.getDateTimeInstance().format(c.getTime()));
					editor.commit();

					boolean showSync = prefs.getBoolean(Constants.SHOW_SYNC, true);
					if (showSync) {
						boolean hour24 = prefs.getBoolean(Constants.HOUR24, true);
						String hours = new String();
						if (hour24) {
							hours = new String("");
							if (c.get(Calendar.HOUR_OF_DAY) < 10) hours += "0";
							hours += c.get(Calendar.HOUR_OF_DAY);
						} else {
							hours = new String("");
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

					remoteViews.setViewVisibility(R.id.prgBar, View.GONE);
					remoteViews.setOnClickPendingIntent(R.id.imgLolo,
							Utils.getOnTouchIntent(context));
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
				}
			} else Log.d(TAG, "No widgets installed");
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
			if (stream != null) stream.close();
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