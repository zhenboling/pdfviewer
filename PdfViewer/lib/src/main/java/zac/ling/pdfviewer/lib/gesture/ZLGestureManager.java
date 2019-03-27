package zac.ling.pdfviewer.lib.gesture;

import android.content.Context;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.OverScroller;

public class ZLGestureManager implements OnGestureListener {
    
    private ZLOnScrollListener mOnScrollListener;
    
    private OverScroller mOverScroller;
    
    public ZLGestureManager(Context context, ZLOnScrollListener onScrollListener) {
        mOverScroller = new OverScroller(context);
        mOnScrollListener = onScrollListener;
    }
    
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        if (!mOverScroller.isFinished()) {
            mOverScroller.forceFinished(true);
            mOnScrollListener.onFlingPaused();
        }
        return true;
    }
    
    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }
    
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
        mOnScrollListener.onDrag(distanceX, distanceY);
        return true;
    }
    
    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }
    
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
        mOverScroller.forceFinished(true);
        
        int startX = mOnScrollListener.getFlingStartX();
        int startY = mOnScrollListener.getFlingStartY();
        int minX = mOnScrollListener.getFlingMinX();
        int maxX = mOnScrollListener.getFlingMaxX();
        int minY = mOnScrollListener.getFlingMinY();
        int maxY = mOnScrollListener.getFlingMaxY();

        mOverScroller.fling(startX, startY, (int) -velocityX, (int) -velocityY, minX, maxX, minY, maxY);
        computeScroll();
        
        return true;
    }
    
    public void computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            mOnScrollListener.onFling(mOverScroller.getCurrX(), mOverScroller.getCurrY(), mOverScroller.getFinalX(), mOverScroller.getFinalY());
        }
    }
}
