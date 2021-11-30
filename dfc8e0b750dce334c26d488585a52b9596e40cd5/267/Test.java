
package cloudManager;

import es.bsc.compss.types.CloudProvider;
import es.bsc.compss.types.CoreElement;
import es.bsc.compss.types.implementations.Implementation;
import es.bsc.compss.types.implementations.MethodImplementation;
import es.bsc.compss.types.implementations.TaskType;
import es.bsc.compss.types.resources.MethodResourceDescription;
import es.bsc.compss.types.resources.components.Processor;
import es.bsc.compss.types.resources.description.CloudImageDescription;
import es.bsc.compss.types.resources.description.CloudInstanceTypeDescription;
import es.bsc.compss.types.resources.description.CloudMethodResourceDescription;
import es.bsc.compss.util.CoreManager;
import es.bsc.compss.util.ResourceManager;


/*
 * Checks the Cloud constraint management
 * Takes the project.xml / resources information and the ITF information
 * Checks the mapping between ITF CoreElement and runnable resources (static constraint check)
 */
public class Test {

    private static final int WAIT_FOR_RUNTIME_TIME = 10_000; // ms


    /*
     * *************************************** MAIN IMPLEMENTATION ***************************************
     */
    public static void main(String[] args) {
        // Wait for Runtime to be loaded
        System.out.println("[LOG] Waiting for Runtime to be loaded");
        try {
            Thread.sleep(WAIT_FOR_RUNTIME_TIME);
        } catch (Exception e) {
            // No need to handle such exceptions
        }

        // Run Cloud Manager Test
        System.out.println("[LOG] Running Cloud Manager Test");
        cloudManagerTest();
    }

    /*
     * *********************************** CLOUD MANAGER TEST IMPLEMENTATION ***********************************
     */
    private static void cloudManagerTest() {
        // Print Out CloudManager static structures
        System.out.println("[LOG] CloudManager Static Structures definition");

        // Check for each implementation the correctness of its resources
        CloudProvider cp = ResourceManager.getCloudProvider("BSC");
        for (CoreElement ce : CoreManager.getAllCores()) {
            int coreId = ce.getCoreId();
            System.out.println("[LOG] Checking Core" + coreId);
            for (Implementation impl : ce.getImplementations()) {
                if (impl.getTaskType().equals(TaskType.METHOD)) {
                    System.out.println("[LOG]\t Checking Implementation: " + impl.getImplementationId());
                    System.out.println("\t\t Checking obtained compatible cloud images");

                    MethodImplementation mImpl = (MethodImplementation) impl;

                    for (CloudImageDescription cid_gci : cp.getCompatibleImages(mImpl.getRequirements())) {
                        System.out.println("\t\t\t Checking compatible Image: " + cid_gci.getImageName());
                        String res = checkImplementationAssignedToCloudImage(mImpl.getRequirements(), cid_gci);
                        if (res != null) {
                            String error = "[ERROR] Implementation: Core = " + coreId + " Impl = "
                                    + impl.getImplementationId() + ". ";
                            error = error.concat("Implementation and cloud image not matching on: " + res);
                            System.out.println(error);
                            System.exit(-1);
                        }
                    }
                    System.out.println("\t\t Checking obtained compatible cloud types");
                    for (CloudInstanceTypeDescription type : cp
                            .getCompatibleTypes(new CloudMethodResourceDescription(mImpl.getRequirements()))) {
                        if (type.getResourceDescription().canHostSimultaneously(mImpl.getRequirements()) < 1) {
                            continue;
                        }
                        System.out.println("\t\t\t Checking compatible Type: " + type.getName());
                        String res = checkImplementationAssignedToType(mImpl.getRequirements(),
                                type.getResourceDescription());
                        if (res != null) {
                            String error = "[ERROR] Implementation: Core = " + coreId + " Impl = "
                                    + impl.getImplementationId() + ". ";
                            error = error.concat("Implementation and type not matching on: " + res);
                            System.out.println(error);
                            System.exit(-1);
                        }
                    }
                }
            }
        }

        // Return success value
        System.out.println("[LOG] * CloudManager test passed");
    }

    private static String checkImplementationAssignedToCloudImage(MethodResourceDescription rd,
            CloudImageDescription cid) {
        // Check Operating System
        if ((!cid.getOperatingSystemType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rd.getOperatingSystemType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!cid.getOperatingSystemType().equals(rd.getOperatingSystemType()))) {
            return "operatingSystemType";
        }
        if ((!cid.getOperatingSystemDistribution().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rd.getOperatingSystemDistribution().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!cid.getOperatingSystemDistribution().equals(rd.getOperatingSystemDistribution()))) {
            return "operatingSystemDistribution";
        }
        if ((!cid.getOperatingSystemVersion().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rd.getOperatingSystemVersion().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!cid.getOperatingSystemVersion().equals(rd.getOperatingSystemVersion()))) {
            return "operatingSystemVersion";
        }

        // Check Software Apps
        if ((!cid.getAppSoftware().isEmpty()) && (!rd.getAppSoftware().isEmpty())
                && (!cid.getAppSoftware().containsAll(rd.getAppSoftware()))) {
            return "softwareApps";
        }

        return null;
    }

