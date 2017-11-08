package org.uab.android.eventreporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.uab.android.eventreporter.utils.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class T11ItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	
	private Context displayContext;
	private HashMap<Integer, OverlayItem> items = new HashMap<Integer, OverlayItem>();

	public T11ItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	
	public T11ItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		displayContext = context;
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
		ArrayList<Integer> list = toArrayList(items);
		final int id = list.get(i);
		final OverlayItem item = items.get(id);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(displayContext);
		builder.setTitle(item.getTitle());
		builder.setMessage(item.getSnippet());
		builder.setPositiveButton(Utils.TAKE, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((MainActivity) displayContext).setConfirmationId(id);
				((MainActivity) displayContext).showDialog(MainActivity.DIALOG_PICK_UP_T11);
			}
		});
		builder.setNegativeButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
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
