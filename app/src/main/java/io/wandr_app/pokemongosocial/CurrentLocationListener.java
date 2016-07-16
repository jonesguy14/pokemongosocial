package io.wandr_app.pokemongosocial;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Location listener to get the current location.
 * Created by Achi Jones on 7/11/2016.
 */
public class CurrentLocationListener implements LocationListener {
    private Location currLocation;
    private MapsActivity mapsAct;

    public CurrentLocationListener(MapsActivity mapsAct) {
        super();
        this.mapsAct = mapsAct;
    }

    public Location getCurrLocation() {
        return currLocation;
    }

    @Override
    public void onLocationChanged(Location loc) {
        currLocation = loc;
        System.out.println("New lat/long: " + loc.getLatitude() + ", " + loc.getLongitude());
        mapsAct.makeRequestGetNearbyPosts();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
