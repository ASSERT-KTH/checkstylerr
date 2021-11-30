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
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=1537
 * ("Add support for realm configuration at virtual server level"):
 *
 * This test deploys a webapp that does not specify any realm-name in its
 * web.xml to the virtual-server "server" that specifies an "authRealm"
 * property whose value references the admin-realm. This test then accesses
 * one of the webapp's protected resources, by providing the admin's
 * credentials.
 *
 * The "authRealm" property of the virtual-server "server" is added (before
 * the webapp's deployment) and removed (after the webapp's undeployment)
 * dynamically,
 */
public class WebTest {

    private static final String TEST_NAME = "virtual-server-auth-realm-property";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String JSESSIONIDSSO = "JSESSIONIDSSO";

    private static final String EXPECTED = "SUCCESS!";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String adminUser;
    private String adminPassword;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        adminUser = args[3];
        adminPassword = args[4];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 1537");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void run() throws Exception {

        String jsessionId = accessIndexDotJsp();
        String redirect = accessLoginPage(jsessionId);
        followRedirect(new URL(redirect).getPath(), jsessionId);

        stat.addStatus(TEST_NAME, stat.PASS);
    }

    /*
     * Attempt to access index.jsp resource protected by FORM based login.
     */
    private String accessIndexDotJsp() throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String line = null;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/index.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Set-Cookie:")
                        || line.startsWith("Set-cookie:")) {
                    break;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (line == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        return getSessionIdFromCookie(line, JSESSIONID);
    }

    /*
     * Access login.jsp.
     */
    private String accessLoginPage(String jsessionId) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String line = null;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot
                + "/j_security_check?j_username=" + adminUser
                + "&j_password=" + adminPassword
                + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            String cookie = "Cookie: " + jsessionId + "\n";
            os.write(cookie.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    break;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (line == null) {
            throw new Exception("Missing Location response header");
        }

        return line.substring("Location:".length()).trim();
    }

    /*
     * Follow redirect to
     * http://<host>:<port>/web-virtual-server-auth-realm-property/index.jsp
     * and access this resource.
     */
    private String followRedirect(String path, String jsessionId)
            throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String cookieHeader = null;
        boolean accessGranted = false;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + path + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            String cookie = "Cookie: " + jsessionId + "\n";
            os.write(cookie.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Set-Cookie:")
                        || line.startsWith("Set-cookie:")) {
                    cookieHeader = line;
                } else if (line.contains("SUCCESS!")) {
                    accessGranted = true;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (cookieHeader == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        if (!accessGranted) {
            throw new Exception("Failed to access index.jsp");
        }

        return getSessionIdFromCookie(cookieHeader, JSESSIONIDSSO);
    }

    /*
     * Attempt to access index.jsp resource protected by FORM based login,
     * supplying JSESSIONIDSSO from previous run.
     */
    private void accessIndexDotJsp(String jsessionIdSSO) throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        boolean jSecurityCheckFound = false;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/index.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            String cookie = "Cookie: " + jsessionIdSSO + "\n";
            os.write(cookie.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.contains("j_security_check")) {
                    jSecurityCheckFound = true;
                    break;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (!jSecurityCheckFound) {
            throw new Exception("No j_security_check action found in response");
        }
    }

    private String getSessionIdFromCookie(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index, endIndex);
            } else {
                ret = cookie.substring(index);
            }
            ret = ret.trim();
        }

        return ret;
    }

    private void close(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(BufferedReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
