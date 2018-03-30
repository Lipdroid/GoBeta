package lipdroid.com.gobeta.customViews;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mdmunirhossain on 3/30/18.
 */

public class MapOverlayLayout<V extends MarkerView> extends FrameLayout {

    protected List<V> markersList;
    protected Polyline currentPolyline;
    protected GoogleMap googleMap;
    protected ArrayList<LatLng> polylines;

    public MapOverlayLayout(final Context context) {
        this(context, null);
    }

    public MapOverlayLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        markersList = new ArrayList<>();
    }

    public void addMarker(final V view) {
        markersList.add(view);
        addView(view);
    }

    public void removeMarker(final V view) {
        markersList.remove(view);
        removeView(view);
    }

    public void showMarker(final int position) {
        markersList.get(position).show();
    }

    private void refresh(final int position, final Point point) {
        markersList.get(position).refresh(point);
    }

    public void setupMap(final GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void refresh() {
        Projection projection = googleMap.getProjection();
        for (int i = 0; i < markersList.size(); i++) {
            refresh(i, projection.toScreenLocation(markersList.get(i).latLng()));
        }
    }

    public void setOnCameraIdleListener(final GoogleMap.OnCameraIdleListener listener) {
        googleMap.setOnCameraIdleListener(listener);
    }

    public void setOnCameraMoveListener(final GoogleMap.OnCameraMoveListener listener) {
        googleMap.setOnCameraMoveListener(listener);
    }

    public void moveCamera(final LatLng latLng) {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(18)
                .tilt(67.5f)
                .bearing(314)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
