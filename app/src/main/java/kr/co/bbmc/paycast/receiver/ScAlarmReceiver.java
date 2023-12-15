package kr.co.bbmc.paycast.receiver;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.compose.foundation.ExperimentalFoundationApi;

import java.util.List;

import kr.co.bbmc.paycast.presentation.main.MainKioskActivity;

@ExperimentalFoundationApi public class ScAlarmReceiver extends BroadcastReceiver {
    public static int REQUEST_CODE = 22345;
    private static String TAG = "ScAlarmReceiver";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return ;
        }

        Intent i = new Intent(context, MainKioskActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
        try {
            pi.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "ScAlarmReceiver() call");
    }

}
