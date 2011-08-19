package pl.zdanek.android.mytrack;

import java.util.ArrayList;

import pl.zdanek.android.mytrack.model.GeoPointSerializable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MyTrackItemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Paint pathPaint;
	private MapView mapView;
	private Integer zoomLevel = -1;
	private ArrayList<OverlayItem> currentOverlay = new ArrayList<OverlayItem>();
	private boolean shouldRewriteVisible = true;
	protected ArrayList<OverlayItem> nextOverlay;
	private final static String tag = "HelloItemizedOverlay";
	
	public MyTrackItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker));
		
		this.mapView = mapView;
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
//		mapView.
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    shouldRewriteVisible = true;
	    populate();
	}
	
	
	@Override
	protected synchronized OverlayItem createItem(int i) {
	  return currentOverlay.get(i);
	}
	

	@Override
	public synchronized int size() {
		return currentOverlay.size();
	}
	
	@Override
	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
		canvas.drawPosText(zoomLevel.toString(), new float[]{10f, 10f, 15f, 10f, 20f, 10f, 25f, 10f}, new Paint(Paint.UNDERLINE_TEXT_FLAG));
		
		tryDrawWaypoints(canvas, mapView);
		
		super.draw(canvas, mapView, shadow);
		if (nextOverlay != null ) {
			currentOverlay = nextOverlay;
			nextOverlay = null;
		}
	}

	private void tryDrawWaypoints(Canvas canvas, MapView mapView) {
		try {
			drawWaypoints(canvas, mapView);
		} catch (Exception e) {
			Log.e(tag, "Exception during points draw " + e);
		}
	}

	private void drawWaypoints(Canvas canvas, MapView mapView) {
		ArrayList<OverlayItem> mOverlays = this.currentOverlay;
		
		if (this.mOverlays.size() > 0) {
			setZoomLevel(mapView.getZoomLevel());
			processZoomLevel();
		}
		
		if (mOverlays.size() > 1) {
			drawMarkerLines(canvas, mapView, mOverlays);
		}
	}

	private void drawMarkerLines(Canvas canvas, MapView mapView,
			ArrayList<OverlayItem> mOverlays) {
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

	private void processZoomLevel() {
		if (!shouldRewriteVisible) {
			return ;
		}
		
		shouldRewriteVisible = false;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
//				shouldRewriteVisible = false;
				
				ArrayList<OverlayItem> newOverlay = new ArrayList<OverlayItem>();	
				int skip = (int) Math.ceil(MyTrackConstants.MARKER_SKIP_ZOOM_FACTOR/Math.pow(2, zoomLevel-1));
				for (int i = 0; i < mOverlays.size(); i += skip) {
					newOverlay.add(mOverlays.get(i));
				}
				synchronized(this) {
					nextOverlay = newOverlay;
				}

				populate();
				mapView.postInvalidate();
			}
		}).start();
	}

	public void clearPoints() {
		mOverlays.clear();
		shouldRewriteVisible = true;
		populate();
		mapView.invalidate();
	}

	public GeoPoint getLatestPoint() {
		if (mOverlays.size() == 0) {
			return null;
		}
		GeoPoint gpoint = mOverlays.get(mOverlays.size()-1).getPoint();
		return new GeoPoint(gpoint.getLatitudeE6(), gpoint.getLongitudeE6());
	}

	public ArrayList<GeoPointSerializable> getAllPointsAsSerializable() {
		ArrayList<GeoPointSerializable> list = new ArrayList<GeoPointSerializable>();
		for (OverlayItem item : mOverlays) {
			list.add(new GeoPointSerializable(item.getPoint()));
		}
		
		return list;
	}

	public void setAllPoints(ArrayList<GeoPointSerializable> pointsList) {
		mOverlays.clear();
		
		
		for (GeoPointSerializable gpoint : pointsList) {
			OverlayItem oitem = new OverlayItem(gpoint.asGeoPoint(), "", "");
			mOverlays.add(oitem);
		}
		
		populate();
	}
	
	public void setZoomLevel(int zoomLevel) {
		if (this.zoomLevel != zoomLevel) {
			this.zoomLevel = zoomLevel;
			shouldRewriteVisible = true;
		}
	}

}
