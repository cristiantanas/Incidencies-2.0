package org.uab.android.eventreporter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.net.SendDataToServer;
import org.uab.android.eventreporter.utils.GPSLocationListener;
import org.uab.android.eventreporter.utils.Utils;
import org.uab.android.eventreporter.utils.XMLT11Parser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class T11DropActivity extends Activity {
	
	private EditText lineDescription;
	private EditText description;
	private EditText timeDisplay;
	private Button setTime;
	private TextView currentTimeDisplay;
	private Button submitT11;
	
	private int hour;
	private int minute;
	
	private LocationManager lm;
	private GPSLocationListener locationListener;
	private Location currentLoc;
	
	private String username;
	
	static final int TIME_DIALOG_ID = 0;
	static final int NO_LOCATION_DIALOG_ID = 1;
	static final int TIME_GREATER_DIALOG_ID = 2;
	static final int ETA_NOT_VALID_DIALOG_ID = 3;
	final Calendar calendar = Calendar.getInstance();
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE);
	
	public String getUsername() {
		return username;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.t11_drop_activity_layout);
		
		lineDescription = (EditText) findViewById(R.id.T11_drop_line_description);
		description = (EditText) findViewById(R.id.T11_drop_description);
		timeDisplay = (EditText) findViewById(R.id.T11_validation_time_string);
		setTime = (Button) findViewById(R.id.T11_set_time);
		currentTimeDisplay = (TextView) findViewById(R.id.T11_current_time);
		submitT11 = (Button) findViewById(R.id.T11_submit);
		
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationListener = new GPSLocationListener();
		
		currentTimeDisplay.setText("Hora actual: " + df.format(new Date(System.currentTimeMillis())));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		username = getIntent().getStringExtra("username");
		
		setTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});
		
		submitT11.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String lineDesc = lineDescription.getText().toString();
				String desc = description.getText().toString();
				
				int cHour = calendar.get(Calendar.HOUR_OF_DAY);
				int cMinute = calendar.get(Calendar.MINUTE);
				int tTime = (cHour*60 + cMinute) - (hour*60 + minute);
				
				if ( tTime<0 ) showDialog(TIME_GREATER_DIALOG_ID);
				
				else if ( tTime>75 ) showDialog(ETA_NOT_VALID_DIALOG_ID);
				
				else {
					int ETA = 75 - tTime;
					currentLoc = locationListener.getLocation();
					if ( currentLoc==null ) {
						showDialog(NO_LOCATION_DIALOG_ID);
						
					} else {
						String T11 = XMLT11Parser.encode(
								ETA, 
								desc,
								lineDesc,
								System.currentTimeMillis(), 
								username, 
								currentLoc.getLatitude(), 
								currentLoc.getLongitude());
						
						String message = NetworkUtils.createMessage(Utils.T11_NOTIFICATION, 
								Utils.DATAGRAM_GN_TYPE, new String[] {T11});
						new SendDataToServer(T11DropActivity.this).execute(message);
					}
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		updateDisplay();
		
		String bestProvider = lm.getBestProvider(new Criteria(), true);
		if ( bestProvider==null )
			showDialog(NO_LOCATION_DIALOG_ID);
		
		else
			lm.requestLocationUpdates(bestProvider, Utils.UPDATE_MIN_TIME, Utils.UPDATE_MIN_DISTANCE, 
					locationListener);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		lm.removeUpdates(locationListener);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch (id) {
		case TIME_DIALOG_ID:
			dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
				
							@Override
							public void onTimeSet(TimePicker view, int hourOfDay, int min) {
								hour = hourOfDay;
								minute = min;
								updateDisplay();
							}
						 }, hour, minute, false);
			break;
			
		case NO_LOCATION_DIALOG_ID:
			builder.setMessage("No s'ha pogut llegir la informació de posicionament. Comproveu que el GPS estigui activat i torneu-ho a provar.")
				   .setCancelable(false)
				   .setNeutralButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			dialog = builder.create();
			break;
			
		case TIME_GREATER_DIALOG_ID:
			builder.setMessage("L'hora de validació no pot ser posterior a l'hora actual!")
				   .setCancelable(false)
				   .setNeutralButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			dialog = builder.create();
			break;
			
		case ETA_NOT_VALID_DIALOG_ID:
			builder.setMessage("El temps màxim de transbordament és de 75 minuts!")
				   .setCancelable(false)
				   .setNeutralButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			dialog = builder.create();
			break;
			
		default:
			dialog = null;
		}
		
		return dialog;
	}
	
	private void updateDisplay() {
	    timeDisplay.setText(new StringBuilder().append("Hora: ").append(pad(hour)).append(":").append(pad(minute)));
	}

	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
}
