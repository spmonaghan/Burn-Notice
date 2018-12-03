package com.nordicsemi.nrfUARTv2;

import android.content.Context;
import android.content.SharedPreferences;

public class BurnNoticeSharedPrefs {

    // AG: Created this class as getter/setter "memory" for user preferences
    // Child name and skin type stored already in Settings by Preference Manager
    // Sunscreen to be accounted for in future iterations

    private static final String CHILD_NAME_PREF = "User Child Name";
    private static final String SKIN_TYPE_PREF = "User Skin Type";
    private static final String SUNSCREEN_PREF = "User Sunscreen Input";
    private static final String DEMO_PREF = "Demo";
    private static final String EXPOSURE_PREF = "UV Sensor Exposure";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Burn-Notice", Context.MODE_PRIVATE);
    }

//
//    // Child name
//    public static String getChildName(Context context){
//        return getSharedPreferences(context).getString(CHILD_NAME_PREF, "");
//    }
//
//    public static void setChildName(Context context, String user_enter_name) {
//        getSharedPreferences(context).edit().putString(CHILD_NAME_PREF, user_enter_name);
//    }
//    // Skin type
//    public static int getSkinType(Context context){
//        return getSharedPreferences(context).getInt(SKIN_TYPE_PREF, 15);
//    }
//
//    public static void setSkinType(Context context, int user_enter_skin_type) {
//        getSharedPreferences(context).edit().putInt(SKIN_TYPE_PREF, user_enter_skin_type);
//    }
//
//    // Sunscreen
//    public static boolean getIsWearingSunscreen(Context context){
//        return getSharedPreferences(context).getBoolean(SUNSCREEN_PREF, false);
//    }
//
//    public static void setIsWearingSunscreen(Context context, boolean user_enter_if_sunscreen) {
//        getSharedPreferences(context).edit().putBoolean(SUNSCREEN_PREF, user_enter_if_sunscreen);
//    }

//    // Demo mode?
//
//    public static boolean getIsDemo(Context context){
//        return getSharedPreferences(context).getBoolean(DEMO_PREF, false);
//    }
//
//    public static void setIsDemo(Context context, boolean isDemo) {
//        getSharedPreferences(context).edit().putBoolean(DEMO_PREF, false);
//    }

    // Store exposure
    public static int getExposure(Context context){
        return getSharedPreferences(context).getInt(EXPOSURE_PREF, 0);
    }

    public static void setExposure(Context context, int uv_sensed_exposure) {
        getSharedPreferences(context).edit().putInt(EXPOSURE_PREF, uv_sensed_exposure);
    }

}
