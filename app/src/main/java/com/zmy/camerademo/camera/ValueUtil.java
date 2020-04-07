package com.zmy.camerademo.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueUtil {

    private static Pattern isIntegerPattern = Pattern.compile("^[0-9]*$");
    private static Pattern isUUNumPattern = Pattern.compile("[\\d]{8}");

    /**
     * 是否字符串有效
     *
     * @param string
     * @return
     */
    public static boolean isStringValid(String string) {
        return string != null && !"".equals(string.trim()) && !"null".equals(string.trim());
    }

    public static <T> boolean isArrayValid(T[] t) {
        return t != null && t.length != 0;
    }

    public static <T> boolean isListValid(List<T> list) {
        return list != null && list.size() != 0;
    }

    public static long[] listStringToArrayLong(List<String> strings) {
        if (strings == null) {
            return null;
        }
        long[] longs = new long[strings.size()];
        for (int i = 0; i < strings.size(); i++) {
            longs[i] = Long.valueOf(strings.get(i));
        }
        return longs;
    }

    public static List<String> listLongToArray(long[] strings) {
        if (strings == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        for (long l : strings) {
            list.add(String.valueOf(l));
        }
        return list;
    }

    public static boolean isIntArrayValid(int[] t) {
        return t != null && t.length != 0;
    }

    public static boolean isUUNum(String strName) {
        if (strName == null) {
            return false;
        }
        Matcher matcher = isUUNumPattern.matcher(strName);
        return matcher.matches();
    }

    public static boolean isEqualList(List l0, List l1) {
        if (l0 == l1) {
            return true;
        }
        if (l0 == null || l1 == null) {
            return false;
        }
        if (l0.size() != l1.size()) {
            return false;
        }
        for (Object o : l0) {
            if (!l1.contains(o)) {
                return false;
            }
        }
        for (Object o : l1) {
            if (!l0.contains(o)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDouble(String str) {
        try {
            Double.valueOf(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 字符串是否可以转为数字
     *
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        if (!isStringValid(str)) {
            return false;
        }
        Matcher matcher = isIntegerPattern.matcher(str);
        return matcher.matches();
    }

    /**
     * List<String> 转字符串
     *
     * @param list
     * @param separator
     * @return
     */
    public static String listToString(List<String> list, String separator) {
        if (list != null && list.size() > 0) {
            if (list.size() == 1) {
                return list.get(0);
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (ValueUtil.isStringValid(list.get(i))) {
                    sb.append(list.get(i));
                    if (i != list.size() - 1) {
                        sb.append(separator);
                    }
                }
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * 判断字符串是否为URL
     *
     * @return true:是URL、false:不是URL
     */
    public static boolean isHttpUrl(String urls) {
        return urls.startsWith("https://") || urls.startsWith("http://");
    }
}
