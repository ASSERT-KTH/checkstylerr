/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import org.glassfish.api.admin.progress.JobInfos;
import org.jvnet.hk2.annotations.Contract;

/**
 * This is the contract for the JobManagerService The JobManager will be responsible for 1. generating unique ids for
 * jobs 2. serving as a registry for jobs 3. creating thread pools for jobs 4.removing expired jobs
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */

@Contract
public interface JobManager {

    /**
     * Container for checkpoint related objects
     */
    public class Checkpoint implements Serializable {

        private static final long serialVersionUID = 1L;

        private Job job;
        private AdminCommand command;
        private AdminCommandContext context;

        public Checkpoint(Job job, AdminCommand command, AdminCommandContext context) {
            this.job = job;
            this.command = command;
            this.context = context;
        }

        public Job getJob() {
            return job;
        }

        public AdminCommand getCommand() {
            return command;
        }

        public AdminCommandContext getContext() {
            return context;
        }

    }

    /**
     * This method is used to generate a unique id for a managed job
     *
     * @return returns a new id for the job
     */
    String getNewId();

    /**
     * This method will register the job in the job registry
     *
     * @param instance job to be registered
     * @throws IllegalArgumentException
     */
    void registerJob(Job instance) throws IllegalArgumentException;

    /**
     * This method will return the list of jobs in the job registry
     *
     * @return list of jobs
     */
    Iterator<Job> getJobs();

    /**
     * This method is used to get a job by its id
     *
     * @param id The id to look up the job in the job registry
     * @return the Job
     */
    Job get(String id);

    /**
     * This will purge the job associated with the id from the registry
     *
     * @param id the id of the Job which needs to be purged
     */
    void purgeJob(String id);

    /**
     * This will get the list of jobs from the job registry which have completed
     *
     * @return the details of all completed jobs using JobInfos
     */
    JobInfos getCompletedJobs(File jobs);

    /**
     * This is a convenience method to get a completed job with an id
     *
     * @param id the completed Job whose id needs to be looked up
     * @return the completed Job
     */
    Object getCompletedJobForId(String id);

    /**
     * This is used to purge a completed job whose id is provided
     *
     * @param id the id of the Job which needs to be purged
     * @return the new list of completed jobs
     */
    Object purgeCompletedJobForId(String id);

    /**
     * This is used to get the jobs file for a job
     *
     * @return the location of the job file
     */
    File getJobsFile();

    /**
     * Stores current command state.
     */
    void checkpoint(AdminCommand command, AdminCommandContext context) throws IOException;

    /**
     * Stores current command state.
     */
    void checkpoint(AdminCommandContext context, Serializable data) throws IOException;

    /**
     * Load checkpoint related data.
     */
    <T extends Serializable> T loadCheckpointData(String jobId) throws IOException, ClassNotFoundException;

}
