package org.uab.android.eventreporter;

import java.util.ArrayList;

import org.uab.android.eventreporter.lists.IncidentListAdapter;
import org.uab.android.eventreporter.lists.RouteSelectExpandableList;
import org.uab.android.eventreporter.utils.Incident;
import org.uab.android.eventreporter.utils.IncidentContentManager;
import org.uab.android.eventreporter.utils.RouteContentManager;
import org.uab.android.eventreporter.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class FavoriteLinesActivity extends TabActivity implements OnTabChangeListener {
	public static final String CLASSTAG = FavoriteLinesActivity.class.getSimpleName().toUpperCase();
	public static final int DIALOG_NO_SERVER_CONN = 0;
	public static final String DIALOG_NO_SERVER_CONN_TITLE = "Servidor no disponible";
	public static final int RSS_FEED_VIEW_REQUEST = 0;

	private Activity thisActivity = this;
	private RouteContentManager manager = new RouteContentManager();
	private IncidentContentManager incidentInfo;
	private TabHost tabHost;
	private TabHost.TabSpec spec;
	private Resources res;
	private String currentTabTag = "favorite1";
	private String username = "";
	private int firstFavoriteLine = -1;
	private int secondFavoriteLine = -1;
	private int thirdFavoriteLine = -1;
	private double userLatitude = .0;
	private double userLongitude = .0;
	
	private ListView listView;
	private TextView textView;
	private ListAdapter listAdapter;
	private ArrayList<Incident> incidentArray;
	
	private boolean onTabChangedCalled = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.incidents_tab_view);
		
		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(this);
		res = getResources();
		incidentInfo = IncidentContentManager.getInstance();
		listView = (ListView) findViewById(android.R.id.list);
		textView = (TextView) findViewById(android.R.id.empty);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
//		if ( !NetworkUtils.isServerOnline() ) {
			
//			buildTabs();
			
//			textView.setVisibility(View.INVISIBLE);
//			showDialog(DIALOG_NO_SERVER_CONN);
			
//		} else {
			
			tabHost.getTabWidget().removeAllViews();
			username = getIntent().getStringExtra("username");
			firstFavoriteLine = getIntent().getExtras().getInt("firstLine");
			secondFavoriteLine = getIntent().getExtras().getInt("secondLine");
			thirdFavoriteLine = getIntent().getExtras().getInt("thirdLine");
			userLatitude = getIntent().getExtras().getDouble("userLat");
			userLongitude = getIntent().getExtras().getDouble("userLon");
			buildTabs();
			
			listView.setVisibility(View.VISIBLE);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					
					if ( incidentArray.get(position).getStatus() == Utils.RSS_FEEDS ) {
						startActivityForResult(new Intent(getApplicationContext(), IncidentRSSActivity.class)
								.putExtra("incidentId", incidentArray.get(position).getId())
								.putExtra("requestUpdate", false)
								.putExtra("tabId", tabHost.getCurrentTabTag())
								.putExtra("username", username), 
								RSS_FEED_VIEW_REQUEST);
						
					} else {
						startActivity(new Intent(getApplicationContext(), IncidentDetailActivity.class)
								.putExtra("incidentId", incidentArray.get(position).getId())
								.putExtra("requestUpdate", false)
								.putExtra("username", username));
					}
				}
			});
			((BaseAdapter)listAdapter).notifyDataSetChanged();
			tabHost.setCurrentTabByTag(currentTabTag);
			tabHost.getTabWidget().focusCurrentTab(tabHost.getCurrentTab());
			
			if ( !onTabChangedCalled )
				onTabChanged(currentTabTag);
//		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		onTabChangedCalled = false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.incident_tab_menu_layout, menu);
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
			
		case R.id.menu_view_map:
			startActivity(new Intent(this, MainActivity.class)
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch (id) {
		case DIALOG_NO_SERVER_CONN:
			
			builder.setTitle("Ups.. error de connexió")
				   .setMessage("No he pogut connectar amb el servidor :(. Potser la connexió a Internet és molt lenta o la xarxa no permet connexions externes.")
				   .setNeutralButton("Ho intentaré més tard", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							thisActivity.finish();
						}
				   });
			dialog = builder.create();
			break;
			
		default:
			dialog = null;
		}
		
		return dialog;
	}

	@Override
	public void onTabChanged(String tabId) {
		onTabChangedCalled = true;
		
		incidentArray = new ArrayList<Incident>();
		if ( tabId.equalsIgnoreCase("favorite1") ) {
			currentTabTag = "favorite1";
			
			try {
				incidentInfo.updateIncidentList(firstFavoriteLine, username, 
						userLatitude, userLongitude, thisActivity);
				incidentArray = incidentInfo.getIncidentList(firstFavoriteLine);
				
			} catch (NullPointerException e) {
				showDialog(DIALOG_NO_SERVER_CONN);
			}
		}
		
		if ( tabId.equalsIgnoreCase("favorite2") ) {
			currentTabTag = "favorite2";
			
			try {
				incidentInfo.updateIncidentList(secondFavoriteLine, username, 
						userLatitude, userLongitude, thisActivity);
				incidentArray = incidentInfo.getIncidentList(secondFavoriteLine);
				
			} catch (NullPointerException e) {
				showDialog(DIALOG_NO_SERVER_CONN);
			}
		}
		
		if ( tabId.equalsIgnoreCase("favorite3") ) {
			currentTabTag = "favorite3";
			
			try {
				incidentInfo.updateIncidentList(thirdFavoriteLine, username, 
						userLatitude, userLongitude, thisActivity);
				incidentArray = incidentInfo.getIncidentList(thirdFavoriteLine);
				
			} catch (NullPointerException e) {
				showDialog(DIALOG_NO_SERVER_CONN);
			}
		}
		
		
		listAdapter = new IncidentListAdapter(incidentArray, thisActivity);
		listView.setAdapter(listAdapter);
		listView.setVisibility(View.VISIBLE);
		if ( !incidentArray.isEmpty() ) {
			textView.setVisibility(View.INVISIBLE);		}
		
		else {
			textView.setVisibility(View.VISIBLE);		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( requestCode == RSS_FEED_VIEW_REQUEST ) {
			if ( resultCode == RESULT_OK ) {
				String tabId = data.getStringExtra("tabId");
				onTabChanged(tabId);
			}
		}
	}
	
	private void buildTabs() {
		if (firstFavoriteLine != -1) {
			spec = tabHost.newTabSpec("favorite1")
			  .setIndicator(manager.getSimpleName(firstFavoriteLine), 
					  		res.getDrawable(manager.getLineIcon(firstFavoriteLine)))
			  .setContent(android.R.id.list);

			tabHost.addTab(spec);
		}
		
		if (secondFavoriteLine != -1) {
			spec = tabHost.newTabSpec("favorite2")
			  .setIndicator(manager.getSimpleName(secondFavoriteLine), 
					  		res.getDrawable(manager.getLineIcon(secondFavoriteLine)))
			  .setContent(android.R.id.list);

			tabHost.addTab(spec);
		}
		
		if (thirdFavoriteLine != -1) {
			spec = tabHost.newTabSpec("favorite3")
			  .setIndicator(manager.getSimpleName(thirdFavoriteLine), 
					  		res.getDrawable(manager.getLineIcon(thirdFavoriteLine)))
			  .setContent(android.R.id.list);

			tabHost.addTab(spec);
		}
	}
}
