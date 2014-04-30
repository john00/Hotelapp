
package personal.john.app;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class MyCustomListData {
    private Bitmap mHotelImg;

    private String mHotelName;

    private String mHotelInfo;

    private String mHotelDistance;

    public void setHotelImage(Bitmap result) {
        mHotelImg = result;
    }

    public Bitmap getHotelImage() {
        return mHotelImg;
    }

    public void setHotelName(String name) {
        mHotelName = name;
    }

    public String getHotelName() {
        return mHotelName;
    }

    public void setHotelInfo(String info) {
        mHotelInfo = info;
    }

    public String getHotelInfo() {
        return mHotelInfo;
    }

    public void setHotelDistance(String distance) {
        mHotelDistance = distance;
    }

    public String getHotelDistance() {
        return mHotelDistance;
    }

}
