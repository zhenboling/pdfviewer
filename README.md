# PDF Viewer Library for Android

Description: PDF Viewer to display PDF pages in Android, using androidx and supporting Android 9.

minSdkVersion 21

targetSdkVersion 28

compileSdkVersion 28 

com.android.tools.build:gradle:3.3.2

implementation 'androidx.appcompat:appcompat:1.0.2'


## How to use the library

Step 1:
Add the following into the build.gradle file of your application

        implementation 'zac.ling.libs:pdfviewer:1.9.0'

Step 2:
Add the following into your activity to display a PDF, assuming your FrameLayout ID is pdf_viewer_frame_layout

        File myPdfFile; // your PDF file
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        ZLPdfViewerFragment fragment = (ZLPdfViewerFragment) fragmentManager.findFragmentById(R.id.pdf_viewer_frame_layout);
        
        if (fragment == null) {
            fragment = ZLPdfViewerFragment.newInstance(Uri.fromFile(myPdfFile));
        }
        if (fragment != null && !fragment.isAdded()) {
            fragmentManager.beginTransaction().add(R.id.pdf_viewer_frame_layout, fragment).commit();
        }

Step 3:
Optionally have your PDF activity implement the following two interfaces

        // on PDF file loaded
        public interface ZLOnPdfLoadListener { void onLoaded(); }
        
        // currentPageNumber is from 0 to totalPageCount-1
        public interface ZLOnPdfPageChangeListener { void atPage(int currentPageNumber, int totalPageCount); }

Step 4:
If your activity declares configChanges like below

        android:configChanges="orientation|keyboardHidden|screenSize"

Then call ZLPdfViewerFragment.refreshView() in onConfigurationChanged like this:

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            if (mPdfViewerFragment != null && mPdfViewerFragment.isAdded()) {
                mPdfViewerFragment.refreshView();
            }
        }
        
### ==== Version 1.9 ====

Fix a fling issue


### ==== Version 1.7 ====

Draw background in gray


### ==== Version 1.5 ====

Fix a dragging issue


### ==== Version 1.0 ====

Implement PDF Viewer Library for Android
