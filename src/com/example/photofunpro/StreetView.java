package com.example.photofunpro;

import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLink;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class StreetView extends FragmentActivity {

    private LatLng LOCATION_VIEWING;

    /**
     * The amount in degrees by which to scroll the camera
     */
    private static final int PAN_BY_DEG = 30;

    private static final float ZOOM_BY = 0.5f;

    private StreetViewPanorama svp;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.street_view_layout);
        
        Bundle mapBundleDetails = getIntent().getExtras();
        Double gpsLat = mapBundleDetails.getDouble("gps_lat");
        Double gpsLon = mapBundleDetails.getDouble("gps_lon");
        LOCATION_VIEWING = new LatLng(gpsLat, gpsLon);

        setUpStreetViewPanoramaIfNeeded(savedInstanceState);
        
    }


    private void setUpStreetViewPanoramaIfNeeded(Bundle savedInstanceState) {
        if (svp == null) {
            svp = ((SupportStreetViewPanoramaFragment)
                getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama))
                    .getStreetViewPanorama();
            if (svp != null) {
                if (savedInstanceState == null) {
                    svp.setPosition(LOCATION_VIEWING);
                }
            }
        }
    }

    /**
     * When the panorama is not ready the PanoramaView cannot be used. This should be called on
     * all entry points that call methods on the Panorama API.
     */
    private boolean checkReady() {
        if (svp == null) {
            Toast.makeText(this, R.string.panorama_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onGoToPhotoLocation(View view) {
        if (!checkReady()) {
            return;
        }
        svp.setPosition(LOCATION_VIEWING);
    }


    public void onZoomIn(View view) {
        if (!checkReady()) {
            return;
        }

        svp.animateTo(
            new StreetViewPanoramaCamera.Builder().zoom(svp.getPanoramaCamera().zoom + ZOOM_BY)
            .tilt(svp.getPanoramaCamera().tilt)
            .bearing(svp.getPanoramaCamera().bearing).build(), getDuration());
    }

    public void onZoomOut(View view) {
        if (!checkReady()) {
            return;
        }

        svp.animateTo(
            new StreetViewPanoramaCamera.Builder().zoom(svp.getPanoramaCamera().zoom - ZOOM_BY)
            .tilt(svp.getPanoramaCamera().tilt)
            .bearing(svp.getPanoramaCamera().bearing).build(), getDuration());
    }

    public void onPanLeft(View view) {
        if (!checkReady()) {
            return;
        }

        svp.animateTo(
            new StreetViewPanoramaCamera.Builder().zoom(svp.getPanoramaCamera().zoom)
            .tilt(svp.getPanoramaCamera().tilt)
            .bearing(svp.getPanoramaCamera().bearing - PAN_BY_DEG).build(), getDuration());
    }

    public void onPanRight(View view) {
        if (!checkReady()) {
            return;
        }

        svp.animateTo(
            new StreetViewPanoramaCamera.Builder().zoom(svp.getPanoramaCamera().zoom)
            .tilt(svp.getPanoramaCamera().tilt)
            .bearing(svp.getPanoramaCamera().bearing + PAN_BY_DEG).build(), getDuration());

    }

    public void onPanUp(View view) {
        if (!checkReady()) {
            return;
        }

        float currentTilt = svp.getPanoramaCamera().tilt;
        float newTilt = currentTilt + PAN_BY_DEG;

        newTilt = (newTilt > 90) ? 90 : newTilt;

        svp.animateTo(
            new StreetViewPanoramaCamera.Builder().zoom(svp.getPanoramaCamera().zoom)
            .tilt(newTilt)
            .bearing(svp.getPanoramaCamera().bearing).build(), getDuration());
    }

    public void onPanDown(View view) {
        if (!checkReady()) {
            return;
        }

        float currentTilt = svp.getPanoramaCamera().tilt;
        float newTilt = currentTilt - PAN_BY_DEG;

        newTilt = (newTilt < -90) ? -90 : newTilt;

        svp.animateTo(
            new StreetViewPanoramaCamera.Builder().zoom(svp.getPanoramaCamera().zoom)
            .tilt(newTilt)
            .bearing(svp.getPanoramaCamera().bearing).build(), getDuration());
    }

    public void onRequestPosition(View view) {
        if (!checkReady()){
            return;
        }
        if (svp.getLocation() != null) {
          Toast.makeText(view.getContext(), svp.getLocation().position.toString(),
              Toast.LENGTH_SHORT).show();
        }
    }

    public void onMovePosition(View view) {
        StreetViewPanoramaLocation location = svp.getLocation();
        StreetViewPanoramaCamera camera = svp.getPanoramaCamera();
        if (location != null && location.links != null) {
            StreetViewPanoramaLink link = findClosestLinkToBearing(location.links, camera.bearing);
            svp.setPosition(link.panoId);
        }
    }

    public static StreetViewPanoramaLink findClosestLinkToBearing(StreetViewPanoramaLink[] links,
        float bearing) {
        float minBearingDiff = 360;
        StreetViewPanoramaLink closestLink = links[0];
        for (StreetViewPanoramaLink link : links) {
            if (minBearingDiff > findNormalizedDifference(bearing, link.bearing)) {
                minBearingDiff = findNormalizedDifference(bearing, link.bearing);
                closestLink = link;
            }
        }
        return closestLink;
    }

    // Find the difference between angle a and b as a value between 0 and 180
    @SuppressLint("FloatMath")
	public static float findNormalizedDifference(float a, float b) {
        float diff = a - b;
        float normalizedDiff = diff - (360.0f * FloatMath.floor(diff / 360.0f));
        return (normalizedDiff < 180.0f) ? normalizedDiff : 360.0f - normalizedDiff;
    }

    private long getDuration() {
        return 1000;
    }
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.streetview_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_take_photo:
			Intent openCamera = new Intent(getApplication(), Camera.class);
			startActivity(openCamera);
			return true;
		case R.id.action_app_main:
			finish();
			return true;
		case R.id.action_app_help:
			Intent openHelp = new Intent(getApplication(), AppHelp.class);
			startActivity(openHelp);
			return true;
		case R.id.action_app_settings:
			Intent openSettings = new Intent(getApplication(), AppSettings.class);
			startActivity(openSettings);
			return true;		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
    
}
