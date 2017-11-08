package org.uab.android.eventreporter.lists;

import static org.uab.android.eventreporter.PreferencesActivity.count;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceMultiSelect extends ListPreference {

	private static final String SEPARATOR = ":"; 
	private boolean[] mClickedDialogEntryIndices;
	
	public ListPreferenceMultiSelect(Context context) {
		this(context, null);
	}
	
	public ListPreferenceMultiSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		mClickedDialogEntryIndices = new boolean[getEntries().length];
	}

	@Override
	public void setEntries(CharSequence[] entries) {
		super.setEntries(entries);
		mClickedDialogEntryIndices = new boolean[entries.length];
	}
	
	@Override
    protected void onPrepareDialogBuilder(Builder builder) {
    	CharSequence[] entries = getEntries();
    	CharSequence[] entryValues = getEntryValues();
    	
        if (entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices, 
                new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean val) {
						mClickedDialogEntryIndices[which] = val;	
						if (val) count++; else {
							count--;
						}
					}
        });
    }
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
		CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
        	StringBuffer value = new StringBuffer();
        	for ( int i=0; i<entryValues.length; i++ ) {
        		if ( mClickedDialogEntryIndices[i] ) {
        			value.append(entryValues[i]).append(SEPARATOR);
        			
        		}
        	}
        	
            if (callChangeListener(value)) {
            	String newValue = getValue();
            	String val = value.toString();
            	if ( val.length() > 0 )
            		val = val.substring(0, val.length()-SEPARATOR.length());
            	setValue(newValue);
            }
        }
    }
	
	private void restoreCheckedEntries() {
    	CharSequence[] entryValues = getEntryValues();
    	
    	if (getValue() != null) {
	    	String[] vals = parseStoredValue(getValue());
	    	if ( vals != null ) {
	        	for ( int j=0; j<vals.length; j++ ) {
	        		String val = vals[j].trim();
	            	for ( int i=0; i<entryValues.length; i++ ) {
	            		CharSequence entry = entryValues[i];
	                	if ( entry.equals(val) ) {
	            			mClickedDialogEntryIndices[i] = true;
	            			break;
	            		}
	            	}
	        	}
	    	}
    	}
    }
	
	public static String[] parseStoredValue(CharSequence val) {
		if ( "".equals(val) )
			return new String[] {};
		else
			return ((String)val).split(SEPARATOR);
    }
	
	public void setCheckedDialogEntry(int which, boolean val) {
		mClickedDialogEntryIndices[which] = val;
		count--;
	}
}
