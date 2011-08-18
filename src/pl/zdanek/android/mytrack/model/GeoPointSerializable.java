package pl.zdanek.android.mytrack.model;

import java.io.Serializable;

import com.google.android.maps.GeoPoint;

public class GeoPointSerializable implements Serializable {

	private static final long serialVersionUID = 1766003194830052639L;
	public int lonE6;
	public int latE6;

	public GeoPointSerializable(int lonE6, int latE6) {
		this.lonE6 = lonE6;
		this.latE6 = latE6;
	}

	public GeoPointSerializable(GeoPoint geoPoint) {
		lonE6 = geoPoint.getLongitudeE6();
		latE6 = geoPoint.getLatitudeE6();
	}

	public GeoPoint asGeoPoint() {
		return new GeoPoint(latE6, lonE6);
	}
}
