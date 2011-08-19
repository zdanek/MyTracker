package pl.zdanek.android.mytrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.zdanek.android.mytrack.model.GeoPointSerializable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IInterface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MyTrackMapActivity extends MapActivity {

	private static final String tag = "MyTrackMapActivity";
    private static final int MENU_SET_SATELLITE = 1;
    private static final int MENU_SET_MAP = 2;
	private static final int MENU_CLEAR_MAP = 3;
	private static final int MENU_ANIMATE_TO_LATEST = 4;
	private static final String POINTS_LIST_BUNDLE_KEY = "POINTS_LIST_BUNDLE_KEY";
	private MapView mapView;
	private ZoomControls zoomControls;
    private MapController mapController;

    List<Overlay> mapOverlays;
    Drawable drawable;
    MyTrackItemizedOverlay itemizedOverlay;
    
	
    private final LocationListener locationListenerRecenterMap = new LocationListener() {

	        @Override
	        public void onLocationChanged(final Location loc) {
	            Log.v(tag, "locationProvider LOCATION CHANGED - " + loc);
	            int lat = (int) (loc.getLatitude() * LocationHelper.MILLION);
	            int lon = (int) (loc.getLongitude() * LocationHelper.MILLION);
	            // animate to new location
	            GeoPoint geoPoint = new GeoPoint(lat, lon);
	            mapController.animateTo(geoPoint);
	            
	            OverlayItem overlayitem = new OverlayItem(geoPoint, "", "");
	            itemizedOverlay.addOverlay(overlayitem);
	        }

	        public void onProviderDisabled(final String s) {
	        }

	        public void onProviderEnabled(final String s) {
	        }

	        public void onStatusChanged(final String s, final int i, final Bundle b) {
	        }
	    };
	private LocationManager locationManager;
	private LocationProvider locationProvider;
	private Drawable defaultMarker;
    


	@Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        Log.v(tag, "onCreate");
        this.setContentView(R.layout.map_view);

        mapView = (MapView) findViewById(R.id.mapvieww);
        mapView.setBuiltInZoomControls(true);
        
        mapOverlays = mapView.getOverlays();
        drawable = this.getResources().getDrawable(R.drawable.redpin);
        itemizedOverlay = new MyTrackItemizedOverlay(drawable, mapView);
        mapOverlays.add(itemizedOverlay);
        
//        this.zoom = (ViewGroup) findViewById(R.id.zoom);
//        this.zoom.addView(this.mapView.getZoomControls());

        this.defaultMarker = getResources().getDrawable(R.drawable.redpin);
        this.defaultMarker.setBounds(0, 0, this.defaultMarker.getIntrinsicWidth(), this.defaultMarker
            .getIntrinsicHeight());

//        this.buoys = new ArrayList<BuoyOverlayItem>();
    }
	
	 @Override
	 public void onStart() {
		    super.onStart();
		    Log.v(tag, "onStart");
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// this.locationProvider = this.locationManager.getBestProvider(myCriteria, true);
		// this.locationProvider = this.locationManager.getProviders(true).get(0);
		this.locationProvider = this.locationManager.getProvider(LocationManager.GPS_PROVIDER);
		
		Log.v(tag, " locationProvider from criteria - " + this.locationProvider);
		
		// get location updates from locationProvider
		// we set minTime(milliseconds) and minDistance(meters) to low values here to get updated
		// often (for emulator/debug)
		// in real life you *DO NOT* want to do this, it may consume too many resources
		// see LocationMangaer in JavaDoc for guidelines (time less than 60000ms for minTime is NOT
		// recommended)
		//
		// we use separate locationListeners for different purposes
		// one to update buoy data only if we move a long distance (185000 meters, just under the
		// 100 nautical miles we are parsing the data for)
		// another to recenter the map, even when we move a short distance (1000 meters)
		if (this.locationProvider != null) {
//		    this.locationManager.requestLocationUpdates(this.locationProvider.getName(), 3000, 185000,
//		        this.locationListenerGetBuoyData);
		    this.locationManager.requestLocationUpdates(this.locationProvider.getName(), 3000, MyTrackConstants.MARKER_DISTANCE_M,
		        this.locationListenerRecenterMap);
		} else {
		    Log.e(tag, " NO LOCATION PROVIDER AVAILABLE");
		    Toast.makeText(this,
		        "Wind and Waves cannot continue, the GPS location provider is not available at this time.",
		        Toast.LENGTH_SHORT).show();
		    finish();
		}
		
		// animate to, and get buoy data for lastKnownPoint on startup (or fake/prime point if no
		// last known)
//		    GeoPoint lastKnownPoint = getLastKnownPoint();
		    this.mapController = this.mapView.getController();
		    this.mapController.setZoom(10);
	        itemizedOverlay.setZoomLevel(mapView.getZoomLevel());
		    
		    
//		    this.mapController.animateTo(lastKnownPoint);
//		    getBuoyData(lastKnownPoint);
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
	    menu.add(0, MyTrackMapActivity.MENU_SET_MAP, 0, "Map").setIcon(android.R.drawable.ic_menu_mapmode);
	    menu.add(0, MyTrackMapActivity.MENU_SET_SATELLITE, 0, "Satellite").setIcon(android.R.drawable.ic_menu_mapmode);
	    menu.add(0, MyTrackMapActivity.MENU_CLEAR_MAP, 0, "Clear map").setIcon(android.R.drawable.ic_menu_mapmode);
	    menu.add(0, MyTrackMapActivity.MENU_ANIMATE_TO_LATEST, 0, "Go to latest").setIcon(android.R.drawable.ic_menu_mapmode);

	    return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case MENU_SET_MAP:
            this.mapView.setSatellite(false);
            break;
        case MENU_SET_SATELLITE:
            this.mapView.setSatellite(true);
            break;
        case MENU_CLEAR_MAP:
        	clearPoints();
        	break;
        case MENU_ANIMATE_TO_LATEST:
        	animateToLatest();
        	break;
        default:
        	throw new RuntimeException("Unknown menu feautureId " + featureId);
        }
        return super.onMenuItemSelected(featureId, item);		
	}
	
	private void animateToLatest() {
		GeoPoint point = itemizedOverlay.getLatestPoint();
		if (point != null) {
			mapController.animateTo(point);
		}
	}

	private void clearPoints() {
		itemizedOverlay.clearPoints();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.v(tag, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putSerializable(POINTS_LIST_BUNDLE_KEY, itemizedOverlay.getAllPointsAsSerializable());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.v(tag, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		
		Object pointsList = savedInstanceState.get(POINTS_LIST_BUNDLE_KEY);
		if (pointsList != null) {
			itemizedOverlay.setAllPoints((ArrayList<GeoPointSerializable>) pointsList);
		}
	}
}
