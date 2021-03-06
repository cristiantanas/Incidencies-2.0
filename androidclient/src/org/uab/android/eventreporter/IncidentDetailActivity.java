package org.uab.android.eventreporter;

import org.apache.commons.codec.binary.Base64;
import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.utils.Incident;
import org.uab.android.eventreporter.utils.IncidentContentManager;
import org.uab.android.eventreporter.utils.RouteContentManager;
import org.uab.android.eventreporter.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class IncidentDetailActivity extends Activity {
	
	public static final int DIALOG_CONFIRMATION = 0;
	public static final String DIALOG_CONFIRMATION_TITLE = "Confirmar incidència";
	private ImageView icon = null;
	private TextView station = null;
	private TextView directionDescription = null;
	private TextView causeDescription = null;
	private TextView timeDescription = null;
	private TextView ttlDescription = null;
	private TextView statusDescription = null;
	private TextView commentDescription = null;
	private Button commentsButton = null;
	private Button confirmationButton = null;
	private Button mapButton = null;
	
	private RouteContentManager routeManager = new RouteContentManager();
	private int incidentId = -1;
	private boolean requestUpdate = false;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private String firstComment = "";
	
	private String username = "";
	private int action = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_events_detail_layout);
		
		icon = (ImageView) findViewById(R.id.detail_line_icon);
		station = (TextView) findViewById(R.id.detail_station_name);
		directionDescription = (TextView) findViewById(R.id.detail_line_direction);
		causeDescription = (TextView) findViewById(R.id.detail_incident_cause);
		timeDescription = (TextView) findViewById(R.id.detail_incident_time);
		ttlDescription = (TextView) findViewById(R.id.detail_incident_duration);
		statusDescription = (TextView) findViewById(R.id.detail_incident_status);
		commentDescription = (TextView) findViewById(R.id.detail_incident_comment);
		
		commentsButton = (Button) findViewById(R.id.detail_comments_button);
		confirmationButton = (Button) findViewById(R.id.detail_confirmation_button);
		mapButton = (Button) findViewById(R.id.detail_map_button);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		username = getIntent().getStringExtra("username");
		incidentId = getIntent().getExtras().getInt("incidentId");
		requestUpdate = getIntent().getExtras().getBoolean("requestUpdate");
		action = getIntent().getExtras().getInt("action");
		IncidentContentManager incidentInfo = IncidentContentManager.getInstance();
		Incident incident = incidentInfo.getIncident(incidentId, requestUpdate);
		icon.setImageResource(routeManager.getLineIcon(incident.getUniqueLineId()));
		station.setText(incident.getStationName());
		directionDescription.setText("Direcció: " + routeManager.getLineDirection(
				incident.getUniqueLineId(), incident.getDirection()));
		long lastReported = (System.currentTimeMillis() - 
				incident.getLastUpdateTime()) / Utils.MIN_TO_MILIS;
		timeDescription.setText("Incidència reportada fa " + (int) lastReported + " minuts");
		ttlDescription.setText("Temps aproximat de resolució: " + incident.getTTL() + " minuts");
		causeDescription.setText("Causa: " + Utils.eventToCharSeq(incident.getCause()));
		statusDescription.setText("Estat: " + Utils.stateToCharSeq(incident.getStatus()));
		
		firstComment = incident.getFirstComment();
		if (!firstComment.equalsIgnoreCase("")) {
			commentDescription.setText("\"" + firstComment + "\"");		}
		
		else {
			commentDescription.setText("L'usuari que ha reportat la incidència no " +
					"ha especificat cap comentari.");
		}
		
		latitude = incident.getLatitude();
		longitude = incident.getLongitude();
		
		commentsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(IncidentDetailActivity.this, CommentsListActivity.class)
						.putExtra("incidentId", incidentId));
			}
		});
		
		confirmationButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CONFIRMATION);
			}
		});
		
		mapButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(IncidentDetailActivity.this, MainActivity.class)
						.putExtra("username", username)
						.putExtra("latitude", latitude)
						.putExtra("longitude", longitude)
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CONFIRMATION:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.confirmation_dialog_layout);
			dialog.setTitle(DIALOG_CONFIRMATION_TITLE);
			
			final EditText comment = (EditText) dialog.findViewById(R.id.confirmation_description);
			Button submitButton = (Button) dialog.findViewById(R.id.confirmation_submit_button);
			submitButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String desc = Utils.escape(comment.getText().toString());
					String d = new String(Base64.encodeBase64(desc.getBytes()));
					NetworkUtils.sendConfirmationToServer(incidentId, d, username, IncidentDetailActivity.this);
				}
			});
			
			Button cancelButton = (Button) dialog.findViewById(R.id.confirmation_cancel_button);
			cancelButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismissDialog(DIALOG_CONFIRMATION);
				}
			});
			
			break;
			
		default:
			dialog = null;
		}
		
		return dialog;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (action == Utils.CLEAR_STACK) {
				startActivity(new Intent(this, MainMenuActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));			}
			
			else {
				finish();			}
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
}
