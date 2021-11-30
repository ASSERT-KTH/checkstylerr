/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import javax.net.ServerSocketFactory;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class CipherTest {

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8181;

    private static final String HTTP_GET_REQUEST = "GET / HTTP/1.0";

    private static String serverHostname = null;
    private static int serverPort = -1;

    // Default and Supported chipher set
    private static String[] DEFAULT_CIPHERS = null;
    private static String[] SUPPORTED_CIPHERS = null;

    // Flags
    private static final String INTERACTIVE_OPTION = "-i";

    private static final String HELP_OPTION = "-h";
    private static final String DEBUG_OPTION = "-d";
    private static final String VERBOSE1_OPTION = "-v";
    private static final String VERBOSE2_OPTION = "-verbose";

    private static final String HOST_OPTION = "-host";
    private static final String PORT_OPTION = "-port";

    private static final String SHOULD_PASS_OPTION = "-shouldPass";
    private static final String SHOULD_FAIL_OPTION = "-shouldFail";
    private static final String ENABLED_CIPHER_OPTION = "-enabledCipher";

    private static boolean VERBOSE_FLAG = false;

    private static void usage() {
        System.out.println("usage: CipherTest [-i] [-d] [-h] [-host hostname] [-port port#] -shouldPass <comma-spa-ciphers> [-v|-verbose]");
        System.out.println("-i interactive flag");
        System.out.println("-d debug mode");
        System.out.println("-v verbose mode");
        System.out.println("-verbose verbose mode");
        System.out.println("-h help/usage");
        System.out.println("-host hostname - host to connect to");
        System.out.println("-port port#    - port to connect to");
        System.out.println("-enabledCiphers - comma separated list of ciphers that should be ");
        System.out.println("-shouldPass    - comma separated list of ciphers that should pass");
        System.out.println("-shouldFail    - comma separated list of ciphers that should fail");
    }

    private static void verbose(String msg) {
        if( VERBOSE_FLAG)
            System.out.println(msg);
    }

    public static void main(String[] args) throws Exception {

        boolean interactive = false;
        boolean debug = false;
        boolean help = false;

        //Set<String> shouldPass = new Set<String>();
        //Set<String> shouldFail = new Set<String>();
        String shouldPass = null;
        String shouldFail = null;
        String enabledCipherAsString = null;

        for(int i=0; i<args.length; i++) {
            if( args[i].intern() == INTERACTIVE_OPTION.intern() ) {
                interactive = true;
            } else if( args[i].intern() == HELP_OPTION.intern() ) {
                help = true;
            } else if( args[i].intern() == VERBOSE1_OPTION.intern() ) {
                VERBOSE_FLAG = true;
            } else if( args[i].intern() == VERBOSE2_OPTION.intern() ) {
                VERBOSE_FLAG = true;
            } else if( args[i].intern() == DEBUG_OPTION.intern() ) {
                debug = true;
            } else if( args[i].intern() == HOST_OPTION.intern() ) {
                serverHostname = args[++i];
            } else if( args[i].intern() == PORT_OPTION.intern() ) {
                serverPort = Integer.parseInt(args[++i]);
            } else if( args[i].intern() == SHOULD_PASS_OPTION.intern() ) {
                shouldPass = args[++i];

                /*
                 * Workaround for JavaSE bug (6518827) where
                 * arguments of length 0 are not passed in on Windows.
                 */
                if (shouldPass.startsWith("-")) {
                    // a "" param was skipped
                    shouldPass = "";
                    i--;
                }
            } else if( args[i].intern() == SHOULD_FAIL_OPTION.intern() ) {
                shouldFail = args[++i];
            } else if( args[i].intern() == ENABLED_CIPHER_OPTION.intern() ) {
                enabledCipherAsString = args[++i];
            } else {
                System.out.println("Unrecognized option: " + args[i]);
                usage();
                System.exit(10);
            }

        }

        for(int i=0; i<args.length; i++) {
            verbose("Arg[" + i + "] " + args[i]);
        }

        if( help) {
            usage();
            System.exit(0);
        }
        if( debug ) {
            System.setProperty("javax.net.debug", "all");
        }

        if( shouldPass == null ) {
            usage();
            System.exit(11);
        }

        if( serverHostname == null )
            serverHostname = DEFAULT_HOSTNAME;

        if( serverPort == -1 )
            serverPort = DEFAULT_PORT;


        initCiphers();

        if( interactive ) {
            interactive();
        } else {
            nonInteractive(enabledCipherAsString, shouldPass, shouldFail);
        }

    }

    private static void interactive()
        throws IOException {

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            System.out.println();
            System.out.print(
                "Enter number of cipher or cipher-name to enable on this client: ");
            String s = reader.readLine();
            CipherTest ct = null;
            try {
                int num = Integer.parseInt(s);
                ct = new CipherTest(num, SUPPORTED_CIPHERS[num], null);
            } catch(Exception e) {
                // try it as a string
                ct = new CipherTest(s, s, null);
            }

            ct.run();
        }
    }

    private static void nonInteractive(String enabledCipher,
        String shouldPass, String shouldFail) {

//         if( enabledCipher == null ) {
//           for(int i=0; i<SUPPORTED_CIPHERS.length; i++) {
//               CipherTest ct = new CipherTest(i, SUPPORTED_CIPHERS[i], shouldFail);
//               ct.run();
//           }
//         } else {
//               CipherTest ct = new CipherTest(enabledCipher, shouldPass, shouldFail);
//               ct.run();
//         }

           CipherTest ct = new CipherTest(enabledCipher, shouldPass, shouldFail);
           ct.run();
    }

    private static void nonInteractive(String[] enabledCipher,
        String shouldPass, String shouldFail) {
        // TODO
    }


    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    private int startCipher = -1;
    String[] ciphers = null;
    String shouldPass;
    String shouldFail;
    private String testId = null;

    public CipherTest(int start, String shouldPass, String shouldFail) {
        startCipher = start;
        ciphers = new String[1];
        ciphers[0] = SUPPORTED_CIPHERS[startCipher];

        this.shouldPass = shouldPass;
        this.shouldFail = shouldFail;

    }

    public CipherTest(String  cipherName, String shouldPass, String shouldFail) {
        if( cipherName != null ) {
            ciphers = new String[1];
            ciphers[0] = cipherName;
        }

        this.shouldPass = shouldPass;
        this.shouldFail = shouldFail;
    }

    public void run() {

        try {

            testId = "SSL cipher test - " + ciphers[0];
            stat.addDescription("Security::SSL cipher test " + ciphers[0]);

            doSSLTest(ciphers);
            verbose("Supported cipher suite: " + ciphers[0]);

            passed(ciphers);
            //System.exit(0);

        } catch(SSLHandshakeException e) {
            failed(ciphers);
            verbose("Unsupported (SSLHandshakeException) ciphers: " + ciphers[0] + " ...");
        } catch(SSLException e) {
            failed(ciphers);
            verbose("SSLException with ciphers: " + ciphers[0] + e.getMessage());
        } catch(IOException e) {
            failed(ciphers);
            verbose("IOException with ciphers: " + ciphers[0] + " ...");
        } finally {
            stat.printSummary(testId);
        }
    }

    private void passed(String[] thatPassed) {
        for(int i=0; i<thatPassed.length; i++) {
            int index = shouldPass.indexOf(thatPassed[i]);
            if( index < 0 ) {
                // is not in the should pass, test failed
                System.out.println("Cipher - " + thatPassed[i] +
                    " - pased, but should not have. Test failed");
                stat.addStatus(testId, stat.FAIL);
                return;
                //System.exit(1);
            }
        }

        System.out.println("Test passed");
        stat.addStatus(testId, stat.PASS);
        //System.exit(0);
    }

    private void failed(String[] thatFailed) {
        for(int i=0; i<thatFailed.length; i++) {
            int index = shouldPass.indexOf(thatFailed[i]);
            if( index >= 0 ) {
                // is in the should pass, but did not
                System.out.println("Cipher - " + thatFailed[i] +
                    " - failed, but should have passed. Test failed");
                stat.addStatus(testId, stat.FAIL);
                return;
                //System.exit(1);
            }
        }

        System.out.println("(Negative) Test passed");
        stat.addStatus(testId, stat.PASS);
        //System.exit(0);
    }

    private  void doSSLTest(String[] enableCiphers) throws IOException {

        SSLSocketFactory sslSocketFactory =
            (SSLSocketFactory)SSLSocketFactory.getDefault();

        SSLSocket secureSocket = (SSLSocket)
            sslSocketFactory.createSocket(serverHostname, serverPort);

        if( enableCiphers != null )
            secureSocket.setEnabledCipherSuites(enableCiphers);

        BufferedWriter buffWriter = new BufferedWriter(
            new OutputStreamWriter(secureSocket.getOutputStream()));
        buffWriter.write(HTTP_GET_REQUEST);
        buffWriter.newLine();
        buffWriter.newLine();
        buffWriter.flush();
        //buffWriter.close();

        //System.out.println("Written request to server: " + HTTP_GET_REQUEST);

        BufferedReader  buffReader = new BufferedReader(
            new InputStreamReader(secureSocket.getInputStream()));
        String readin = null;
        while( (readin=buffReader.readLine()) != null ) {
            //System.out.println(readin);
        }
    }

    private static void initCiphers() {
        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        SSLServerSocketFactory sslssf = null;
        if( ssf instanceof SSLServerSocketFactory ) {
            sslssf = (SSLServerSocketFactory)ssf;
        } else {
            System.out.println(ssf.getClass().getName());
            System.exit(1);
        }
        DEFAULT_CIPHERS = sslssf.getDefaultCipherSuites();
        SUPPORTED_CIPHERS = sslssf.getSupportedCipherSuites();

        for(int i=0; i<DEFAULT_CIPHERS.length; i++) {
          verbose("Default cipher[" + i + "] "+DEFAULT_CIPHERS[i]);
        }
        for(int i=0; i<SUPPORTED_CIPHERS.length; i++) {
          verbose("Supported cipher[" + i + "] " + SUPPORTED_CIPHERS[i]);
        }

    }

}
