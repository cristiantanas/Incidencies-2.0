package org.uab.android.eventreporter;

import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	private Thread authThread = null;
	private Handler handler = new Handler();
	private String username;
	private EditText usernameEdit;
	private String password;
	private EditText passwordEdit;
	private CheckBox anonimousUser;
	private TextView message;
	private TextView errorMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.login_layout);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.logo);
		
		usernameEdit = (EditText) findViewById(R.id.username_edit);
		passwordEdit = (EditText) findViewById(R.id.password_edit);
		anonimousUser = (CheckBox) findViewById(R.id.anonimous_user);
		message = (TextView) findViewById(R.id.message);
		errorMessage = (TextView) findViewById(R.id.message_bottom);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		anonimousUser.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				usernameEdit.setEnabled(!isChecked);
				passwordEdit.setEnabled(!isChecked);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		String username = getIntent().getStringExtra("username");
		if (username != null) {
			usernameEdit.setText(username);
			getIntent().removeExtra("username");
			
		} else {		
			SharedPreferences pref = getSharedPreferences(PreferencesActivity.CUSTOM_PREF_NAME, 
					MODE_PRIVATE);
			username = pref.getString(PREF_USERNAME, null);
		
			if (username != null) {
				this.username = username;
				startActivity(new Intent(this, MainMenuActivity.class)
						.putExtra("username", username));
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage(getString(R.string.ui_login_authenticating));
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (authThread != null) {
					authThread.interrupt();
					finish();
				}
			}
		});
		return dialog;
	}
	
	public void handleLogin(View view) {
		if (anonimousUser.isChecked()) {
			loginAnonimousUser();
			
		} else {
			username = usernameEdit.getText().toString();
			password = passwordEdit.getText().toString();
			
			if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
				message.setText(getString(R.string.login_newaccount_text));
			} else {
				showDialog(0);
			
				// Start authenticating...
				authThread = NetworkUtils.attemptAuth(username, password, handler, this);
			}
		}
	}
	
	private void loginAnonimousUser() {
		username = Long.toHexString(Double.doubleToLongBits(Math.random()));
		SharedPreferences pref = getSharedPreferences(PreferencesActivity.CUSTOM_PREF_NAME, 
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putString(PREF_USERNAME, username);
		prefEditor.remove("email");
		prefEditor.commit();
		startActivity(new Intent(this, MainMenuActivity.class)
					.putExtra("username", username));
	}
	
	public void onAuthenticationResult(int result) {
		dismissDialog(0);
		
		switch (result) {
		case Utils.USER_ACCESS_GRANTED:
			SharedPreferences pref = getSharedPreferences(PreferencesActivity.CUSTOM_PREF_NAME, 
					MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = pref.edit();
			prefEditor.putString(PREF_USERNAME, username);
			prefEditor.putString(PREF_PASSWORD, password);
			prefEditor.commit();
			startActivity(new Intent(this, MainMenuActivity.class)
					.putExtra("username", username));
			break;
			
		case Utils.USER_UNAUTHORIZED:
			errorMessage.setText(R.string.login_username_failed);
			errorMessage.setTextColor(Color.RED);
			break;
			
		case Utils.USER_PASSWORD_FAILED:
			errorMessage.setText(R.string.login_password_failed);
			errorMessage.setTextColor(Color.RED);
			break;
			
		case Utils.SERVER_ERROR:
			errorMessage.setText(R.string.login_server_failed);
			errorMessage.setTextColor(Color.RED);
			break;
		}
	}
}
