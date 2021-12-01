/*
 * Copyright (c) 2009-2018, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.admingui.services.impl;

import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;

import gov.hhs.fha.nhinc.admingui.services.PingService;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jassmit
 */
public class PingServiceImpl implements PingService {

    private static final Logger LOG = LoggerFactory.getLogger(PingServiceImpl.class);

    private static final String WSDL_SUFFIX = "?wsdl";
    private static final String LOG_WSDL_KEY = "logWsdlPing";
    private static final String MSG_EXCEPTION_TIMEOUT = "Connection timed out";

    private List<String> timeoutList = new ArrayList<>();

    @Override
    public int ping(String url) {
        int responseCode = HTTP_CLIENT_TIMEOUT;
        URL webserviceUrl = null;

        try {
            webserviceUrl = new URL(prepUrl(url));
            if (!timeoutList.contains(webserviceUrl.getHost())) {
                HttpsURLConnection.setDefaultHostnameVerifier(getHostNameVerifier());
                HttpURLConnection con = (HttpURLConnection) webserviceUrl.openConnection();
                responseCode = con.getResponseCode();
                logWsdl(readInputStreamFrom(con));
                con.disconnect();
            } else {
                LOG.warn("skip-known-timeout-server-host: {}", url);
            }
        } catch (IOException ex) {
            LOG.warn("Problem pinging endpoint: {}", ex.getLocalizedMessage());
            LOG.trace("Problem pinging endpoint: {}", ex.getLocalizedMessage(), ex);

            if (null != webserviceUrl && ex.getMessage().indexOf(MSG_EXCEPTION_TIMEOUT) > -1) {
                timeoutList.add(webserviceUrl.getHost());
                responseCode = HTTP_CLIENT_TIMEOUT;
            }
        }

        return responseCode;
    }

    private static String prepUrl(String serviceUrl) {
        if (!serviceUrl.endsWith(WSDL_SUFFIX)) {
            return serviceUrl.concat(WSDL_SUFFIX);
        }
        return serviceUrl;
    }

    private static HostnameVerifier getHostNameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession sslSession) {
                return hostname.equalsIgnoreCase(sslSession.getPeerHost());
            }
        };
    }

    protected static void logWsdl(String output) {
        try {
            PropertyAccessor propAccessor = PropertyAccessor.getInstance();
            String logOutputValue = propAccessor.getProperty(NhincConstants.ADAPTER_PROPERTY_FILE_NAME, LOG_WSDL_KEY);

            if (logOutputValue.equalsIgnoreCase("true") || logOutputValue.equalsIgnoreCase("t")) {
                LOG.info(output);
            }
        } catch (PropertyAccessException ex) {
            LOG.warn("Could not access properties: {}", ex.getLocalizedMessage());
            LOG.trace("Could not access properties: {}", ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void resetTimeoutList() {
        LOG.info("Timeout-servers-list is resetted");
        timeoutList = new ArrayList<>();
    }

    private static String readInputStreamFrom(HttpURLConnection urlConn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()))) {
            String inputLine;
            StringBuilder pingOutput = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                pingOutput.append(inputLine);
            }
            return pingOutput.toString();
        }
    }
}
