package zac.ling.pdfviewer.lib.pdf;

import android.graphics.Bitmap;

public interface ZLOnPdfPageRenderListener {
    void onRendered(Bitmap... bitmaps);
}
