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
 * Unit test for 6330300 ("URL forwarding feature as in AS7.x is gone from
 * AS8.x"):
 *
 * Virtual server "server" is configured with this property (all in a single
 * line):
 *
 *  <property
 *    name="redirect"
 *    value="from=/someprefix url-prefix=http://tmpserver:1234/tempcontext"/>
 *
 * Make sure that a request with a URI of "/someprefix/5678" will be
 * redirected to:
 *
 *   http://tmpserver:1234/tempcontext/5678
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_LOCATION_RESPONSE_HEADER
        = "Location: http://tmpserver:1234/tempcontext/5678";

    private static final String TEST_NAME
        = "virtual-server-redirect-property-url-prefix";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6330300");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeServlet();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET /someprefix/5678 HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            if (EXPECTED_LOCATION_RESPONSE_HEADER.equals(line)) {
                break;
            }
        }

        if (line != null) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Missing response header: "
                               + EXPECTED_LOCATION_RESPONSE_HEADER);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

    }
}
