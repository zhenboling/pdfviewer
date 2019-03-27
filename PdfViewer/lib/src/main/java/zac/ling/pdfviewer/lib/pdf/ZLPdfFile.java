package zac.ling.pdfviewer.lib.pdf;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.graphics.pdf.PdfRenderer.Page;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import zac.ling.pdfviewer.lib.utilities.ZLBitmapCache;
import zac.ling.pdfviewer.lib.utilities.ZLFileUtils;
import zac.ling.pdfviewer.lib.utilities.ZLLogUtils;

public class ZLPdfFile {
    
    private File mPdfFile;
    
    private int mPageCount;
    
    private ZLBitmapCache mPageCache;
    
    public ZLPdfFile(@NonNull File pdfFile) {
        try {
            mPdfFile = File.createTempFile("PDF", null);
            ZLFileUtils.copy(pdfFile, mPdfFile);
        } catch (IOException ignore) {
        
        }
        initPageCount();
        mPageCache = new ZLBitmapCache();
    }
    
    private void initPageCount() {
        ParcelFileDescriptor parcelFileDescriptor = null;
        PdfRenderer pdfRenderer = null;
        
        try {
            parcelFileDescriptor = getSeekableFileDescriptor();
            pdfRenderer = getPdfRenderer(parcelFileDescriptor);
            mPageCount = pdfRenderer.getPageCount();
            
        } catch (IOException e) {
            mPageCount = 0;
        } finally {
            ZLFileUtils.closeAutoCloseable(parcelFileDescriptor);
            ZLFileUtils.closeAutoCloseable(pdfRenderer);
        }
    }
    
    public boolean isValid() {
        return mPageCount > 0;
    }
    
    public int getPageCount() {
        return mPageCount;
    }
    
    private PdfRenderer getPdfRenderer(ParcelFileDescriptor seekableFileDescriptor) throws IOException {
        return new PdfRenderer(seekableFileDescriptor);
    }
    
    private ParcelFileDescriptor getSeekableFileDescriptor() throws IOException {
        return ParcelFileDescriptor.open(mPdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
    }
    
    public AsyncTask getPages(final int pageFrom, final int pageTo, final ZLOnPdfPageRenderListener listener) {
        if (pageFrom == pageTo) {
            return null;
        }
        if (pageFrom > pageTo) {
            return getPages(pageTo, pageFrom, listener);
        }
        if (pageFrom < 0 || pageFrom >= mPageCount || pageTo > mPageCount || !mPdfFile.exists()) {
            return null;
        }
        return new AsyncTask<Integer, Void, Bitmap[]>() {
            
            @Override
            protected Bitmap[] doInBackground(Integer... integers) {
                return getPages(integers[0], integers[1]);
            }
            
            @Override
            protected void onPostExecute(Bitmap[] result) {
                if (listener != null) {
                    listener.onRendered(result);
                }
            }
        }.execute(pageFrom, pageTo);
    }
    
    public Bitmap getPage(int pageNum, boolean renderIfNotExist) {
        final String cacheKey = getCacheKey(pageNum);
        Bitmap bitmap = mPageCache.getBitmapFromMemCache(cacheKey);
        if (bitmap != null || !renderIfNotExist) {
            return bitmap;
        }
        
        Bitmap[] bitmaps = getPages(pageNum, pageNum + 1);
        return (bitmaps == null ? null : bitmaps[0]);
    }
    
    public synchronized Bitmap[] getPages(int pageFrom, int pageTo) {
        if (pageFrom == pageTo) {
            return null;
        }
        if (pageFrom > pageTo) {
            return getPages(pageTo, pageFrom);
        }
        if (pageFrom < 0 || pageFrom >= mPageCount || pageTo > mPageCount) {
            return null;
        }
        
        Bitmap[] bitmaps = new Bitmap[pageTo - pageFrom];
        
        final float quality = 2.0F;
    
        ParcelFileDescriptor parcelFileDescriptor = null;
        PdfRenderer pdfRenderer = null;
        
        try {
            parcelFileDescriptor = getSeekableFileDescriptor();
            pdfRenderer = getPdfRenderer(parcelFileDescriptor);
            
            for (int i = 0; i < bitmaps.length; ++i) {
                final String cacheKey = getCacheKey(i + pageFrom);
                Bitmap bitmap = mPageCache.getBitmapFromMemCache(cacheKey);
                
                if (bitmap == null) {
                    Page page = pdfRenderer.openPage(i + pageFrom);
    
                    int width = Math.round(page.getWidth() * quality);
                    int height = Math.round(page.getHeight() * quality);
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
    
                    page.close();
                    mPageCache.addBitmapToMemoryCache(cacheKey, bitmap);
                }
                bitmaps[i] = bitmap;
            }
        } catch (IOException e) {
            ZLLogUtils.i("IOException: " + e.toString());
        } finally {
            ZLFileUtils.closeAutoCloseable(parcelFileDescriptor);
            ZLFileUtils.closeAutoCloseable(pdfRenderer);
        }
    
        return bitmaps;
    }
    
    private String getCacheKey(int pageNum) {
        return String.format("PdfPage%04d", pageNum);
    }
}
