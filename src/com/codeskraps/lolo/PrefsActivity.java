package com.codeskraps.lolo;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = PrefsActivity.class.getSimpleName();
	private static String CONFIGURE_ACTION = "android.appwidget.action.APPWIDGET_CONFIGURE";
	public static final String FORCE_WIDGET_UPDATE = "com.codeskraps.lolo.FORCE_WIDGET_UPDATE";
	public static final String ONCLICK = "lstOnClick";

	private SharedPreferences prefs = null;
	private ListPreference lstOnClick = null;
	private String[] entries_OnClick = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		// setContentView(R.layout.header);
		addPreferencesFromResource(R.xml.preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		entries_OnClick = getResources().getStringArray(R.array.OnClick_entries);

		lstOnClick = (ListPreference) findPreference(ONCLICK);

		String onClick = prefs.getString(ONCLICK, "Nothing");
		lstOnClick.setSummary(entries_OnClick[Integer.parseInt(onClick)]);
	}

	@Override
	protected void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(ONCLICK)) {
			String onClick = prefs.getString(ONCLICK, "Nothing");
			lstOnClick.setSummary(entries_OnClick[Integer.parseInt(onClick)]);
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            //Log.d(TAG, "SDK < Eclair");
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // This will be called either automatically for you on 2.0
        // or later, or by the code above on earlier versions of the
        // platform.
        //Log.d(TAG, "onBackPressed");
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
