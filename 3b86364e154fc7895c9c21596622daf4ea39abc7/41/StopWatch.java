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

/**
 * A stopwatch, useful for 'quick and dirty' performance testing. Typical usage:
 * <pre>
 * StopWatch sw = new StopWatch();  // automatically starts
 * // do something here...
 * sw.stop();
 * System.out.println(sw.toString());   // print the total
 * sw.start();  // restart the stopwatch
 * // do some more things...
 * sw.stop();
 * System.out.println(sw.format(sw.elapsed()); // print the time since the last start
 * System.out.println(sw.toString()); // print the cumulative total
 * </pre>
 * <p>Developed for use with Antelope, migrated to ant-contrib Oct 2003.</p>
 *
 * @author <a href="mailto:danson@germane-software.com">Dale Anson</a>
 * @version $Revision: 1.4 $
 */
public class StopWatch {
    /**
     * an identifying name for this stopwatch.
     */
    private String name = "";

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
     * is the stopwatch running.
     */
    private boolean running = false;

    /**
     * Starts the stopwatch.
     */
    public StopWatch() {
        this("");
    }

    /**
     * Starts the stopwatch.
     *
     * @param name an identifying name for this StopWatch
     */
    public StopWatch(String name) {
        this.name = name;
        start();
    }

    /**
     * Starts/restarts the stopwatch. <code>stop</code> must be called prior
     * to restart.
     *
     * @return the start time, the long returned System.currentTimeMillis().
     */
    public long start() {
        if (!running) {
            startTime = System.currentTimeMillis();
        }
        running = true;
        return startTime;
    }

    /**
     * Stops the stopwatch.
     *
     * @return the stop time, the long returned System.currentTimeMillis().
     */
    public long stop() {
        stopTime = System.currentTimeMillis();
        if (running) {
            totalTime += stopTime - startTime;
        }
        startTime = stopTime;
        running = false;
        return stopTime;
    }

    /**
     * Total cumulative elapsed time.
     *
     * @return the total time
     */
    public long total() {
        stop();
        long rtn = totalTime;
        totalTime = 0;
        return rtn;
    }

    /**
     * Elapsed time, difference between the last start time and now.
     *
     * @return the elapsed time
     */
    public long elapsed() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * getName() method.
     *
     * @return the name of this StopWatch
     */
    public String getName() {
        return name;
    }

    /**
     * Formats the given time into decimal seconds.
     *
     * @param ms long
     * @return the time formatted as mm:ss.ddd
     */
    public String format(long ms) {
        long min = ms / 60000;
        long sec = (min > 0) ? (ms - (min * 60000)) / 1000 : ms / 1000;
        return (ms < 1000) ? String.format("0.%03d sec", ms)
                : (min > 0) ? String.format("%d:%02d.%03d min", min, sec, ms % 1000)
                : String.format("%d.%03d sec", sec, ms % 1000);
    }

    /**
     * Returns the total elapsed time of the stopwatch formatted in decimal seconds.
     *
     * @return [name: mm:ss.ddd]
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (name != null) {
            sb.append(name).append(": ");
        }
        sb.append(format(totalTime));
        sb.append("]");
        return sb.toString();
    }

    /**
     * Method main.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        StopWatch sw = new StopWatch("test");

        // test the formatter
        System.out.println(sw.format(1));
        System.out.println(sw.format(10));
        System.out.println(sw.format(100));
        System.out.println(sw.format(1000));
        System.out.println(sw.format(100000));
        System.out.println(sw.format(128000));
        System.out.println(sw.format(1000000));

        // test the stopwatch
        try {
            System.out.println("StopWatch: " + sw.getName());
            Thread.sleep(2000);
            sw.stop();
            System.out.println(sw.toString());
            sw.start();
            Thread.sleep(2000);
            sw.stop();
            System.out.println("elapsed: " + sw.format(sw.elapsed()));
            System.out.println("total: " + sw.format(sw.total()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
