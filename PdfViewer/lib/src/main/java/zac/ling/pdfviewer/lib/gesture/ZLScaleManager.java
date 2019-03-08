package zac.ling.pdfviewer.lib.gesture;

import android.view.ScaleGestureDetector;

public class ZLScaleManager extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private ZLOnScaleListener mOnScaleListener;
    
    public ZLScaleManager(ZLOnScaleListener onScaleListener) {
        mOnScaleListener = onScaleListener;
    }
    
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }
    
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();
        mOnScaleListener.onScale(scaleFactor, focusX, focusY);
        return true;
    }
}
