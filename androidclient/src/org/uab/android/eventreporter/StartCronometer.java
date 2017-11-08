package org.uab.android.eventreporter;

import java.util.Random;

import org.json.JSONObject;
import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

public class StartCronometer extends Activity {
	
	static String START_JOURNEY_LABEL = "Opció per mesurar el temps real d'un trajecte i comparar-lo " +
			"amb l'estimat pels horaris oficials. " +
			"Necessitem que ens indiquis les estacions de sortida i d'arribada.";
	static String FINISH_JOURNEY_LABEL = "Opció per mesurar el temps real d'un trajecte i comparar-lo " +
			"amb l'estimat pels horaris oficials.";
	static int DEFAULT_STATION_LIST = 0;
	
	//-------------DIALOG CONSTANTS---------------------------------------------------------------------
	static final int ORIGEN_DESTI_MATCH = 1;
	static String ORIGEN_DESTI_MATCH_MESS = "L'estació de destí no pot coincidir amb l'estació origen.";
	static final int END_OF_ROUTE = 2;
	static String END_OF_ROUTE_LABEL = "";
	static final int START_CHRONO = 3;
	static String START_CHRONO_MESSAGE = "Inciant trajecte ...";
	static final int STOP_CHRONO = 4;
	static String STOP_CHRONO_MESSAGE = "Trajecte finalitzat. Recuperant informació ...";
	//--------------------------------------------------------------------------------------------------
	
	static int MAX_RNG = (int) Math.pow(2, 128);
	
	private SharedPreferences chronoPreferences;
	//private SharedPreferences.Editor prefsEditor;
	
	private Random randomGenerator;
	
	private String username;
	private boolean startNewJourney;
	
	TextView	introLabel;
	Spinner 	transportType;
	Spinner 	estacioOrigen;
	Spinner 	estacioDesti;
	Chronometer chrono;
	Button 		chronometerControl;
	
	Handler handler = new Handler();
	Runnable start = new Runnable() {
		
		@Override
		public void run() {
			startChronometer();
		}
	};
	
	Runnable stop = new Runnable() {
		
		@Override
		public void run() {
			stopChronometer();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_cronometer_layout);
		
		chronoPreferences = getSharedPreferences("chronoPrefs", Activity.MODE_PRIVATE);
		//prefsEditor = chronoPreferences.edit();
		randomGenerator = new Random();
		
		introLabel = (TextView) findViewById(R.id.new_traj_info);
		transportType = (Spinner) findViewById(R.id.tipusTranspPub);
		estacioOrigen = (Spinner) findViewById(R.id.estacioOrigen);
		estacioDesti = (Spinner) findViewById(R.id.estacioDesti);
		chrono = (Chronometer) findViewById(R.id.chronometer);
		chronometerControl = (Button) findViewById(R.id.start_stop_button);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		username = getIntent().getStringExtra("username");
		startNewJourney = getIntent().getBooleanExtra("startNewJourney", true);
		
		if ( startNewJourney ) initStartJourney(); else initFinishJourney();
		
		//-- Spinner per seleccionar el tipus de transport p�blic : info en l'array 'public_transportation' --//
		ArrayAdapter<CharSequence> pubTransportationAdapter = ArrayAdapter.createFromResource
		(
				this, 
				R.array.transport_public, 
				android.R.layout.simple_spinner_item
		);
		pubTransportationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
		transportType.setAdapter(pubTransportationAdapter);
		
		transportType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				ArrayAdapter<CharSequence> stationsAdapter = ArrayAdapter.createFromResource
				(
						StartCronometer.this, 
						getResources().getIdentifier(getResourceName((int) id), "array", getPackageName()), 
						android.R.layout.simple_spinner_item
				);
				stationsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
				estacioOrigen.setAdapter(stationsAdapter);
				estacioDesti.setAdapter(stationsAdapter);
				
				estacioOrigen.setSelection(chronoPreferences.getInt("origen", 0), true);
				estacioDesti.setSelection(chronoPreferences.getInt("desti", 0), true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		ArrayAdapter<CharSequence> stationListAdapter = ArrayAdapter.createFromResource
		(
				this, 
				getResources().getIdentifier("renfe_c1_line", "array", getPackageName()), 
				android.R.layout.simple_spinner_item
		);
		stationListAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
		estacioOrigen.setAdapter(stationListAdapter);
		estacioDesti.setAdapter(stationListAdapter);
		
		int selectedLine = chronoPreferences.getInt("servei", 0);
		int selectedOrigin = chronoPreferences.getInt("origen", 0);
		int selectedDestination = chronoPreferences.getInt("desti", 0);
		
		transportType.setSelection(selectedLine, true);
		estacioOrigen.setSelection(selectedOrigin, true);
		estacioDesti.setSelection(selectedDestination, true);
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch (id) {
		case ORIGEN_DESTI_MATCH:
			dialog = builder.setMessage(ORIGEN_DESTI_MATCH_MESS)
							.setCancelable(false)
							.setNeutralButton(Utils.OK, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.create();
			break;
			
		case END_OF_ROUTE:
			dialog = builder.setMessage(END_OF_ROUTE_LABEL)
							.setCancelable(false)
							.setNeutralButton(Utils.OK, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									finish();
								}
							})
							.create();
			break;
			
		case START_CHRONO:
			dialog = new ProgressDialog(this);
			((ProgressDialog) dialog).setMessage(START_CHRONO_MESSAGE);
			break;
			
		case STOP_CHRONO:
			dialog = new ProgressDialog(this);
			((ProgressDialog) dialog).setMessage(STOP_CHRONO_MESSAGE);
			break;
			
		default:
			dialog = null;
		}
		
		return dialog;
	}
	
