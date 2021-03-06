package org.uab.android.eventreporter.lists;

import java.util.ArrayList;

import org.uab.android.eventreporter.GeneralIncDetailActivity;
import org.uab.android.eventreporter.MainActivity;
import org.uab.android.eventreporter.R;
import org.uab.android.eventreporter.utils.GeneralNotification;
import org.uab.android.eventreporter.utils.IncidentContentManager;
import org.uab.android.eventreporter.utils.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;

public class GeneralIncidentList extends ListActivity {

	private ArrayList<GeneralNotification> generalIncList;
	
	private String username;
	private double userLatitude;
	private double userLongitude;
	
	static final int DIALOG_INTERNET_CONN_PROBLEM = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_general_inc_layout);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		username = getIntent().getStringExtra("username");
		userLatitude = getIntent().getDoubleExtra("userLat", .0);
		userLongitude = getIntent().getDoubleExtra("userLon", .0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IncidentContentManager incidentInfo = IncidentContentManager.getInstance();
		
		try {
			incidentInfo.updateIncidentList(Utils.GENERAL_INCIDENTS, username, userLatitude, userLongitude);
			generalIncList = incidentInfo.getGeneralIncidentList(Utils.GENERAL_INCIDENTS);
			ListAdapter adapter = new GeneralListAdapter(generalIncList, this);
			setListAdapter(adapter);
			
			getListView().setOnItemClickListener(new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					startActivity(new Intent(getApplicationContext(), GeneralIncDetailActivity.class)
							.putExtra("incidentId", generalIncList.get(position).getId())
							.putExtra("requestUpdate", false)
							.putExtra("username", username));
				}
				
			});
			
		} catch (NullPointerException e) {
			showDialog(DIALOG_INTERNET_CONN_PROBLEM);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.incident_gen_menu_layout, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_view_map:
			startActivity(new Intent(this, MainActivity.class)
				.putExtra("username", username)
				.putExtra("userLat", userLatitude)
				.putExtra("userLon", userLongitude)
				.putExtra("incidentType", Utils.GENERAL_INCIDENTS));
			finish();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		
		switch (id) {
		case DIALOG_INTERNET_CONN_PROBLEM:
			dialog = new AlertDialog.Builder(this)
						.setTitle("Ups.. error de connexió")
						.setMessage("No he pogut connectar amb el servidor :(. Potser la connexió a Internet és molt lenta o la xarxa no permet connexions externes.")
						.setNeutralButton("Ho intentaré més tard", new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							}
						})
						.create();
			break;
			
		default:
			dialog = null;
		}
		
		return dialog;
	}
}
