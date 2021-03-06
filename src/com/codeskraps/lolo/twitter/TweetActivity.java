package com.codeskraps.lolo.twitter;

import java.lang.reflect.Type;
import java.util.List;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codeskraps.lolo.R;
import com.codeskraps.lolo.misc.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TweetActivity extends Activity implements OnClickListener {
	private static final String TAG = TweetActivity.class.getSimpleName();
	private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1/statuses/update.json";

	private OAuthService service = null;
	private EditText etxtTweet = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		service = new ServiceBuilder().provider(TwitterApi.class).apiKey("Z4lEh5rSu0rV3fXt37gw7A")
				.apiSecret("vLLTqO311ZhlVXhl1GaB72DnIwdCOPwzeozNRWy3I").build();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.tweet);

		etxtTweet = (EditText) findViewById(R.id.twe_etxt_data);
		TextView txtCount = (TextView) findViewById(R.id.twe_txt_count);
		txtCount.setText("0");
		etxtTweet.addTextChangedListener(new TextCount(txtCount));
		((Button) findViewById(R.id.twe_btn_submit)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		new PostTask().execute();
	}

	private class PostTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(Boolean.TRUE);
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplication());
				String token = prefs.getString(Constants.ACCESS_TOKEN, null);
				String secret = prefs.getString(Constants.ACCESS_SECRET, null);

				Token accessToken = new Token(token, secret);

				OAuthRequest request = new OAuthRequest(Verb.POST, PROTECTED_RESOURCE_URL);
				request.addBodyParameter("status", etxtTweet.getText().toString());
				service.signRequest(accessToken, request);
				Response response = request.send();
				Log.d(TAG, response.getBody());

				return response.getBody();

			} catch (Exception e) {
				Log.i(TAG, "Handled: posting - " + e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			setProgressBarIndeterminateVisibility(Boolean.FALSE);

			TextView txtFeedBack = (TextView) findViewById(R.id.twe_txt_feedback);
			String message = null;
			if (result != null && result.startsWith("{")) {
				Type collectionType = new TypeToken<Map<String, Object>>() {}.getType();
				Map<String, Object> json = new Gson().fromJson(result, collectionType);
				if (json.containsKey("error")) message = (String) json.get("error");
				else message = "Tweet posted succesfully!";
			} else {
				message = "Something has gone wrong!";
			}
			txtFeedBack.setText(message);
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
