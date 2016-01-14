package lu.acel.lidderbuch.helper;

import android.os.Build;

/**
 * Created by luis-fleta on 14/01/16.
 */

public final class AndroidHelper {
    public static boolean isLollipop() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
