package lu.acel.lidderbuch.network;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Scanner;

import lu.acel.lidderbuch.Settings;

/**
 * Created by luis-fleta on 13/01/16.
 */
public class LBFetchSongs {
    public static void run(final Context context, Date updateTime, final LBFetchSongsCallback callback) {

        AsyncHttpClient client = new AsyncHttpClient();


        Log.i("FetchSongs", "updateTime:" + updateTime.getTime() / 1000);
        String arg0 = String.valueOf(1440754883);  // updateTime.getTime() / 1000
        //String url = MessageFormat.format(Settings.SONGBOOK_API, arg0);

        String url = "https://dev.acel.lu/api/v1/songs/";

        Log.i("FetchSongs", "url:" + url);
        client.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        client.setEnableRedirects(true, true, true);
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                for( int i = 0 ; i < response.length() ; i++) {
                    try {
                        JSONObject songJson = response.getJSONObject(i);
                        Log.i("FetchSongs", "JSON OBJECT : " + songJson.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.i("FetchSongs", "onFailure : " + s);
            }
        });

        client.get("https://dev.acel.lu/api/v1/songs/", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Log.i("FetchSong", "onStart");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.i("FetchSong", "onsuccess:" + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i("FetchSong", "onFailure:" + statusCode);
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                Log.i("FetchSong", "onRetry");
            }
        });

        HttpClient httpClient = new DefaultHttpClient();
        String result = null;
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = null;
        InputStream instream = null;

        try {
            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                instream = entity.getContent();
                result = convertStreamToString(instream);
            }

        } catch (Exception e) {
            // manage exceptions
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (Exception exc) {

                }
            }
        }

        Log.i("FetchSongs", "result:" + result);
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

        return sb.toString();
    }

    public static JSONArray requestWebService(String serviceUrl) {
        disableConnectionReuseIfNecessary();

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            //urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            //urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
            }

            // create JSON object from content
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            String responseString = getResponseText(in);
            Log.i("FetchSongs", "RESPONSE STRING : " + responseString);
            return new JSONArray(responseString);

        } catch (MalformedURLException e) {
            Log.i("FetchSongs", "Invalid URL");
            // URL is invalid
        } catch (SocketTimeoutException e) {
            // data retrieval or connection timed out
            Log.i("FetchSongs", "SocketTimeoutException ");
        } catch (IOException e) {
            Log.i("FetchSongs", "IOException");
            // could not read response body
            // (could not create input stream)
        } catch (JSONException e) {
            // response body is no valid JSON string
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    /**
     * required in order to prevent issues in earlier Android version.
     */
    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }
}