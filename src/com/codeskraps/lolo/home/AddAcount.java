package com.codeskraps.lolo.home;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.codeskraps.lolo.R;
import com.codeskraps.lolo.misc.Utils;
import com.codeskraps.lolo.wordpress.ApiHelper;
import com.codeskraps.lolo.wordpress.XMLRPCClient;
import com.codeskraps.lolo.wordpress.XMLRPCException;

public class AddAcount extends Activity implements OnClickListener {
	private static final String TAG = AddAcount.class.getSimpleName();
	private static final String URL_091LABS = "http://091labs.com";

	private EditText etxtUsername = null;
	private EditText etxtPassword = null;

	private XMLRPCClient client = null;
	private ProgressDialog pd = null;
	private String xmlrpcURL = null;
	private String httpuser = "";
	private String httppassword = "";
	private boolean isCustomURL = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wordpress_acount);

		etxtUsername = (EditText) findViewById(R.id.username);
		etxtPassword = (EditText) findViewById(R.id.password);

		((Button) findViewById(R.id.save)).setOnClickListener(this);
		((Button) findViewById(R.id.cancel)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			if (Utils.isNetworkAvailable(this)) {

				pd = ProgressDialog.show(this, getString(R.string.account_setup),
						getString(R.string.attempting_configure), true, false);

				Thread action = new Thread() {
					public void run() {
						Looper.prepare();
						configureAccount();
						Looper.loop();
					}
				};
				action.start();
			} else {
				new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.no_network_title)
						.setPositiveButton(android.R.string.ok, null)
						.setMessage(R.string.no_network_message).create().show();
			}
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}

	private void configureAccount() {

		String blogURL = URL_091LABS;

		final String username = etxtUsername.getText().toString().trim();
		final String password = etxtPassword.getText().toString().trim();

		if (blogURL.equals("") || username.equals("") || password.equals("")) {
			pd.dismiss();
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.required_fields)
					.setPositiveButton(android.R.string.ok, null)
					.setMessage(R.string.url_username_password_required).create().show();
			return;
		}

		// attempt to get the XMLRPC URL via RSD
		String rsdUrl = getRSDMetaTagHrefRegEx(blogURL);
		if (rsdUrl == null) {
			rsdUrl = getRSDMetaTagHref(blogURL);
		}

		if (rsdUrl != null) {
			xmlrpcURL = ApiHelper.getXMLRPCUrl(rsdUrl, false);
			if (xmlrpcURL == null) xmlrpcURL = rsdUrl.replace("?rsd", "");
		} else {
			isCustomURL = false;
			// try the user entered path
			try {
				client = new XMLRPCClient(blogURL, username, password);
				try {
					client.call("system.listMethods");
					xmlrpcURL = blogURL;
					isCustomURL = true;
				} catch (XMLRPCException e) {
					// guess the xmlrpc path
					String guessURL = blogURL;
					if (guessURL.substring(guessURL.length() - 1, guessURL.length()).equals("/")) {
						guessURL = guessURL.substring(0, guessURL.length() - 1);
					}
					guessURL += "/xmlrpc.php";
					client = new XMLRPCClient(guessURL, httpuser, httppassword);
					try {
						client.call("system.listMethods");
						xmlrpcURL = guessURL;
					} catch (XMLRPCException ex) {}
				}
			} catch (Exception e) {}
		}

		if (xmlrpcURL == null) {
			pd.dismiss();
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.error).setPositiveButton(android.R.string.ok, null)
					.setMessage(R.string.no_site_error).create().show();
		} else {
			pd.dismiss();
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("horay").setPositiveButton(android.R.string.ok, null)
					.setMessage("woho").create().show();
		}
	}

	private static final Pattern rsdLink = Pattern
			.compile(
					"<link\\s*?rel=\"EditURI\"\\s*?type=\"application/rsd\\+xml\"\\s*?title=\"RSD\"\\s*?href=\"(.*?)\"\\s*?/>",
					Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private String getRSDMetaTagHrefRegEx(String urlString) {
		InputStream in = ApiHelper.getResponse(urlString);
		if (in != null) {
			try {
				String html = ApiHelper.convertStreamToString(in);
				Matcher matcher = rsdLink.matcher(html);
				if (matcher.find()) {
					String href = matcher.group(1);
					return href;
				}
			} catch (IOException e) {
				Log.e("wp_android", "IOEX", e);
				return null;
			}
		}
		return null;
	}

	private String getRSDMetaTagHref(String urlString) {
		// get the html code
		InputStream in = ApiHelper.getResponse(urlString);

		// parse the html and get the attribute for xmlrpc endpoint
		if (in != null) {
			XmlPullParser parser = Xml.newPullParser();
			try {
				// auto-detect the encoding from the stream
				parser.setInput(in, null);
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					String name = null;
					String rel = "";
					String type = "";
					String href = "";
					switch (eventType) {
					case XmlPullParser.START_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase("link")) {
							for (int i = 0; i < parser.getAttributeCount(); i++) {
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);
								if (attrName.equals("rel")) {
									rel = attrValue;
								} else if (attrName.equals("type")) type = attrValue;
								else if (attrName.equals("href")) href = attrValue;
							}

							if (rel.equals("EditURI") && type.equals("application/rsd+xml")) { return href; }
							// currentMessage.setLink(parser.nextText());
						}
						break;
					}
					eventType = parser.next();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null; // never found the rsd tag
	}

}
