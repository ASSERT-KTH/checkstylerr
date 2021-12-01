package fi.helsinki.cs.tmc.intellij.services.exercises;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.intellij.io.SettingsTmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/** Gets the list of exercises stored on drive. */
public class CheckForExistingExercises {

    private static final Logger logger = LoggerFactory.getLogger(CheckForExistingExercises.class);

    public List<Exercise> clean(List<Exercise> exercises, SettingsTmc settings) {
        logger.info("Checking for existing exercises. @CheckForExistingExercises");
        exercises.removeAll(getListOfDownloadedExercises(exercises, settings));
        return exercises;
    }

    public List<Exercise> getListOfDownloadedExercises(
            List<Exercise> exercises, SettingsTmc settingsTmc) {
        logger.info("Parsing already existing exercises. @CheckForExistingExercises");
        return exercises.stream()
                .filter(e -> e.isDownloaded(settingsTmc.getTmcProjectDirectory()))
                .collect(Collectors.toList());
    }
}
