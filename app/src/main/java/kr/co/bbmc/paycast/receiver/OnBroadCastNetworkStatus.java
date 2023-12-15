package kr.co.bbmc.paycast.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.co.bbmc.selforderutil.NetworkUtil;

public class OnBroadCastNetworkStatus extends BroadcastReceiver {
    private static String TAG = "OnBroadCastNetwrokStatus";
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_CONNECTED = 3;
    public static int TYPE_NOT_CONNECTED = 0;


    @Override
    public void onReceive(Context context, Intent intent) {
        int status = TYPE_NOT_CONNECTED;

        if (!NetworkUtil.isConnected(context)) {
            status = TYPE_NOT_CONNECTED;
        }
        else
            status = TYPE_CONNECTED;
        Log.e(TAG, "OnBroadCastNetwrokStatus() status="+status);
    }
}
