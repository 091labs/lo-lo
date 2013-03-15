package com.codeskraps.lolo.twitter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.codeskraps.lolo.misc.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TwitterService extends IntentService {
	private static final String TAG = TwitterService.class.getSimpleName();
	private static final String RESOURCE_URL = "https://api.twitter.com/1.1/statuses/home_timeline.json";

	private OAuthService service = null;

	public TwitterService() {
		super(TAG);
	}

	public TwitterService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		new GetTimeline().execute();
	}

	private class GetTimeline extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				service = new ServiceBuilder().provider(TwitterApi.class)
						.apiKey("Z4lEh5rSu0rV3fXt37gw7A")
						.apiSecret("vLLTqO311ZhlVXhl1GaB72DnIwdCOPwzeozNRWy3I").build();

				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplication());
				String token = prefs.getString(Constants.ACCESS_TOKEN, null);
				String secret = prefs.getString(Constants.ACCESS_SECRET, null);

				Token accessToken = new Token(token, secret);

				OAuthRequest request = new OAuthRequest(Verb.GET, RESOURCE_URL);
				service.signRequest(accessToken, request);
				return request.send().getBody();

			} catch (Exception e) {
				Log.i(TAG, "Handled: GetTimeline - " + e, e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new Gson();

			if (result != null) {
				Log.v(TAG, result);
				Type collectionType = new TypeToken<List<Map<String, Object>>>() {}.getType();
				List<Map<String, Object>> tweets = new Gson().fromJson(result, collectionType);
				for (Map m : tweets) {
					String blah = (String) m.get("text");
					Log.d(TAG, blah);
				}
			}
		}
	}
}
