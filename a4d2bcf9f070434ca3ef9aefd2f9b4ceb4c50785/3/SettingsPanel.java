package fi.helsinki.cs.tmc.intellij.ui.settings;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.intellij.actions.buttonactions.DownloadExerciseAction;
import fi.helsinki.cs.tmc.intellij.holders.TmcCoreHolder;
import fi.helsinki.cs.tmc.intellij.holders.TmcSettingsManager;
import fi.helsinki.cs.tmc.intellij.io.SettingsTmc;
import fi.helsinki.cs.tmc.intellij.services.ObjectFinder;
import fi.helsinki.cs.tmc.intellij.services.login.LoginManager;
import fi.helsinki.cs.tmc.intellij.services.persistence.PersistentTmcSettings;
import fi.helsinki.cs.tmc.intellij.spyware.ButtonInputListener;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import fi.helsinki.cs.tmc.intellij.ui.courseselection.CourseListWindow;
import fi.helsinki.cs.tmc.intellij.ui.login.LoginDialog;
import fi.helsinki.cs.tmc.intellij.ui.organizationselection.OrganizationListWindow;
import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Swing component displayed in settings window.
 */
public class SettingsPanel {

    private static final Logger logger = LoggerFactory.getLogger(SettingsPanel.class);
    private JPanel panel1;
    private JFormattedTextField projectPathField;
    private JCheckBox checkForNewOrCheckBox;
    private JCheckBox checkThatAllActiveCheckBox;
    private JCheckBox sendDiagnosticsCheckBox;
    private JCheckBox sendSnapshotsOfYourCheckBox;
    private JComboBox<String> selectErrorLanguageField;
    private JButton browseButton;
    private JButton okButton;
    private JButton cancelButton;
    private JButton downloadCourseExercisesButton;
    private JButton logoutButton;
    private JLabel loggedUser;
    private JLabel currentOrganization;
    private JButton changeOrganizationButton;
    private JLabel currentCourse;
    private JButton changeCourseButton;

    public JFormattedTextField getProjectPathField() {
        return projectPathField;
    }

    public JCheckBox getCheckForNewOrCheckBox() {
        return checkForNewOrCheckBox;
    }

    public JCheckBox getCheckThatAllActiveCheckBox() {
        return checkThatAllActiveCheckBox;
    }

    public JCheckBox getSendDiagnosticsCheckBox() {
        return sendDiagnosticsCheckBox;
    }

    public JCheckBox getSendSnapshotsOfYourCheckBox() {
        return sendSnapshotsOfYourCheckBox;
    }

    public JComboBox<String> getselectErrorLanguageField() {
        return selectErrorLanguageField;
    }

    public JPanel getPanel() {
        doClicks();
        return this.panel1;
    }

    public SettingsPanel(final JFrame frame) {
        logger.info("Building SettingsPanel");
        SettingsTmc settingsTmc = TmcSettingsManager.get();

        loggedUser.setText("Logged in as " + settingsTmc.getUsername().get());

        if (settingsTmc.getOrganization().isPresent()) {
            currentOrganization.setText(settingsTmc.getOrganization().get().getName());
        }

        if (settingsTmc.getCourse().getName() != null) {
            currentCourse.setText(settingsTmc.getCourseName());
        }

        ActionListener browseListener = createActionListener();
        browseButton.addActionListener(browseListener);

        changeOrganizationButton.addActionListener(createActionListenerChangeOrganization());

        changeCourseButton.addActionListener(createActionListenerChangeCourse());

        doClicks();

        projectPathField.setText(settingsTmc.getProjectBasePath());
        selectErrorLanguageField.addItem("English");

        ActionListener okListener = createActionListenerOk(frame);
        okButton.addActionListener(okListener);
        ActionListener cancelListener = createActionListenerCancel(frame);
        cancelButton.addActionListener(cancelListener);

        ActionListener downloadListener = createActionListenerDownload(frame);
        downloadCourseExercisesButton.addActionListener(downloadListener);

        ActionListener logoutListener = createActionListenerLogout(frame);
        logoutButton.addActionListener(logoutListener);
    }

    public void doClicks() {
        if (TmcSettingsManager.get().isSpyware() != sendSnapshotsOfYourCheckBox.isSelected()) {
            sendSnapshotsOfYourCheckBox.doClick();
        }
        if (TmcSettingsManager.get().isCheckForExercises() != checkForNewOrCheckBox.isSelected()) {
            checkForNewOrCheckBox.doClick();
        }
        if (TmcSettingsManager.get().getSendDiagnostics() != sendDiagnosticsCheckBox.isSelected()) {
            sendDiagnosticsCheckBox.doClick();
        }
    }

    private ActionListener createActionListenerLogout(final JFrame frame) {
        return actionEvent -> {
            logger.info("Logout button pressed. @SettingsPanel");

            LoginManager loginManager = new LoginManager();
            loginManager.logout();

            frame.dispose();
            frame.setVisible(false);

            LoginDialog.display();
        };
    }

    private ActionListener createActionListenerDownload(final JFrame frame) {
        return actionEvent -> {
            logger.info("Download button pressed. @SettingsPanel");

            saveInformation();

            Project project = new ObjectFinder().findCurrentProject();
            DownloadExerciseAction action = new DownloadExerciseAction();
            action.downloadExercises(project, false);

            frame.dispose();
            frame.setVisible(false);
        };
    }

    public void saveInformation() {
        logger.info("Saving settings information. @SettingsPanel");
        final PersistentTmcSettings saveSettings =
                ServiceManager.getService(PersistentTmcSettings.class);
        SettingsTmc settingsTmc = ServiceManager.getService(PersistentTmcSettings.class)
                .getSettingsTmc();

        settingsTmc.setCheckForExercises(checkForNewOrCheckBox.isSelected());
        settingsTmc.setProjectBasePath(projectPathField.getText());
        settingsTmc.setSpyware(sendSnapshotsOfYourCheckBox.isSelected());
        settingsTmc.setSendDiagnostics(sendDiagnosticsCheckBox.isSelected());
        saveSettings.setSettingsTmc(settingsTmc);
        if (sendDiagnosticsCheckBox.isSelected()) {
            try {
                TmcCoreHolder.get().sendDiagnostics(ProgressObserver.NULL_OBSERVER).call();
            } catch (Exception e) {
            }
        }
    }

    private ActionListener createActionListenerChangeOrganization() {
        return actionEvent -> {
            try {
                OrganizationListWindow.display();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private ActionListener createActionListenerChangeCourse() {
        return actionEvent -> {
            try {
                CourseListWindow.display();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private ActionListener createActionListenerOk(final JFrame frame) {
        logger.info("Create action listener for SettingsPanel ok button. @SettingsPanel");
        return actionEvent -> {
            new ButtonInputListener().receiveSettings();
            logger.info("Ok button pressed. @SettingsPanel");
            saveInformation();

            frame.dispose();
            frame.setVisible(false);
        };
    }

    private ActionListener createActionListenerCancel(final JFrame frame) {
        logger.info("Create action listener for SettingsPanel cancel button. @SettingsPanel");
        return actionEvent -> {
            logger.info("Cancel button pressed. @SettingsPanel");
            frame.dispose();
            frame.setVisible(false);
        };
    }

    @NotNull
    private ActionListener createActionListener() {
        logger.info("Creating action listener for browsing. @SettingsPanel");
        return actionEvent -> {
            logger.info("Browsing action performed. @SettingsPanel", actionEvent);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select path for projects");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(panel1);

            if (fileChooser.getSelectedFile() == null) {
                return;
            }
            projectPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        };
    }

    /**
     * .
     *
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return panel1;
    }
}
