package in.datacha.classes;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.datacha.BuildConfig;

class ActivityLifecycleHandler {

    static Activity curActivity;
    static long sessionStartTimestamp=0;
    static boolean nextResumeIsFirstActivity;
    private static FocusHandlerThread focusHandlerThread = new FocusHandlerThread();

    private static void setCurActivity(Activity activity) {
        if(sessionStartTimestamp==0){
            sessionStartTimestamp = new Date().getTime();
        }
        curActivity = activity;
    }

    static void onActivityCreated(Activity activity) {
    }
    static void onActivityStarted(Activity activity) {
    }

    static void onActivityResumed(Activity activity){
        handleFocus();
        setCurActivity(activity);
    }

    static void onActivityPaused(Activity activity){

    }


    static void onActivityStopped(Activity activity){

    }

    static void onActivityDestroyed(Activity activity){

        if (activity == curActivity) {
            curActivity = null;
            handleLostFocus();
        }

    }

    static private void handleFocus() {
        if (focusHandlerThread.hasBackgrounded() || nextResumeIsFirstActivity) {
            nextResumeIsFirstActivity = false;
            focusHandlerThread.resetBackgroundState();
            DataChain.onAppFocus();
        }
        else{
            focusHandlerThread.stopScheduledRunnable();
        }
    }

    static private void handleLostFocus() {
        focusHandlerThread.runRunnable(new AppFocusRunnable());
    }


    static class FocusHandlerThread extends HandlerThread {
        Handler mHandler;
        private AppFocusRunnable appFocusRunnable;

        FocusHandlerThread() {
            super("FocusHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        Looper getHandlerLooper() {
            return  mHandler.getLooper();
        }

        void resetBackgroundState() {
            if (appFocusRunnable != null)
                appFocusRunnable.backgrounded = false;
        }

        void stopScheduledRunnable() {
            mHandler.removeCallbacksAndMessages(null);
        }

        void runRunnable(AppFocusRunnable runnable) {
            if (appFocusRunnable != null && appFocusRunnable.backgrounded && !appFocusRunnable.completed)
                return;

            appFocusRunnable = runnable;
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(runnable, 2000);
        }

        boolean hasBackgrounded() {
            return appFocusRunnable != null && appFocusRunnable.backgrounded;
        }
    }

    static private class AppFocusRunnable implements Runnable {
        private boolean backgrounded, completed;

        public void run() {
            if (curActivity != null)
                return;

            backgrounded = true;
            DataChain.onAppLostFocus();

            completed = true;
        }
    }

}
