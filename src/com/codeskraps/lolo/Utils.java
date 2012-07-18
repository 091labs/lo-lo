package com.codeskraps.lolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utils {
	private static final String TAG = Utils.class.getSimpleName();
	private static final String URL = "http://scruffy.091labs.com/lolo/json/status.json";

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	public static boolean getLolo() throws IOException {
		long startTime = System.currentTimeMillis();
		Log.d(TAG, "download begining");
		Log.d(TAG, "download url:" + URL);

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(URL);
		HttpResponse response = client.execute(request);

		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity()
				.getContent(), "UTF-8"));
		String json = reader.readLine();
		Log.d(TAG, "json: " + json);
		JSONTokener tokener = new JSONTokener(json);

		String lolo = null;
		try {
			JSONObject finalResult = new JSONObject(tokener);
			lolo = finalResult.getString("open");
			Log.d(TAG, "lolo: " + lolo);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}

		Log.d(TAG, "download ready in"
				+ ((System.currentTimeMillis() - startTime) / 1000) + " sec");

		if (lolo.equals("true")) {
			return true;
		} else
			return false;
	}
}