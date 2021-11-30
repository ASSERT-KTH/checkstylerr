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
 * Unit test for CR 6376017 ("Erroneous values for request.getPathInfo() and
 * request.getPathTranslated()").
 */
public class WebTest {

    private static final String TEST_NAME =
        "servlet-request-getPathInfo-getPathTranslated";

    private static final String EXPECTED_RESPONSE = "/Page1.jsp";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String appserverHome;
    private String expectedResponse;
    private boolean fail = false;
    private Socket sock = null;

    public WebTest(String[] args) {

        host = args[0];
        port = args[1];
        contextRoot = args[2];
        appserverHome = args[3];

        expectedResponse = appserverHome
                + "/domains/domain1/applications"
                + contextRoot + "-web"
                + EXPECTED_RESPONSE;
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6376017");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        try {
            invoke();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail = true;
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (fail) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }

    public void invoke() throws Exception {

        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            int i = 0;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if (line.startsWith("Location:")) {
                    break;
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (line == null) {
            System.err.println("Missing Location response header");
            fail = true;
            return;
        }

        int index = line.indexOf("http");
        if (index == -1) {
            System.err.println(
                "Missing http address in Location response header");
            fail = true;
            return;
        }

        String redirectTo = line.substring(index);
        System.out.println("Redirect to: " + redirectTo);
        URL url = new URL(redirectTo);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            fail = true;
            return;
        }


        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
            processResponse(br);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }


    private void processResponse(BufferedReader br) throws Exception {

        boolean first = true;
        String line = null;
        int pos = expectedResponse.indexOf("workspace");
        if (pos>=0) {
            expectedResponse = expectedResponse.substring(pos);
        }
        while ((line = br.readLine()) != null) {
            pos = line.indexOf("workspace");
            if (pos>=0) {
                line = line.substring(pos);
            }
            if (first) {
                if (!EXPECTED_RESPONSE.equals(line)) {
                    System.err.println("Wrong response, expected: "
                                       + EXPECTED_RESPONSE
                                       + ". received: " + line);
                    fail = true;
                    return;
                }
                first = false;
            } else if (!expectedResponse.equals(line)) {
                System.err.println("Wrong response, expected: "
                                   + expectedResponse
                                   + ". received: " + line);
                fail = true;
                return;
            }
        }
    }
}
