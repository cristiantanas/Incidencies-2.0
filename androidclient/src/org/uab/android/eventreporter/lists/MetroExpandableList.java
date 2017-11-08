package org.uab.android.eventreporter.lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uab.android.eventreporter.R;
import org.uab.android.eventreporter.ReportSendActivity;
import org.uab.android.eventreporter.utils.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class MetroExpandableList extends ExpandableListActivity {
	private static final int METRO_ID = 2;
	private static final String GROUP_ID = "group";
	private static final String CHILDREN_ID = "children";

	private String username = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		username = getIntent().getStringExtra("username");
		
		setListAdapter(new SimpleExpandableListAdapter(this, 
				getGroups(), 
				android.R.layout.simple_expandable_list_item_1,
				new String[] {GROUP_ID}, 
				new int[] {android.R.id.text1}, 
				getChildren(), 
				android.R.layout.simple_list_item_single_choice, 
				new String[] {CHILDREN_ID}, 
				new int[] {android.R.id.text1}));
		getExpandableListView().setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
								
				int uniqueLineId = METRO_ID * 10 + groupPosition;
				String stationName = ((TextView) v).getText().toString();
				
				startActivity(new Intent(getApplicationContext(), ReportSendActivity.class)
						.putExtra("username", username)
						.putExtra("service", Utils.METRO_INCIDENTS)
						.putExtra("line", uniqueLineId)
						.putExtra("station", stationName)
						.putExtra("stationHash", Utils.md5(stationName)));
				
				return true;
			}
		});
	}
	
	private List<Map<String,String>> getGroups() {
		String[] groups = getResources().getStringArray(R.array.metro_lines);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> map;
		
		for (String item : groups) {
			map = new HashMap<String,String>();
			map.put(GROUP_ID, item);
			list.add(map);
		}
		
		return list;
	}
	
	private List<List<Map<String,String>>> getChildren() {
		String[][] childrens = new String[][] {
				getResources().getStringArray(R.array.metro_l1_line),
				getResources().getStringArray(R.array.metro_l2_line),
				getResources().getStringArray(R.array.metro_l3_line),
				getResources().getStringArray(R.array.metro_l4_line),
				getResources().getStringArray(R.array.metro_l5_line),
				getResources().getStringArray(R.array.metro_l9_line),
				getResources().getStringArray(R.array.metro_l10_line),
				getResources().getStringArray(R.array.metro_l11_line)
		};
		List<List<Map<String,String>>> list = new ArrayList<List<Map<String,String>>>();
		List<Map<String,String>> sublist;
		Map<String,String> map;
		
		for (String[] items : childrens) {
			sublist = new ArrayList<Map<String,String>>();
			for (String item : items) {
				map = new HashMap<String,String>();
				map.put(CHILDREN_ID, item);
				sublist.add(map);
			}
			list.add(sublist);
		}
		
		return list;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		
		if (id == 0) {
			dialog = new ProgressDialog(this);
			((ProgressDialog) dialog).setMessage("Obtenint informació...");
			dialog.show();
			
		} else if (id == 1) {
			dialog = new AlertDialog.Builder(this)
						.setMessage("No s'ha pogut obtenir la informació en aquest moment! " +
								"Per favor, torni a intentar-ho més tard.")
						.setNeutralButton(Utils.DISCARD, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();
				
		}
		return dialog;
	}
}
