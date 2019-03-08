package zac.ling.pdfviewer.lib.utilities;

import android.util.Log;

public final class ZLLogUtils {
    
    private static final String TAG = "Zac";
    
    private ZLLogUtils() {}
    
    public static void i(String s) {
        Log.i(TAG, s);
    }
}
