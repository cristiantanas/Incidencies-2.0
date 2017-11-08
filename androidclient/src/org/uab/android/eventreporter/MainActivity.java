package org.uab.android.eventreporter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.utils.RouteContentManager;
import org.uab.android.eventreporter.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity implements LocationListener {
	public static final String CLASSTAG = MainActivity.class.getSimpleName().toUpperCase();
	
	private Activity thisActivity = this;
	public static final int DIALOG_CONFIRMATION = 0;
	public static final String DIALOG_CONFIRMATION_TITLE = "Confirmar incidència";
	public static final int DIALOG_GENERAL_INC_CONFIRMATION = 1;
	public static final int DIALOG_FILTER_BY_SERVICE = 2;
	public static final String DIALOG_FILTER_BY_SERVICE_TITLE = "Incidències";
	public static final int DIALOG_PICK_UP_T11 = 3;
	public static final String DIALOG_PICK_UP_T11_TITLE = "Agafar la T-11";
	public static final int DIALOG_INTERNET_CONN_PROBLEM = 4;
	private static final int INITIAL_ZOOM_LEVEL = 12;
	private static final int MY_LOCATION_ZOOM_LEVEL = 12;
	private static final int T11_ZOOM_LEVEL = 15;
	
	private RouteContentManager routeManager = new RouteContentManager();
	private Handler handler = new Handler();
	private MapView map = null;
	private MapController mapController = null;
	private List<Overlay> mapOverlays = null;
	private ArrayList<PrintableEventsItemizedOverlay> itemizedOverlays = 
				new ArrayList<PrintableEventsItemizedOverlay>();
	private boolean atLeastOneIncident = false;
	private boolean atLeastOneGenIncident = false;
	private boolean atLeastOneT11 = false;
	private PrintableEventsItemizedOverlay generalIncOverlay;
	private MyLocationItemizedOverlay itemizedLocation = null;
	private MyLocationOverlay myLocation = null;
	private LocationManager locationMng = null;
	private Location currentLoc = null;
	private String username = "";
	private int confirmationId = -1;
	private double incidentLatitude = .0;
	private double incidentLongitude = .0;
	private double userLatitude = .0;
	private double userLongitude = .0;
	private int incidentType;
	
	private boolean t11Only;
	private T11ItemizedOverlay t11Overlay;
	
	public String getUsername() {
		return this.username;
	}
	
	public void setConfirmationId(int id) {
		this.confirmationId = id;
	}
	
	public boolean getT11Only() {
		return this.t11Only;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);
		
		map = (MapView) findViewById(R.id.main_view);
		mapController = map.getController();
		mapOverlays = map.getOverlays();
		
		itemizedLocation = new MyLocationItemizedOverlay(this.getResources().getDrawable(R.drawable.red_pushpin), 
														 this);
		generalIncOverlay = new PrintableEventsItemizedOverlay(this.getResources().getDrawable(R.drawable.gi_marker), 
				   											   thisActivity);
		t11Overlay = new T11ItemizedOverlay(getResources().getDrawable(R.drawable.ticket), 
				this);
		
		PrintableEventsItemizedOverlay renfeEvents = 
			new PrintableEventsItemizedOverlay(this.getResources().getDrawable(R.drawable.renfe_marker), 
											   thisActivity);
		PrintableEventsItemizedOverlay fgcEvents = 
			new PrintableEventsItemizedOverlay(this.getResources().getDrawable(R.drawable.fgc_marker), 
											   thisActivity);
		PrintableEventsItemizedOverlay metroEvents = 
			new PrintableEventsItemizedOverlay(this.getResources().getDrawable(R.drawable.metro_marker), 
											   thisActivity);
		PrintableEventsItemizedOverlay unconfirmedEvents = 
			new PrintableEventsItemizedOverlay(this.getResources().getDrawable(R.drawable.unconfirmed_marker), 
											   thisActivity);
			
		itemizedOverlays.add(renfeEvents);
		itemizedOverlays.add(fgcEvents);
		itemizedOverlays.add(metroEvents);
		itemizedOverlays.add(unconfirmedEvents);
		
		myLocation = new MyLocationOverlay(thisActivity, map);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		username = getIntent().getStringExtra("username");
		incidentLatitude = getIntent().getExtras().getDouble("latitude");
		incidentLongitude = getIntent().getExtras().getDouble("longitude");
		userLatitude = getIntent().getExtras().getDouble("userLat");
		userLongitude = getIntent().getExtras().getDouble("userLon");
		incidentType = getIntent().getIntExtra("incidentType", -1);
		t11Only = getIntent().getBooleanExtra("t11Only", false);
		
		map.setSatellite(false);
		map.setBuiltInZoomControls(true);
		
		locationMng = (LocationManager) getSystemService(LOCATION_SERVICE);		
		myLocation.enableMyLocation();
		myLocation.runOnFirstFix(new Runnable() {
			
			@Override
			public void run() {
				currentLoc = myLocation.getLastFix();
				if ( currentLoc!=null && incidentLatitude==.0 )
					centerMap(currentLoc.getLatitude(), currentLoc.getLongitude());
				
				myLocation.disableMyLocation();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if ((incidentLatitude != 0.0) && (incidentLongitude != 0.0)) {
			centerMap(incidentLatitude, incidentLongitude);		}
		
		else if (currentLoc != null) {
			centerMap(currentLoc.getLatitude(), currentLoc.getLongitude());		}
		
		else {
			centerMap(0.0, 0.0);		}
		
		locationMng.requestLocationUpdates(LocationManager.GPS_PROVIDER, Utils.UPDATE_MIN_TIME, 
				Utils.UPDATE_MIN_DISTANCE, this);

		if ( t11Only ) loadT11Locations();
		
		else loadIncidents();
	}
	
	public void loadIncidents() {
		mapOverlays.clear();
		map.postInvalidate();
		for (PrintableEventsItemizedOverlay i : itemizedOverlays) {
			i.clear();
		}
		
		String json = NetworkUtils.getIncidentsForLine(Utils.ALL_INCIDENTS, username, 
				userLatitude, userLongitude, handler, thisActivity);
		
		if ( json!=null ) {
			onJSONDataReceived(json);
		
			generalIncOverlay.clear();
			if ( currentLoc!=null ) {
				json = NetworkUtils.getGeneralIncidentsList(username, currentLoc.getLatitude(), 
						currentLoc.getLongitude());
			
			} else {
				json = NetworkUtils.getGeneralIncidentsList(username, .0, .0);
			}
			
			if ( json!=null )
				onGeneralIncidentsReceived(json);
			
			else
				showDialog(DIALOG_INTERNET_CONN_PROBLEM);
		}
		else {
			showDialog(DIALOG_INTERNET_CONN_PROBLEM);
		}
	}
	
	public void loadT11Locations() {
		mapOverlays.clear();
		map.postInvalidate();
		
		t11Overlay.clear();
		
		double lat = currentLoc!=null ? currentLoc.getLatitude() : .0;
		double lon = currentLoc!=null ? currentLoc.getLongitude() : .0;
		
		String json = NetworkUtils.getAvailableT11(username, lat, lon);
		
		if ( json!=null )
			onT11Received(json);
		
		else
			showDialog(DIALOG_INTERNET_CONN_PROBLEM);
	}

	private void centerMap(double latitude, double longitude) {
		if ((latitude != 0.0) && (longitude != 0.0)) {
			GeoPoint p = new GeoPoint((int)(latitude*1E6), (int)(longitude*1E6));
			mapController.setCenter(p);
			mapController.setZoom(MY_LOCATION_ZOOM_LEVEL);
			
		} else {
			GeoPoint p = new GeoPoint(41389173, 2175292);
			mapController.setCenter(p);
			mapController.setZoom(INITIAL_ZOOM_LEVEL);
		}
		
		if ( t11Only ) mapController.setZoom(T11_ZOOM_LEVEL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationMng.removeUpdates(this);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_layout, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.my_location:
			if (currentLoc != null) {
				GeoPoint p = new GeoPoint((int)(currentLoc.getLatitude()*1E6), 
						(int)(currentLoc.getLongitude()*1E6));
				OverlayItem overlay = new OverlayItem(p, "Posició aproximada", 
						"Latitud: " + (currentLoc.getLatitude()/1E6) + "\n" + 
						"Longitud: " + (currentLoc.getLongitude()/1E6) + "\n");
				itemizedLocation.addOverlay(overlay);
				mapOverlays.add(itemizedLocation);
				centerMap(currentLoc.getLatitude(), currentLoc.getLongitude());
				
			} else {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setMessage(Utils.NO_LOCATION_AVAILABLE)
					 .setCancelable(false)
					 .setPositiveButton("Menú principal", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(getApplicationContext(),MainMenuActivity.class)
									.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
									.putExtra("username", username));
						}
					})
					.setNegativeButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				alert.show();
			}
			return true;
			
		case R.id.layers:
			map.setSatellite(!map.isSatellite());
			return true;
			
		case R.id.view_events:
			showDialog(DIALOG_FILTER_BY_SERVICE);
			return true;
			
		case R.id.report:
			startActivity(new Intent(this, NewIncidentActivity.class)
					.putExtra("username", username));
			return true;
			
		case R.id.exit:
			startActivity(new Intent(thisActivity, MainMenuActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
					.putExtra("username", username));
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
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
					NetworkUtils.sendConfirmationToServer(confirmationId, d, username, thisActivity);
					thisActivity.dismissDialog(MainActivity.DIALOG_CONFIRMATION);
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
			
		case DIALOG_GENERAL_INC_CONFIRMATION:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.confirmation_dialog_layout);
			dialog.setTitle(DIALOG_CONFIRMATION_TITLE);
			
			final EditText generalIncComment = (EditText) dialog.findViewById(R.id.confirmation_description);
			Button submit = (Button) dialog.findViewById(R.id.confirmation_submit_button);
			submit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String desc = Utils.escape(generalIncComment.getText().toString());
					String d = new String(Base64.encodeBase64(desc.getBytes()));
					NetworkUtils.sendGeneralConfToServer(confirmationId, d, username, thisActivity);
					thisActivity.dismissDialog(MainActivity.DIALOG_GENERAL_INC_CONFIRMATION);
				}
			});
			
			Button cancel = (Button) dialog.findViewById(R.id.confirmation_cancel_button);
			cancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismissDialog(DIALOG_GENERAL_INC_CONFIRMATION);
				}
			});
			break;
			
		case DIALOG_FILTER_BY_SERVICE:
			boolean[] selected = new boolean[5];
			if ( incidentType==Utils.GENERAL_INCIDENTS ) {
				for ( int i=0; i<5; i++ ) {
					if ( i==4 ) 
						selected[i] = true;					
					else
						selected[i] = false;

				}
				
			} else {
				for ( int i=0; i<5; i++ ) {
					if ( i==4 ) 
						selected[i] = false;					
					else
						selected[i] = true;

				}
			}
			dialog = new AlertDialog.Builder(thisActivity)
							.setTitle(DIALOG_FILTER_BY_SERVICE_TITLE)
							.setMultiChoiceItems(R.array.map_events_list, selected, 
									new DialogInterface.OnMultiChoiceClickListener() {
						
										@Override
										public void onClick(DialogInterface dialog, int which, boolean isChecked) {
											processEventSelection(which, isChecked);
										}
							})
							.setPositiveButton(Utils.OK, new DialogInterface.OnClickListener() {
				
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
							})
							.create();
			break;
			
		case DIALOG_PICK_UP_T11:
			dialog = new AlertDialog.Builder(thisActivity)
							.setTitle(DIALOG_PICK_UP_T11_TITLE)
							.setMessage("Estàs a punt d'agafar la T-11. Estàs segur?")
							.setPositiveButton(Utils.TAKE, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									double lat = .0, lon = .0;
									if ( currentLoc!=null ) {
										lat = currentLoc.getLatitude();
										lon = currentLoc.getLongitude();
									}
									
									NetworkUtils.sendT11HasBeenPickedUp(confirmationId, username, lat, lon, thisActivity);
									dialog.cancel();
								}
							})
							.setNegativeButton(Utils.CANCEL, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							})
							.create();
			break;
			
		case DIALOG_INTERNET_CONN_PROBLEM:
			dialog = new AlertDialog.Builder(thisActivity)
							.setTitle("Ups.. error de connexió")
							.setMessage("No he pogut connectar amb el servidor :(. Potser la connexió a Internet és molt lenta o la xarxa no permet connexions externes.")
							.setNeutralButton("Ho intentaré més tard", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									thisActivity.finish();
								}
							})
							.create();
			break;
			
		default:
			dialog = null;
		}
		
		return dialog;
	}
	
	private void processEventSelection(int which, boolean isChecked) {
		if ( which<4 ) {
			if (isChecked) {
				if ( itemizedOverlays.get(which).size()>0 ) {
					mapOverlays.add(itemizedOverlays.get(which));
				}
				
			} else {
				mapOverlays.remove(itemizedOverlays.get(which));		
			}
		} else {
			if (isChecked) {
				if ( generalIncOverlay.size()>0 ) {
					mapOverlays.add(generalIncOverlay);
				}
				
			} else {
				mapOverlays.remove(generalIncOverlay);
			}
		}
		
		map.postInvalidate();
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLoc = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
	public void onJSONDataReceived(String json) {
		try {
			JSONArray jArray = new JSONArray(json);
			for (int i = 0; i < jArray.length(); i++) {
				JSONArray items = jArray.getJSONArray(i);
				parseJSONData(items);
			}
			
			if ( incidentType!=Utils.GENERAL_INCIDENTS ) {
				if (itemizedOverlays.get(itemizedOverlays.size()-1).size() > 0) {
					mapOverlays.add(itemizedOverlays.get(itemizedOverlays.size()-1));
					atLeastOneIncident = true;
				}
				map.postInvalidate();
				
				if (!atLeastOneIncident) {
					Toast noIncidents = Toast.makeText(thisActivity, 
							R.string.no_incidents, Toast.LENGTH_LONG);
					noIncidents.show();
				}
			}
			
		} catch (JSONException e) {
			Log.e(CLASSTAG, "JSONException parsing data " + e.getMessage());
		}
	}
	
	public void onGeneralIncidentsReceived(String json) {
		try {
			JSONArray jArray = new JSONArray(json);
			for ( int i=0; i<jArray.length(); i++ ) {
				JSONObject incident = jArray.getJSONObject(i);
				GeoPoint p = new GeoPoint((int)(incident.getDouble("latitude")*1E6), 
						(int)(incident.getDouble("longitude")*1E6));
				int time = (int)(System.currentTimeMillis() - incident.getLong("lastUpdate")) / Utils.MIN_TO_MILIS;
				OverlayItem overlayItem = new OverlayItem(p, "Incidència general", 
						new String(Base64.decodeBase64(incident.getString("description").getBytes())) + "\n" + 
						"Incidència reportada fa " + time + " minuts");
				generalIncOverlay.addOverlay(incident.getInt("id"), overlayItem);
			}
			
			if ( incidentType==Utils.GENERAL_INCIDENTS ) {
				if ( generalIncOverlay.size()>0 ) {
					mapOverlays.add(generalIncOverlay);
					atLeastOneGenIncident = true;
				}
				map.postInvalidate();
				
				if (!atLeastOneGenIncident) {
					Toast noIncidents = Toast.makeText(thisActivity, 
							R.string.no_incidents, Toast.LENGTH_LONG);
					noIncidents.show();
				}
			}
			
		} catch (JSONException e) {
			Log.e(CLASSTAG, "JSONException accessing data " + e.getMessage());
		}
	}
	
	public void onT11Received(String json) {
		try {
			JSONArray t11Array = new JSONArray(json);
			for ( int i=0; i<t11Array.length(); i++ ) {
				JSONObject T11 = t11Array.getJSONObject(i);
				GeoPoint p = new GeoPoint((int)(T11.getDouble("latitude")*1E6), 
						(int)(T11.getDouble("longitude")*1E6));
				int ETA = T11.getInt("eta");
				String description = 
					new String(Base64.decodeBase64(T11.getString("line_ref").getBytes())) + " " +
					new String(Base64.decodeBase64(T11.getString("desc").getBytes()));
				
				OverlayItem T11Point = new OverlayItem(p, 
						"T11 disponible", 
						description + "\n" +
						"Valida durant " + ETA + " minuts");
				t11Overlay.addOverlay(T11.getInt("id"), T11Point);
			}
			
			if ( t11Overlay.size()>0 ) {
				mapOverlays.add(t11Overlay);
				atLeastOneT11 = true;
			}
			map.postInvalidate();
			
			if (!atLeastOneT11) {
				Toast noIncidents = Toast.makeText(thisActivity, 
						R.string.no_T11_available, Toast.LENGTH_LONG);
				noIncidents.show();
			}
			
		} catch (JSONException e) {
			Log.e(CLASSTAG, "JSONException accessing data " + e.getMessage());
		}
	}
	
	private void parseJSONData(JSONArray jArray) {
		try {
			int service = jArray.getInt(0);
			
			for (int i = 1; i < jArray.length(); i++) {
				JSONObject incident = jArray.getJSONObject(i);
				GeoPoint p = new GeoPoint((int)(incident.getDouble("latitude")*1E6), 
							(int)(incident.getDouble("longitude")*1E6));
				int time = (int)(System.currentTimeMillis() - incident.getLong("lastUpdate")) / Utils.MIN_TO_MILIS;
				String direction = routeManager.getLineDirection(incident.getInt("line"), incident.getInt("direction"));
				OverlayItem overlayItem = new OverlayItem(p, Utils.typeToCharSeq(service), 
						"Estació: " + incident.getString("station") + "\n" + 
						"Direcció: " + direction + "\n" +
						"Causa: " + Utils.eventToCharSeq(incident.getInt("cause")) + "\n" + 
						"Incidència reportada fa: " + time  + " minuts" + "\n" + 
						"Temps aproximat de resolució: " + incident.getInt("ttl") + "\n" + 
						"Estat: " + Utils.stateToCharSeq(incident.getInt("status")));
				
				boolean isActive = (incident.getInt("status") == 1);
				if (isActive) {
					itemizedOverlays.get(service).addOverlay(incident.getInt("id"), overlayItem);
				} else {
					itemizedOverlays.get(itemizedOverlays.size()-1).addOverlay(incident.getInt("id"), overlayItem);
				}
			}
			
			if ( incidentType!=Utils.GENERAL_INCIDENTS ) {
				if (itemizedOverlays.get(service).size() > 0) {
					mapOverlays.add(itemizedOverlays.get(service));
					atLeastOneIncident = true;
				}
			}
			
		} catch (JSONException e) {
			Log.e(CLASSTAG, "JSONException accessing data " + e.getMessage());
		}
	}
	
	public void updateJSONData() {
		mapOverlays.clear();
		map.postInvalidate();
		for (PrintableEventsItemizedOverlay i : itemizedOverlays) {
			i.clear();
		}
		
		NetworkUtils.attemptEventSearch(Utils.ALL_INCIDENTS, handler, thisActivity);
	}
}