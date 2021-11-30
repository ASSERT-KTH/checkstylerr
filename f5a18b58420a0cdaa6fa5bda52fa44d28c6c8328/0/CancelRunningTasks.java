package cancelRunningTasks;

import java.io.File;
import java.io.IOException;

import es.bsc.compss.api.COMPSs;
import es.bsc.compss.api.COMPSsGroup;
import es.bsc.compss.worker.COMPSsException;


public class CancelRunningTasks {

    public static final int N = 3;
    public static final int M = 4;

    public static final String FILE_NAME = "/tmp/sharedDisk/taskGroups.txt";


    public static void main(String[] args) throws Exception {
        newFile(FILE_NAME, true);

        System.out.println("[LOG] Test task group exceptions cancellation");
        testCancelation();

        System.out.println("[LOG] Test task group exceptions cancellation without barrier");
        testCancelationNoImplicitBarrier();
    }

    private static void testCancelation() throws InterruptedException {
        try (COMPSsGroup a = new COMPSsGroup("FailedGroup", true)) {
            System.out.println("Executing task group that throws COMPSsException ");
            for (int j = 0; j < M; j++) {
                // The exception is thrown by the second task of the group
                CancelRunningTasksImpl.throwException(FILE_NAME, j);
            }
            // COMPSs.getFile(FILE_NAME);
            for (int j = 0; j < M; j++) {
                CancelRunningTasksImpl.throwException(FILE_NAME, j + 4);
            }
        } catch (COMPSsException e) {
            // CancelRunningTasksImpl.writeTwo(FILE_NAME);
            System.out.println("Exception caught!!");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        for (int j = 0; j < N; j++) {
            CancelRunningTasksImpl.writeTwo(FILE_NAME);
        }
    }

    private static void testCancelationNoImplicitBarrier() throws InterruptedException {
        try (COMPSsGroup a = new COMPSsGroup("FailedGroup2", false)) {
            System.out.println("Executing task group that throws COMPSsException ");
            for (int j = 0; j < M; j++) {
                // The exception is thrown by the second task of the group
                CancelRunningTasksImpl.throwException(FILE_NAME, j);
            }
            // COMPSs.getFile(FILE_NAME);
            for (int j = 0; j < M; j++) {
                CancelRunningTasksImpl.throwException(FILE_NAME, j + 4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            COMPSs.barrierGroup("FailedGroup2");
        } catch (COMPSsException e) {
            // CancelRunningTasksImpl.writeTwo(FILE_NAME);
            System.out.println("Exception caught!!");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        for (int j = 0; j < N; j++) {
            CancelRunningTasksImpl.writeTwo(FILE_NAME);
        }
    }

    // Creation of a new blank file
    private static void newFile(String fileName, boolean create) throws IOException {
        File file = new File(fileName);
        // Delete previous occurrences of the file
        if (file.exists()) {
            file.delete();
        }
        if (create) {
            // Create the file and directories if required
            boolean createdFile = file.createNewFile();
            if (!createdFile) {
                throw new IOException("[ERROR] Cannot create test file");
            }
        }

    }
}
