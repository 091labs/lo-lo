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

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = PrefsActivity.class.getSimpleName();
	private static String CONFIGURE_ACTION = "android.appwidget.action.APPWIDGET_CONFIGURE";
	public static final String FORCE_WIDGET_UPDATE = "com.codeskraps.lolo.FORCE_WIDGET_UPDATE";
	public static final String ONCLICK = "lstOnClick";
	public static final String EURL = "eURL";
	public static final String LAST_SYNC = "sync";
	public static final String SHOW_SYNC = "chkSync";

	private SharedPreferences prefs = null;
	private ListPreference lstOnClick = null;
	private EditTextPreference eURL = null;
	private CheckBoxPreference chkSync = null;
	private String[] entries_OnClick = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		setContentView(R.layout.prefs);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		entries_OnClick = getResources().getStringArray(R.array.OnClick_entries);

		lstOnClick = (ListPreference) findPreference(ONCLICK);
		eURL = (EditTextPreference) findPreference(EURL);
		chkSync = (CheckBoxPreference) findPreference(SHOW_SYNC);
		
		String lstSync = prefs.getString(LAST_SYNC, null);
		if (lstSync == null)
			chkSync.setSummary(getString(R.string.prefsSync_summarNot));
		else
			chkSync.setSummary(lstSync);
	}

	@Override
	protected void onResume() {
		super.onResume();

		String onClick = prefs.getString(ONCLICK, entries_OnClick[0]);
		int action = Integer.parseInt(onClick);
		lstOnClick.setSummary(entries_OnClick[action]);

		String url = prefs.getString(EURL, getString(R.string.prefsURL_default));
		String urlSummary = String.format("%s %s", getString(R.string.prefsURL_summary), url);
		eURL.setSummary(urlSummary);

		if (action != 3)
			eURL.setEnabled(false);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, "onSharedPreferenceChanged");
		if (key.equals(ONCLICK)) {
			String onClick = prefs.getString(ONCLICK, entries_OnClick[0]);
			int action = Integer.parseInt(onClick);
			lstOnClick.setSummary(entries_OnClick[action]);

			if (action != 3)
				eURL.setEnabled(false);
			else
				eURL.setEnabled(true);
		} else if (key.equals(EURL)) {
			String url = prefs.getString(EURL, getString(R.string.prefsURL_default));
			String urlSummary = String.format("%s %s", getString(R.string.prefsURL_summary), url);
			eURL.setSummary(urlSummary);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown");
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
				&& keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// Take care of calling this method on earlier versions of
			// the platform where it doesn't exist.
			Log.d(TAG, "SDK < Eclair");
			onBackPressed();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		// This will be called either automatically for you on 2.0
		// or later, or by the code above on earlier versions of the
		// platform.
		Log.d(TAG, "onBackPressed");
		resultIntent();
		finish();
		return;
	}

	private void resultIntent() {
		Log.d(TAG, "resultIntent");

		if (CONFIGURE_ACTION.equals(getIntent().getAction())) {
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				Intent result = new Intent();

				result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, result);
			}
		}
		sendBroadcast(new Intent(FORCE_WIDGET_UPDATE));
	}
}