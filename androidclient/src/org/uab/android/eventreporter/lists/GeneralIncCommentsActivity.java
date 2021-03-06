package org.uab.android.eventreporter.lists;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uab.android.eventreporter.R;
import org.uab.android.eventreporter.net.NetworkUtils;
import org.uab.android.eventreporter.utils.Comment;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GeneralIncCommentsActivity extends ListActivity {

	private int incidentId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view_layout);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		incidentId = getIntent().getExtras().getInt("incidentId");
		TextView tv = (TextView) findViewById(android.R.id.empty);
		tv.setText("No hi ha comentaris sobre la incidència");
		ArrayList<Comment> commentsList = loadComments(incidentId);
		setListAdapter(new CommentListAdapter(commentsList, this));
	}

	private ArrayList<Comment> loadComments(int id) {
		ArrayList<Comment> commentList = new ArrayList<Comment>();
		String json = NetworkUtils.getGeneralIncComments(id);
		try {
			JSONArray jArray = new JSONArray(json);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject comment = jArray.getJSONObject(i);
				Comment comm = new Comment(
						comment.getLong("date"),
						new String(Base64.decodeBase64(comment.getString("desc").getBytes())));
				commentList.add(comm);
			}
			
			return commentList;
			
		} catch (JSONException e) {
			Log.e("GENERAL_INC_COMMENTS_ACTIVITY", "JSONException accessing data " + e.getMessage());
			return null;
		}
	}
}
