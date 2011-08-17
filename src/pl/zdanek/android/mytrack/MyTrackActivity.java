package pl.zdanek.android.mytrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MyTrackActivity extends Activity {
	
	 private static final String tag = "MyTrackActivity";
	private final Handler handler = new Handler() {

	        @Override
	        public void handleMessage(final Message msg) {
	            startActivity(new Intent(MyTrackActivity.this, MyTrackMapActivity.class));
	        }
	    };

	    @Override
	    public void onCreate(final Bundle icicle) {
	        super.onCreate(icicle);
	        Log.v(tag, "onCreate!");

	        this.setContentView(R.layout.main);
	    }

	    @Override
	    public void onStart() {
	        super.onStart();
	        // move to the next screen via a delayed message
	        new Thread() {

	            @Override
	            public void run() {
	                handler.sendMessageDelayed(handler.obtainMessage(), 3000);
	            };
	        }.start();
	    }

	    @Override
	    public void onPause() {
	        super.onPause();
	    }

}