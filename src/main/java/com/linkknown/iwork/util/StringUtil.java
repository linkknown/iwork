package com.linkknown.iwork.util;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static boolean contains(String[] strArr, String str) {
        return Arrays.asList(strArr).parallelStream().filter(s -> s.equals(str)).count() > 1;
    }

    public static boolean containsIgnoreCase(String[] strArr, String str) {
        return Arrays.asList(strArr).parallelStream().filter(s -> s.equalsIgnoreCase(str)).count() > 1;
    }

    public static Set<String> getNoRepeatSubStringWithRegexp(String str, String regex) {
        return new HashSet<>(getSubStringWithRegexp(str, regex));
    }

    public static List<String> getSubStringWithRegexp(String str, String regex) {
        List<String> lst = new LinkedList<>();
        if (str != null) {
            Pattern datePattern = Pattern.compile(regex);
            Matcher dateMatcher = datePattern.matcher(str);
            while (dateMatcher.find()) {
                lst.add(dateMatcher.group());
            }
        }
        return lst;
    }

    // 根据分隔符进行分割得到数组,同时保留分隔符 sep
    public static List<String> splitWithSepRetain(String str, String sep) {
        List<String> lst = new ArrayList<>();
        String[] strArr = StringUtils.splitByWholeSeparator(str, sep);

        for (int index=0; index<strArr.length; index++) {
            String _str = strArr[index];
            lst.add(_str);
            if (index < strArr.length - 1) {
                lst.add(sep);
            }
        }
        return lst;
    }
}
