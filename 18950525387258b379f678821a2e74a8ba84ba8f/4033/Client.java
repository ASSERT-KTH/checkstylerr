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

package client;

import jakarta.xml.ws.WebServiceRef;

import com.example.hello.Hello�Service;
import com.example.hello.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/mbyte/Hello�Service?WSDL")
        static Hello�Service service;

        public static void main(String[] args) {
            stat.addDescription("service-with-mbyte-char");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("service-with-mbyte-char");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloPort();
                String ret = port.sayHello("Appserver Tester !" + args[0]);
                if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
                }
                if(ret.indexOf(args[0]) == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
                }
                System.out.println(ret);
                ret = port.sayDoubleHello("Appserver Tester !" + args[0]);
                if(ret.indexOf("WebSvcTest-Double-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
                }
                if(ret.indexOf(args[0]) == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
                }
                System.out.println(ret);
                stat.addStatus(args[0], stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
       }
}

