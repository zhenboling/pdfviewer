package zac.ling.pdfviewer.lib.gesture;

import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;

public class ZLTapManager implements OnDoubleTapListener {
    
    private ZLOnTapListener mOnTapListener;
    
    public ZLTapManager(ZLOnTapListener onTapListener) {
        mOnTapListener = onTapListener;
    }
    
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return true;
    }
    
    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        mOnTapListener.onDoubleTap(motionEvent.getX(), motionEvent.getY());
        return true;
    }
    
    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return true;
    }
}
