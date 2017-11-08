package org.uab.android.eventreporter.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSLocationListener implements LocationListener {
	private Location currLocation = null;
	
	public Location getLocation() {
		return currLocation;
	}

	@Override
	public void onLocationChanged(Location location) {
		currLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		currLocation = null;
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
