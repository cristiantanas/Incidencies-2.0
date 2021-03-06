package org.uab.android.eventreporter;

import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.net.SendDataToServer;
import org.uab.android.eventreporter.utils.Incident;
import org.uab.android.eventreporter.utils.IncidentContentManager;
import org.uab.android.eventreporter.utils.RouteContentManager;
import org.uab.android.eventreporter.utils.Utils;
import org.uab.android.eventreporter.utils.XMLIncidentNotificationParser;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class IncidentRSSActivity extends Activity {

	public static final int DIALOG_CONFIRMATION = 0;
	public static final String DIALOG_CONFIRMATION_TITLE = "Confirmar incidència";
	public static final String DEFAULT_DIRECTION = "Sense determinar";
	private RouteContentManager routeMng = new RouteContentManager();
	private ImageView icon;
	private TextView title;
	private TextView content;
	private TextView timeInfo;
	private Button confirmButton;
	
	private String username = "";
	private int incidentId = -1;
	private int serviceId = -1;
	private int lineId = -1;
	private boolean requestUpdate = false;
	private String currentTab = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.rss_detail);
		
		icon = (ImageView) findViewById(R.id.rss_line_icon);
		title = (TextView) findViewById(R.id.rss_title);
		content = (TextView) findViewById(R.id.rss_content);
		timeInfo = (TextView) findViewById(R.id.rss_time_detail);
		confirmButton = (Button) findViewById(R.id.rss_confirm_button);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		username = getIntent().getStringExtra("username");
		incidentId = getIntent().getIntExtra("incidentId", -1);
		requestUpdate = getIntent().getBooleanExtra("requestUpdate", false);
		currentTab = getIntent().getStringExtra("tabId");
		IncidentContentManager contentMng = IncidentContentManager.getInstance();
		Incident incident = contentMng.getIncident(incidentId, requestUpdate);
		serviceId = incident.getTransportService();
		lineId = incident.getUniqueLineId();
		
		icon.setImageResource(routeMng.getLineIcon(lineId));
		String rssSource = incident.getStationName();
		title.setText(rssSource.substring(rssSource.indexOf("@")));
		content.setText(incident.getFirstComment());
		long lastReported = (System.currentTimeMillis() - 
				incident.getLastUpdateTime()) / Utils.MIN_TO_MILIS;
		timeInfo.setText("Incidència reportada fa " + (int) lastReported + " minuts");
		
		confirmButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CONFIRMATION);
			}
		});
	}
	
	public void exit() {		
		setResult(RESULT_OK, new Intent().putExtra("tabId", currentTab));
		finish();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_CONFIRMATION:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.rss_confirmation_dialog_layout);
			dialog.setTitle(DIALOG_CONFIRMATION_TITLE);
			
			/*-- Fill spinner adapter with the corresponding station names --*/			
			final Spinner chooseStation = (Spinner) dialog.findViewById(R.id.rss_confirm_station_spinner);
			ArrayAdapter<CharSequence> stationsAdapter = ArrayAdapter.createFromResource(this, 
					routeMng.getStationsIdentifier(lineId), android.R.layout.simple_spinner_item);
			stationsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
			chooseStation.setAdapter(stationsAdapter);
			
			/*-- Fill spinner adapter with the corresponding directions for this line --*/	
			final Spinner chooseDirection = (Spinner) dialog.findViewById(R.id.rss_confirm_direction_spinner);
			ArrayAdapter<CharSequence> directionAdapter = new ArrayAdapter<CharSequence>(this, 
					android.R.layout.simple_spinner_item, new String[] 
					{DEFAULT_DIRECTION, routeMng.getLineBegining(lineId), routeMng.getLineEnding(lineId)});
			directionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
			chooseDirection.setAdapter(directionAdapter);
			
			/*-- Fill spinner adapter with the corresponding causes --*/
			final Spinner chooseCause = (Spinner) dialog.findViewById(R.id.rss_confirm_cause_spinner);
			ArrayAdapter<CharSequence> causeAdapter = ArrayAdapter.createFromResource(this, 
					R.array.report_class, android.R.layout.simple_spinner_item);
			causeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
			chooseCause.setAdapter(causeAdapter);
			
			/*-- Fill spinner adapter with the corresponding severity values --*/
			final Spinner chooseSeverity = (Spinner) dialog.findViewById(R.id.rss_confirm_severity_spinner);
			ArrayAdapter<CharSequence> severityAdapter = ArrayAdapter.createFromResource(this, 
					R.array.severity_type, android.R.layout.simple_spinner_item);
			severityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
			chooseSeverity.setAdapter(severityAdapter);
			
			final EditText comment = (EditText) dialog.findViewById(R.id.rss_confirm_comment);
			Button submitButton = (Button) dialog.findViewById(R.id.rss_confirm_submit_button);
			submitButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String station = (String)chooseStation.getSelectedItem();
					String data = XMLIncidentNotificationParser.encode(
							serviceId, 
							lineId, 
							Utils.md5(station), 
							chooseDirection.getSelectedItemPosition(), 
							chooseCause.getSelectedItemPosition(), 
							chooseSeverity.getSelectedItemPosition(), 
							comment.getText().toString(), 
							System.currentTimeMillis(), 
							username);
					String message = NetworkUtils.createMessage(Utils.INCIDENT_NOTIFICATION, 
							new String[] {data});
					new SendDataToServer(IncidentRSSActivity.this).execute(message);
				}
			});
			
			Button cancelButton = (Button) dialog.findViewById(R.id.rss_confirm_cancel_button);
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
}
