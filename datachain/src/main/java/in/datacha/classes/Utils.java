package in.datacha.classes;

import android.util.Log;

class Utils {
    static void Log(String message){
        if(DataChain.getInstance()!=null)
            if(DataChain.getInstance().getDebugMode()) {
                Log.d("DatachainPublisherSDK", message);
            }
    }
}
