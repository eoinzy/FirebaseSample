package com.example.firebasesample.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Wrapper class for {@link SharedPreferences}
 */
public class AppPrefs {

    public String getLastSavedData() {
        return sharedPrefs.getString(LAST_SAVED_DATA, null);
    }

    public void setLastSavedData(String lastSavedData) {
        editor.putString(LAST_SAVED_DATA, lastSavedData);
        editor.apply();
    }

    public String getCompressedImage() {
        return sharedPrefs.getString(ENCODED_IMAGE, null);
    }

    public void setCompressedImage(String encodedImage) {
        editor.putString(ENCODED_IMAGE, encodedImage);
        editor.apply();
    }


    /******************************************************/
    /******************************************************/
    /******************************************************/
    /*******************PREFERENCE KEYS********************/
    /******************************************************/
    /******************************************************/
    /******************************************************/

    private String LAST_SAVED_DATA = "last_saved_data";
    private String ENCODED_IMAGE = "encoded_image";


    /******************************************************/
    /******************************************************/
    /******************************************************/
    /*******************STANDARD METHODS*******************/
    /******************************************************/
    /******************************************************/
    /******************************************************/

    private final String TAG = AppPrefs.class.getSimpleName();

    private static AppPrefs mInstance;

    private SharedPreferences sharedPrefs;
    private final SharedPreferences.Editor editor;

    public static AppPrefs getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppPrefs(context);
        }
        return mInstance;
    }

    private AppPrefs(Context ctx) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        //sharedPrefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();
        editor.apply();
    }
}
