package com.codeskraps.lolo.twitter;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.codeskraps.lolo.R;
import com.codeskraps.lolo.misc.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TwitterSignInActivity extends Activity implements OnClickListener {
	private static final String TAG = TwitterSignInActivity.class.getSimpleName();

	private OAuthService service = null;
	private Token requestToken = null;

	private enum STEP {
		zero, one, two
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		service = new ServiceBuilder().provider(TwitterApi.class).apiKey("Z4lEh5rSu0rV3fXt37gw7A")
				.apiSecret("vLLTqO311ZhlVXhl1GaB72DnIwdCOPwzeozNRWy3I").build();
		new OathTask().execute(STEP.zero);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.twitter_signin);

		((Button) findViewById(R.id.twi_btn_goto)).setOnClickListener(this);
		((Button) findViewById(R.id.twi_submit)).setOnClickListener(this);
		((Button) findViewById(R.id.twi_cancel)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// @formatter:off
		switch (v.getId()) {
		case R.id.twi_btn_goto: new OathTask().execute(STEP.one); break;
		case R.id.twi_submit: 
			((Button) findViewById(R.id.twi_submit)).setOnClickListener(this);
			new OathTask().execute(STEP.two); 
			break;
		case R.id.twi_cancel: finish(); break;
		// @formatter:on
		}
	}

	private class OathTask extends AsyncTask<STEP, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(Boolean.TRUE);
		}

		@Override
		protected Boolean doInBackground(STEP... params) {
			STEP step = params[0];

			if (step == STEP.zero) {
				requestToken = service.getRequestToken();

			} else if (step == STEP.one) {
				String token = service.getAuthorizationUrl(requestToken);

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(token));
				startActivity(browserIntent);

			} else if (step == STEP.two) {
				try {
					TextView txtCode = (TextView) findViewById(R.id.twi_etxt_key);
					String code = txtCode.getText().toString().trim();
					Log.d(TAG, "code:" + code);

					Verifier verifier = new Verifier(code);
					Token accessToken = service.getAccessToken(requestToken, verifier);
					Log.d(TAG, "accessToken:" + accessToken);

					SharedPreferences.Editor editor = PreferenceManager
							.getDefaultSharedPreferences(getApplication()).edit();
					editor.putString(Constants.ACCESS_TOKEN, accessToken.getToken());
					editor.putString(Constants.ACCESS_SECRET, accessToken.getSecret());
					editor.commit();

					TwitterSignInActivity.this.finish();
					return false;

				} catch (Exception e) {
					Log.i(TAG, "Handled: twitter key - " + e.getMessage(), e);
					return true;
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			setProgressBarIndeterminateVisibility(Boolean.FALSE);

			if (result)
				new AlertDialog.Builder(TwitterSignInActivity.this)
						.setIcon(R.drawable.alerts_and_states_error)
						.setMessage(R.string.twi_dia_message)
						.setTitle(R.string.twi_dia_title)
						.setPositiveButton(R.string.twi_dia_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
									}
								}).create().show();
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
