/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the Apache License; either
 * version 2.0 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.master.configuration;

import com.griddynamics.jagger.engine.e1.scenario.*;
import com.griddynamics.jagger.master.CompositableTask;
import com.griddynamics.jagger.master.CompositeTask;
import com.griddynamics.jagger.monitoring.InfiniteDuration;
import com.griddynamics.jagger.monitoring.MonitoringTask;
import com.griddynamics.jagger.user.ProcessingConfig;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: dkotlyarov
 */
public class UserTaskGenerator implements ApplicationContextAware {
    private ProcessingConfig config;
    private ApplicationContext applicationContext;
    private boolean monitoringEnable = false;

    public UserTaskGenerator() {
    }

    public ProcessingConfig getConfig() {
        return config;
    }

    public void setConfig(ProcessingConfig config) {
        this.config = config;
    }

    public List<Task> generate() {
        List<Task> result = new LinkedList<Task>();
        int number = 0;
        HashSet<String> names = new HashSet<String>();
        if (config == null) {
            config=initConfig();
        }
        for (ProcessingConfig.Test testConfig : config.getTests()) {
            ++number;

            CompositeTask compositeTask = new CompositeTask();
            compositeTask.setLeading(new ArrayList<CompositableTask>());
            compositeTask.setAttendant(new ArrayList<CompositableTask>());

            for (ProcessingConfig.Test.Task taskConfig : testConfig.getTasks()) {
                String name = String.format("%s [%s]", testConfig.getName(), taskConfig.getName());
                if (!names.contains(name)) {
                    names.add(name);
                    AtomicBoolean shutdown = new AtomicBoolean(false);
                    TerminateStrategyConfiguration terminationStrategy;
                    if (taskConfig.isAttendant()) {
                        terminationStrategy = new InfiniteTerminationStrategyConfiguration();
                        WorkloadTask workloadTask = createWorklod(number, name, shutdown, taskConfig, terminationStrategy);
                        compositeTask.getAttendant().add(workloadTask);
                    } else {
                        terminationStrategy = new UserTerminateStrategyConfiguration(testConfig, taskConfig, shutdown);
                        WorkloadTask workloadTask = createWorklod(number, name, shutdown, taskConfig, terminationStrategy);
                        compositeTask.getLeading().add(workloadTask);
                    }
                } else {
                    throw new IllegalArgumentException(String.format("Task with name '%s' already exists", name));
                }
            }

            if (monitoringEnable) {
                MonitoringTask attendantMonitoring = new MonitoringTask(number, testConfig.getName() + " --- monitoring", compositeTask.getTaskName(), new InfiniteDuration());
                compositeTask.getAttendant().add(attendantMonitoring);
            }
            result.add(compositeTask);
        }
        return result;
    }

    private WorkloadTask createWorklod(int number, String name, AtomicBoolean shutdown, ProcessingConfig.Test.Task taskConfig, TerminateStrategyConfiguration terminationStrategy) {
        WorkloadTask prototype = applicationContext.getBean(taskConfig.getBean(), WorkloadTask.class);
        WorkloadTask workloadTask = prototype.copy();
        workloadTask.setNumber(number);
        workloadTask.setName(name);
        workloadTask.setTerminateStrategyConfiguration(terminationStrategy);
        workloadTask.setClockConfiguration(new UserClockConfiguration(1000, taskConfig, shutdown));
        return workloadTask;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public boolean isMonitoringEnable() {
        return monitoringEnable;
    }

    public void setMonitoringEnable(boolean monitoringEnable) {
        this.monitoringEnable = monitoringEnable;
    }

    public static ProcessingConfig getConfigFromFile(String fileName) {
        try {
            return new Persister().read(ProcessingConfig.class, new File(fileName));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ProcessingConfig initConfig() {
        String processingConfigFileName = System.getProperty("jagger.user.processing.config");
        if (processingConfigFileName != null) {
            return getConfigFromFile(processingConfigFileName);
        } else {
            return null;
        }
    }
}
