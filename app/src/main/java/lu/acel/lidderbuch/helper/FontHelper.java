package lu.acel.lidderbuch.helper;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by luis-fleta on 14/01/16.
 */
public class FontHelper {

    public static Typeface georgia;
    public static Typeface georgiaBold;
    public static Typeface georgiaItalic;
    public static Typeface georgiaItalicBold;

    public static void init(Context ctx)
    {
        georgia = Typeface.createFromAsset(ctx.getAssets(), "fonts/Georgia.ttf");
        georgiaBold = Typeface.createFromAsset(ctx.getAssets(), "fonts/Georgia_Bold.ttf");
        georgiaItalic = Typeface.createFromAsset(ctx.getAssets(), "fonts/Georgia_Italic.ttf");
        georgiaItalicBold = Typeface.createFromAsset(ctx.getAssets(), "fonts/Georgia_Italic_Bold.ttf");
    }

    public static boolean isInitialized() {
        return !(georgia == null || georgiaItalic == null);
    }
}
