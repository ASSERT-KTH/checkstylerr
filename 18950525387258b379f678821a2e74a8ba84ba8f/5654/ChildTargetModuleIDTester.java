/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * DeployTargetModuleIDTester.java
 *
 * Created on January 27, 2004, 10:07 AM
 */

package jsr88.deploymentmanager.getXModules.childtmid;

import java.io.File;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.*;

import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;

import devtests.deployment.util.JSR88Deployer;

/**
 *
 * @author Jerome Dochez
 */
public class ChildTargetModuleIDTester extends JSR88Deployer {

    /** Creates a new instance of DeployTargetModuleIDTester */
    public ChildTargetModuleIDTester(String host, String port, String user, String password) {
        super(host, port, user, password);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            ChildTargetModuleIDTester deployer = getDeployer(args);
            TargetModuleID[] ids = deployer.deploy(args);
            if (deployer.test(ids, args[6])) {
                log("Test Passed");
                System.exit(0);
            } else {
                log("Test FAILED");
                deployer.dumpModulesIDs("", ids);
                System.exit(1);
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected static ChildTargetModuleIDTester getDeployer(String[] args) {
       return new ChildTargetModuleIDTester(args[1], args[2], args[3], args[4]);
    }


    protected TargetModuleID[] deploy(String[] args) throws Exception {

        File inputFile = new File(args[6]);
        if (!inputFile.exists()) {
            error("File not found : " + inputFile.getPath());
            System.exit(1);
        }
        File deploymentFile = null;
        if (args.length > 6) {
            deploymentFile = new File(args[7]);
            if (!args[7].equals("null")) {
                if (!deploymentFile.exists()) {
                    error("Deployment File not found : " + deploymentFile.getPath());
                    System.exit(1);
                }
            }
        }

        log("Deploying " + inputFile + " plan: " + deploymentFile);
        ProgressObject po = deploy(inputFile, deploymentFile, false);
        return po.getResultTargetModuleIDs();
    }

    protected boolean test(TargetModuleID[] moduleIDs, String path)
        throws Exception
    {

            // hack for std modules

            TargetModuleID[] aTargetModuleIDs = findApplication("sayhello", ModuleType.WAR, null);

            dumpModulesIDs("war", aTargetModuleIDs);

        if (moduleIDs.length==0) {
            // deployment failed ?
            log("Deployment failed, got zero TargetModuleID");
            System.exit(1);
        }

        // we are loading the deployed file and checking that the moduleIDs are
        // correct
        Application app = ApplicationArchivist.openArchive(new File(path));

        // check of non running modules
        ModuleType modType;
        if (app.isVirtual()) {
            modType = app.getStandaloneBundleDescriptor().getModuleType();
        } else {
            modType = ModuleType.EAR;
        }

        // now we need to start the application
        for (int i=0;i<moduleIDs.length;i++) {
            TargetModuleID aTargetModuleID = moduleIDs[i];

            // should be part of the non running
            TargetModuleID[] targetModuleIDs = findApplication(aTargetModuleID.getModuleID(),
                                                    modType, Boolean.FALSE);
            check(path, app, targetModuleIDs);

            // should be part of the available apps
            targetModuleIDs = findApplication(aTargetModuleID.getModuleID(),
                                                    modType, null);
            check(path, app, targetModuleIDs);

            // now we start it..
            start(aTargetModuleID.getModuleID());

            // should be part of the running
            targetModuleIDs = findApplication(aTargetModuleID.getModuleID(),
                                                    modType, Boolean.TRUE);
            check(path, app, targetModuleIDs);

            // should be part of the available apps
            targetModuleIDs = findApplication(aTargetModuleID.getModuleID(),
                                                    modType, null);
            check(path, app, targetModuleIDs);

        }
        return true;

    }

    private boolean check(String path, Application app, TargetModuleID[] moduleIDs) {

        if (app.isVirtual()) {
            BundleDescriptor bd = app.getStandaloneBundleDescriptor();

            // standalone module, should be fast.
            if (moduleIDs.length!=1) {
                // wrong number...
                log("Error " + path + " is a standalone module, got more than 1 targetmoduleID");
                dumpModulesIDs("", moduleIDs);
                return false;
            }
        } else {
            for (int i=0;i<moduleIDs.length;i++) {
                TargetModuleID parent = moduleIDs[i];
                Target target = parent.getTarget();
                log("Deployed on " + target.getName() + " with module ID " + parent.getModuleID());

                // now look at all the children
                TargetModuleID[] children = parent.getChildTargetModuleID();
                if (children==null) {
                    log("ERROR : App from " + path + " has " + app.getBundleDescriptors().size() +
                        " modules but I didn't get any children TagetModuleID");
                    return false;
                }

                // size is consistent ?
                if (children.length!=app.getBundleDescriptors().size()) {
                    log("ERROR : App from " + path + " has " + app.getBundleDescriptors().size() +
                        " modules but I got only " + children.length +
                        " children TagetModuleID");
                    return false;
                } else {
                    log("Expected " + app.getBundleDescriptors().size() +
                        " children TargetModuleIDs and got " + children.length);
                }

                for (int j=0;j<children.length;j++) {
                    TargetModuleID aChild = children[j];
                    log("Processing " + aChild.getModuleID());

                    String childModuleID = aChild.getModuleID();
                    String[] splitted = childModuleID.split("#");
                    if (splitted.length!=2) {
                        log("Unknown sub module id " + childModuleID);
                        return false;
                    }

                    // check that parent TargeTModuleID is correct
                    if (aChild.getParentTargetModuleID().equals(parent)) {
                        log("Child's parent TargetModuleID is correctly set");
                    } else {
                        log("Child's parent TargetModuleID is incorrect");
                        return false;
                    }

                    String first = splitted[0];
                    if (first.equals(parent.getModuleID())) {
                        log("Correct parent module id for child " + childModuleID);
                    } else {
                        log("Incorrect parent module id for child " + childModuleID);
                    }

                    // look for the right module descriptor..
                    ModuleDescriptor md = app.getModuleDescriptorByUri(splitted[1]);
                    if (md==null) {
                        log("Cannot find module descriptor for " + childModuleID);
                        //return false;
                    } else {
                        log("Found module descriptor for " + childModuleID);
                    }
                    if (md.getModuleType().equals(ModuleType.WAR)) {
                        log("Web module deployed at : " + aChild.getWebURL());
                    }
                }
            }
        }
        // if we are here, it's good !
        return true;
    }
}