    private static String checkImplementationAssignedToType(MethodResourceDescription rdImpl,
            MethodResourceDescription rdType) {
        /*
         * *********************************************** COMPUTING UNITS
         ***********************************************/
        if ((rdImpl.getTotalCPUComputingUnits() >= MethodResourceDescription.ONE_INT)
                && (rdType.getTotalCPUComputingUnits() >= MethodResourceDescription.ONE_INT)
                && (rdType.getTotalCPUComputingUnits() < rdImpl.getTotalCPUComputingUnits())) {
            return "computingUnits";
        }

        /*
         * *********************************************** PROCESSOR
         ***********************************************/
        for (Processor ip : rdImpl.getProcessors()) {
            // Check if processor can be executed in worker
            boolean canBeHosted = false;
            for (Processor wp : rdType.getProcessors()) {
                // Static checks
                if (!ip.getName().equals(MethodResourceDescription.UNASSIGNED_STR)
                        && !wp.getName().equals(MethodResourceDescription.UNASSIGNED_STR)
                        && !wp.getName().equals(ip.getName())) {
                    // System.out.println("DUE TO: " + ip.getName() + " != " + wp.getName());
                    continue;
                }
                if (ip.getSpeed() != MethodResourceDescription.UNASSIGNED_FLOAT
                        && wp.getSpeed() != MethodResourceDescription.UNASSIGNED_FLOAT
                        && wp.getSpeed() < ip.getSpeed()) {
                    // System.out.println("DUE TO: " + ip.getSpeed() + " != " + wp.getSpeed());
                    continue;
                }
                if (!ip.getArchitecture().equals(MethodResourceDescription.UNASSIGNED_STR)
                        && !wp.getArchitecture().equals(MethodResourceDescription.UNASSIGNED_STR)
                        && !wp.getArchitecture().equals(ip.getArchitecture())) {
                    // System.out.println("DUE TO: " + ip.getArchitecture() + " != " + wp.getArchitecture());
                    continue;
                }
                if ((!ip.getPropName().equals(MethodResourceDescription.UNASSIGNED_STR))
                        && (!wp.getPropName().equals(MethodResourceDescription.UNASSIGNED_STR))
                        && (!ip.getPropName().equals(wp.getPropName()))) {
                    // System.out.println("DUE TO: " + ip.getPropName() + " != " + wp.getPropName());
                    continue;
                }
                if ((!ip.getPropValue().equals(MethodResourceDescription.UNASSIGNED_STR))
                        && (!wp.getPropValue().equals(MethodResourceDescription.UNASSIGNED_STR))
                        && (!ip.getPropValue().equals(wp.getPropValue()))) {
                    // System.out.println("DUE TO: " + ip.getPropValue() + " != " + wp.getPropValue());
                    continue;
                }

                // Dynamic checks
                if (wp.getComputingUnits() >= ip.getComputingUnits()) {
                    canBeHosted = true;
                    break;
                } else {
                    // System.out.println("DUE TO: " + ip.getComputingUnits() + " != " + wp.getComputingUnits());
                }
            }
            if (!canBeHosted) {
                return "processor";
            }
        }

        /*
         * *********************************************** MEMORY
         ***********************************************/
        if ((rdImpl.getMemorySize() != MethodResourceDescription.UNASSIGNED_FLOAT)
                && (rdType.getMemorySize() != MethodResourceDescription.UNASSIGNED_FLOAT)
                && (rdType.getMemorySize() < rdImpl.getMemorySize())) {
            return "memorySize";
        }

        if ((!rdImpl.getMemoryType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdImpl.getMemoryType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getMemoryType().equals(rdImpl.getMemoryType()))) {
            return "memoryType";
        }

        /*
         * *********************************************** STORAGE
         ***********************************************/
        if ((rdImpl.getStorageSize() != MethodResourceDescription.UNASSIGNED_FLOAT)
                && (rdType.getStorageSize() != MethodResourceDescription.UNASSIGNED_FLOAT)
                && (rdType.getStorageSize() < rdImpl.getStorageSize())) {
            return "storageSize";
        }

        if ((!rdImpl.getStorageType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdImpl.getStorageType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getStorageType().equals(rdImpl.getStorageType()))) {
            return "storageType";
        }

        /*
         * *********************************************** OPERATING SYSTEM
         ***********************************************/
        if ((!rdImpl.getOperatingSystemType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getOperatingSystemType().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getOperatingSystemType().equals(rdImpl.getOperatingSystemType()))) {
            return "operatingSystemType";
        }

        if ((!rdImpl.getOperatingSystemDistribution().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getOperatingSystemDistribution().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getOperatingSystemDistribution().equals(rdImpl.getOperatingSystemDistribution()))) {
            return "operatingSystemDistribution";
        }

        if ((!rdImpl.getOperatingSystemVersion().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getOperatingSystemVersion().equals(MethodResourceDescription.UNASSIGNED_STR))
                && (!rdType.getOperatingSystemVersion().equals(rdImpl.getOperatingSystemVersion()))) {
            return "operatingSystemVersion";
        }

        /*
         * *********************************************** APPLICATION SOFTWARE
         ***********************************************/
        if (!(rdImpl.getAppSoftware().isEmpty()) && !(rdType.getAppSoftware().containsAll(rdImpl.getAppSoftware()))) {
            return "appSoftware";
        }

        /*
         * *********************************************** HOST QUEUE
         ***********************************************/
        if (!(rdImpl.getHostQueues().isEmpty()) && !(rdType.getHostQueues().containsAll(rdImpl.getHostQueues()))) {
            return "hostQueues";
        }

        /*
         * *********************************************** ALL CONTRAINT VALUES OK
         ***********************************************/
        return null;
    }

}
