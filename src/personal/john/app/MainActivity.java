
package personal.john.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
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
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements OnInfoWindowClickListener,
        RakutenClientReceiver {

    private static GoogleMap mMap;
    
    private static ArrayList<Marker> mMarkerList;

    private static ListView mListView;

    private static MyLocationSource mLocationSource;

    /* data */
    private static ArrayList<HotelInfo> mTargetList;

    private static RakutenClient mRakutenClient;

    // DB用オブジェクト
    private static GeoSearcherDB mDatabaseObject;

    @Override
    protected void onCreate(final Bundle sIS) {
        super.onCreate(sIS);

        // ActionBar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        mListView = (ListView) findViewById(R.id.main_listview);

        // DB作成
        mDatabaseObject = new GeoSearcherDB(this);

        mTargetList = null;
        mMarkerList = new ArrayList<Marker>();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onActivated();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        onInactivated();
        super.onPause();
    }

    private void setupMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                mLocationSource = new MyLocationSource(this, mMap);

                // 初期値はとりあえず東京駅
                double lat = 35.681283;
                double lon = 139.766092;
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc == null) {
                    loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }
                if (loc != null) {
                    // TODO 以下はテスト用コード（リリース前に削除すること）
                     lat = loc.getLatitude();
                     lon = loc.getLongitude();
                    //lat = 35.681283;
                    //lon = 139.766092;
                }
                CameraPosition.Builder builder = new CameraPosition.Builder().bearing(0).tilt(0)
                        .zoom(16).target(new LatLng(lat, lon));
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setLocationSource(mLocationSource);
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    private void onActivated() {
        setupMapIfNeeded();
        if (mLocationSource != null) {
            mLocationSource.start();
        }
    }

    private void onInactivated() {
        if (mLocationSource != null) {
            mLocationSource.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_search:
                searchHotel();
                break;
            case R.id.listitem_sort:
                // リスト表示用の画面を表示する。
                final String[] sortlist = {
                        "ホテル名", "距離", "価格", "空き部屋"
                };
                // リストの並べ替え用ダイアログを表示し、選択に応じた並べ替えを行う。
                // ホテル名：ホテル名で並び替え
                // 距離：現在地からの距離で並び替え（近い順）
                // 価格：ホテルの宿泊費で並び替え（安い順）
                // 空き部屋：空き部屋があるホテルが先になるよう並び替え
                new AlertDialog.Builder(this).setTitle(this.getString(R.string.menulistitem_sort))
                        .setItems(sortlist, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Collections.sort(mTargetList, new MyComparator(
                                                MyComparator.ASC, MyComparator.MODE_HOTELNAME));
                                        break;
                                    case 1:
                                        Collections.sort(mTargetList, new MyComparator(
                                                MyComparator.ASC, MyComparator.MODE_DISTANCE));
                                        break;
                                    case 2:
                                        Collections.sort(mTargetList, new MyComparator(
                                                MyComparator.ASC, MyComparator.MODE_MINCHARGE));
                                        break;
                                    case 3:
                                        Collections.sort(mTargetList, new MyComparator(
                                                MyComparator.ASC, MyComparator.MODE_VACANT));
                                        break;
                                    default:
                                }
                                makeList();
                                updateMarker();
                            }
                        }).setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
                break;

            case R.id.item_streetview:
                // ストリートビュー表示
                String strStreetUrl = "google.streetview:cbll=";
                strStreetUrl += mMap.getMyLocation().getLatitude() + ","
                        + mMap.getMyLocation().getLongitude();

                Intent intentStreet = new Intent();
                intentStreet.setAction(Intent.ACTION_VIEW);
                intentStreet.setClassName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity");
                intentStreet.setData(Uri.parse(strStreetUrl));
                startActivity(intentStreet);

                break;

            case R.id.item_range:
                // ホテルの検索範囲設定
                final String[] distancelist = {
                        "100m", "500m", "1000m", "2000m", "3000m"
                };
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.title_searchranges))
                        .setItems(distancelist, new DialogInterface.OnClickListener() {

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
                                makeList();
                            }
                        })
                        .setNegativeButton(getString(R.string.bt_cancel),
                                new DialogInterface.OnClickListener() {
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
        mMarkerList = new ArrayList<Marker>();

        for (int iHotel = 0; iHotel < size; iHotel++) {
            BitmapDescriptor icon;
            MarkerOptions options = new MarkerOptions();

            if (mTargetList.get(iHotel).getNo() != "") {
                mDatabaseObject.openGeoSearcherDB();
                mDatabaseObject.closeGeoSearcherDB();
            }

            double destLat = Double.valueOf(mTargetList.get(iHotel).getLatitude());
            double destLon = Double.valueOf(mTargetList.get(iHotel).getLongitude());
            mTargetList.get(iHotel).setDistance(mRakutenClient.getmMyLatitute(),
                    mRakutenClient.getmMyLongitude(), destLat, destLon);

            switch (iHotel) {
                case 0:
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_a);
                    break;
                case 1:
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_b);
                    break;
                case 2:
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_c);
                    break;
                default:
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    break;
            }

            LatLng latlng = new LatLng(destLat, destLon);
            String title = mTargetList.get(iHotel).getName();

            String vacant = "空き部屋なし";
            if (mTargetList.get(iHotel).getVacant()) {
                vacant = "空き部屋あり";
            }
            options.position(latlng).title(title).icon(icon).snippet(vacant);
            mMarkerList.add(mMap.addMarker(options));
        }
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                for (int index = 0; index < mListView.getCount(); index++) {
                    if (marker.getTitle().equals(
                            ((MyCustomListAdapter) mListView.getAdapter()).getHotelName(index))) {
                        ((MyCustomListAdapter) mListView.getAdapter())
                                .setSelectedItemPosition(index);
                        makeList();
                        mListView.setSelection(index);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // ホテルの住所からmTargetListのインデックスを検索
        int index;
        for (index = 0; index < mTargetList.size(); index++) {
            if (marker.getTitle().equals(mTargetList.get(index).getName()))
                break;
        }

        final int targetListIndex = index;
        final CharSequence[] items = {
                getString(R.string.menuitem_reservedwithphone),
                getString(R.string.menuitem_showroute), getString(R.string.menuitem_memo),
                getString(R.string.menuitem_rakutenpage), getString(R.string.menuitem_close)
        };

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(marker.getTitle());

        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // 電話
                        String strTelphoneNo = "tel:"
                                + mTargetList.get(targetListIndex).getTelephoneNo();
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(strTelphoneNo));
                        startActivity(intent);
                        break;
                    case 1: // ルート表示
                        String strRouteUrl = "http://maps.google.com/maps?dirflg=w";
                        strRouteUrl += "&saddr=" + mMap.getMyLocation().getLatitude() + ","
                                + mMap.getMyLocation().getLongitude() + "(現在地)";
                        strRouteUrl += "&daddr=" + mTargetList.get(targetListIndex).getLatitude()
                                + "," + mTargetList.get(targetListIndex).getLongitude() + "(目的地)";

                        Intent intentRote = new Intent();
                        intentRote.setAction(Intent.ACTION_VIEW);
                        intentRote.setClassName("com.google.android.apps.maps",
                                "com.google.android.maps.MapsActivity");
                        intentRote.setData(Uri.parse(strRouteUrl));
                        startActivity(intentRote);
                        break;
                    case 2: // メモ
                        String[] strInfo = {
                                mTargetList.get(targetListIndex).getNo(), "0", ""
                        };
                        String strHotelId = mTargetList.get(targetListIndex).getNo();
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
                        final Intent intentWeb = new Intent(Intent.ACTION_VIEW, Uri
                                .parse(mTargetList.get(targetListIndex).getInfomationUrl()));
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
        mMarkerList = new ArrayList<Marker>();
        // 現在地周辺のホテルを検索する。
        // TODO 以下はテスト用コード（リリース前に削除すること）
        // mRakutenClient.setmMyLatitute(mLocationSource.getMyLocation().getLatitude());
        // mRakutenClient.setmMyLongitude(mLocationSource.getMyLocation().getLongitude());
        double lat = 35.681283;
        double lon = 139.766092;
        mRakutenClient.setmMyLatitute(lat);
        mRakutenClient.setmMyLongitude(lon);
        mRakutenClient.queryInfo(getString(R.string.flag_mode_normal), "");
    }

    public void checkVacantHotel() {
        if (mTargetList == null) {
            return;
        }

        int size = mTargetList.size();
        for (int iHotel = 0; iHotel < size; iHotel++) {
            if (mTargetList.get(iHotel).getNo() != "") {
                mRakutenClient.queryInfo(getString(R.string.flag_mode_vacant),
                        mTargetList.get(iHotel).getNo());
            }
        }
    }

    // リスト項目生成メソッド
    public void makeList() {
        List<MyCustomListData> object = new ArrayList<MyCustomListData>();

        if (mTargetList == null) {
            final MyCustomListData tmpItem = new MyCustomListData();
            tmpItem.setHotelName(getResources().getString(R.string.no_searche));
            tmpItem.setHotelInfo(getResources().getString(R.string.no_searche_detail));
            object.add(tmpItem);

            MyCustomListAdapter myCustomListAdapter = new MyCustomListAdapter(this, 0, object);
            mListView.setAdapter(myCustomListAdapter);

            return;
        }

        for (int iTargetCount = 0; iTargetCount < mTargetList.size(); iTargetCount++) {
            final MyCustomListData tmpItem = new MyCustomListData();

            double destLat = Double.valueOf(mTargetList.get(iTargetCount).getLatitude());
            double destLon = Double.valueOf(mTargetList.get(iTargetCount).getLongitude());
            mTargetList.get(iTargetCount).setDistance(mRakutenClient.getmMyLatitute(),
                    mRakutenClient.getmMyLongitude(), destLat, destLon);

            tmpItem.setHotelName(mTargetList.get(iTargetCount).getName());
            tmpItem.setHotelInfo(mTargetList.get(iTargetCount).getSpecial());
            tmpItem.setHotelDistance("ここから "
                    + Integer.toString(Math.round(mTargetList.get(iTargetCount).getDistance()))
                    + "m");
            tmpItem.setHotelMinCharge("価格：" + mTargetList.get(iTargetCount).getHotelMinCharge()
                    + "円 ～");

            switch (iTargetCount) {
                case 0:
                    tmpItem.setHotelImage(BitmapFactory.decodeResource(getResources(),
                            R.drawable.marker_a));
                    break;
                case 1:
                    tmpItem.setHotelImage(BitmapFactory.decodeResource(getResources(),
                            R.drawable.marker_b));
                    break;
                case 2:
                    tmpItem.setHotelImage(BitmapFactory.decodeResource(getResources(),
                            R.drawable.marker_c));
                    break;
                default:
                    break;
            }
            object.add(tmpItem);
        }

        MyCustomListAdapter myCustomListAdapter = new MyCustomListAdapter(this, 0, object);
        mListView.setAdapter(myCustomListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String targetHotelName = mTargetList.get(position).getName();
                for (int index = 0; index < mTargetList.size(); index++) {
                    if (mMarkerList.get(index).getTitle().equals(targetHotelName)) {
                        double lat = mMarkerList.get(index).getPosition().latitude;
                        double lon = mMarkerList.get(index).getPosition().longitude;
                        CameraPosition.Builder builder = new CameraPosition.Builder().bearing(0).tilt(0)
                                .zoom(16).target(new LatLng(lat, lon));
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                        mMarkerList.get(index).showInfoWindow();
                    }
                }
                ((MyCustomListAdapter) mListView.getAdapter()).setSelectedItemPosition(position);
                makeList();
                mListView.setSelection(position);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                final int iTargetListIndex = arg2;
                final CharSequence[] items = {
                        "電話で予約", "ルート表示", "メモ", "楽天Webページを開く", "閉じる"
                };

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(mTargetList.get(iTargetListIndex).getName());

                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // 電話
                                String strTelphoneNo = "tel:"
                                        + mTargetList.get(iTargetListIndex).getTelephoneNo();
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri
                                        .parse(strTelphoneNo));
                                startActivity(intent);
                                break;
                            case 1: // ルート表示
                                String url = "http://maps.google.com/maps?dirflg=w";
                                url += "&saddr=" + mRakutenClient.getmMyLatitute() + ","
                                        + mRakutenClient.getmMyLongitude() + "(現在地)";
                                url += "&daddr=" + mTargetList.get(iTargetListIndex).getLatitude()
                                        + "," + mTargetList.get(iTargetListIndex).getLongitude()
                                        + "(目的地)";

                                Intent intentRote = new Intent();
                                intentRote.setAction(Intent.ACTION_VIEW);
                                intentRote.setClassName("com.google.android.apps.maps",
                                        "com.google.android.maps.MapsActivity");
                                intentRote.setData(Uri.parse(url));
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
                                intentToSettingWindow
                                        .putExtra("personal.john.app.Arrived", strInfo);

                                startActivity(intentToSettingWindow);
                                break;
                            case 3: // 楽天Webページを開く
                                Intent intentWeb = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(mTargetList.get(iTargetListIndex)
                                                .getInfomationUrl()));
                                startActivity(intentWeb);
                            default:
                        }
                    }
                });

                dialog.show();
                return true;
            }
        });

    }

}
