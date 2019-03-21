package zac.ling.pdfviewer.lib.gesture;

public interface ZLOnScrollListener {
    
    void onDrag(float deltaX, float deltaY);
    
    int getFlingStartX();
    
    int getFlingStartY();
    
    int getFlingMinX();
    
    int getFlingMaxX();
    
    int getFlingMinY();
    
    int getFlingMaxY();
    
    void onFling(float currentX, float currentY, float finalX, float finalY);
    
    void onFlingPaused();
    
}
