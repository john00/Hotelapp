
package personal.john.app;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.util.Log;

public class RakutenClient {
    private static final String LOG_TAG = "RakutenClient";

    private static final String LOG_HOTEL = "RakutenClientHotel";

    private static final String API_URI = "http://api.rakuten.co.jp/rws/3.0/rest?";

    private static final String DEV_ID = "developerId=939efa54bb0986afd2f2ca7cfc89ad41";

    private static final String OPERATION = "&operation=SimpleHotelSearch";

    private static final String VERSION = "&version=2009-10-20";

    private static final String LATITUDE = "&latitude=";

    private static final String LONGITUDE = "&longitude=";

    private static final String SEARCHRADIUS = "&searchRadius=";

    private static final String DATUMTYPE = "&datumType=1";
    
    private static final String VACANTHOTEL_URI = "https://app.rakuten.co.jp/services/api/Travel/VacantHotelSearch/20131024?";
    
    private static final String APP_ID = "&applicationId=939efa54bb0986afd2f2ca7cfc89ad41";
    
    private static final String FORMAT = "&format=xml";

    private static final String HOTEL_ID = "&HotelNo=";
    

    // private static String mAccessKey = null;
    private SAXParser mParser = null;

    private RakutenClientReceiver mRakutenClientReceiver = null;

    private HotelHandler mHotelHandler = null;

    private String mRecordCount = "0";

    private Activity mActivity = null;

    public static final int ERROR_GENERAL = 1;

    public static final int ERROR_FATAL = 2;

    public static final int RANGE_300 = 1;

    public static final int RANGE_500 = 2;

    public static final int RANGE_1000 = 3;

    public static final int RANGE_2000 = 4;

    public static final int RANGE_3000 = 5;

    public static final int CATEGORY_LARGE = 0;

    public static final int CATEGORY_SMALL = 1;

    private double mMyLatitude = 0;

    private double mMyLongitude = 0;

    private double mRange;

