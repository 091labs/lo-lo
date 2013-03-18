package com.codeskraps.lolo.twitter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.codeskraps.lolo.R;
import com.codeskraps.lolo.home.AboutActivity;
import com.codeskraps.lolo.home.DataBase;
import com.codeskraps.lolo.home.LoloApp;
import com.codeskraps.lolo.home.PrefsActivity;
import com.codeskraps.lolo.home.TweetItem;
import com.codeskraps.lolo.misc.Constants;
import com.codeskraps.lolo.misc.ImageHelper;

public class TweetsFeedActivity extends Activity {
	private static final String TAG = TweetsFeedActivity.class.getSimpleName();

	private TweetsAdapter adapter = null;
	private BroadcastReceiver refreshReceiver = new RefreshReciever();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.tweets);

		adapter = new TweetsAdapter(this);

		ListView lstTweets = (ListView) findViewById(R.id.tws_lst);
		lstTweets.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new GetTimeline().execute();

		IntentFilter filter = new IntentFilter(Constants.ACTION_REFRESH);
		registerReceiver(refreshReceiver, filter);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean(Constants.FIRST_LAUNCH, true)) {
			startActivity(new Intent(this, AboutActivity.class));
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(Constants.FIRST_LAUNCH, false);
			editor.commit();
		}

		String token = prefs.getString(Constants.ACCESS_TOKEN, null);
		if (token != null) ((TextView) findViewById(R.id.tws_signin)).setVisibility(View.GONE);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(refreshReceiver);
	}

	private class RefreshReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			new GetTimeline().execute();
		}
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
		case R.id.men_refresh:
			setProgressBarIndeterminateVisibility(Boolean.TRUE);
			startService(new Intent(this, TwitterService.class));
			break;
		}
		return true;
	}

	private class GetTimeline extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(Boolean.TRUE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			Cursor cursor = null;
			try {
				LoloApp app = (LoloApp) getApplication();
				cursor = app.getDataBase().query(DataBase.DB_TABLE_TWITTER);
				startManagingCursor(cursor);

				final int createdColumnIndex = cursor.getColumnIndex(DataBase.C_TWEET_CREATED);
				final int imgColumnIndex = cursor.getColumnIndex(DataBase.C_TWEET_IMAGE);
				final int nameColumnIndex = cursor.getColumnIndex(DataBase.C_TWEET_NAME);
				final int screenColumnIndex = cursor.getColumnIndex(DataBase.C_TWEET_SCREEN);
				final int textColumnIndex = cursor.getColumnIndex(DataBase.C_TWEET_TEXT);

				Log.d(TAG, ("Got cursor with records: " + cursor.getCount()));

				String created, image, name, screen, text;
				Log.d(TAG, "tweets:" + cursor.getCount());
				ArrayList<TweetItem> tweets = new ArrayList<TweetItem>();
				while (cursor.moveToNext()) {
					created = cursor.getString(createdColumnIndex);
					image = cursor.getString(imgColumnIndex);
					name = cursor.getString(nameColumnIndex);
					screen = cursor.getString(screenColumnIndex);
					text = cursor.getString(textColumnIndex);
					TweetItem tweet = new TweetItem(created, image, text, name, screen);
					tweets.add(tweet);
				}

				adapter.setTweet(tweets);

			} finally {
				stopManagingCursor(cursor);
				cursor.close();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setProgressBarIndeterminateVisibility(Boolean.FALSE);
			adapter.notifyDataSetChanged();
		}
	}

	private class TweetsAdapter extends BaseAdapter {
		private List<TweetItem> tweets;
		private LayoutInflater mInflater = null;

		public TweetsAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public void setTweet(ArrayList<TweetItem> tweets) {
			this.tweets = tweets;
		}

		@Override
		public int getCount() {
			if (tweets == null) return 0;
			if (tweets.isEmpty()) return 0;
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
				vHolder.text = (WebView) convertView.findViewById(R.id.tws_row_txt_text);
				vHolder.txtUser = (TextView) convertView.findViewById(R.id.tws_row_txt_user);
				vHolder.txtScreenName = (TextView) convertView
						.findViewById(R.id.tws_row_txt_screen_name);
				vHolder.txtCreated = (TextView) convertView.findViewById(R.id.tws_row_txt_created);

				convertView.setTag(vHolder);
				convertView.setId(position);
			}

			int back = position == 0 ? R.drawable.top_tow : R.drawable.middle_row;

			convertView.setBackgroundResource(back);

			try {
				TweetItem tweet = tweets.get(position);

				ImageHelper.loadBitmap(getResources(), tweet.getImgUser(), vHolder.ImgUser);
				vHolder.txtUser.setText(tweet.getTxtUser());
				vHolder.txtScreenName.setText("@" + tweet.getTxtScreenName());
				StringBuilder sb = new StringBuilder("<html><body>");
				sb.append(fixLinks(tweet.getText()));
				sb.append("</body></html>");
				vHolder.text.loadDataWithBaseURL(null, sb.toString(), "text/html", "UTF-8", null);
				vHolder.text.setBackgroundColor(Color.parseColor("#dcdcdc"));

				long created = Long.parseLong(tweet.getCreated());
				created = (System.currentTimeMillis() - created) / (1000 * 60);
				if (created > 59) {
					created = created / 60;
					if (created > 24) {
						Date date = new Date(Long.parseLong(tweet.getCreated()));
						String sDate = new SimpleDateFormat("ddMMM", Locale.ENGLISH).format(date);
						vHolder.txtCreated.setText(sDate);
					} else vHolder.txtCreated.setText(String.valueOf(created) + "h");
				} else vHolder.txtCreated.setText(String.valueOf(created) + "m");

			} catch (Exception e) {
				Log.i(TAG, "Handled: " + e, e);
			}
			return convertView;
		}
	}

	private String fixLinks(String body) {
		String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		body = body.replaceAll(regex, "<a href=\"$0\">$0</a>");
		body = body.replace(">http://", ">");
		Log.d(TAG, body);
		return body;
	}

	public static class ViewHolder {
		ImageView ImgUser;
		WebView text;
		TextView txtUser;
		TextView txtScreenName;
		TextView txtCreated;
	}
}
