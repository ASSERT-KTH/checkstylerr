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
 * Unit test for 6412405 ("ParserUtils picks up parser from web app").
 * See also https://glassfish.dev.java.net/issues/show_bug.cgi?id=7968
 * ("Problem with tlds and --libraries")
 */
public class WebTest {

    private static final String TEST_NAME = "jsp-in-webapp-with-bundled-xercesImpl";

    private static final String EXPECTED_RESPONSE = "Hello, world!";

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6412405");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {

        URL url = new URL("http://" + host + ":" + port + contextRoot + "/test.jsp");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new Exception("Unexpected return code: " + responseCode);
        }

        try (BufferedReader bis = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (EXPECTED_RESPONSE.equals(line)) {
                    break;
                }
            }

            if (line == null) {
                throw new Exception("Wrong response body. Could not find " + "expected string: " + EXPECTED_RESPONSE);
            }
        }
    }
}
