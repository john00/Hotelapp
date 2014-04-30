
package personal.john.app;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.net.Uri;

public class DirectionsData {
    private String strJSONData;

    private final static String SCHEME = "https";

    private final static String AUTHORITY = "maps.googleapis.com";

    private final static String PATH = "/maps/api/directions/json";

    private String mRequestUri;

    private String mCopyright; // コピーライト

    private ArrayList<String> mWarningList; // 警告文

    private String mDistance; // 距離

    private String mDuration; // 所要時間

    private ArrayList<LatLng> mStartLatLngList; // 各ステップのスタート位置記録用

    private ArrayList<LatLng> mEndLatLngList; // 各ステップのエンド位置記録用

    public DirectionsData() {
        mCopyright = "";
        mWarningList = new ArrayList<String>();
        mStartLatLngList = new ArrayList<LatLng>();
        mEndLatLngList = new ArrayList<LatLng>();
    }

    public void setRequestUri(String origin, String destination, String sensor) {
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme(SCHEME);
        uriBuilder.authority(AUTHORITY);
        uriBuilder.path(PATH);
        uriBuilder.appendQueryParameter("origin", origin);
        uriBuilder.appendQueryParameter("destination", destination);
        uriBuilder.appendQueryParameter("sensor", sensor);

        mRequestUri = uriBuilder.toString();
    }

    public boolean setJSONData(String origin, String destination, String sensor) {
        setRequestUri(origin, destination, sensor);
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
        strJSONData = httpRequestHandler.getJSONData(mRequestUri);
        httpRequestHandler.httpClientClose();

        if (strJSONData == "")
            return false;

        return true;
    }

    public String getJSONData() {
        return strJSONData;
    }

    public void setDirectionsList() {
        try {
            JSONObject rootObject = new JSONObject(strJSONData);
            JSONArray routesArray = rootObject.getJSONArray("routes");
            JSONObject routesObject = routesArray.getJSONObject(0);
            JSONArray legsArray = routesObject.getJSONArray("legs");
            JSONObject legsObject = legsArray.getJSONObject(0);
            JSONArray stepsArray = legsObject.getJSONArray("steps");

            for (int i = 0; i < routesArray.length(); i++) {
                if (routesObject.has("copyrights")) {
                    mCopyright = routesObject.getString("copyrights");
                }
                if (routesObject.has("warnings")) {
                    JSONArray warningsArray = routesObject.getJSONArray("warnings");
                    for (int j = 0; j < warningsArray.length(); j++) {
                        mWarningList.add(warningsArray.getString(j));
                    }
                }
            }

            for (int i = 0; i < legsArray.length(); i++) {
                if (legsObject.has("distance")) {
                    JSONObject distanceObject = legsObject.getJSONObject("distance");
                    mDistance = distanceObject.getString("text");
                }
                if (legsObject.has("duration")) {
                    JSONObject durationObject = legsObject.getJSONObject("duration");
                    mDuration = durationObject.getString("text");
                }
            }

            // 各ステップのスタート位置とエンド位置を取得
            for (int i = 0; i < stepsArray.length(); i++) {
                JSONObject stepObject = stepsArray.getJSONObject(i);
                if (stepObject.has("start_location")) {
                    JSONObject startLatLngObject = stepObject.getJSONObject("start_location");
                    LatLng latlng = new LatLng(startLatLngObject.getDouble("lat"),
                            startLatLngObject.getDouble("lng"));
                    mStartLatLngList.add(latlng);
                }
                if (stepObject.has("end_location")) {
                    JSONObject endLatLngObject = stepObject.getJSONObject("end_location");
                    LatLng latlng = new LatLng(endLatLngObject.getDouble("lat"),
                            endLatLngObject.getDouble("lng"));
                    mEndLatLngList.add(latlng);
                }
            }
        } catch (JSONException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    public String getmDistance() {
        return mDistance;
    }

    public String getmDuration() {
        return mDuration;
    }

    public String getCopyright() {
        return mCopyright;
    }

    public ArrayList<String> getWarningList() {
        return mWarningList;
    }

    public ArrayList<LatLng> getStepsStartList() {
        return mStartLatLngList;
    }

    public ArrayList<LatLng> getStepsEndList() {
        return mEndLatLngList;
    }

}
