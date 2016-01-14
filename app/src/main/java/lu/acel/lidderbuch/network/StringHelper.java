package lu.acel.lidderbuch.network;

import org.json.JSONObject;

/**
 * Created by luis-fleta on 14/01/16.
 */
public class StringHelper {

    //http://stackoverflow.com/questions/18226288/json-jsonobject-optstring-returns-string-null
    /** Return the value mapped by the given key, or {@code null} if not present or null. */
    public static String optString(JSONObject json, String key)
    {
        // http://code.google.com/p/android/issues/detail?id=13830
        if (json.isNull(key))
            return null;
        else
            return json.optString(key, null);
    }
}
