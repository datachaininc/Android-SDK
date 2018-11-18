package in.datacha.classes;

import java.util.List;
import java.util.TimeZone;

 class SessionDuration {

    private String advertising_id;
    private String publisher_key;
    private List<SessionDetails> sessions;
    private String sdk_version;
     private String app_package_name;

    SessionDuration() {
        this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;

    }

     SessionDuration(String advertising_id, String publisher_key, List<SessionDetails> sessions) {
        this.advertising_id = advertising_id;
        this.publisher_key = publisher_key;
        this.sessions = sessions;
         this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;

     }

     public String getApp_package_name() {
         return app_package_name;
     }

     public void setApp_package_name(String app_package_name) {
         this.app_package_name = app_package_name;
     }

     String getAdvertising_id() {
        return advertising_id;
    }

    void setAdvertising_id(String advertising_id) {
        this.advertising_id = advertising_id;
    }

     String getPublisher_key() {
        return publisher_key;
    }

    void setPublisher_key(String publisher_key) {
        this.publisher_key = publisher_key;
    }

    List<SessionDetails> getSessions() {
        return sessions;
    }

    void setSessions(List<SessionDetails> sessions) {
        this.sessions = sessions;
    }

     static class SessionDetails{
        private long start_time;
        private long end_time;
        private int timezone;

         SessionDetails() {
        }

        SessionDetails(long start_time, long end_time) {
            this.start_time = start_time;
            this.end_time = end_time;
            this.timezone = TimeZone.getDefault().getRawOffset();
        }

         long getStart_time() {
            return start_time;
        }

         void setStart_time(long start_time) {
            this.start_time = start_time;
        }

         float getEnd_time() {
            return end_time;
        }

         void setEnd_time(long end_time) {
            this.end_time = end_time;
        }

         int getTimezone() {
            return timezone;
        }

         void setTimezone(int timezone) {
            this.timezone = timezone;
        }
    }


}
