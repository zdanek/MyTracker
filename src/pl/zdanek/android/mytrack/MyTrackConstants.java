package pl.zdanek.android.mytrack;

public class MyTrackConstants {

	public final static int EQUATOR_LENGTH_M = 20000000;
	public final static int MARKER_DISTANCE_M = 1000;
	public final static int MARKER_MARGIN_PIX = 10;
	public final static int MAP_PIX_PER_EQUATOR_ZOOM1 = 256;

	public static final int MAX_ZOOM_LEVEL_TO_PROCESS = 16;

	public final static float MARKER_SKIP_ZOOM_FACTOR = EQUATOR_LENGTH_M * MARKER_MARGIN_PIX / (MAP_PIX_PER_EQUATOR_ZOOM1 * MARKER_DISTANCE_M);	
	
}
