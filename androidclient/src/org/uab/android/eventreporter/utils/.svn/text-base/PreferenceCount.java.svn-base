package org.uab.android.eventreporter.utils;

public class PreferenceCount {

	private static PreferenceCount _instance;
	private FavoriteArray renfeArray;
	private FavoriteArray fgcArray;
	private FavoriteArray metroArray;
	
	private PreferenceCount() {
		renfeArray = new FavoriteArray();
		fgcArray = new FavoriteArray();
		metroArray = new FavoriteArray();
	}
	
	public static PreferenceCount getInstance() {
		if ( _instance==null ) {
			_instance = new PreferenceCount();
		}
		
		return _instance;
	}
	
	public FavoriteArray getRenfeArray() {
		return renfeArray;
	}
	
	public FavoriteArray getFgcArray() {
		return fgcArray;
	}
	
	public FavoriteArray getMetroArray() {
		return metroArray;
	}
}
