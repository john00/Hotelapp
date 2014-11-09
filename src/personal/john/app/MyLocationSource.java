
package personal.john.app;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;

public class MyLocationSource implements LocationSource, LocationListener, ConnectionCallbacks,
        OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private final static long LOCATION_UPDATE_DURATION = 2000;

    private final static int LOCATION_SMALLEST_DISPLACEMENT_METER = 10;

    private LocationManager mLocMgr = null; // 位置マネージャ

    private LocationClient mLocClient = null;
    
    private static MainActivity mActivity;

    private boolean mReconnect = false;

    private OnLocationChangedListener mSourceListener = null;

    private LocationListener mGpsListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            MyLocationSource.this.onLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    MyLocationSource(Activity activity, GoogleMap map) {
        mActivity = (MainActivity) activity;
        mLocClient = new LocationClient(mActivity, this, this);
        mLocMgr = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE); // 位置マネージャ取得
    }

    void start() {
        if (mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_DURATION,
                    LOCATION_UPDATE_DURATION, mGpsListener);
        } else {
            mLocMgr.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, LOCATION_UPDATE_DURATION,
                    LOCATION_UPDATE_DURATION, mGpsListener);
        }
        if (!mLocClient.isConnected() && !mLocClient.isConnecting()) {
            mReconnect = false;
            mLocClient.connect();
        }
    }

    void stop() {
        mReconnect = false;
        try {
            mLocClient.removeLocationUpdates(this);
        } catch (Exception e) {
            Log.e("MyLocationSource:stop", e.toString());
        }
        mLocClient.disconnect();
        try {
            mLocMgr.removeUpdates(mGpsListener);
        } catch (Exception e) {
            Log.e("MyLocationSource:stop", e.toString());
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO 接続失敗時の処理

    }

    @Override
    public void onConnected(Bundle bundle) {
        mReconnect = true;
        LocationRequest req = LocationRequest.create();
        req.setFastestInterval(LOCATION_UPDATE_DURATION);
        req.setInterval(LOCATION_UPDATE_DURATION);
        req.setSmallestDisplacement(LOCATION_SMALLEST_DISPLACEMENT_METER);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocClient.requestLocationUpdates(req, this);
    }

    @Override
    public void onDisconnected() {
        if (mReconnect) {
            start();
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        if (mSourceListener != null) {
            mSourceListener.onLocationChanged(loc);
        }
        
        mActivity.searchHotel();
        mActivity.makeList();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mSourceListener = listener;
    }

    @Override
    public void deactivate() {
        mSourceListener = null;
    }

    public Location getMyLocation() {
        return mLocClient.getLastLocation();
    }
    
}
