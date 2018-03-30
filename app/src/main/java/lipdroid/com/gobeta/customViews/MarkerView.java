package lipdroid.com.gobeta.customViews;

import android.content.Context;
import android.graphics.Point;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mdmunirhossain on 3/30/18.
 */

public abstract class MarkerView extends View {

    protected Point point;
    protected LatLng latLng;

    private MarkerView(final Context context) {
        super(context);
    }

    public MarkerView(final Context context, final LatLng latLng, final Point point) {
        this(context);
        this.latLng = latLng;
        this.point = point;
    }

    public double lat() {
        return latLng.latitude;
    }

    public double lng() {
        return latLng.longitude;
    }

    public Point point() {
        return point;
    }

    public LatLng latLng() {
        return latLng;
    }

    public abstract void show();

    public abstract void hide();

    public abstract void refresh(final Point point);
}
