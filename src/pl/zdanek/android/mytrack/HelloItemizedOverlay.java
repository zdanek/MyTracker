package pl.zdanek.android.mytrack;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class HelloItemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Paint pathPaint;
	private final static String tag = "HelloItemizedOverlay";
	
	public HelloItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker));
		
		preparePathPaint();
		registerMapListener(mapView);
	}

	private void preparePathPaint() {
		pathPaint = new Paint();
		pathPaint.setStrokeWidth(3);
		pathPaint.setAlpha(128);
		pathPaint.setAntiAlias(true);
	}

	private void registerMapListener(MapView mapView) {
//		mapView.get
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		canvas.drawPosText("BZD", new float[]{10f, 10f, 20f, 10f, 30f, 10f}, new Paint(Paint.UNDERLINE_TEXT_FLAG));
		
		tryDrawWaypoints(canvas, mapView);
		
		super.draw(canvas, mapView, shadow);
	}

	private void tryDrawWaypoints(Canvas canvas, MapView mapView) {
		try {
			drawWaypoints(canvas, mapView);
		} catch (Exception e) {
			Log.e(tag, "Exception during points draw " + e);
		}
	}

	private void drawWaypoints(Canvas canvas, MapView mapView) {
		if (mOverlays.size() > 1) {
			int xs =Integer.MIN_VALUE, xe = Integer.MIN_VALUE, ys=0, ye=0;
			Projection projection = mapView.getProjection();
			for (OverlayItem oitem : mOverlays) {
				Point point = projection.toPixels(oitem.getPoint(), null);
				Log.v(tag, "Tworze punkt na ekranie z punktu geo " + point);
				
				if (xs == Integer.MIN_VALUE) {
					xs = point.x;
					ys = point.y;
				} else {
					xe = xs;
					ye = ys;
					xs = point.x;
					ys = point.y;
					canvas.drawLine(xs, ys, xe, ye, pathPaint);
				}
			}
		}
	}

	public void clearPoints() {
		mOverlays.clear();
		populate();
	}

	public GeoPoint getLatestPoint() {
		if (mOverlays.size() == 0) {
			return null;
		}
		GeoPoint gpoint = mOverlays.get(mOverlays.size()-1).getPoint();
		return new GeoPoint(gpoint.getLatitudeE6(), gpoint.getLongitudeE6());
	}

}
