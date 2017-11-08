package org.uab.android.eventreporter;

import java.util.ArrayList;

import org.uab.android.eventreporter.lists.IncidentListAdapter;
import org.uab.android.eventreporter.lists.RouteSelectExpandableList;
import org.uab.android.eventreporter.utils.Incident;
import org.uab.android.eventreporter.utils.IncidentContentManager;
import org.uab.android.eventreporter.utils.RouteContentManager;
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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class IncidentListActivity extends ListActivity {
	public static final String CLASSTAG = IncidentListActivity.class.getSimpleName().toUpperCase();
	
	private ImageView logo = null;
	private TextView beginStation = null;
	private TextView endStation = null;
	private int uniqueLineId = -1;
	private RouteContentManager routeManager = new RouteContentManager();
	
	private ArrayList<Incident> incidentsArray = null;	
	
	private String username = "";
	private double userLatitude = .0;
	private double userLongitude = .0;	
	
	static final int DIALOG_INTERNET_CONN_PROBLEM = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_events_layout);
				
		logo = (ImageView) findViewById(R.id.route_icon);
		beginStation = (TextView) findViewById(R.id.route_begining);
		endStation = (TextView) findViewById(R.id.route_end);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		username = getIntent().getStringExtra("username");
		uniqueLineId = getIntent().getExtras().getInt("id");
		userLatitude = getIntent().getExtras().getDouble("userLat");
		userLongitude = getIntent().getExtras().getDouble("userLon");
		logo.setImageResource(routeManager.getLineIcon(uniqueLineId));
		beginStation.setText(routeManager.getLineBegining(uniqueLineId));
		endStation.setText(routeManager.getLineEnding(uniqueLineId));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IncidentContentManager incidentInfo = IncidentContentManager.getInstance();
		
		try {
			incidentInfo.updateIncidentList(uniqueLineId, username, userLatitude, 
					userLongitude, this);
			incidentsArray = incidentInfo.getIncidentList(uniqueLineId);
			ListAdapter adapter = new IncidentListAdapter(incidentsArray, this);
			setListAdapter(adapter);
			
			getListView().setOnItemClickListener(new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					if ( incidentsArray.get(position).getStatus() == Utils.RSS_FEEDS ) {
						startActivityForResult(new Intent(getApplicationContext(), IncidentRSSActivity.class)
								.putExtra("incidentId", incidentsArray.get(position).getId())
								.putExtra("requestUpdate", false)
								.putExtra("username", username), 
								0);
						
					} else {
						startActivity(new Intent(getApplicationContext(), IncidentDetailActivity.class)
								.putExtra("incidentId", incidentsArray.get(position).getId())
								.putExtra("requestUpdate", false)
								.putExtra("username", username));
					}
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
		inflater.inflate(R.menu.incident_list_menu_layout, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_more_lines:
			startActivity(new Intent(this, RouteSelectExpandableList.class)
					.putExtra("username", username)
					.putExtra("userLat", userLatitude)
					.putExtra("userLon", userLongitude));
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
}
