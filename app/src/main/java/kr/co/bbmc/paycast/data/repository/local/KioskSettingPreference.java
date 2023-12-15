package kr.co.bbmc.paycast.data.repository.local;

import android.content.Context;
import android.content.SharedPreferences;

public class KioskSettingPreference {
    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mPrefsEditor;
    private static Context mContext;
    private static String SETTING_STB_OPTION_ENV = "pref_key_last_order_num";

    private KioskSettingPreference() {

    }

    public static void initPrefs(Context context) {
        if( mPrefs == null){
            mContext = context;
            mPrefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
            mPrefsEditor = mPrefs.edit();
        }
    }
    public static int getLastOrderNumber() {
        return mPrefs.getInt(SETTING_STB_OPTION_ENV, 0);
    }

    public static void setLastOrderNumber(int value) {
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putInt(SETTING_STB_OPTION_ENV, value);
        mPrefsEditor.commit();
    }

}
