package com.codeskraps.lolo;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		TextView txtOk = (TextView) findViewById(R.id.content_ok);
		TextView txtContent = (TextView) findViewById(R.id.txtContent);

		txtOk.setOnClickListener(this);

		StringBuilder sb = new StringBuilder();
		sb.append("091 Labs is a collaborative community space based in Galway City, Ireland. It is a shared physical space for any and all creative projects: art, woodwork, software, photography and electronics – to name but a few. Our aim is to provide Galway with a place for people to work and collaborate on creative projects, to learn and to share their knowledge. We welcome all skill levels and all creative ideas.<br /><br />");
		sb.append("<h2><font color='red'>lo-lo</font></h2>");
		sb.append("This is a way for members and the general public when the Labs on or labs off.<br /><br />");
		sb.append("<font color='#AAAAAA'>Website:</font> 091labs.com<br />");
		sb.append("<font color='#AAAAAA'>Email:</font> info@091labs.com<br />");
		String version = getString(R.string.prefsInfo_Title);
		sb.append(String.format("<font color='#AAAAAA'>Version:</font> %s<br />", version));
		sb.append("<font color='#AAAAAA'>License:</font> GNU GPL v3<br />");
		sb.append("<font color='#AAAAAA'>Code:</font> https://github.com/091labs/lo-lo<br />");

		txtContent.setText(Html.fromHtml(sb.toString()));

	}

	@Override
	public void onClick(View v) {
		this.finish();
	}
}
