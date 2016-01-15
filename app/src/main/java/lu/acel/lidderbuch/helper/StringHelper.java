package lu.acel.lidderbuch.helper;

import org.json.JSONObject;

import java.text.Normalizer;

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

    public static int countOccurrences(String inputStr, String findStr) {
        int lastIndex = 0;
        int count = 0;

        String str = inputStr.toLowerCase();
        str = removeDiacriticalMarks(str);
        while(lastIndex != -1){

            lastIndex = str.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += findStr.length();
            }
        }

        return count;
    }

    public static String removeDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
