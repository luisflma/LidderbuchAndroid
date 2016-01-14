package lu.acel.lidderbuch.helper;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by luis-fleta on 14/01/16.
 */
public class FontHelper {

    public static Typeface georgia;
    public static Typeface georgiaItalic;

    public static void init(Context ctx)
    {
        georgia = Typeface.createFromAsset(ctx.getAssets(), "fonts/Georgia.ttf");
        georgiaItalic = Typeface.createFromAsset(ctx.getAssets(), "fonts/Georgia_Italic.ttf");
    }

    public static boolean isInitialized() {
        return !(georgia == null || georgiaItalic == null);
    }
}
