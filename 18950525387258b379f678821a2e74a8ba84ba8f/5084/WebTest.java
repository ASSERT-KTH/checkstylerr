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
 * Unit test for 6057385 ("WS6.1 Web Application: form fields out of order,
 * works fine in WS6.0").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "servlet-request-params-preserve-order";
    private static final String EXPECTED_RESPONSE =
        "param1=value1param2=value2param3=value3";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6057385");

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {

        String url = "http://" + host + ":" + port + contextRoot
                    + "/TestServlet?param1=value1&param2=value2&param3=value3";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (!EXPECTED_RESPONSE.equals(line)) {
                throw new Exception("Wrong response. " + "Expected: " +
                    EXPECTED_RESPONSE + ", received: " + line);
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
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
