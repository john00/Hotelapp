
package personal.john.app;

import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements LocationListener,
        OnInfoWindowClickListener, LocationSource, RakutenClientReceiver {

    private GoogleMap mMap;

    private OnLocationChangedListener mListener;

    private static LocationManager mLocationManager;

    /* data */
    ArrayList<HotelInfo> mTargetList = null;

    RakutenClient mRakutenClient = null;

    static ArrayList<DirectionsData> mDirectionsList = null;

    // DB用オブジェクト
    private GeoSearcherDB mDatabaseObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActionBar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (mLocationManager != null) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 2.0f,
                        this);
            }
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L,
                        2.0f, this);
            }
        } else {
            Toast.makeText(this, "GPSを有効に設定してください。", Toast.LENGTH_SHORT).show();
        }

        setupMapIfNeeded();

        // 初期位置を現在地に設定
        String provider = "";
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        Location loc = mLocationManager.getLastKnownLocation(provider);
        if (loc != null) {
            final CameraUpdate iniCamera = CameraUpdateFactory
                    .newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(14.0f)
                            .build());
            mMap.moveCamera(iniCamera);
        }

        /* Rakuten Client */
        try {
            mRakutenClient = new RakutenClient(this, this);
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // DB作成
        mDatabaseObject = new GeoSearcherDB(this);

        mDirectionsList = new ArrayList<DirectionsData>();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setupMapIfNeeded();

        if (mLocationManager != null) {
            mMap.setMyLocationEnabled(true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMarker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setupMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setLocationSource(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String[] itemslist = {
                "100m", "500m", "1000m", "2000m", "3000m"
        };

        switch (item.getItemId()) {
            case R.id.item_search:
                searchHotel();
                break;
            case R.id.item_list:
                // リスト表示用の画面を表示する。
                double[] myLocation = {
                        mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()
                };

                Intent intentToResultListView = new Intent(MainActivity.this, ResultListView.class);
                intentToResultListView.putExtra("personal.john.app.list", myLocation);
                intentToResultListView.setAction(Intent.ACTION_VIEW);

                startActivity(intentToResultListView);
                break;
            case R.id.item_range:
                // ホテルの検索範囲設定
                new AlertDialog.Builder(MainActivity.this).setTitle("検索範囲を選択してください")
                        .setItems(itemslist, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mRakutenClient.setSearchRange(0.1);
                                        break;
                                    case 1:
                                        mRakutenClient.setSearchRange(0.5);
                                        break;
                                    case 2:
                                        mRakutenClient.setSearchRange(1);
                                        break;
                                    case 3:
                                        mRakutenClient.setSearchRange(2);
                                        break;
                                    case 4:
                                        mRakutenClient.setSearchRange(3);
                                        break;
                                    default:
                                        mRakutenClient.setSearchRange(1);
                                }
                                
                                searchHotel();
                            }
                        }).setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case R.id.item_exit:
                finish();
                break;
        }

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null) {
            mListener.onLocationChanged(location);
        }
    }

    @Override
    public void onProviderDisabled(String arg0) {

    }

    @Override
    public void onProviderEnabled(String arg0) {

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public void receiveHotel(final ArrayList<HotelInfo> infoList) {
        if (mTargetList != null) {
            mTargetList.clear();
        }
        mTargetList = infoList;
    }

    @Override
    public void receiveError(int id) {
        switch (id) {
            case RakutenClient.ERROR_GENERAL:
                break;
            case RakutenClient.ERROR_FATAL:
                break;
            default:
                break;
        }
    }

    public void updateMarker() {
        if (mTargetList == null) {
            return;
        }

        int size = mTargetList.size();
        mMap.clear();

        for (int iHotel = 0; iHotel < size; iHotel++) {
            int iArrived = 0;
            BitmapDescriptor icon;
            MarkerOptions options = new MarkerOptions();

            if (mTargetList.get(iHotel).getNo() != "") {
                mDatabaseObject.openGeoSearcherDB();
                iArrived = mDatabaseObject.readArrivedData(mTargetList.get(iHotel).getNo());
                mDatabaseObject.closeGeoSearcherDB();
            }

            if (iArrived != 1) {
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            } else {
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            }

            LatLng latlng = new LatLng(mTargetList.get(iHotel).getLocation().getLatitude(),
                    mTargetList.get(iHotel).getLocation().getLongitude());
            String title = mTargetList.get(iHotel).getName();

            options.position(latlng).title(title).icon(icon)
                    .snippet(mTargetList.get(iHotel).getAddress());
            mMap.addMarker(options);
        }
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // ホテルの住所からmTargetListのインデックスを検索
        int index;
        for (index = 0; index < mTargetList.size(); index++) {
            if (marker.getSnippet().equals(mTargetList.get(index).getAddress()))
                break;
        }

        final int iTargetListIndex = index;
        final CharSequence[] items = {
                "電話で予約", "ルート表示", "メモ", "楽天Webページを開く", "閉じる"
        };

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(marker.getTitle());

        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // 電話
                        String strTelphoneNo = "tel:"
                                + mTargetList.get(iTargetListIndex).getTelephoneNo();
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(strTelphoneNo));
                        startActivity(intent);
                        break;
                    case 1: // ルート表示
                        String strRouteUrl = "http://maps.google.com/maps?dirflg=w";
                        strRouteUrl += "&saddr=" + mMap.getMyLocation().getLatitude() + ","
                                + mMap.getMyLocation().getLongitude() + "(現在地)";
                        strRouteUrl += "&daddr=" + mTargetList.get(iTargetListIndex).getLatitude()
                                + "," + mTargetList.get(iTargetListIndex).getLongitude() + "(目的地)";

                        Intent intentRote = new Intent();
                        intentRote.setAction(Intent.ACTION_VIEW);
                        intentRote.setClassName("com.google.android.apps.maps",
                                "com.google.android.maps.MapsActivity");
                        intentRote.setData(Uri.parse(strRouteUrl));
                        startActivity(intentRote);
                        break;
                    case 2: // メモ
                        String[] strInfo = {
                                mTargetList.get(iTargetListIndex).getNo(), "0", ""
                        };
                        String strHotelId = mTargetList.get(iTargetListIndex).getNo();
                        mDatabaseObject.openGeoSearcherDB();
                        if (mDatabaseObject.readArrivedData(strHotelId) != 0)
                            strInfo[1] = "1";
                        strInfo[2] = mDatabaseObject.readMemoData(strHotelId);
                        mDatabaseObject.closeGeoSearcherDB();
                        Intent intentToSettingWindow = new Intent();
                        intentToSettingWindow.setClassName("personal.john.app",
                                "personal.john.app.MemoWindow");
                        intentToSettingWindow.putExtra("personal.john.app.Arrived", strInfo);

                        startActivity(intentToSettingWindow);
                        break;
                    case 3: // 楽天Webページを開く
                        Intent intentWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(mTargetList
                                .get(iTargetListIndex).getInfomationUrl()));
                        startActivity(intentWeb);
                    default:
                }
                dialog.cancel();
            }
        });

        dialog.show();

    }
    
    public void searchHotel() {
        mMap.clear();
        // 現在地周辺のホテルを検索する。
        mRakutenClient.setmMyLatitute(mMap.getMyLocation().getLatitude());
        mRakutenClient.setmMyLongitude(mMap.getMyLocation().getLongitude());
        mRakutenClient.queryInfo();
    }

}
