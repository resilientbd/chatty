package com.faisal.util.local;
/**
 * Create by Sk. Faisal
 * purpose: fetch and store preference data
 * email:faisal.hossain.pk@gmail.com
 * github:https://github.com/resilientbd
 * Date: 03/07/2020
 */

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {
    public static final String DEFAULT_NAME = "app_preference";

    public static void ADD_PREFERENCE(Context context, String name, String value) {

        SharedPreferences pref = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(name, value);
        editor.commit();
    }

    public static void CLEAR_PREFERENCE(Context context) {

        SharedPreferences pref = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public static String GET_PREFERENCE(Context context, String name) {
        SharedPreferences pref = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
        String value = pref.getString(name, "");
        return value;
    }

}
