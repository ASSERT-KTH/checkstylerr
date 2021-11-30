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

/**
 * Unit test for GlassFish Issue 659: "Include of JSP documents with custom
 * tag libs" (https://glassfish.dev.java.net/issues/show_bug.cgi?id=659).
 */
public class WebTest {

    private static final String TEST_NAME =
        "jsp-tag-library-validator-xml-view-static-include";

    private static final String EXPECTED_RESPONSE = "Hello World";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 659");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {

        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp/a.jspx");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (EXPECTED_RESPONSE.equals(line)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    System.err.println("Wrong response body. Expected: "
                                       + EXPECTED_RESPONSE + ", received: "
                                       + line);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            } else {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
