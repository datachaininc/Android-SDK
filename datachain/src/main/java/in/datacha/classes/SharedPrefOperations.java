package in.datacha.classes;

import android.content.Context;

import in.datacha.BuildConfig;

class SharedPrefOperations {
    static void putInt(Context context, String key, int value){
        context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).edit().putInt(key,value).apply();
    }
    public static void putLong(Context context, String key, long value ){
        context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).edit().putLong(key,value).apply();
    }
    public static long getLong(Context context, String key, long defValue ){
        return context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).getLong(key,defValue);
    }
    static void putString(Context context, String key, String value){
        context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).edit().putString(key,value).apply();
    }

    static int getInt(Context context, String key, int defValue){
        return context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).getInt(key,defValue);
    }

    static String getString(Context context, String key, String defValue){
        return context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).getString(key,defValue);
    }

    static void removeKey(Context context, String key){
        context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).edit().remove(key).apply();
    }

    static boolean getBoolean(Context context, String key, Boolean defValue){
        return context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).getBoolean(key, defValue);
    }

    static void putBoolean(Context context, String key, Boolean value){
        context.getSharedPreferences(BuildConfig.DATACHAIN_SHARED_PREF,Context.MODE_PRIVATE).edit().putBoolean(key,value).apply();
    }

}
