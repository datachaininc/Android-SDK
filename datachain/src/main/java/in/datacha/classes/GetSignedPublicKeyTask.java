package in.datacha.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import in.datacha.BuildConfig;

public class GetSignedPublicKeyTask extends AsyncTask<String ,Void, Boolean> {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    GetSignedPublicKeyTask(Context ctx){
        this.context = ctx;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        try {

            if(params!=null && params.length>=1) {

                try {
                    String publisher_url = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_PUBLISHER_SERVER_URL, "");
                    String publisher_key = SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, "");
                    if(publisher_url.length()<=0 && publisher_key.length()<=0)
                        return false;
                    URL url = new URL(publisher_url);
                    String public_key = params[0];
                    String token = params[1];

                    // Create the urlConnection
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    urlConnection.setRequestMethod("POST");


                    urlConnection.setRequestProperty("x-api-key", publisher_key);
                    JSONObject payload = new JSONObject();
                    payload.put("key", public_key);
                    payload.put("token",token);
                    // Send the post body

                        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                        writer.write(payload.toString());
                        writer.flush();
                        writer.close();


                    int statusCode = urlConnection.getResponseCode();
                    if(statusCode==200){
                        String response = readStream(urlConnection.getInputStream());
                        JSONObject res = new JSONObject(response);
                        if(res.getBoolean("status")){
                            String signData = res.getString("sign");
                            SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_SIGNED_KEY, signData);
                        }
                        else{
                            Utils.Log("Key not signed");
                        }

                    }
                    else{
                        Utils.Log("Publisher api call error");
                    }


                }catch (IOException e){
                    e.printStackTrace();
                }
                finally {
                    if(urlConnection!=null){
                        try {
                            urlConnection.getInputStream().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        urlConnection.disconnect();
                    }
                }
                return true;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result){
            Main.continueInitialize();
        }
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
