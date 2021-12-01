/*
 * Copyright (c) 2001-2004 Ant-Contrib project.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.antcontrib.perf;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * This BuildListener keeps track of the total time it takes for each target
 * and task to execute, then prints out the totals when the build is finished.
 * This can help pinpoint the areas where a build is taking a lot of time so
 * optimization efforts can focus where they'll do the most good. Execution
 * times are grouped by targets and tasks, and are sorted from fastest running
 * to slowest running.
 * <p>Output can be saved to a file by setting a property in Ant. Set
 * "<code>performance.log</code>" to the name of a file. This can be set either
 * on the command line with the <code>-D</code> option
 * (<code>-Dperformance.log=/tmp/performance.log</code>)
 * or in the build file itself (<code>&lt;property name="performance.log"
 * location="/tmp/performance.log"/&gt;</code>).</p>
 * <p>Developed for use with Antelope, migrated to ant-contrib Oct 2003.</p>
 *
 * @author <a href="mailto:danson@germane-software.com">Dale Anson</a>
 * @version $Revision: 1.5 $
 */
public class AntPerformanceListener implements BuildListener {
    /**
     * Field targetStats.
     */
    private Map<Target, StopWatch> targetStats = new ConcurrentHashMap<Target, StopWatch>();

    /**
     * Field taskStats.
     */
    private Map<Task, StopWatch> taskStats = new ConcurrentHashMap<Task, StopWatch>();

    /**
     * Field master.
     */
    private StopWatch master = null;

    /**
     * Field swStartTime.
     */
    private long swStartTime = 0;

    /**
     * Starts a 'running total' stopwatch.
     *
     * @param be BuildEvent
     * @see org.apache.tools.ant.BuildListener#buildStarted(BuildEvent)
     */
    public void buildStarted(BuildEvent be) {
        master = new StopWatch();
        swStartTime = master.start();
    }

    /**
     * Sorts and prints the results.
     *
     * @param be BuildEvent
     * @see org.apache.tools.ant.BuildListener#buildFinished(BuildEvent)
     */
    public void buildFinished(BuildEvent be) {
        long swStopTime = master.stop();

        // sort targets, key is StopWatch, value is Target
        TreeMap<StopWatch, Target> sortedTargets = new TreeMap<StopWatch, Target>(new StopWatchComparator());
        for (Map.Entry<Target, StopWatch> entry : targetStats.entrySet()) {
            sortedTargets.put(entry.getValue(), entry.getKey());
        }

        // sort tasks, key is StopWatch, value is Task
        TreeMap<StopWatch, Task> sortedTasks = new TreeMap<StopWatch, Task>(new StopWatchComparator());
        for (Map.Entry<Task, StopWatch> entry : taskStats.entrySet()) {
            sortedTasks.put(entry.getValue(), entry.getKey());
        }

        // print the sorted results
        StringBuilder msg = new StringBuilder();
        String lSep = System.getProperty("line.separator");
        msg.append(lSep).append("Statistics:").append(lSep);
        msg.append("-------------- Target Results ---------------------").append(lSep);
        for (Map.Entry<StopWatch, Target> entry : sortedTargets.entrySet()) {
            StringBuilder sb = new StringBuilder();
            Target target = entry.getValue();
            if (target != null) {
                Project p = target.getProject();
                if (p != null && p.getName() != null) {
                    sb.append(p.getName()).append(".");
                }
                String total = format(entry.getKey().total());
                String targetName = target.getName();
                if (targetName == null || targetName.length() == 0) {
                    targetName = "<implicit>";
                }
                sb.append(targetName).append(": ").append(total);
            }
            msg.append(sb.toString()).append(lSep);
        }
        msg.append(lSep);
        msg.append("-------------- Task Results -----------------------").append(lSep);
        for (Map.Entry<StopWatch, Task> entry : sortedTasks.entrySet()) {
            Task task = entry.getValue();
            StringBuilder sb = new StringBuilder();
            Target target = task.getOwningTarget();
            if (target != null) {
                Project p = target.getProject();
                if (p != null && p.getName() != null) {
                    sb.append(p.getName()).append(".");
                }
                String targetName = target.getName();
                if (targetName == null || targetName.length() == 0) {
                    targetName = "<implicit>";
                }
                sb.append(targetName).append(".");
            }
            sb.append(task.getTaskName()).append(": ").append(format(entry.getKey().total()));
            msg.append(sb.toString()).append(lSep);
        }

        msg.append(lSep);
        msg.append("-------------- Totals -----------------------------").append(lSep);
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss.SSS");
        msg.append("Start time: ").append(format.format(new Date(swStartTime))).append(lSep);
        msg.append("Stop time: ").append(format.format(new Date(swStopTime))).append(lSep);
        msg.append("Total time: ").append(format(master.total())).append(lSep);
        System.out.println(msg.toString());

        // write stats to file?
        Project p = be.getProject();
        File outfile = null;
        if (p != null) {
            String f = p.getProperty("performance.log");
            if (f != null) {
                outfile = new File(f);
            }
        }
        if (outfile != null) {
            try {
                FileWriter fw = new FileWriter(outfile);
                fw.write(msg.toString());
                fw.flush();
                fw.close();
                System.out.println("Wrote stats to: " + outfile.getAbsolutePath() + lSep);
            } catch (Exception e) {
                // ignored
            }
        }

        // reset the stats registers

        targetStats = new ConcurrentHashMap<Target, StopWatch>();
        taskStats = new ConcurrentHashMap<Task, StopWatch>();
    }

