package org.uab.android.eventreporter;

import org.uab.android.eventreporter.lists.FGCExpandableList;
import org.uab.android.eventreporter.lists.MetroExpandableList;
import org.uab.android.eventreporter.lists.RenfeExpandableList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChooseLocationActivity extends Activity {
	private Button renfeButton = null;
	private Button fgcButton = null;
	private Button metroButton = null;
	private String username = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_location_layout);
		
		renfeButton = (Button) findViewById(R.id.renfe_button);
		fgcButton = (Button) findViewById(R.id.fgc_button);
		metroButton = (Button) findViewById(R.id.metro_button);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		username = getIntent().getStringExtra("username");
		
		renfeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						RenfeExpandableList.class)
						.putExtra("username", username));
			}
		});

		fgcButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						FGCExpandableList.class)
						.putExtra("username", username));
			}
		});

		metroButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						MetroExpandableList.class)
						.putExtra("username", username));
			}
		});
	}
}