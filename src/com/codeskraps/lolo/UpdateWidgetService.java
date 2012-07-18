package com.codeskraps.lolo;

import java.io.IOException;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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
					appWidgetManager.updateAppWidget(widgetId, remoteViews);
				}
			} else {
				Log.d(TAG, "No widgets installed");
			}
		}
	}
}