    public RakutenClient(RakutenClientReceiver receiver, Activity activity)
            throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        mParser = factory.newSAXParser();
        mRakutenClientReceiver = receiver;
        mHotelHandler = new HotelHandler();
        mRange = 1;
        mActivity = activity;
    }

    public void requestHotel() throws SAXException, IOException {
        Log.v(LOG_TAG, "request Hotels.");
        String request = new String(API_URI + DEV_ID + OPERATION + VERSION + LATITUDE + mMyLatitude
                + LONGITUDE + mMyLongitude + SEARCHRADIUS + mRange + DATUMTYPE);
        mParser.parse(request, mHotelHandler);
    }

    public void requestHotel(double latitude, double longitude, double range) throws SAXException,
            IOException {
        Log.v(LOG_TAG, "request Hotels.");
        String request = new String(API_URI + DEV_ID + OPERATION + VERSION + LATITUDE + latitude
                + LONGITUDE + longitude + SEARCHRADIUS + range + DATUMTYPE);
        mParser.parse(request, mHotelHandler);
    }

    public void requestVacantHotel(String hotelId) throws SAXException,
            IOException {
        Log.v(LOG_TAG, "request Vacant Hotels.");
        String request = new String(VACANTHOTEL_URI + APP_ID + FORMAT + HOTEL_ID + hotelId);
        mParser.parse(request, mHotelHandler);
    }
    
    public void queryInfo(String mode, String hotelId) {
        RakutenClientExecuteThread rcExeThread = new RakutenClientExecuteThread(mActivity);
        rcExeThread.setExecModeAndHotelID(mode, hotelId);
        rcExeThread.execute(this);
    }

    public void setRecordCount(String recordcount) {
        mRecordCount = recordcount;
    }

    public int getRecordCount() {
        return Integer.parseInt(mRecordCount);
    }

    public double getmMyLatitute() {
        return mMyLatitude;
    }

    public void setmMyLatitute(double latitude) {
        mMyLatitude = latitude;
    }

    public double getmMyLongitude() {
        return mMyLongitude;
    }

    public void setmMyLongitude(double longitude) {
        mMyLongitude = longitude;
    }

    public void setSearchRange(double range) {
        mRange = range;
    }

    public double getSearchRange() {
        return mRange;
    }

    class HotelHandler extends DefaultHandler {
        private ArrayList<HotelInfo> mInfoList = null;

        private HotelInfo mHotelInfo = null;

        private String mText = null;

        private boolean mOnCatchText = false;

        public void startDocument() {
            Log.d(LOG_HOTEL, "startDocument");
            mInfoList = new ArrayList<HotelInfo>();
        }

        public void endDocument() {
            Log.d(LOG_HOTEL, "endDocument");
            mRakutenClientReceiver.receiveHotel(mInfoList);
            mInfoList = null;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            Log.d(LOG_HOTEL, "startElment:uri=" + uri.toString() + ", " + "localName=" + localName
                    + ", qName=" + qName + ",attributes length=" + attributes.getLength());
            if (localName.equals("hotel")) {
                mHotelInfo = new HotelInfo();
            } else if (localName.equals("recordCount")) {
                mOnCatchText = true;
            } else if (localName.equals("hotelNo")) {
                mOnCatchText = true;
            } else if (localName.equals("hotelName")) {
                mOnCatchText = true;
            } else if (localName.equals("hotelKanaName")) {
                mOnCatchText = true;
            } else if (localName.equals("latitude")) {
                mOnCatchText = true;
            } else if (localName.equals("longitude")) {
                mOnCatchText = true;
            } else if (localName.equals("hotelInformationUrl")) {
                mOnCatchText = true;
            } else if (localName.equals("planListUrl")) {
                mOnCatchText = true;
            } else if (localName.equals("telephoneNo")) {
                mOnCatchText = true;
            } else if (localName.equals("hotelSpecial")) {
                mOnCatchText = true;
            } else if (localName.equals("address1")) {
                mOnCatchText = true;
            } else if (localName.equals("address2")) {
                mOnCatchText = true;
            } else if (localName.equals("hotelMinCharge")) {
                mOnCatchText = true;
            } else if (localName.equals("roomName")) {
                mOnCatchText = true;
            }
        }

        public void endElement(String uri, String localName, String qName) {
            Log.d(LOG_HOTEL, "endElement:uri=" + uri.toString() + ", localName=" + localName
                    + ", qName=" + qName);
            if (localName.equals("hotel")) {
                mInfoList.add(mHotelInfo);
            } else if (localName.equals("recordCount")) {
                setRecordCount(mText);
                mOnCatchText = false;
            } else if (localName.equals("hotelNo")) {
                mHotelInfo.setNo(mText);
                mOnCatchText = false;
            } else if (localName.equals("hotelName")) {
                mHotelInfo.setName(mText);
                mOnCatchText = false;
            } else if (localName.equals("hotelKanaName")) {
                mHotelInfo.setKanaName(mText);
                mOnCatchText = false;
            } else if (localName.equals("latitude")) {
                mHotelInfo.setLatitude(mText);
                mOnCatchText = false;
            } else if (localName.equals("longitude")) {
                mHotelInfo.setLongitude(mText);
                mOnCatchText = false;
            } else if (localName.equals("hotelInformationUrl")) {
                mHotelInfo.setInfomationUrl(mText);
                mOnCatchText = false;
            } else if (localName.equals("planListUrl")) {
                mHotelInfo.setPlanListUrl(mText);
                mOnCatchText = false;
            } else if (localName.equals("telephoneNo")) {
                mHotelInfo.setTelephoneNo(mText);
                mOnCatchText = false;
            } else if (localName.equals("hotelSpecial")) {
                mHotelInfo.setSpecial(mText);
                mOnCatchText = false;
            } else if (localName.equals("address1")) {
                mHotelInfo.setAddress1(mText);
                mOnCatchText = false;
            } else if (localName.equals("address2")) {
                mHotelInfo.setAddress2(mText);
                mOnCatchText = false;
            } else if (localName.equals("hotelMinCharge")) {
                mHotelInfo.setHotelMinCharge(mText);
                mOnCatchText = false;
            } else if (localName.equals("roomName")) {
                mHotelInfo.setVacant(true);
                mOnCatchText = false;
            }
        }

        public void characters(char[] ch, int start, int length) {
            if (mOnCatchText) {
                mText = new String(ch, start, length);
                Log.d(LOG_HOTEL, "characters:" + mText);
            }
        }

        public void error(SAXParseException e) {
            Log.d(LOG_HOTEL, "error:" + e.toString());
            mText = null;
            mHotelInfo = null;
            mInfoList.clear();
            mInfoList = null;
            mRakutenClientReceiver.receiveError(ERROR_GENERAL);
        }

        public void fatalError(SAXParseException e) {
            Log.d(LOG_HOTEL, "fatalError:" + e.toString());
            mText = null;
            mHotelInfo = null;
            mInfoList.clear();
            mInfoList = null;
            mRakutenClientReceiver.receiveError(ERROR_GENERAL);
        }

        public void warning(SAXParseException e) {
            Log.d(LOG_HOTEL, "warning:" + e.toString());
        }

    }
}
