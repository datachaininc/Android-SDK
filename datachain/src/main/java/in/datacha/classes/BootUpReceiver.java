package in.datacha.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.LocationRequest;

public class BootUpReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null)
            return;
        String action = intent.getAction();

        if(action == null)
            return;
        if(!action.equals(Intent.ACTION_BOOT_COMPLETED)
                && !action.equals("android.location.PROVIDERS_CHANGED"))
            return;
        LocationManager.changeLocationAccuracy(context, LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}