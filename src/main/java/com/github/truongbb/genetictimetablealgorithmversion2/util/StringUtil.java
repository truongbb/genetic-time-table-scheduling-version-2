package com.github.truongbb.genetictimetablealgorithmversion2.util;

public class StringUtil {

    public static String convert3DigitsNumber(Long id) {
        if (id >= 100) {
            return String.valueOf(id);
        } else if (id < 100 && id >= 10) {
            return "0" + id;
        }
        return "00" + id;
    }

    public static String convert2DigitsNumber(Long id) {
        if (id >= 10) {
            return String.valueOf(id);
        }
        return "0" + id;
    }

}
