/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Bugzilla 33463
 */
public class WebTest {

    private static int count = 0;
    private static int EXPECTED_COUNT = 1;

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("Remove Attribute on destroy");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        try {
            goGet(host, port, "DESTROY", contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("attributeListenerClear", stat.FAIL);
        }
        stat.printSummary("web/attributeListenerClear---> expect " + EXPECTED_COUNT);
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        long time = System.currentTimeMillis();
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            int i=0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(i++ + ": " + line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);

                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-attributeListenerClear: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-attributeListenerClear: " + line.substring(0,index), stat.FAIL);
                    }
                    count++;
                }
            }
        } catch( Exception ex){
            ex.printStackTrace();
        }
   }

}
