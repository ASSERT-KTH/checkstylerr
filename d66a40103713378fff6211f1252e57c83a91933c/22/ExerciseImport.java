package fi.helsinki.cs.tmc.intellij.importexercise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/*
 * Class handles as main tool for imports
 */
public class ExerciseImport {
    private static final Logger logger = LoggerFactory.getLogger(NewProjectUtilModified.class);
    /*
     * Handles Exercise import also possibly decides what to import in future.
     * @param path project root dir
     * @throws IOException when writing or reading files.
     */

    public static boolean importExercise(String path) {
        if ((isUnImportedNbProject(path))) {
            try {
                NewProjectUtilModified.importExercise(path);
                return true;
            } catch (Exception e) {
                logger.warn("{} @ExerciseImport.importExercise", e);
            }
        }
        return false;
    }

    private static boolean isUnImportedNbProject(String path) {
        logger.info("Check if dir has idea file @ExerciseImport");
        File file = new File(path);
        return file.isDirectory() && isChild(file, "nbproject") && !isChild(file, ".idea");
    }

    private static boolean isChild(File file, String name) {
        for (String child : file.list()) {
            if (child.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
