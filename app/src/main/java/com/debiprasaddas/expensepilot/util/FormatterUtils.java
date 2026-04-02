package com.debiprasaddas.expensepilot.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class FormatterUtils {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    private FormatterUtils() {
    }

    public static String currency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String shortDate(long timestamp) {
        return DAY_FORMAT.format(new Date(timestamp));
    }

    public static String fullDate(long timestamp) {
        return FULL_DATE_FORMAT.format(new Date(timestamp));
    }
}
