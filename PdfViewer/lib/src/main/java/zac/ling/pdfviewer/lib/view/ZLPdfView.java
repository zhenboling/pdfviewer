package zac.ling.pdfviewer.lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import zac.ling.pdfviewer.lib.R;
import zac.ling.pdfviewer.lib.gesture.ZLGestureManager;
import zac.ling.pdfviewer.lib.gesture.ZLOnScaleListener;
import zac.ling.pdfviewer.lib.gesture.ZLOnScrollListener;
import zac.ling.pdfviewer.lib.gesture.ZLOnTapListener;
import zac.ling.pdfviewer.lib.gesture.ZLScaleManager;
import zac.ling.pdfviewer.lib.gesture.ZLTapManager;
import zac.ling.pdfviewer.lib.pdf.ZLOnPdfLoadListener;
import zac.ling.pdfviewer.lib.pdf.ZLOnPdfPageChangeListener;
import zac.ling.pdfviewer.lib.pdf.ZLOnPdfPageRenderListener;
import zac.ling.pdfviewer.lib.pdf.ZLPdfFile;

public class ZLPdfView extends View implements ZLOnScrollListener, ZLOnScaleListener, ZLOnTapListener {
    
    private ZLPdfFile mPdfFile;
    
    private ZLOnPdfLoadListener mOnPdfLoadListener;
    private ZLOnPdfPageChangeListener mOnPdfPageChangeListener;
    
    private GestureDetector.OnDoubleTapListener mOnDoubleTapListener;
    
    private ZLGestureManager mOnGestureListener;
    
    private GestureDetectorCompat mGestureDetector;
    
    private ZLOnPdfPageRenderListener mOnPdfPageRenderListener;
    private AsyncTask mRenderTask;
    
    private ScaleGestureDetector mScaleDetector;
    
    private float mCanvasWidth;
    private float mCanvasHeight;
    
    private float mOffsetX; // length in pixel to the left of the original point
    private float mOffsetY; // height in pixel to the top of the original point
    
    private float mMinScale;
    private float mMaxScale;
    
    private float mOriginalBitmapWidth;
    private float mOriginalBitmapHeight;
    
    private float mCurrentScale;
    private float mCurrentBitmapWidth;
    private float mCurrentBitmapHeight;
    
    private Rect mSourceRect = new Rect();
    private Rect mDestinationRect = new Rect();
    
    private Paint mPagePaint;
    private Paint mSeparatorPaint;
    private Paint mBackgroundPaint;
    
    private boolean mFlinging = false;
    
    private ZLPdfViewMode mViewMode = ZLPdfViewMode.FIT_WIDTH;
    
    public ZLPdfView(Context context) {
        super(context);
        init();
    }
    
    public ZLPdfView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ZLPdfView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    public ZLPdfView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    
    private void init() {
        setupPagePaint();
        setupSeparatorPaint();
        setupBackgroundPaint();
    
        mOnDoubleTapListener = new ZLTapManager(this);
        mOnGestureListener = new ZLGestureManager(getContext(), this);
        mGestureDetector = new GestureDetectorCompat(getContext(), mOnGestureListener);
        mGestureDetector.setOnDoubleTapListener(mOnDoubleTapListener);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ZLScaleManager(this));
        
