package zac.ling.pdfviewer.lib.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public final class ZLFileUtils {
    
    private ZLFileUtils() {}
    
    public static void copy(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            closeAutoCloseable(outputChannel);
            closeAutoCloseable(inputChannel);
        }
    }
    
    public static void closeAutoCloseable(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception ignore) {
            
            }
        }
    }
}
