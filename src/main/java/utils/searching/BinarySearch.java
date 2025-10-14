package utils.searching;

import java.util.List;

import constants.ID;

public class BinarySearch {
    public static int upperBound(List<ID> ids, ID value) {
        int lo = -1, hi = ids.size();
        while (lo + 1 < hi) {
            int mi = (lo + hi) >> 1;
            if (ids.get(mi).compareTo(value) > 0) {
                hi = mi;
            } else {
                lo = mi;
            }
        }
        return hi;
    }
}
