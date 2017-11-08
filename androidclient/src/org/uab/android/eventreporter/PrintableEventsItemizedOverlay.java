package org.uab.android.eventreporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class PrintableEventsItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private HashMap<Integer, OverlayItem> items = new HashMap<Integer, OverlayItem>();
	private Context displayContext = null;

	public PrintableEventsItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	
	public PrintableEventsItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		displayContext = context;
	}
	
	public void addOverlay(OverlayItem item) {
		overlays.add(item);
		populate();
	}
	
	public void addOverlay(int id, OverlayItem item) {
		items.put(id, item);
		populate();
	}
	
	public void clear() {
		items.clear();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		ArrayList<Integer> iList = toArrayList(items);
		return items.get(iList.get(i));
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	protected boolean onTap(int i) {
		ArrayList<Integer> iList = toArrayList(items);
		final int itemId = iList.get(i);
		final OverlayItem item = items.get(itemId);
		AlertDialog.Builder dialog = new AlertDialog.Builder(displayContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				((MainActivity) displayContext).setConfirmationId(itemId);
				if ( item.getTitle().equalsIgnoreCase("Incidència general") ) {
					((MainActivity) displayContext).showDialog(MainActivity.DIALOG_GENERAL_INC_CONFIRMATION);
					
				} else {
					((MainActivity) displayContext).showDialog(MainActivity.DIALOG_CONFIRMATION);
				}
			}
		});
		dialog.setNegativeButton("Veure detall", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if ( item.getTitle().equalsIgnoreCase("Incidència general") ) {
					displayContext.startActivity(new Intent(displayContext, GeneralIncDetailActivity.class)
						.putExtra("incidentId", itemId)
						.putExtra("requestUpdate", false)
						.putExtra("username", ((MainActivity) displayContext).getUsername()));
					
				} else {
					displayContext.startActivity(new Intent(displayContext, IncidentDetailActivity.class)
							.putExtra("incidentId", itemId)
							.putExtra("requestUpdate", true)
							.putExtra("username", ((MainActivity) displayContext).getUsername()));
				}
			}
		});
		dialog.show();
		return true;
	}
	
	private ArrayList<Integer> toArrayList(HashMap<Integer, OverlayItem> map) {
		ArrayList<Integer> iList = new ArrayList<Integer>();
		Iterator<Integer> it = items.keySet().iterator();
		
		while (it.hasNext()) {
			iList.add(it.next());		}
		
		return iList;
	}
}
