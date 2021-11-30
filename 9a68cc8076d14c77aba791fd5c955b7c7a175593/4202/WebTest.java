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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6181949 ("JspServlet init param 'modificationTestInterval'
 * ignored").
 *
 * This test:
 *
 *  - accesses a JSP,
 *
 *  - updates the JSP (through a servlet),
 *
 *  - accesses the JSP again within modificationTestInterval seconds (and
 *    therefore is expected to get the old contents), and
 *
 *  - accesses the JSP once again after modificationTestInterval seconds have
 *    expired (and therefore is expected to get the updated contents).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME
        = "jsp-config-modification-test-interval";
    private static final String ORIGINAL_CONTENT = "original jsp";
    private static final String UPDATED_CONTENT = "updated jsp";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for modificationTestInterval "
                            + "jsp-config property");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void run() throws Exception {

        String bodyLine = null;

        // Access JSP
        bodyLine = getBodyLine("jsp/test.jsp");
        if (!ORIGINAL_CONTENT.equals(bodyLine)) {
            throw new Exception("Wrong response: Expected: " +
                ORIGINAL_CONTENT);
        }

        // Update JSP
        System.out.println("Updating JSP ...");
        bodyLine = getBodyLine("UpdateJsp");
        System.out.println(bodyLine);

        /*
         * Access JSP. Must get original contents, because the
         * modificationTestInterval specified in sun-web.xml has not yet
         * expired, which means that we must not (yet) check for any
         * modifications of the JSP
         */
        bodyLine = getBodyLine("jsp/test.jsp");
        if (!ORIGINAL_CONTENT.equals(bodyLine)) {
            throw new Exception("Wrong response: Expected: " +
                ORIGINAL_CONTENT + ", received: " + bodyLine);
        }

        /*
         * Sleep for the amount of seconds specified for
         * modificationTestInterval jsp-config property in sun-web.xml
         */
        System.out.println("Sleeping for 60s ...");
        Thread.sleep(60 * 1000L);

        /*
         * Access JSP. In this case, we do check for JSP's modification date,
         * recompile, and return updated content
         */
        bodyLine = getBodyLine("jsp/test.jsp");
        if (!UPDATED_CONTENT.equals(bodyLine)) {
            throw new Exception("Wrong response: Expected: " +
                UPDATED_CONTENT + ", received: " + bodyLine);
        }
    }

    private String getBodyLine(String resource) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/" + resource + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String bodyLine = null;
            while ((line = bis.readLine()) != null) {
                bodyLine = line;
            }
            return bodyLine;
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }
}