	private void initStartJourney() {
		
		introLabel.setText(START_JOURNEY_LABEL);
		chronometerControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if ( estacioOrigen.getSelectedItemPosition() == estacioDesti.getSelectedItemPosition() )
					showDialog(ORIGEN_DESTI_MATCH);
				
				else {
					
					showDialog(START_CHRONO);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							handler.post(start);
							dismissDialog(START_CHRONO);
						}
					}).start();
					
				}
			}
		});
		
	}
	
	private void initFinishJourney() {
		
		introLabel.setText(FINISH_JOURNEY_LABEL);
		setStopChronoButton();
		
		long startTime = chronoPreferences.getLong("startTime", 0);
		long elapsedTime = System.currentTimeMillis() - startTime;
		
		chrono.setBase(SystemClock.elapsedRealtime() - elapsedTime);
		chrono.start();
		
	}
	
	private void setStopChronoButton() {
		
		chronometerControl.setText(R.string.stop_chronometer);
		chronometerControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				showDialog(STOP_CHRONO);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						handler.post(stop);
						dismissDialog(STOP_CHRONO);
					}
				}).start();
				
			}
		});
		
	}
	
	private void startChronometer() {
		
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.start();
		
		int routeId = randomGenerator.nextInt(MAX_RNG);
	
		SharedPreferences.Editor prefsEditor = chronoPreferences.edit();
		prefsEditor.putLong("startTime", System.currentTimeMillis());
		prefsEditor.putInt("routeId", routeId);
		prefsEditor.putBoolean("chronoIsRunning", true);
		int selectedLine = transportType.getSelectedItemPosition();
		prefsEditor.putInt("servei", selectedLine);
		int selectedOrigin = estacioOrigen.getSelectedItemPosition();
		prefsEditor.putInt("origen", selectedOrigin);
		int selectedDestination = estacioDesti.getSelectedItemPosition();
		prefsEditor.putInt("desti", selectedDestination);
		prefsEditor.commit();
	
		setStopChronoButton();
		recordRoute
		(
				routeId,
				username, 
				(String) estacioOrigen.getSelectedItem(), 
				(String) estacioDesti.getSelectedItem(),
				transportType.getSelectedItemPosition(),
				System.currentTimeMillis()
		);
		
	}
	
	private void stopChronometer() {
		
		chrono.stop();
		
		SharedPreferences.Editor prefsEditor = chronoPreferences.edit();
		prefsEditor.putBoolean("chronoIsRunning", false);
		prefsEditor.putInt("servei", 0);
		prefsEditor.putInt("origen", 0);
		prefsEditor.putInt("desti", 0);
		prefsEditor.commit();
		
		int routeId = chronoPreferences.getInt("routeId", 0);
		endOfRoute(routeId, username, System.currentTimeMillis());
		
	}
	
	private void recordRoute(int routeId, String username, String beginStation, String endStation, 
			int service, long startTime) {
		
		NetworkUtils.recordRoute(routeId, username, beginStation, endStation, service, startTime);
		
	}
	
	private void endOfRoute(int routeId, String username, long stopTime) {
		
		String json = NetworkUtils.endOfRoute(routeId, username, stopTime);
		String message;
		try {
			JSONObject response = new JSONObject(json);
			int duration = response.getInt("duration") / 1000;
			int expectedD = response.getInt("expectedDuration") / 1000;
			
			//String durationMin = (duration/60) < 10 ? "0" + (duration/60) : "" + (duration/60);
			String durationSec = (duration%60) < 10 ? "0" + (duration%60) : "" + (duration%60);
			
			//String expectedMin = (expectedD/60) < 10 ? "0" + (expectedD/60) : "" + (expectedD/60);
			String expectedSec = (expectedD%60) < 10 ? "0" + (expectedD%60) : "" + (expectedD%60);
			
			message = "Trajecte finalitzat " +
				estacioOrigen.getSelectedItem() + " - " + estacioDesti.getSelectedItem() + "\n" + 
				"Durada del trajecte: " + (duration/60) + "min" + durationSec + "s\n" +
				"Durada esperada: " + (expectedD/60) + "min" + expectedSec + "s";
			
		} catch (Exception e) {
			message = "Trajecte finalitzat " +
				estacioOrigen.getSelectedItem() + " - " + estacioDesti.getSelectedItem() + "\n" + 
				"Durada del trajecte: " + chrono.getText() + "\n" +
				"Durada esperada: " + "<sense determinar>";
		}
		
		END_OF_ROUTE_LABEL = message;
		showDialog(END_OF_ROUTE);
	}
	
	private String getResourceName(int id) {
		
		switch (id) {
		case 0:
			return "renfe_c1_line";
			
		case 1:
			return "renfe_c2_nord_line";
			
		case 2:
			return "renfe_c2_sud_line";
			
		case 3:
			return "renfe_c3_line";
			
		case 4:
			return "renfe_c4_line";
			
		case 5:
			return "renfe_c7_line";
			
		case 6:
			return "renfe_c8_line";
			
		case 7:
			return "fgc_l6_line";
			
		case 8:
			return "fgc_s1_line";
			
		case 9:
			return "fgc_s55_line";
			
		case 10:
			return "fgc_s5_line";
			
		case 11:
			return "fgc_s2_line";
			
		case 12:
			return "fgc_l7_line";
			
		case 13:
			return "metro_l1_line";
			
		case 14:
			return "metro_l2_line";
			
		case 15:
			return "metro_l3_line";
			
		case 16:
			return "metro_l4_line";
			
		case 17:
			return "metro_l5_line";
			
		case 18:
			return "metro_l9_line";
			
		case 19:
			return "metro_l10_line";
		
		case 20:
			return "metro_l11_line";
			
		default:
			return "";
		}
	}
}
