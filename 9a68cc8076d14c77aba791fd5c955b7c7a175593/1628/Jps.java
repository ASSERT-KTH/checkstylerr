/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package admin.util;

import java.io.*;
import java.util.*;
import static admin.util.ProcessUtils.isWindows;
import static admin.util.ProcessUtils.ok;

/**
 * Run a native process with jps
 * -- get the pid for a running JVM
 * note:  dropping in an implementation for jps is not hard.
 * @author bnevins
 */
public class Jps {
    public static void main(String[] args) {
        Set<Map.Entry<Integer, String>> set = getProcessTable().entrySet();
        System.out.println("** Got " + set.size() + " process entries");
        for (Map.Entry<Integer, String> e : set) {
            System.out.printf("%d %s\n", e.getKey(), e.getValue());
        }
        if(args.length > 0) {
            System.out.printf("Jps.isPid(%s) ==> %b\n", args[0], Jps.isPid(Integer.parseInt(args[0])));
        }
    }

    final static public Map<Integer, String> getProcessTable() {
        return new Jps().pidMap;
    }

    /**
     * return the platform-specific process-id of a JVM
     * @param mainClassName The main class - this is how we identify the right JVM.
     * You can pass in a fully-qualified name or just the classname.
     * E.g. com.sun.enterprise.glassfish.bootstrap.ASMain and ASMain work the same.
     * @return the process id if possible otherwise 0
     */
    final static public List<Integer> getPid(String mainClassName) {
        if (mainClassName == null)
            return Collections.emptyList();

        String plainName = plainClassName(mainClassName);
        Jps jps = new Jps();
        List<Integer> ints = new LinkedList<Integer>();
        Set<Map.Entry<Integer, String>> set = jps.pidMap.entrySet();
        Iterator<Map.Entry<Integer, String>> it = set.iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, String> entry = it.next();
            String valueFull = entry.getValue();
            String valuePlain = plainClassName(valueFull);

            if (mainClassName.equals(valueFull) || plainName.equals(valuePlain))
                // got a match!
                ints.add(entry.getKey());
        }
        return ints;
    }

    /**
     * Is this pid owned by a process?
     * @param apid the pid of interest
     * @return whether there is a process running with that id
     */
    final static public boolean isPid(int apid) {
        return new Jps().pidMap.containsKey(apid);
    }

    private Jps() {
        try {
            if (jpsExe == null) {
                return;
            }
            ProcessManager pm = new ProcessManager(jpsExe.getPath(), "-l");
            pm.setEcho(false);
            pm.execute();
            String jpsOutput = pm.getStdout();

            // get each line
            String[] ss = jpsOutput.split("[\n\r]");

            for (String line : ss) {
                if (line == null || line.length() <= 0) {
                    continue;
                }

                String[] sublines = line.split(" ");
                if (sublines == null || sublines.length != 2) {
                    continue;
                }

                int aPid = 0;
                try {
                    aPid = Integer.parseInt(sublines[0]);
                }
                catch (Exception e) {
                    continue;
                }
                if (!isJps(sublines[1])) {
                    pidMap.put(aPid, sublines[1]);
                }
            }
        }
        catch (Exception e) {
        }
    }

    private boolean isJps(String id) {
        if (!ok(id)) {
            return false;
        }

        if (id.equals(getClass().getName())) {
            return true;
        }

        if (id.equals("sun.tools.jps.Jps")) {
            return true;
        }

        return false;
    }

    /**
     * This is a bit tricky.  "jps -l" will return a FQ classname
     * But it also might return a path if you start with "java -jar"
     * E.g.
     <pre>
     2524 sun.tools.jps.Jps
     5324 com.sun.enterprise.glassfish.bootstrap.ASMain
     4120 D:\glassfish6\glassfish\bin\..\modules\admin-cli.jar
     </pre>
     * If there is a path -- then there is no classname and vice-versa
     * @param s
     * @return
     */
    private static String plainClassName(String s) {
        if(s == null)
            return null;

        if(hasPath(s))
            return stripPath(s);

        if (!s.contains(".") || s.endsWith("."))
            return s;


        // we handled a/b/c/foo.jar
        // now let's handle foo.jar
        if(s.endsWith(".jar"))
            return s;

        return s.substring(s.lastIndexOf('.') + 1);
    }

    private static boolean hasPath(String s) {
        if(s.indexOf('/') >= 0)
            return true;
        if(s.indexOf('\\') >= 0)
            return true;
        return false;
    }

    /**
     * return whatever comes after the last file separator
     */
    private static String stripPath(String s) {
        // Don't bother with the annoying back vs. forward
        s = s.replace('\\', '/');
        int index = s.lastIndexOf('/');

        if(index < 0)
            return s;

        // don't forget about handling a name that ends in a slash!
        // should not happen!!  But if it does return the original
        if(s.length() - 1 <= index)
            return s;

        // we are GUARANTEED to have at least one char past the final slash...
        return s.substring(index + 1);
    }

    private Map<Integer, String> pidMap = new HashMap<Integer, String>();
    private static final File jpsExe;
    private static final String jpsName;

    static {
        if (isWindows()) {
            jpsName = "jps.exe";
        }
        else {
            jpsName = "jps";
        }

        final String javaroot = System.getProperty("java.home");
        final String relpath = "/bin/" + jpsName;
        final File fhere = new File(javaroot + relpath);
        File fthere = new File(javaroot + "/.." + relpath);

        if (fhere.isFile()) {
            jpsExe = SmartFile.sanitize(fhere);
        }
        else if (fthere.isFile()) {
            jpsExe = SmartFile.sanitize(fthere);
        }
        else {
            jpsExe = null;
        }
    }
}
