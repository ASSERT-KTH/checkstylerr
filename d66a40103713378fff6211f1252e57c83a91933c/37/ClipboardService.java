package fi.helsinki.cs.tmc.intellij.services;

import com.intellij.openapi.application.ex.ClipboardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/** Offers method for copying text to clip board. */
public class ClipboardService {

    private static final Logger logger = LoggerFactory.getLogger(ClipboardService.class);

    public static void copyToClipBoard(String stringToCopy) {
        logger.info("Copying {} to the clip board. @ClipboardService", stringToCopy);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(stringToCopy);
        clipboard.setContents(selection, null);
    }

    public static String getClipBoard() {
        return ClipboardUtil.getTextInClipboard();
    }
}
