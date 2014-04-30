
package personal.john.app;

import java.util.Comparator;

public class MyComparator implements Comparator<HotelInfo> {
    public static final int ASC = 1; // 昇順

    public static final int DESC = -1; // 降順

    public static final int MODE_HOTELNAME = 1; // ホテル名でソート

    public static final int MODE_DISTANCE = 2; // 距離でソート

    private int sort;

    private int mode;

    public MyComparator() {
        sort = ASC;
        mode = MODE_HOTELNAME;
    }

    public MyComparator(int sort, int mode) {
        this.sort = sort;
        this.mode = mode;
    }

    @Override
    public int compare(HotelInfo lhs, HotelInfo rhs) {
        if (lhs == null && rhs == null) {
            return 0; // arg0 = arg1
        } else if (lhs == null) {
            return 1 * sort; // arg1 > arg2
        } else if (rhs == null) {
            return -1 * sort; // arg1 < arg2
        }

        switch (mode) {
            case MODE_HOTELNAME:
                return ((Comparable) lhs.getName()).compareTo((Comparable) rhs.getName()) * sort;
            case MODE_DISTANCE:
                return ((Comparable) lhs.getDistance()).compareTo((Comparable) rhs.getDistance())
                        * sort;
            default:
                return ((Comparable) lhs.getName()).compareTo((Comparable) rhs.getName()) * sort;
        }
    }
}
