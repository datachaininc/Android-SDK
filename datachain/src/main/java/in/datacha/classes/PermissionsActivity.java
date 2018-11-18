package in.datacha.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

public class PermissionsActivity extends Activity {

    private static final int REQUEST_LOCATION = 2;

    static boolean waiting, answered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android sets android:hasCurrentPermissionsRequest if the Activity was recreated while
        //  the permission prompt is showing to the user.
        // This can happen if the task is cold resumed from the Recent Apps list.
        if (savedInstanceState != null &&
                savedInstanceState.getBoolean("android:hasCurrentPermissionsRequest", false))
            waiting = true;
        else
            requestPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (DataChain.initialized)
            requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            finish();
            return;
        }

        if (!waiting) {
            waiting = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        answered = true;
        waiting = false;

        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                LocationManager.startLocationUpdates();
        }

        finish();
    }


    static void startPrompt() {
        if (PermissionsActivity.waiting || PermissionsActivity.answered)
            return;

        Intent intent = new Intent(DataChain.getInstance().getContext(),PermissionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        DataChain.getInstance().getContext().startActivity(intent);

    }
}

