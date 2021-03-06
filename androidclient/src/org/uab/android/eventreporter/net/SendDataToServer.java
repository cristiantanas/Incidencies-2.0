package org.uab.android.eventreporter.net;

import java.io.IOException;

import org.uab.android.eventreporter.GeneralIncDetailActivity;
import org.uab.android.eventreporter.GeneralReportActivity;
import org.uab.android.eventreporter.IncidentDetailActivity;
import org.uab.android.eventreporter.IncidentRSSActivity;
import org.uab.android.eventreporter.MainActivity;
import org.uab.android.eventreporter.MainMenuActivity;
import org.uab.android.eventreporter.ReportSendActivity;
import org.uab.android.eventreporter.T11DropActivity;
import org.uab.android.eventreporter.utils.Utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class SendDataToServer extends AsyncTask<String, Void, String> {
	public static final String CLASSTAG = SendDataToServer.class.getSimpleName().toUpperCase();
	
	private static ServerConnection server = new ServerConnection();
	private static int incidentId = -1;
	private String username;
	 
	private Context context = null;
	private ProgressDialog loadProgress = null;
	
	public SendDataToServer(Context context) {
		this.loadProgress = new ProgressDialog(context);
		this.context = context;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		loadProgress.setMessage("Connectant amb el servidor...");
		loadProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel·lar", 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancel(true);
					}
				});
		loadProgress.show();
	}
	
	@Override
	protected String doInBackground(String... params) {
		String result = "";
		
		try {
			server.connect();
			server.send(params[0]);
			
			String response;
			while ((response = server.recv()) != null) {
				String[] respContents = response.split(Utils.DATAGRAM_SEPARATOR);
				int respCode = NetworkUtils.analizeResponseHeader(respContents[0]);
				int options = NetworkUtils.getOptions(respContents[0]);
				int payloadLength = NetworkUtils.getPayloadLength(respContents[0]);				
				
				if (respCode == Utils.DATA_RECEIVED_CORECTLY) {
					if (payloadLength == 1) {
						incidentId = Integer.parseInt(respContents[1]);				}
					result = Utils.INFORMATION_OK;
					server.send("EXIT" + "\n");
					
				} else if (respCode == Utils.T11_CORRECTLY_UPDATED) {
					result = "T-11 agafada. Bon viatge!";
					server.send("EXIT" + "\n");
					
				} else if (respCode == Utils.T11_RECEIVED_CORECTLY) {
					result = "La teva T-11 s'ha deixat correctament. Gràcies.";
					
					if ( options==Utils.DATAGRAM_T11_PICKED_UP_OPTION ) {
						result += "\n\n" + Utils.T11_PICKED_UP_NOTIFICATION;
					}
						
					server.send("EXIT" + "\n");
					
				} else if (respCode == Utils.ALREADY_CONFIRMED_EVENT) { 
					result = Utils.CONFIRMED_EVENT;
					server.send("EXIT" + "\n");
					
				}else if (respCode == Utils.BAD_INPUT_FORMAT) {
					result = Utils.FORMAT_ERROR;
					server.send("EXIT" + "\n");
					
				} else if (respCode == Utils.INVALID_XML_FORMAT) {
					result = Utils.XML_MALFORMED;
					server.send("EXIT" + "\n");
					
				} else if (respCode == Utils.SERVER_ERROR) {
					result = Utils.SERVER_RECEIVE_FAIL;
					server.send("EXIT" + "\n");					
				}
			}

			Log.d(CLASSTAG, "Received data from server - " +  result);
			server.close();
			
		} catch (IOException e) {
			Log.e(CLASSTAG, "IOException with socket - " + e.getMessage());
			return null;
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		loadProgress.dismiss();
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		
		if (result == null) {
			alert.setMessage(Utils.SERVER_CONNECTION_FAIL)
				 .setCancelable(false)
				 .setNegativeButton(Utils.DISCARD, new DialogInterface.OnClickListener() {

					 @Override
					 public void onClick(DialogInterface dialog, int which) {
						 context.startActivity(new Intent(context.getApplicationContext(),
								 MainMenuActivity.class)
						 			.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
						 			.putExtra("username", 
						 					  ((ReportSendActivity) context).getUsername()));
						 dialog.cancel();
					 }
				 });
			alert.show();
			
		} else if (result.equalsIgnoreCase(Utils.SERVER_RECEIVE_FAIL)) {
			if ( context instanceof ReportSendActivity ) {
				username = ((ReportSendActivity) context).getUsername();
			} else if ( context instanceof GeneralReportActivity ) {
				username = ((GeneralReportActivity) context).getUsername();
			}
			alert.setMessage(result)
			 .setCancelable(false)
			 .setNegativeButton(Utils.DISCARD, new DialogInterface.OnClickListener() {

				 @Override
				 public void onClick(DialogInterface dialog, int which) {
					 context.startActivity(new Intent(context.getApplicationContext(),
							 MainMenuActivity.class)
					 			.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
					 			.putExtra("username", username));
					 dialog.cancel();
				 }
			 });
		alert.show();
			
		} else {
			alert.setMessage(result)
				 .setCancelable(false);
			
			if (context instanceof ReportSendActivity) {
				alert.setPositiveButton("Veure detall", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							context.startActivity(new Intent(context.getApplicationContext(), 
									IncidentDetailActivity.class)
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
										.putExtra("incidentId", incidentId)
										.putExtra("username", 
												  ((ReportSendActivity) context).getUsername())
										.putExtra("action", Utils.CLEAR_STACK));
							dialog.cancel();
						}
					 })
					 .setNegativeButton("Menú principal", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							context.startActivity(new Intent(context.getApplicationContext(), 
									MainMenuActivity.class)
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
										.putExtra("username", 
												  ((ReportSendActivity) context).getUsername()));
							dialog.cancel();
						}
					});
				
			} else if (context instanceof GeneralReportActivity) {
				alert.setPositiveButton("Veure detall", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							context.startActivity(new Intent(context.getApplicationContext(), 
									GeneralIncDetailActivity.class)
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
										.putExtra("incidentId", incidentId)
										.putExtra("username", ((GeneralReportActivity) context).getUsername())
										.putExtra("action", Utils.CLEAR_STACK));
							dialog.cancel();
						}
					})
					.setNegativeButton("Menú principal", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							context.startActivity(new Intent(context.getApplicationContext(), 
									MainMenuActivity.class)
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
										.putExtra("username", 
												  ((GeneralReportActivity) context).getUsername()));
							dialog.cancel();
						}
					});
				
			} else if (context instanceof T11DropActivity) {
				alert.setNeutralButton("Menú principal", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							context.startActivity(new Intent(context.getApplicationContext(), 
									MainMenuActivity.class)
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
										.putExtra("username", ((T11DropActivity) context).getUsername()));
							dialog.cancel();
						}
					});
				
			} else if (context instanceof MainActivity) {
				alert.setNeutralButton(Utils.OK, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if ( ((MainActivity) context).getT11Only() )
							((MainActivity) context).loadT11Locations();
						
						else
							((MainActivity) context).loadIncidents();
						
						dialog.cancel();
					}
				});
				
			} else if (context instanceof IncidentDetailActivity) {
				alert.setNeutralButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						((IncidentDetailActivity) context).finish();
					}
				});
				
			} else if (context instanceof IncidentRSSActivity) {
				alert.setNeutralButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						((IncidentRSSActivity) context).exit();
					}
				});
				
			} else if (context instanceof GeneralIncDetailActivity) {
				alert.setNeutralButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						((GeneralIncDetailActivity) context).finish();
					}
				});
			}
			
			alert.show();
		}
	}
}
