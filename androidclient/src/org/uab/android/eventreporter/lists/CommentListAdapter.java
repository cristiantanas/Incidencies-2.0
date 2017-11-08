package org.uab.android.eventreporter.lists;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.uab.android.eventreporter.R;
import org.uab.android.eventreporter.utils.Comment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentListAdapter extends BaseAdapter {
	
	private ArrayList<Comment> commentList;
	private Context context;
	
	public CommentListAdapter(ArrayList<Comment> cList, Context cnt) {
		this.commentList = cList;
		this.context = cnt;
	}

	@Override
	public int getCount() {
		return commentList.size();
	}

	@Override
	public Comment getItem(int position) {
		return commentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// Si se crea aquí se va a crear un LayoutInflater cada vez que se 
		// crea una vista!!
		RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(context)
							.inflate(R.layout.comment_layout, parent, false);
		Comment comm = commentList.get(position);
		
		TextView date = (TextView) itemLayout.findViewById(R.id.date_time_info);
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT, Locale.FRANCE);
		date.setText(df.format(new Date(comm.getDate())));
		
		TextView comment = (TextView) itemLayout.findViewById(R.id.comment_desc);
		comment.setText(comm.getComment());
		
		return itemLayout;
	}

}
