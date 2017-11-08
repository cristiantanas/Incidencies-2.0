package org.uab.android.eventreporter;

import org.uab.android.eventreporter.lists.ListPreferenceMultiSelect;
import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.utils.FavoriteArray;
import org.uab.android.eventreporter.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity {
	public static final String CUSTOM_PREF_NAME = "userPreferences";
	public static final int MAX_FAVORITE_LINES = 3;
	public static final int REGISTER_DIALOG = 0;
	public static final int UPDATE_ACCOUNT_DIALOG = 1;
	public static final int REGISTER_ERROR_DIALOG = 2;
	public static final int CONFIRM_PASSWD_NO_MATCH = 3;
	private Activity prefActivity = this;
	private SharedPreferences customPref;
	private SharedPreferences.Editor editor;
	private String[] favoriteLines = new String[] {"firstLine", "secondLine", "thirdLine"};
	private String summary;

	private FavoriteArray renfeArray;
	private FavoriteArray fgcArray;
	private FavoriteArray metroArray;

	public static int count = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		customPref = getSharedPreferences(CUSTOM_PREF_NAME, MODE_PRIVATE);
		editor = customPref.edit();
		
		Preference register = (Preference) findPreference("emailAddress");
		register.setSummary(customPref.getString("email", null));
		register.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				prefActivity.showDialog(PreferencesActivity.REGISTER_DIALOG);
				return true;
			}
		});
		
		ListPreferenceMultiSelect renfe = (ListPreferenceMultiSelect) findPreference("renfeMultiLines");
		summary = customPref.getString("renfeSummary", null);
		String val = summary != null ? summary : "Selecciona una línia de Rodalies";
		renfe.setSummary(val);
		renfe.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				renfeArray = new FavoriteArray(loadPreferences(Utils.RENFE_INCIDENTS));
				fgcArray = new FavoriteArray(loadPreferences(Utils.FGC_INCIDENTS));
				metroArray = new FavoriteArray(loadPreferences(Utils.METRO_INCIDENTS));
				String entry = ((StringBuffer)newValue).toString();
				String[] selected = ListPreferenceMultiSelect.parseStoredValue(entry);
				int maxAllowed = MAX_FAVORITE_LINES - fgcArray.length() - metroArray.length();
				if ( selected.length>maxAllowed ) {
					Toast.makeText(prefActivity, "Només és poden seleccionar 3 línies preferides!", 
							Toast.LENGTH_SHORT).show();
				}
				
				renfeArray.reset();
				ListPreferenceMultiSelect list = (ListPreferenceMultiSelect) preference;
				int index, ok;
				StringBuffer buf = new StringBuffer();
				StringBuffer newSelectValue = new StringBuffer();
				for ( int i=0; i<selected.length; i++ ) {
					index = list.findIndexOfValue(selected[i]);
					ok = renfeArray.add(Integer.parseInt(selected[i]), maxAllowed);
					if ( ok!=-1 ) {
						buf.append(list.getEntries()[index]).append(",");
						newSelectValue.append(selected[i]).append(":");
					}
					
					else {
						list.setCheckedDialogEntry(index, false);					}
				}
				savePreferences(Utils.RENFE_INCIDENTS, renfeArray.elements());
				savePreferencesToFile();
				
				String value = buf.toString();
				String setNewValue = newSelectValue.toString();
				if ( !value.equalsIgnoreCase("") ) {
					value = value.substring(0, value.length() - 1);
					setNewValue = setNewValue.substring(0, setNewValue.length()-1);
					
				} else {
					value = "Selecciona una línia de Rodalies";				}
				
				list.setValue(setNewValue);
				list.setSummary(value);	
				editor.putString("renfeSummary", value);
				editor.commit();
				return true;
			}
		});
		
		ListPreferenceMultiSelect fgc = (ListPreferenceMultiSelect) findPreference("fgcMultiLines");
		summary = customPref.getString("fgcSummary", null);
		val = summary != null ? summary : "Selecciona una línia de FGC";
		fgc.setSummary(val);
		fgc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				renfeArray = new FavoriteArray(loadPreferences(Utils.RENFE_INCIDENTS));
				fgcArray = new FavoriteArray(loadPreferences(Utils.FGC_INCIDENTS));
				metroArray = new FavoriteArray(loadPreferences(Utils.METRO_INCIDENTS));
				String entry = ((StringBuffer)newValue).toString();
				String[] selected = ListPreferenceMultiSelect.parseStoredValue(entry);
				int maxAllowed = MAX_FAVORITE_LINES - renfeArray.length() - metroArray.length();
				if ( selected.length>maxAllowed ) {
					Toast.makeText(prefActivity, "Només és poden seleccionar 3 línies preferides!", 
							Toast.LENGTH_SHORT).show();
				}
				
				fgcArray.reset();
				ListPreferenceMultiSelect list = (ListPreferenceMultiSelect) preference;
				int index, ok;
				StringBuffer buf = new StringBuffer();
				StringBuffer newSelectValue = new StringBuffer();
				for ( int i=0; i<selected.length; i++ ) {
					index = list.findIndexOfValue(selected[i]);
					ok = fgcArray.add(Integer.parseInt(selected[i]), maxAllowed);
					if ( ok!=-1 ) {
						buf.append(list.getEntries()[index]).append(",");
						newSelectValue.append(selected[i]).append(":");
					}
					
					else {
						list.setCheckedDialogEntry(index, false);					}
				}
				savePreferences(Utils.FGC_INCIDENTS, fgcArray.elements());
				savePreferencesToFile();
				
				String value = buf.toString();
				String setNewValue = newSelectValue.toString();
				if ( !value.equalsIgnoreCase("") ) {
					value = value.substring(0, value.length() - 1);
					setNewValue = setNewValue.substring(0, setNewValue.length()-1);
					
				} else {
					value = "Selecciona una línia de FGC";				}
				
				list.setValue(setNewValue);
				list.setSummary(value);	
				editor.putString("fgcSummary", value);
				editor.commit();
				return true;
			}
		});
		
		ListPreferenceMultiSelect metro = (ListPreferenceMultiSelect) findPreference("metroMultiLines");
		summary = customPref.getString("metroSummary", null);
		val = summary != null ? summary : "Selecciona una línia de Metro";
		metro.setSummary(val);
		metro.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				renfeArray = new FavoriteArray(loadPreferences(Utils.RENFE_INCIDENTS));
				fgcArray = new FavoriteArray(loadPreferences(Utils.FGC_INCIDENTS));
				metroArray = new FavoriteArray(loadPreferences(Utils.METRO_INCIDENTS));
				String entry = ((StringBuffer)newValue).toString();
				String[] selected = ListPreferenceMultiSelect.parseStoredValue(entry);
				int maxAllowed = MAX_FAVORITE_LINES - renfeArray.length() - fgcArray.length();
				if ( selected.length>maxAllowed ) {
					Toast.makeText(prefActivity, "Només és poden seleccionar 3 línies preferides!", 
							Toast.LENGTH_SHORT).show();
				}
				
				metroArray.reset();
				ListPreferenceMultiSelect list = (ListPreferenceMultiSelect) preference;
				int index, ok;
				StringBuffer buf = new StringBuffer();
				StringBuffer newSelectValue = new StringBuffer();
				for ( int i=0; i<selected.length; i++ ) {
					index = list.findIndexOfValue(selected[i]);
					ok = metroArray.add(Integer.parseInt(selected[i]), maxAllowed);
					if ( ok!=-1 ) {
						buf.append(list.getEntries()[index]).append(",");
						newSelectValue.append(selected[i]).append(":");
					}
					
					else {
						list.setCheckedDialogEntry(index, false);					}
				}
				savePreferences(Utils.METRO_INCIDENTS, metroArray.elements());
				savePreferencesToFile();
				
				String value = buf.toString();
				String setNewValue = newSelectValue.toString();
				if ( ! value.equalsIgnoreCase("") ) {
					value = value.substring(0, value.length() - 1);
					setNewValue = setNewValue.substring(0, setNewValue.length()-1);
					
				} else {
					value = "Selecciona una línia de Metro";				}
				
				list.setValue(setNewValue);
				list.setSummary(value);	
				editor.putString("metroSummary", value);
				editor.commit();
				return true;
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case REGISTER_DIALOG:
			dialog = new Dialog(prefActivity);
			dialog.setContentView(R.layout.register_layout);
			dialog.setTitle("Registrar-se");
			
			final EditText email = (EditText) dialog.findViewById(R.id.email_register_edit);
			final EditText passwd = (EditText) dialog.findViewById(R.id.password_register_edit);
			final EditText confirm_passwd = (EditText) dialog.findViewById(R.id.confirm_passwd_edit);
			Button register = (Button) dialog.findViewById(R.id.register_button);
			register.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!passwd.getText().toString()
							.equalsIgnoreCase(confirm_passwd.getText().toString())) {
						showDialog(CONFIRM_PASSWD_NO_MATCH);
						
					} else {
						dismissDialog(REGISTER_DIALOG);
						showDialog(UPDATE_ACCOUNT_DIALOG);
						String username = customPref.getString("username", "");
						int result = NetworkUtils.updateUserAccount(
								username,
								email.getText().toString(), 
								passwd.getText().toString());
					
						if (result != -1) {
							dismissDialog(UPDATE_ACCOUNT_DIALOG);
							editor.putString("email", email.getText().toString());
							editor.commit();
						
							Preference registerPref = (Preference) findPreference("emailAddress");
							registerPref.setSummary(email.getText().toString());
						
						} else {
							dismissDialog(UPDATE_ACCOUNT_DIALOG);
							showDialog(REGISTER_ERROR_DIALOG);
						}
					}
				}
			});
			
			Button cancel = (Button) dialog.findViewById(R.id.cancel_button);
			cancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismissDialog(PreferencesActivity.REGISTER_DIALOG);
				}
			});
			break;
			
		case UPDATE_ACCOUNT_DIALOG:
			dialog = new ProgressDialog(prefActivity);
			((ProgressDialog) dialog).setMessage("Actualitzant informació...");
			((ProgressDialog) dialog).setIndeterminate(true);
			break;
			
		case REGISTER_ERROR_DIALOG:
			dialog = new AlertDialog.Builder(prefActivity)
						.setMessage("El registre no s'ha pogut dur a terme amb éxit. Per favor torni a intentar-ho més tard.")
						.setCancelable(false)
						.setNegativeButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();
			break;
			
		case CONFIRM_PASSWD_NO_MATCH:
			dialog = new AlertDialog.Builder(prefActivity)
						.setMessage("La contrasenya no coincideix!")
						.setCancelable(false)
						.setNegativeButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();
			break;
			
		default:
			dialog = null;
		}
		
		return dialog;
	}
	
	private void savePreferencesToFile() {
		int j=0;
		
		for ( int i=0; i<favoriteLines.length; i++ ) {
			editor.remove(favoriteLines[i]);		}
		
		for ( int r : renfeArray.elements() ) {
			if ( r!=-1 ) {
				editor.putInt(favoriteLines[j], r); j++;			}
		}
		
		for ( int f : fgcArray.elements() ) {
			if ( f!=-1 ) {
				editor.putInt(favoriteLines[j], f); j++;			}
		}
		
		for ( int m : metroArray.elements() ) {
			if ( m!=-1 ) {
				editor.putInt(favoriteLines[j], m); j++;			}
		}
	}
	
	private void savePreferences(int preference, int[] values) {
		switch (preference) {
		case Utils.RENFE_INCIDENTS:
			for ( int i=0; i<values.length; i++ ) {
				editor.putInt("renfeArray_"+i, values[i]);			}
			break;
			
		case Utils.FGC_INCIDENTS:
			for ( int i=0; i<values.length; i++ ) {
				editor.putInt("fgcArray_"+i, values[i]);			}
			break;
			
		case Utils.METRO_INCIDENTS:
			for ( int i=0; i<values.length; i++ ) {
				editor.putInt("metroArray_"+i, values[i]);			}
			break;
		}
	}
	
	private int[] loadPreferences(int preference) {
		int[] array = new int[FavoriteArray.MAX_FAVORITE_LINES];
		switch (preference) {
		case Utils.RENFE_INCIDENTS:
			for ( int i=0; i<array.length; i++ ) {
				array[i] = customPref.getInt("renfeArray_"+i, -1);			}
			break;
			
		case Utils.FGC_INCIDENTS:
			for ( int i=0; i<array.length; i++ ) {
				array[i] = customPref.getInt("fgcArray_"+i, -1);			}
			break;
			
		case Utils.METRO_INCIDENTS:
			for ( int i=0; i<array.length; i++ ) {
				array[i] = customPref.getInt("metroArray_"+i, -1);			}
			break;
		}
		return array;
	}
}
