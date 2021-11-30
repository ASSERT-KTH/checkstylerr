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
 * Unit test for 4817642 ("RN: get two different session objects in
 * consecutive request dispatching runs") and 4876454 ("get two different
 * session objects in consecutive request dispatching runs").
 *
 * Client specifies JSESSIONID in Cookie request header. The JSP being accessed
 * creates a session. Since the web module's sun-web.xml has reuseSessionID set
 * to TRUE, the container is supposed to assign the client-provided
 * JSESSIONID to the newly generated session, and return it in the response.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "reuse-sessionid";
    private static final String JSESSION_ID = "1234";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 4817642,4876454");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        try {
            Socket sock = new Socket(host, new Integer(port).intValue());
            OutputStream os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write(("Cookie: JSESSIONID=" + JSESSION_ID + "\n").getBytes());
            os.write("\n".getBytes());

            InputStream is = sock.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("Set-Cookie:")
                        || line.startsWith("Set-cookie:") ) {
                    break;
                }
            }

            if (line != null) {
                System.out.println(line);
                // Check jsessionid
                String sessionId = getCookieField(line, "JSESSIONID=");
                if (sessionId != null) {
                    if (!sessionId.equals(JSESSION_ID)) {
                        System.err.println("Wrong JSESSIONID: " + sessionId
                                           + ", expected: \"" + JSESSION_ID
                                           + "\"");
                        stat.addStatus(TEST_NAME, stat.FAIL);
                    } else {
                        stat.addStatus(TEST_NAME, stat.PASS);
                    }
                } else {
                    System.err.println("Missing JSESSIONID");
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            } else {
                System.err.println("Missing Set-Cookie response header");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private String getCookieField(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index + field.length(), endIndex);
            } else {
                ret = cookie.substring(index + field.length());
            }
            ret = ret.trim();
        }

        return ret;
    }

}
