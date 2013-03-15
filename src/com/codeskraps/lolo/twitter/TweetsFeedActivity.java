package com.codeskraps.lolo.twitter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.codeskraps.lolo.R;
import com.codeskraps.lolo.home.PrefsActivity;
import com.codeskraps.lolo.misc.Constants;
import com.codeskraps.lolo.misc.ImageHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TweetsFeedActivity extends Activity {
	private static final String TAG = TweetsFeedActivity.class.getSimpleName();
	private static final String RESOURCE_URL = "https://api.twitter.com/1.1/statuses/home_timeline.json";

	private OAuthService service = null;
	private TweetsAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		service = new ServiceBuilder().provider(TwitterApi.class).apiKey("Z4lEh5rSu0rV3fXt37gw7A")
				.apiSecret("vLLTqO311ZhlVXhl1GaB72DnIwdCOPwzeozNRWy3I").build();

		setContentView(R.layout.tweets);

		adapter = new TweetsAdapter(this);

		ListView lstTweets = (ListView) findViewById(R.id.tws_lst);
		lstTweets.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new GetTimeline().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.men_tweet:
			startActivity(new Intent(this, TweetActivity.class));
			break;
		case R.id.men_settings:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		}
		return true;
	}

	private class GetTimeline extends AsyncTask<Void, Void, String> {

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

			setProgressBarIndeterminateVisibility(Boolean.FALSE);

			if (result != null) {
				try {
					Log.v(TAG, result);
					Type collectionType = new TypeToken<List<Map<String, Object>>>() {}.getType();
					List<Map<String, Object>> tweets = new Gson().fromJson(result, collectionType);
					adapter.setTweets(tweets);
					adapter.notifyDataSetChanged();
				} catch (Exception e) {
					Log.i(TAG, "Handle: onPostExecute - " + e, e);
					Log.i(TAG, "Handle: result:" + result);
				}
			}
		}
	}

	private class TweetsAdapter extends BaseAdapter {
		private List<Map<String, Object>> tweets = null;
		private LayoutInflater mInflater = null;

		public TweetsAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public void setTweets(List<Map<String, Object>> tweets) {
			this.tweets = tweets;
		}

		@Override
		public int getCount() {
			if (tweets == null || tweets.isEmpty()) return 0;
			return tweets.size();
		}

		@Override
		public Object getItem(int position) {
			return tweets.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vHolder = null;

			if (convertView != null) vHolder = (ViewHolder) convertView.getTag();
			else {
				convertView = (View) mInflater.inflate(R.layout.row_tweet, null);

				vHolder = new ViewHolder();
				vHolder.ImgUser = (ImageView) convertView.findViewById(R.id.tws_row_img_user);
				vHolder.text = (TextView) convertView.findViewById(R.id.tws_row_txt_text);
				vHolder.txtUser = (TextView) convertView.findViewById(R.id.tws_row_txt_user);

				convertView.setTag(vHolder);
				convertView.setId(position);
			}

			try {
				Map<String, Object> tweet = tweets.get(position);
				Map<String, Object> user = (Map<String, Object>) tweet.get("user");
				
				String url = (String) user.get("profile_image_url");
				ImageHelper.loadBitmap(getResources(), url, vHolder.ImgUser);
				vHolder.txtUser.setText((String) user.get("name"));
				vHolder.text.setText((String) tweet.get("text"));
			} catch (Exception e) {
				Log.i(TAG, "Handled: " + e, e);
			}
			return convertView;
		}
	}

	public static class ViewHolder {
		ImageView ImgUser;
		TextView text;
		TextView txtUser;
	}
}
