package io.wandr_app.pokemongosocial;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Location listener to get the current location.
 * Created by Achi Jones on 7/11/2016.
 */
public class MyLocationListener implements LocationListener {

    private double latitude;
    private double longitude;
    private Location currLocation;
    private MapsActivity mapsAct;

    public MyLocationListener(MapsActivity mapsAct) {
        super();
        this.mapsAct = mapsAct;
    }

    public Location getCurrLocation() {
        return currLocation;
    }

    @Override
    public void onLocationChanged(Location loc) {
        longitude = loc.getLongitude();
        latitude = loc.getLatitude();
        System.out.println("New lat/long: " + latitude + ", " + longitude);
        currLocation = loc;
        mapsAct.makeRequestGetNearbyPosts();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}