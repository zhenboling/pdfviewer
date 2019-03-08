package zac.ling.pdfviewer.lib;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import zac.ling.pdfviewer.lib.pdf.ZLOnPdfLoadListener;
import zac.ling.pdfviewer.lib.pdf.ZLOnPdfPageChangeListener;
import zac.ling.pdfviewer.lib.pdf.ZLPdfFile;
import zac.ling.pdfviewer.lib.view.ZLPdfView;

public class ZLPdfViewerFragment extends Fragment implements ZLOnPdfLoadListener, ZLOnPdfPageChangeListener {
    
    private static final String ARG_PDF_URI = "ArgPdfUri";
    private static final String ARG_PDF_PAGE_NUMBER = "ArgPdfPageNumber";
    
    private TextView mPageNumberView;
    private ZLPdfView mPdfView;
    
    private ZLPdfFile mPdfFile;
    
    private AlphaAnimation mPageNumberAnimation;
    
    private int mCurrentPageNumber;
    
    public static ZLPdfViewerFragment newInstance(@NonNull Uri pdfUri) {
        ZLPdfViewerFragment fragment = new ZLPdfViewerFragment();
        
        Bundle argument = new Bundle();
        argument.putParcelable(ARG_PDF_URI, pdfUri);
        fragment.setArguments(argument);
        
        return fragment;
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            Uri pdfUri = getArguments().getParcelable(ARG_PDF_URI);
            
            if (pdfUri != null && pdfUri.getPath() != null) {
                File pdfFile = new File(pdfUri.getPath());
    
                if (pdfFile.exists()) {
                    mPdfFile = new ZLPdfFile(pdfFile);
                }
            }
        }
        
        if (savedInstanceState != null) {
            mCurrentPageNumber = savedInstanceState.getInt(ARG_PDF_PAGE_NUMBER, 0);
        }
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_viewer_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mPageNumberView = view.findViewById(R.id.pdf_page_number);
        
        mPdfView = view.findViewById(R.id.pdf_view);
        if (mPdfFile != null) {
            mPdfView.setPdfFile(mPdfFile);
            mPdfView.setOnPdfPageChangeListener(this);
            mPdfView.setOnPdfLoadListener(this);
        }
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_PDF_PAGE_NUMBER, mCurrentPageNumber);
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
    }
    
    @Override
    public void atPage(int currentPageNumber, int totalPageNumber) {
        if (mPageNumberAnimation != null) {
            mPageNumberAnimation.cancel();
            mPageNumberAnimation = null;
        }
        final String pageNumberText = (currentPageNumber + 1) + " / " + totalPageNumber;
        mPageNumberView.setText(pageNumberText);
        mPageNumberView.setVisibility(View.VISIBLE);
        
        mPageNumberAnimation = new AlphaAnimation(1.0F, 0.0F);
        mPageNumberAnimation.setDuration(TimeUnit.SECONDS.toMillis(3L));
        mPageNumberAnimation.setFillAfter(true);
        mPageNumberView.startAnimation(mPageNumberAnimation);
        
        mCurrentPageNumber = currentPageNumber;
    }
    
    @Override
    public void onLoaded() {
        if (mCurrentPageNumber != 0) {
            mPdfView.jumpToPage(mCurrentPageNumber);
        }
    }
}
