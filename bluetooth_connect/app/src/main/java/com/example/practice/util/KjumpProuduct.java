package com.example.practice.util;

import java.util.regex.Pattern;

public class KjumpProuduct {
    public static boolean ProductFilter(final String name) {
        Pattern pattern = Pattern.compile("^K[BDISP]-[0-9]{4}$", Pattern.CASE_INSENSITIVE);
        return name != null && pattern.matcher(name).find();
    }
}