        mOnPdfPageRenderListener = new ZLOnPdfPageRenderListener() {
            @Override
            public void onRendered(Bitmap... bitmaps) {
                invalidate();
            }
        };
    }
    
    private void setupPagePaint() {
        mPagePaint = new Paint();
    }
    
    private void setupSeparatorPaint() {
        mSeparatorPaint = new Paint();
        mSeparatorPaint.setStyle(Style.FILL_AND_STROKE);
        mSeparatorPaint.setStrokeWidth(getResources().getDimension(R.dimen.zl_pdf_page_separator_height));
        mSeparatorPaint.setColor(getBackgroundColor());
    }
    
    private void setupBackgroundPaint() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getBackgroundColor());
    }
    
    @ColorInt
    public int getBackgroundColor() {
        return ContextCompat.getColor(getContext(), R.color.zl_pdf_page_background);
    }
    
    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        float previousScale = mCurrentScale;
        float newScale = scaleFactor * mCurrentScale;
        if (newScale >= mMaxScale) {
            setCurrentScale(mMaxScale);
        } else if (newScale <= mMinScale) {
            setCurrentScale(mMinScale);
        } else {
            setCurrentScale(newScale);
        }
        
        if (mCurrentBitmapWidth <= mCanvasWidth || mCurrentBitmapHeight * mPdfFile.getPageCount() <= mCanvasHeight) {
            focusX = mCanvasWidth / 2.0F;
            focusY = mCanvasHeight / 2.0F;
        }
        float newOffsetX = (mOffsetX + focusX) * mCurrentScale / previousScale - focusX;
        float newOffsetY = (mOffsetY + focusY) * mCurrentScale / previousScale - focusY;
        setOffset(newOffsetX, newOffsetY);
    }
    
    @Override
    public boolean performClick() {
        return super.performClick();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        mScaleDetector.onTouchEvent(event);
        
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        onMeasured();
    }
    
    private void onMeasured() {
        mCanvasWidth = getMeasuredWidth();
        mCanvasHeight = getMeasuredHeight();
    }
    
    @Override
    public void computeScroll() {
        super.computeScroll();
        mOnGestureListener.computeScroll();
    }
    
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (mOriginalBitmapWidth <= 0 || mOriginalBitmapHeight <= 0) {
            return;
        }
        
        final int pageFrom = getPageFromInclusive();
        final int pageTo = getPageToExclusive();
        
        float x = -mOffsetX;
        float y = -(mOffsetY - pageFrom * mCurrentBitmapHeight);
        
        for (int i = pageFrom; i < pageTo; ++i) {
            // draw PDF page
            Bitmap bitmap = mPdfFile.getPage(i, !mFlinging);
            if (bitmap != null) {
                Rect dst = setAndGetDestinationRect(x, y, mCurrentBitmapWidth + x, mCurrentBitmapHeight + y);
                canvas.drawBitmap(bitmap, setAndGetSourceRect(), dst, mPagePaint);
            }
    
            // draw left and right side of background
            if (x > 0) {
                canvas.drawRect(0, Math.max(0, y), x, mCurrentBitmapHeight + y, mBackgroundPaint);
                canvas.drawRect(mCanvasWidth - x, Math.max(0, y), mCanvasWidth, mCurrentBitmapHeight + y, mBackgroundPaint);
            }
    
            // draw separator line
            if (i != 0) {
                canvas.drawLine(0, y, mCanvasWidth, y, mSeparatorPaint);
            }
            
            y += mCurrentBitmapHeight;
        }
        
        // draw bottom of background
        if (y < mCanvasHeight) {
            canvas.drawRect(0, y, mCanvasWidth, mCanvasHeight, mBackgroundPaint);
        }
    }
    
    private int getPageFromInclusive(float offsetY) {
        return (int) Math.floor(offsetY / mCurrentBitmapHeight);
    }
    
    private int getPageFromInclusive() {
        return getPageFromInclusive(mOffsetY);
    }
    
    private int getPageToExclusive(float offsetY) {
        return Math.min(mPdfFile.getPageCount(), (int) Math.ceil((offsetY + mCanvasHeight) / mCurrentBitmapHeight));
    }
    
    private int getPageToExclusive() {
        return getPageToExclusive(mOffsetY);
    }
    
    private int getCurrentPageNumber() {
        return (int) Math.floor((mOffsetY + mCanvasHeight / 2.0F) / mCurrentBitmapHeight);
    }
    
    private void notifyPageNumber() {
        if (mOnPdfPageChangeListener != null) {
            mOnPdfPageChangeListener.atPage(getCurrentPageNumber(), mPdfFile.getPageCount());
        }
    }
    
    private void notifyPdfLoaded() {
        if (mOnPdfLoadListener != null) {
            mOnPdfLoadListener.onLoaded();
        }
    }
    
    @Override
    public void onDoubleTap(float x, float y) {
        float newScale = (mCurrentScale == mMaxScale ? mMinScale : mMaxScale);
        onScale(newScale / mCurrentScale, x, y);
    }
    
    @Override
    public void onDrag(float deltaX, float deltaY) {
        adjustOffset(deltaX, deltaY);
        notifyPageNumber();
    }
    
    @Override
    public int getFlingStartX() {
        return (int) mOffsetX;
    }
    
    @Override
    public int getFlingStartY() {
        return (int) mOffsetY;
    }
    
    @Override
    public int getFlingMinX() {
        if (mCurrentBitmapWidth <= mCanvasWidth) {
            return getFlingStartX();
        }
        return 0;
    }
    
    @Override
    public int getFlingMaxX() {
        if (mCurrentBitmapWidth <= mCanvasWidth) {
            return getFlingStartX();
        }
        return (int) (mCurrentBitmapWidth - mCanvasWidth);
    }
    
    @Override
    public int getFlingMinY() {
        if (mCurrentBitmapHeight * mPdfFile.getPageCount() <= mCanvasHeight) {
            return getFlingStartY();
        }
        return 0;
    }
    
    @Override
    public int getFlingMaxY() {
        if (mCurrentBitmapHeight * mPdfFile.getPageCount() <= mCanvasHeight) {
            return getFlingStartY();
        }
        return (int) (mCurrentBitmapHeight * mPdfFile.getPageCount() - mCanvasHeight);
    }
    
    @Override
    public void onFling(float currentX, float currentY, float finalX, float finalY) {
        mFlinging = (currentY != finalY);
        int pageFrom = getPageFromInclusive(finalY);
        int pageTo = getPageToExclusive(finalY);
        mPdfFile.getPages(pageFrom, pageTo, null);
        setOffset(currentX, currentY);
        if (mFlinging) {
            notifyPageNumber();
        }
    }
    
    @Override
    public void onFlingPaused() {
        mFlinging = false;
        redraw(true);
    }
    
    @Override
    public View getView() {
        return this;
    }
    
    public void setPdfFile(@NonNull ZLPdfFile pdfFile) {
        mPdfFile = pdfFile;
        setupOriginalBitmap();
    }
    
    public void setOnPdfPageChangeListener(ZLOnPdfPageChangeListener onPdfPageChangeListener) {
        mOnPdfPageChangeListener = onPdfPageChangeListener;
    }
    
    public void setOnPdfLoadListener(ZLOnPdfLoadListener onPdfLoadListener) {
        mOnPdfLoadListener = onPdfLoadListener;
    }
    
    public void jumpToPage(int pageNumber) {
        if (pageNumber < 0 || pageNumber >= mPdfFile.getPageCount()) {
            return;
        }
        setOffsetY(pageNumber * mCurrentBitmapHeight);
        redraw(true);
    }
    
    public void refresh() {
        setupOriginalBitmap();
    }
    
    private void resetScaleRange() {
        mMinScale = 1.0F;
        mMaxScale = 4.0F;
    }
    
    private void setupOriginalBitmap() {
        mPdfFile.getPages(0, 1, new ZLOnPdfPageRenderListener() {
            @Override
            public void onRendered(Bitmap... bitmap) {
                mOriginalBitmapWidth = bitmap[0].getWidth();
                mOriginalBitmapHeight = bitmap[0].getHeight();
                setupInitialBitmap();
            }
        });
    }
    
    private float getOriginalScale() {
        return 1.0F;
    }
    
    private float getFitWidthScale() {
        return mCanvasWidth / mOriginalBitmapWidth;
    }
    
    private float getFitWidthAndHeightScale() {
        return Math.min(mCanvasWidth / mOriginalBitmapWidth, mCanvasHeight / mOriginalBitmapHeight);
    }
    
    private void setupInitialBitmap() {
        resetScaleRange();
        switch (mViewMode) {
            case ORIGINAL:
                setCurrentScale(getOriginalScale());
                break;
            case FIT_WIDTH:
                setCurrentScale(getFitWidthScale());
                break;
            case FIT_WIDTH_AND_HEIGHT:
                setCurrentScale(getFitWidthAndHeightScale());
                break;
        }
    
        float originalScale = getOriginalScale();
        float fitWidthScale = getFitWidthScale();
        float fitWidthAndHeightScale = getFitWidthAndHeightScale();
        float minScale = Math.min(originalScale, Math.min(fitWidthScale, fitWidthAndHeightScale));
        float maxScale = Math.max(originalScale, Math.max(fitWidthScale, fitWidthAndHeightScale));
        mMinScale = Math.min(mMinScale, minScale);
        mMaxScale = Math.max(mMaxScale, maxScale);
        
        setOffsetX(0);
        setOffsetY(0);
        
        notifyPdfLoaded();
        notifyPageNumber();
        redraw(true);
    }
    
    private void setCurrentScale(float scale) {
        mCurrentScale = scale;
        mCurrentBitmapWidth = mCurrentScale * mOriginalBitmapWidth;
        mCurrentBitmapHeight = mCurrentScale * mOriginalBitmapHeight;
    }
    
    private Rect setAndGetSourceRect() {
        mSourceRect.set(0, 0, (int) mOriginalBitmapWidth, (int) mOriginalBitmapHeight);
        return mSourceRect;
    }
    
    private Rect setAndGetDestinationRect(float left, float top, float right, float bottom) {
        mDestinationRect.set((int) left, (int) top, (int) right, (int) bottom);
        return mDestinationRect;
    }
    
    private void setOffsetX(float offsetX) {
        if (mCurrentBitmapWidth <= mCanvasWidth) {
            mOffsetX = (mCurrentBitmapWidth - mCanvasWidth) / 2.0F;
        } else {
            float min = 0;
            float max = mCurrentBitmapWidth - mCanvasWidth;
            mOffsetX = Math.max(min, Math.min(max, offsetX));
        }
    }
    
    private void setOffsetY(float offsetY) {
        if (mCurrentBitmapHeight * mPdfFile.getPageCount() <= mCanvasHeight) {
            mOffsetY = 0;
        } else {
            float min = 0;
            float max = mCurrentBitmapHeight * mPdfFile.getPageCount() - mCanvasHeight;
            mOffsetY = Math.max(min, Math.min(max, offsetY));
        }
    }
    
    private void setOffset(float offsetX, float offsetY) {
        setOffsetX(offsetX);
        setOffsetY(offsetY);
        redraw(false);
    }
    
    private void adjustOffset(float deltaX, float deltaY) {
        adjustOffsetX(deltaX);
        adjustOffsetY(deltaY);
        redraw(true);
    }
    
    private void adjustOffsetX(float deltaX) {
        setOffsetX(mOffsetX + deltaX);
    }
    
    private void adjustOffsetY(float deltaY) {
        setOffsetY(mOffsetY + deltaY);
    }
    
    private void redraw(boolean forceDraw) {
        if (forceDraw || mFlinging) {
            invalidate();
        } else {
            if (mRenderTask != null) {
                mRenderTask.cancel(true);
            }
            mRenderTask = mPdfFile.getPages(getPageFromInclusive(), getPageToExclusive(), mOnPdfPageRenderListener);
        }
    }
}
