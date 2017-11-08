package org.uab.android.eventreporter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {

	ImageView icon;
	TextView title;
	TextView version;
	TextView developers;
	TextView moreInfo;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about_layout);
		
		icon = (ImageView) findViewById(R.id.application_icon);
		title = (TextView) findViewById(R.id.application_title);
		version = (TextView) findViewById(R.id.application_version);
		developers = (TextView) findViewById(R.id.application_developers);
		moreInfo = (TextView) findViewById(R.id.application_more_info);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		final SpannableString link = new SpannableString("http://www.incidencies.org");
		Linkify.addLinks(link, Linkify.WEB_URLS);
		moreInfo.setText("Visita " + link + " per més informació.");
		moreInfo.setMovementMethod(LinkMovementMethod.getInstance());
		moreInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.incidencies.org")));
			}
		});
	}
}
