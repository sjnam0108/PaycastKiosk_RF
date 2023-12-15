package kr.co.bbmc.paycast.data.repository.local;

import android.content.Context;
import android.content.SharedPreferences;

public class PayCastPreference {
    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mPrefsEditor;
    private static Context mContext;
    private static String PAYCAST_LOCK_CODE = "pref_key_type_lock_code";

    private PayCastPreference(Context context) {
        initPrefs(context);
    }

    public static void initPrefs(Context context) {
        if( mPrefs == null){
            mContext = context;
            mPrefs = context.getSharedPreferences(context.getPackageName() + "_paycastpreferences", Context.MODE_PRIVATE);
            mPrefsEditor = mPrefs.edit();
        }
    }
    public static String getPaycastLockCode() {
        return mPrefs.getString(PAYCAST_LOCK_CODE, "0000");
    }

    public static void setPaycastLockCode(String value) {
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString(PAYCAST_LOCK_CODE, value);
        mPrefsEditor.commit();
    }

}
