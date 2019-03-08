package zac.ling.pdfviewer;

import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import zac.ling.pdfviewer.lib.ZLPdfViewerFragment;
import zac.ling.pdfviewer.lib.utilities.ZLFileUtils;

public class PdfViewerActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_viewer_activity);
        
        if (savedInstanceState == null) {
            addPdfFragment();
        }
    }
    
    private void addPdfFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ZLPdfViewerFragment fragment = (ZLPdfViewerFragment) fragmentManager.findFragmentById(R.id.pdf_viewer_frame_layout);
        
        if (fragment == null) {
            File file = getFileFromResource(R.raw.test);
            if (file != null) {
                fragment = ZLPdfViewerFragment.newInstance(Uri.fromFile(file));
            }
        }
        if (fragment != null && !fragment.isAdded()) {
            fragmentManager.beginTransaction().add(R.id.pdf_viewer_frame_layout, fragment).commit();
        }
    }
    
    private File getFileFromResource(@RawRes int resource) {
        File file = null;
        
        InputStream inputStream = null;
        OutputStream outputStream = null;
        
        try {
            inputStream = getResources().openRawResource(resource);
            file = File.createTempFile("PDF", null);
            outputStream = new FileOutputStream(file);
        
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        
        } catch (IOException ignore) {
        
        } finally {
            ZLFileUtils.closeAutoCloseable(outputStream);
            ZLFileUtils.closeAutoCloseable(inputStream);
        }
        
        return file;
    }
}
