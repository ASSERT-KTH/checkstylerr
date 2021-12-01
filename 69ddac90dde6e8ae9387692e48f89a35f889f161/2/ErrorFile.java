package org.slc.sli.bulk.extract.files.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.slc.sli.domain.Entity;

public class ErrorFile {
    
    public static final String ERROR_FILE_NAME = "errors.txt";
    final Map<String, MutableLong> entityToErrorCounts = new HashMap<String, MutableLong>();
    final File errorFile;
    
    public ErrorFile(File parent) {
        errorFile = new File(parent, ERROR_FILE_NAME);
    }
    
    private static class MutableLong {
        long value = 0;
        
        public void increment() {
            value += 1;
        }
        
        public long getValue() {
            return value;
        }
    }
    
    public void logEntityError(Entity entity) {
        MutableLong count = entityToErrorCounts.get(entity.getType());
        if (count == null) {
            count = new MutableLong();
            entityToErrorCounts.put(entity.getType(), count);
        }
        count.increment();
    }
    
    protected void writeErrorFile(PrintWriter out) {
        out.println("Errors occured during generation of the extract.");
        out.println();
        for (String type : entityToErrorCounts.keySet()) {
            out.printf("%d errors occurred for entity type %s\n", entityToErrorCounts.get(type).getValue(), type);
        }
    }
    
    /**
     * Generate a file containing error messages.
     * If there were no errors to report, null is returned.
     */
    public File getFile() {
        if (entityToErrorCounts.size() == 0) {
            return null;
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(errorFile);
            this.writeErrorFile(out);
            return errorFile;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }
}