package in.datacha.classes;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

 class InAppPurchase {
    private String advertising_id;
    private String publisher_key;
    private long time;
    private int timezone;
    private List<PurchaseInfo> purchaseInfos;
    private String sdk_version;
     private String app_package_name;


    InAppPurchase(String advertising_id, String publisher_key, List<PurchaseInfo> purchaseInfos) {
        this.advertising_id = advertising_id;
        this.publisher_key = publisher_key;
        this.purchaseInfos = purchaseInfos;
        this.time = new Date().getTime();
        this.timezone = TimeZone.getDefault().getRawOffset();
        this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;
    }

    InAppPurchase() {
        this.time = new Date().getTime();
        this.timezone = TimeZone.getDefault().getRawOffset();
        this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;

    }

     public String getApp_package_name() {
         return app_package_name;
     }

     public void setApp_package_name(String app_package_name) {
         this.app_package_name = app_package_name;
     }

     List<PurchaseInfo> getPurchaseInfos() {
        return purchaseInfos;
    }

     void setPurchaseInfos(List<PurchaseInfo> purchaseInfos) {
        this.purchaseInfos = purchaseInfos;
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

     long getTime() {
        return time;
    }

     void setTime(long time) {
        this.time = time;
    }

     int getTimezone() {
        return timezone;
    }

     void setTimezone(int timezone) {
        this.timezone = timezone;
    }

 static class PurchaseInfo{
     private String sku;
     private String iso;
     private String amount;

      PurchaseInfo() {
     }

      PurchaseInfo(String sku, String iso, String amount) {
         this.sku = sku;
         this.iso = iso;
         this.amount = amount;
     }

      String getSku() {
         return sku;
     }

      void setSku(String sku) {
         this.sku = sku;
     }

      String getIso() {
         return iso;
     }

      void setIso(String iso) {
         this.iso = iso;
     }

      String getAmount() {
         return amount;
     }

      void setAmount(String amount) {
         this.amount = amount;
     }
 }
}
