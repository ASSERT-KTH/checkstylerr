package com.intuit.tank.standalone.agent;

/*
 * #%L
 * Agent Standalone
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.intuit.tank.vm.agent.messages.StandaloneAgentRequest;
import com.intuit.tank.vm.api.enumerated.WatsAgentCommand;

public class CommandListener implements Container {

    private static Logger LOG = LogManager.getLogger(CommandListener.class);

    public static final int PORT = 1090;
    private static boolean started = false;

    private static StandaloneAgentStartup agentStarter;

    public synchronized static void startHttpServer(int port, StandaloneAgentStartup standaloneAgentStartup) {
        if (!started) {
            agentStarter = standaloneAgentStartup;
            try {
                Container container = new CommandListener();
                @SuppressWarnings("resource") Connection connection = new SocketConnection(container);
                SocketAddress address = new InetSocketAddress(port);
                System.out.println("Starting httpserver on port " + port);
                connection.connect(address);
                started = true;
            } catch (IOException e) {
                LOG.error("Error starting httpServer: " + e, e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void handle(Request req, Response response) {
        try {
            String msg = "unknown path";
            int code = 200;
            String path = req.getPath().getPath();
            if (path.equals(WatsAgentCommand.request.getPath())) {
                msg = "Requesting users ";
                StandaloneAgentRequest agentRequest = getRequest(req.getInputStream());
                if (agentRequest == null) {
                    msg = "Invalid StandaloneAgentRequest.";
                    code = 406;
                } else if (agentRequest.getJobId() != null && agentRequest.getUsers() > 0) {
                    // launch the harness with the specified details.
                    agentStarter.startTest(agentRequest);
                } else {
                    msg = "invalid request.";
                    code = 400;
                }
            }
            long time = System.currentTimeMillis();
            response.setCode(code);
            response.set("Content-Type", "text/plain");
            response.set("Server", "TAnk Agent/1.0");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            PrintStream body = response.getPrintStream();
            body.println(msg);
            body.close();
        } catch (Exception e) {
            LOG.error("error sending response");
            response.setCode(500);
        }
    }

    private StandaloneAgentRequest getRequest(InputStream inputStream) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(StandaloneAgentRequest.class.getPackage().getName());
            Object unmarshalled = ctx.createUnmarshaller().unmarshal(inputStream);
            return (StandaloneAgentRequest) unmarshalled;
        } catch (JAXBException e) {
            LOG.error("Error unmarshalling body.");
        }
        return null;
    }

}
