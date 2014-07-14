
package personal.john.app;

import android.location.Location;

public class HotelInfo {
    private Location mLocation = null;

    private String mNo = null;

    private String mName = null;

    private String mKanaName = null;

    private String mInfomationUrl = null;

    private String mPlanListUrl = null;

    private String mLatitude = null;

    private String mLongitude = null;

    private String mTelephoneNo = null;

    private String mSpecial = null;

    private String mAddress1 = null;

    private String mAddress2 = null;
    
    private String mHotelMinCharge = null;
    
    private boolean mVacant = false;

    private float mDistance = -1;

    public HotelInfo() {
        Location location = new Location("RakutenWebService");
        mLocation = location;
    }

    public HotelInfo(final Location location, final String name) {
        mLocation = location;
        mName = name;
    }

    public void setNo(String no) {
        mNo = no;
    }

    public String getNo() {
        return mNo;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setKanaName(String kananame) {
        mKanaName = kananame;
    }

    public String getKanaName() {
        return mKanaName;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public Location getLocation() {
        mLocation.setAltitude(0);
        mLocation.setLatitude(Location.convert(mLatitude));
        mLocation.setLongitude(Location.convert(mLongitude));
        return mLocation;
    }

    public void setInfomationUrl(String infomationurl) {
        mInfomationUrl = infomationurl;
    }

    public String getInfomationUrl() {
        return mInfomationUrl;
    }

    public void setPlanListUrl(String planlisturl) {
        mPlanListUrl = planlisturl;
    }

    public String getPlanListUrl() {
        return mPlanListUrl;
    }

    public void setTelephoneNo(String telephoneno) {
        mTelephoneNo = telephoneno;
    }

    public String getTelephoneNo() {
        return mTelephoneNo;
    }

    public String toString() {
        if (mLocation != null) {
            return new String("Name:" + mName + ",Location:" + mLocation.toString());
        } else {
            return new String("Name:" + mName + ",Location:nodata");
        }
    }

    public void setSpecial(String special) {
        mSpecial = special;
    }

    public String getSpecial() {
        return mSpecial;
    }

    public void setAddress1(String address1) {
        mAddress1 = address1;
    }

    public void setAddress2(String address2) {
        mAddress2 = address2;
    }

    public String getAddress() {
        return mAddress1 + mAddress2;
    }

    public void setHotelMinCharge(String hotelMinCharge) {
        mHotelMinCharge = hotelMinCharge;
    }

    public String getHotelMinCharge() {
        return mHotelMinCharge;
    }

    public void setVacant(Boolean vacant) {
        mVacant = vacant;
    }

    public boolean getVacant() {
        return mVacant;
    }

    public void setDistance(double mylat, double mylon, double destlat, double destlon) {
        float[] fDistance = new float[1];
        Location.distanceBetween(mylat, mylon, destlat, destlon, fDistance);
        mDistance = fDistance[0];
    }

    public float getDistance() {
        return mDistance;
    }
}
