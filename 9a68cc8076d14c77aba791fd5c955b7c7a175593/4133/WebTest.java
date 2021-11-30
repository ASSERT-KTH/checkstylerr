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
 * Unit test for 6346738 ("getParameter() fails to return correct paramter
 * when locale-charset used QueryString not considered"):
 *
 * Make sure query param takes precedence (i.e., is returned as the first
 * element by ServletRequest.getParameterValues()) over param with same name in
 * POST body even when form-hint-field has been declared in sun-web.xml (which
 * causes the POST body to be parsed in order to determine request encoding).
 */
public class WebTest {

    private static final String TEST_NAME =
        "form-hint-field-post-with-query-param-precedence";

    private static final String EXPECTED_RESPONSE = "value1,value2";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private Socket socket = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6346738");
        WebTest test = new WebTest(args);
        try {
            test.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (test.socket != null) {
                    test.socket.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {

        String body = "param1=value2";

        // Create a socket to the host
        socket = new Socket(host, new Integer(port).intValue());

        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                                            socket.getOutputStream()));
        wr.write("POST " + contextRoot + "/TestServlet?param1=value1"
                 + " HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");

        // Send body
        wr.write(body);
        wr.flush();

        // Read response
        BufferedReader bis = null;
        String lastLine = null;
        try {
            bis = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                lastLine = line;
            }
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!EXPECTED_RESPONSE.equals(lastLine)) {
            throw new Exception("Wrong response. Expected: " +
                                EXPECTED_RESPONSE + ", received: " +
                                lastLine);
        }
    }
}
