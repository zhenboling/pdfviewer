# PDF Viewer Library for Android

Description: PDF Viewer to display PDF pages in Android, using androidx and supporting Android 9.

minSdkVersion 21

targetSdkVersion 28

compileSdkVersion 28 

com.android.tools.build:gradle:3.2.1

implementation 'androidx.appcompat:appcompat:1.0.2'


## How to use the library

Step 1:
Add the following into your application build.gradle

implementation 'zac.ling.libs:pdfviewer:1.7.0'

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

### ==== Version 1.7 ====

Draw background in gray.


### ==== Version 1.5 ====

Fix a dragging issue.


### ==== Version 1.0 ====

Implement PDF Viewer Library for Android