    /**
     * Formats the milliseconds from a StopWatch into decimal seconds.
     *
     * @param ms long
     * @return String
     */
    private String format(long ms) {
        return (ms < 1000) ? String.format("0.%03d sec", ms)
                : String.format("%d.%03d sec", ms / 1000, ms % 1000);
    }

    /**
     * Start timing the given target.
     *
     * @param be BuildEvent
     * @see org.apache.tools.ant.BuildListener#targetStarted(BuildEvent)
     */
    public void targetStarted(BuildEvent be) {
        StopWatch sw = new StopWatch();
        sw.start();
        targetStats.put(be.getTarget(), sw);
    }

    /**
     * Stop timing the given target.
     *
     * @param be BuildEvent
     * @see org.apache.tools.ant.BuildListener#targetFinished(BuildEvent)
     */
    public void targetFinished(BuildEvent be) {
        StopWatch sw = targetStats.get(be.getTarget());
        sw.stop();
    }

    /**
     * Start timing the given task.
     *
     * @param be BuildEvent
     * @see org.apache.tools.ant.BuildListener#taskStarted(BuildEvent)
     */
    public void taskStarted(BuildEvent be) {
        StopWatch sw = new StopWatch();
        sw.start();
        taskStats.put(be.getTask(), sw);
    }

    /**
     * Stop timing the given task.
     *
     * @param be BuildEvent
     * @see org.apache.tools.ant.BuildListener#taskFinished(BuildEvent)
     */
    public void taskFinished(BuildEvent be) {
        StopWatch sw = taskStats.get(be.getTask());
        if (sw != null) {
            sw.stop();
        }
    }

    /**
     * no-op.
     *
     * @param be BuildEvent
     * @see org.apache.tools.ant.BuildListener#messageLogged(BuildEvent)
     */
    public void messageLogged(BuildEvent be) {
        // does nothing
    }

    /**
     * Compares the total times for two StopWatches.
     */
    @SuppressWarnings("serial")
    public static class StopWatchComparator implements Comparator<StopWatch>, Serializable {
        /**
         * Compares the total times for two StopWatches.
         *
         * @param a StopWatch
         * @param b StopWatch
         * @return int
         */
        public int compare(StopWatch a, StopWatch b) {
            return a.total() < b.total() ? -1 : a.total() == b.total() ? 0 : 1;
        }
    }

    /**
     * A stopwatch, useful for 'quick and dirty' performance testing.
     *
     * <a href="mailto:danson@germane-software.com">Dale Anson</a>
     * @version $Revision: 1.5 $
     */
    public static class StopWatch {

        /**
         * storage for start time.
         */
        private long startTime = 0;

        /**
         * storage for stop time.
         */
        private long stopTime = 0;

        /**
         * cumulative elapsed time.
         */
        private long totalTime = 0;

        /**
         * Starts the stopwatch.
         */
        public StopWatch() {
            start();
        }

        /**
         * Starts/restarts the stopwatch.
         *
         * @return the start time, the long returned System.currentTimeMillis().
         */
        public long start() {
            startTime = System.currentTimeMillis();
            return startTime;
        }

        /**
         * Stops the stopwatch.
         *
         * @return the stop time, the long returned System.currentTimeMillis().
         */
        public long stop() {
            stopTime = System.currentTimeMillis();
            totalTime += stopTime - startTime;
            startTime = 0;
            this.stopTime = 0;
            return stopTime;
        }

        /**
         * Total cumulative elapsed time.
         *
         * @return the total time
         */
        public long total() {
            return totalTime;
        }

        /**
         * Elapsed time, difference between the last start time and now.
         *
         * @return the elapsed time
         */
        public long elapsed() {
            return System.currentTimeMillis() - startTime;
        }
    }

    // quick test for the formatter

    /**
     * Method main.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        AntPerformanceListener apl = new AntPerformanceListener();

        System.out.println(apl.format(1));
        System.out.println(apl.format(10));
        System.out.println(apl.format(100));
        System.out.println(apl.format(1000));
        System.out.println(apl.format(100000));
        System.out.println(apl.format(1000000));
        System.out.println(apl.format(10000000));
        System.out.println(apl.format(100000000));
        System.out.println(apl.format(1000000000));
    }
}
