package org.uab.android.eventreporter.utils;


public class FavoriteArray {
	public static final int MAX_FAVORITE_LINES = 3;
	private int[] favoriteLinesArray;
	
	public FavoriteArray() {
		favoriteLinesArray = new int[MAX_FAVORITE_LINES];
		favoriteLinesArray[0] = -1;
		favoriteLinesArray[1] = -1;
		favoriteLinesArray[2] = -1;
	}
	
	public FavoriteArray(int[] array) {
		favoriteLinesArray = array;
	}
	
	public int add(int lineId, int max) {
		int index = getFirstAvailableIndex(max);
		if ( index!=-1 ) {
			favoriteLinesArray[index] = lineId;
			return 1;
		}
		return -1;
	}
	
	public boolean contains(int lineId) {
		for ( int i=0; i<MAX_FAVORITE_LINES; i++ ) {
			if ( favoriteLinesArray[i]==lineId ) {
				return true;			}
		}
		
		return false;
	}
	
	public int getFirstAvailableIndex(int max) {
		for ( int i=0; i<max; i++ ) {
			if ( favoriteLinesArray[i]==-1 ) {
				return i;
			}
		}
		
		return -1;
	}
	
	public int length() {
		int num = 0;
		for ( int i=0; i<MAX_FAVORITE_LINES; i++ ) {
			if ( favoriteLinesArray[i]!=-1 ) {
				num++;			}
		}
		
		return num;
	}
	
	public void reset() {
		for ( int i=0; i<MAX_FAVORITE_LINES; i++ ) {
			favoriteLinesArray[i] = -1;
		}
	}
	
	public int[] elements() {
		return favoriteLinesArray;
	}
}
