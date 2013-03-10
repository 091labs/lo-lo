package com.codeskraps.lolo.twitter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeskraps.lolo.R;
import com.codeskraps.lolo.misc.Constants;
import com.google.gson.Gson;

public class TwitterAccountActivity extends Activity {
	private static final String TAG = TwitterAccountActivity.class.getSimpleName();

	private OAuthService service = null;
	private Bitmap bm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.twitter_account);
	}

	@Override
	protected void onResume() {
		super.onResume();

		service = new ServiceBuilder().provider(TwitterApi.class).apiKey("Z4lEh5rSu0rV3fXt37gw7A")
				.apiSecret("vLLTqO311ZhlVXhl1GaB72DnIwdCOPwzeozNRWy3I").build();

		new GetTask().execute();
	}

	private class GetTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(Boolean.TRUE);
		}

		@Override
		protected String doInBackground(Void... params) {

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplication());
			String token = prefs.getString(Constants.ACCESS_TOKEN, null);
			String secret = prefs.getString(Constants.ACCESS_SECRET, null);

			Token accessToken = new Token(token, secret);

			OAuthRequest request = new OAuthRequest(Verb.GET,
					"https://api.twitter.com/1/account/verify_credentials.json");
			service.signRequest(accessToken, request);
			Response response = request.send();
			Log.d(TAG, response.getBody());

			Map<String, Object> JResult = new HashMap<String, Object>();
			JResult = new Gson().fromJson(response.getBody(), JResult.getClass());

			try {
				URL url = new URL((String) JResult.get("profile_image_url"));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				bm = BitmapFactory.decodeStream(is);
			} catch (Exception e) {
				Log.i(TAG, "Handled: image - " + e.getMessage(), e);
			}

			return response.getBody();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			setProgressBarIndeterminateVisibility(Boolean.FALSE);

			((ImageView) findViewById(R.id.tac_img_user)).setImageBitmap(bm);

			Map<String, Object> JResult = new HashMap<String, Object>();
			JResult = new Gson().fromJson(result, JResult.getClass());

			TextView user = (TextView) findViewById(R.id.tac_txt_name);
			user.setText((String) JResult.get("name") + "\n" + (String) JResult.get("location"));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
}
