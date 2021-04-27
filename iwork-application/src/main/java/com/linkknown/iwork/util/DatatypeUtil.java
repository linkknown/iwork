package com.linkknown.iwork.util;

import java.util.ArrayList;
import java.util.List;

public class DatatypeUtil {
    public static List<Object> objectConvertToSlice(Object object) {
        List<Object> lst = new ArrayList<>();
        if (object == null) {
            return lst;
        }
        if (object instanceof List) {
            List<Object> _lst = (List<Object>) object;
            lst.addAll(_lst);
        } else {
            lst.add(object);
        }
        return lst;
    }

    public static int objectToInt(Object obj, int ... defaultVal) {
        if (obj instanceof Integer) {
            return (int)obj;
        } else if (obj instanceof Long) {
            return new Long((long)obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt((String)obj);
        }
        return defaultVal[0];
    }
}
