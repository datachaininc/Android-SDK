package in.datacha.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.datacha.BuildConfig;

class TrackGooglePurchase {

    static private int iapEnabled = -99;
    private static ServiceConnection mServiceConn;
    private static Class<?> IInAppBillingServiceClass;
    private Object mIInAppBillingService;
    private Method getPurchasesMethod, getSkuDetailsMethod;
    private Context appContext;

    private ArrayList<String> purchaseTokens;

    // Any new purchases found count as pre-existing.
    // The constructor sets it to false if we already saved any purchases or already found out there isn't any.
    private boolean newAsExisting = true;
    private boolean isWaitingForPurchasesRequest = false;

    TrackGooglePurchase(Context activity) {
        appContext = activity;

        purchaseTokens = new ArrayList<>();
        try {
            String purchaseTokensString = SharedPrefOperations.getString(appContext,
                    DatachainConstants.DATACHAIN_PREFS_PURCHASE_TOKENS,"[]");

            JSONArray jsonPurchaseTokens = new JSONArray(purchaseTokensString);
            for (int i = 0; i < jsonPurchaseTokens.length(); i++)
                purchaseTokens.add(jsonPurchaseTokens.get(i).toString());
            newAsExisting = (jsonPurchaseTokens.length() == 0);
            if (newAsExisting)
                newAsExisting = SharedPrefOperations.getBoolean(appContext,
                        DatachainConstants.DATACHAIN_PREFS_EXISTING_PURCHASES, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        trackIAP();
    }

    @SuppressLint({"WrongConstant", "PrivateApi"})
    static boolean CanTrack(Context context) {
        if (iapEnabled == -99)
            iapEnabled = context.checkCallingOrSelfPermission("com.android.vending.BILLING");
        try {
            if (iapEnabled == PackageManager.PERMISSION_GRANTED)
                IInAppBillingServiceClass = Class.forName("com.android.vending.billing.IInAppBillingService");
        } catch (Throwable t) {
            iapEnabled = 0;
            return false;
        }

        return (iapEnabled == PackageManager.PERMISSION_GRANTED);
    }

    void trackIAP() {
        if (mServiceConn == null) {
            mServiceConn = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    iapEnabled = -99;
                    mIInAppBillingService = null;
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    try {
                        @SuppressLint("PrivateApi") Class<?> stubClass = Class.forName("com.android.vending.billing.IInAppBillingService$Stub");
                        Method asInterfaceMethod = getAsInterfaceMethod(stubClass);

                        if (asInterfaceMethod != null) {
                            asInterfaceMethod.setAccessible(true);
                            mIInAppBillingService = asInterfaceMethod.invoke(null, service);
                            QueryBoughtItems();
                        }

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            };

            Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");

            appContext.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        } else if (mIInAppBillingService != null)
            QueryBoughtItems();
    }

    private void QueryBoughtItems() {
        if (isWaitingForPurchasesRequest)
            return;

        new Thread(new Runnable() {
            public void run() {
                isWaitingForPurchasesRequest = true;
                try {
                    if (getPurchasesMethod == null) {
                        getPurchasesMethod = getGetPurchasesMethod(IInAppBillingServiceClass);
                        getPurchasesMethod.setAccessible(true);
                    }
                    Bundle ownedItems = (Bundle) getPurchasesMethod.invoke(mIInAppBillingService, 3, appContext.getPackageName(), "inapp", null);
                    if (ownedItems.getInt("RESPONSE_CODE") == 0) {
                        ArrayList<String> skusToAdd = new ArrayList<>();
                        ArrayList<String> newPurchaseTokens = new ArrayList<>();

                        ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                        for (int i = 0; i < purchaseDataList.size(); i++) {
                            String purchaseData = purchaseDataList.get(i);
                            String sku = ownedSkus.get(i);
                            JSONObject itemPurchased = new JSONObject(purchaseData);
                            String purchaseToken = itemPurchased.getString("purchaseToken");

                            if (!purchaseTokens.contains(purchaseToken) && !newPurchaseTokens.contains(purchaseToken)) {
                                newPurchaseTokens.add(purchaseToken);
                                skusToAdd.add(sku);
                            }
                        }

                        if (skusToAdd.size() > 0)
                            sendPurchases(skusToAdd, newPurchaseTokens);
                        else if (purchaseDataList.size() == 0) {
                            newAsExisting = false;

                            SharedPrefOperations.putBoolean(appContext,
                                    DatachainConstants.DATACHAIN_PREFS_EXISTING_PURCHASES,false);
                        }

                        // TODO: Handle very large list. Test for continuationToken != null then call getPurchases again
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                isWaitingForPurchasesRequest = false;
            }
        }).start();
    }

    private void sendPurchases(final ArrayList<String> skusToAdd, final ArrayList<String> newPurchaseTokens) {
        try {
            if (getSkuDetailsMethod == null) {
                getSkuDetailsMethod = getGetSkuDetailsMethod(IInAppBillingServiceClass);
                getSkuDetailsMethod.setAccessible(true);
            }

            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skusToAdd);
            Bundle skuDetails = (Bundle)getSkuDetailsMethod.invoke(mIInAppBillingService, 3, appContext.getPackageName(), "inapp", querySkus);

            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                Map<String, JSONObject> currentSkus = new HashMap<>();
                JSONObject jsonItem;
                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    BigDecimal price = new BigDecimal(object.getString("price_amount_micros"));
                    price = price.divide(new BigDecimal(1000000));

                    jsonItem = new JSONObject();
                    jsonItem.put("sku", sku);
                    jsonItem.put("iso", object.getString("price_currency_code"));
                    jsonItem.put("amount", price.toString());
                    currentSkus.put(sku, jsonItem);
                }

                JSONArray purchasesToReport = new JSONArray();
                for (String sku : skusToAdd) {
                    if (!currentSkus.containsKey(sku))
                        continue;
                    purchasesToReport.put(currentSkus.get(sku));
                }

                // New purchases to report. If successful then mark them as tracked.
                if (purchasesToReport.length() > 0) {

                    Utils.Log("Purchases To Report "+purchasesToReport);
                    List<InAppPurchase.PurchaseInfo> purchaseInfos= new ArrayList<>();

                    for (int i = 0; i <purchasesToReport.length(); i++) {
                        JSONObject jsonData = purchasesToReport.getJSONObject(i);
                        purchaseInfos.add(new InAppPurchase.PurchaseInfo(jsonData.getString("sku"), jsonData.getString("iso"), jsonData.getString("amount")));

                    }
                    Main.setUserPurchase(purchaseInfos);

                    purchaseTokens.addAll(newPurchaseTokens);


                    SharedPrefOperations.putString(appContext,
                            DatachainConstants.DATACHAIN_PREFS_PURCHASE_TOKENS, purchaseTokens.toString());
                    SharedPrefOperations.putBoolean(appContext,
                            DatachainConstants.DATACHAIN_PREFS_EXISTING_PURCHASES, true);

                    newAsExisting = false;
                    isWaitingForPurchasesRequest = false;






                }
            }
        } catch (Throwable t) {
            Utils.Log("Failed to track IAP purchases "+ t);
        }
    }

    private static Method getAsInterfaceMethod(Class clazz) {
        for(Method method : clazz.getMethods()) {
            Class<?>[] args = method.getParameterTypes();
            if (args.length == 1 && args[0] == android.os.IBinder.class)
                return method;
        }

        return  null;
    }

    private static Method getGetPurchasesMethod(Class clazz) {
        for(Method method : clazz.getMethods()) {
            Class<?>[] args = method.getParameterTypes();
            if (args.length == 4
                    && args[0] == int.class && args[1] == String.class && args[2] == String.class && args[3] == String.class)
                return method;
        }

        return  null;
    }

    private static Method getGetSkuDetailsMethod(Class clazz) {
        for(Method method : clazz.getMethods()) {
            Class<?>[] args = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();

            if (args.length == 4
                    && args[0] == int.class && args[1] == String.class && args[2] == String.class && args[3] == Bundle.class
                    && returnType == Bundle.class)
                return method;
        }

        return  null;
    }

    static void unBindServiceConnection(Activity activity){
        if(mServiceConn!=null && activity!=null){
            try {
                activity.unbindService(mServiceConn);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}