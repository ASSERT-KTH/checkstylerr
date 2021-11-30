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
 * Unit test for 6269102 ("SSL termination is not working, Appserver replaces
 * the https to http during redirection").
 *
 * This test
 *
 * - sets the authPassthroughEnabled property of http-listener-1 to TRUE [1],
 * - configures a server-name attribute for http-listener-1 with a value of
 *   https://lbhost:8888 [2],
 * - includes a 'Proxy-keysize' header with a value > 0 in the request [3],
 *
 * and expects that the host name and port# of the server-name attribute be
 * reflected in the Location response header, along with an https scheme
 * (due to [1] and [3] above for AS installations that do not bundle native
 * webcore, and due to the https scheme in the server-name attribute (see [2])
 * for AS installations that do bundle the native webcore (since the request
 * for /{contextRoot}, which results in a redirect to /{contextRoot}/, is
 * handled by the native webcore and not forwarded to the web container)).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "auth-passthrough-send-redirect-with-https-server-name";

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6269102");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        try {
            testRemoteAddress();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private void testRemoteAddress() throws Exception {

        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Proxy-keysize: 512\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("Location:")) {
                    break;
                }
            }
            if (line == null) {
                throw new Exception("Missing Location response header");
            }
            System.out.println("Location header: " + line);
            String location = line.substring("Location:".length()).trim();
            String expectedLocation = "https://lbhost:8888" + contextRoot
                                                                    + "/";
            if (!expectedLocation.equals(location)) {
                throw new Exception(
                    "Wrong Location response header, expected: " +
                    expectedLocation + ", received: " + location);
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
    }
}
