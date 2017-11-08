package org.uab.android.eventreporter;

import org.uab.android.eventreporter.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainMenuActivity extends Activity {
	private Activity thisActivity = this;
	public static final int DIALOG_SELECT_MAP_ID = 0;
	public static final String DIALOG_SELECT_MAP_TITLE = "Com vol visualitzar les incidències?";
	public static final int DIALOG_NO_NETWORK_CONN = 1;
	
	private SharedPreferences customPreferences;
	
	private boolean exit = false;
	
	//private ImageView startCronometer;
	//private ImageView stopCronometer;
	private Button newEventButton = null;
	private Button viewEventsButton = null;
	private Button T11Button = null;
	private Button controlTemps;
	private ImageView twitter;
	
	private boolean cronometerHasBeenStarted;
	
	private String username = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_layout);
		
		Eula.show(thisActivity);
		
		//startCronometer = (ImageView) findViewById(R.id.startCronometer);
		//stopCronometer = (ImageView) findViewById(R.id.stopCronometer);
		newEventButton = (Button) findViewById(R.id.report_button);
		viewEventsButton = (Button) findViewById(R.id.view_events_button);
		T11Button = (Button) findViewById(R.id.t11_button);
		controlTemps = (Button) findViewById(R.id.controlTemps);
		twitter = (ImageView) findViewById(R.id.twitter_image);
	}
	
	@Override
	protected void onStart() {
		super.onStart();		
		customPreferences = getSharedPreferences(PreferencesActivity.CUSTOM_PREF_NAME, 
				Activity.MODE_PRIVATE);
		username = customPreferences.getString("username", null);
		if (username == null) {
			TelephonyManager telMng = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			username = Utils.md5(telMng.getDeviceId());
			if ( username==null ) {
				username = Long.toHexString(Double.doubleToLongBits(Math.random()));
			}
			SharedPreferences.Editor prefEditor = customPreferences.edit();
			prefEditor.putString("username", username);
			prefEditor.commit();
		}
		
//		cronometerHasBeenStarted = customPreferences.getBoolean("chronoIsRunning", false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!isNetworkEnabled()) {
			showDialog(DIALOG_NO_NETWORK_CONN);
			
		} else {			
			newEventButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					exit = false;
					startActivity(new Intent(thisActivity, NewIncidentActivity.class)
							.putExtra("username", username));
				}
			});
			
			viewEventsButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					exit = false;
					startActivity(new Intent(thisActivity, NewVisualizationOrderActivity.class)
							.putExtra("username", username));
				}
			});
			
			T11Button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					exit = false;
					startActivity(new Intent(thisActivity, T11Menu.class)
							.putExtra("username", username));
				}
			});
			
			twitter.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String URL = "https://twitter.com/#!/Incidencies20";
					Intent twitterIntent = new Intent(Intent.ACTION_VIEW)
							.setData(Uri.parse(URL));
					startActivity(twitterIntent);
				}
			});
			
			SharedPreferences chronoPrefs = getSharedPreferences("chronoPrefs", Activity.MODE_PRIVATE);
			cronometerHasBeenStarted = chronoPrefs.getBoolean("chronoIsRunning", false);
			
			controlTemps.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					exit = false;
					startActivity(new Intent(thisActivity, StartCronometer.class)
							.putExtra("username", username)
							.putExtra("startNewJourney", !cronometerHasBeenStarted));
				}
			});
			
//			startCronometer.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					exit = false;
//					startActivity(new Intent(thisActivity, StartCronometer.class)
//							.putExtra("username", username)
//							.putExtra("startNewJourney", true));
//					
////					stopCronometer.setVisibility(View.VISIBLE);
////					startCronometer.setVisibility(View.INVISIBLE);
//				}
//			});
			
//			stopCronometer.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					exit = false;
//					startActivity(new Intent(thisActivity, StartCronometer.class)
//							.putExtra("username", username)
//							.putExtra("startNewJourney", false));
//					
////					startCronometer.setVisibility(View.VISIBLE);
////					stopCronometer.setVisibility(View.INVISIBLE);
//				}
//			});
			
//			if ( cronometerHasBeenStarted ) { 	startCronometer.setVisibility(ImageView.INVISIBLE);
//												stopCronometer.setVisibility(ImageView.VISIBLE);
//																										}
//			else {	startCronometer.setVisibility(ImageView.VISIBLE);
//					stopCronometer.setVisibility(ImageView.INVISIBLE);
//																				}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
		switch (id) {			
		case DIALOG_NO_NETWORK_CONN:
			builder.setMessage(Utils.NO_NETWORK_CONNECTION)
			   	   .setCancelable(false)
			   	   .setNeutralButton(Utils.OK, new DialogInterface.OnClickListener() {
				
			   		   @Override
			   		   public void onClick(DialogInterface dialog, int which) {
			   			   startActivity(new Intent(Intent.ACTION_MAIN)
									.addCategory(Intent.CATEGORY_HOME));
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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preferences_menu_layout, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			exit = false;
			startActivity(new Intent(thisActivity, PreferencesActivity.class));
			return true;
			
		case R.id.menu_about:
			exit = false;
			startActivity(new Intent(thisActivity, AboutActivity.class));
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (exit) {
				finish();			}
			
			else {
				exit = true;
				Toast.makeText(thisActivity, "Prèmer un altre cop per sortir", 
						Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private boolean isNetworkEnabled() {
		ConnectivityManager connectMng = 
			(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = connectMng.getActiveNetworkInfo();
		
		if ((info == null) || (!info.isConnectedOrConnecting())) {
			return false;
		} 
			
		return true;
	}
}
