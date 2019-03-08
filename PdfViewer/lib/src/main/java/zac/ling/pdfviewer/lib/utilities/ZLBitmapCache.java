package zac.ling.pdfviewer.lib.utilities;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ZLBitmapCache {
    private final LruCache<String, Bitmap> mMemoryCache;
    
    public ZLBitmapCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }
    
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (bitmap != null && getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }
    
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
