package org.uab.android.eventreporter.lists;

import java.util.ArrayList;

import org.uab.android.eventreporter.R;
import org.uab.android.eventreporter.utils.Incident;
import org.uab.android.eventreporter.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IncidentListAdapter extends BaseAdapter {
	
	private ArrayList<Incident> incidentList;
	private Context context;

	public IncidentListAdapter(ArrayList<Incident> incidentList, Context context) {
		this.incidentList = incidentList;
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return incidentList.size();
	}

	@Override
	public Incident getItem(int position) {
		return incidentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return incidentList.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(context)
						.inflate(R.layout.incidents_row_layout, parent, false);
		Incident incident = incidentList.get(position);
		
		TextView station = (TextView) itemLayout.findViewById(R.id.station_name_label);
		station.setText("Incidència " + incident.getStationName());
		
		TextView time = (TextView) itemLayout.findViewById(R.id.incident_reporting_time);
		long lastReported = (System.currentTimeMillis() - 
				incident.getLastUpdateTime()) / Utils.MIN_TO_MILIS;
		time.setText("Reportada fa " + (int) lastReported + " minuts");
		
		ImageView confirmed = (ImageView) itemLayout.findViewById(R.id.confirmed_incident_icon);
		if ( incident.getStatus() == 0 ) {
			confirmed.setImageResource(R.drawable.i_unconfirmed);		}
		
		else if ( incident.getStatus() == 1 ) {
			confirmed.setImageResource(R.drawable.i_confirmed);		}
		
		else if ( incident.getStatus() == Utils.RSS_FEEDS ) {
			confirmed.setImageResource(R.drawable.i_rss);
		}
		
		return itemLayout;
	}

}
