/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2009  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package davmail.http;

import davmail.BundleMessage;
import davmail.Settings;
import davmail.exception.*;
import davmail.exchange.dav.ExchangeDavMethod;
import davmail.exchange.dav.ExchangeSearchMethod;
import davmail.ui.tray.DavGatewayTray;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.CopyMethod;
import org.apache.jackrabbit.webdav.client.methods.DavMethodBase;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create HttpClient instance according to DavGateway Settings
 */
public final class DavGatewayHttpClientFacade {
    static final Logger LOGGER = Logger.getLogger("davmail.http.DavGatewayHttpClientFacade");

    static final String IE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)";
    static final int MAX_REDIRECTS = 10;
    static final Object LOCK = new Object();
    private static boolean needNTLM;

    static final long ONE_MINUTE = 60000;

    static String WORKSTATION_NAME = "UNKNOWN";

    private static IdleConnectionTimeoutThread httpConnectionManagerThread;

    static {
        // workaround for TLS Renegotiation issue see http://java.sun.com/javase/javaseforbusiness/docs/TLSReadme.html    
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");

        DavGatewayHttpClientFacade.start();

        // register custom cookie policy
        CookiePolicy.registerCookieSpec("DavMailCookieSpec", DavMailCookieSpec.class);

        AuthPolicy.registerAuthScheme(AuthPolicy.BASIC, LenientBasicScheme.class);
        try {
            WORKSTATION_NAME = InetAddress.getLocalHost().getHostName();
        } catch (Throwable t) {
            // ignore
        }
    }


    private DavGatewayHttpClientFacade() {
    }

    /**
     * Create basic http client with default params.
     *
     * @return HttpClient instance
     */
    private static HttpClient getBaseInstance() {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, IE_USER_AGENT);
        httpClient.getParams().setParameter(HttpClientParams.MAX_REDIRECTS, MAX_REDIRECTS);
        httpClient.getParams().setCookiePolicy("DavMailCookieSpec");
        return httpClient;
    }

    /**
     * Create a configured HttpClient instance.
     *
     * @param url target url
     * @return httpClient
     * @throws DavMailException on error
     */
    public static HttpClient getInstance(String url) throws DavMailException {
        // create an HttpClient instance
        HttpClient httpClient = getBaseInstance();
        configureClient(httpClient, url);
        return httpClient;
    }

    /**
     * Set credentials on HttpClient instance.
     *
     * @param httpClient httpClient instance
     * @param userName   user name
     * @param password   user password
     */
    public static void setCredentials(HttpClient httpClient, String userName, String password) {
        // some Exchange servers redirect to a different host for freebusy, use wide auth scope
        AuthScope authScope = new AuthScope(null, -1);
        int backSlashIndex = userName.indexOf('\\');
        if (needNTLM && backSlashIndex >= 0) {
            // separate domain from username in credentials
            String domain = userName.substring(0, backSlashIndex);
            userName = userName.substring(backSlashIndex + 1);
            httpClient.getState().setCredentials(authScope, new NTCredentials(userName, password, WORKSTATION_NAME, domain));
        } else {
            httpClient.getState().setCredentials(authScope, new NTCredentials(userName, password, WORKSTATION_NAME, ""));
        }
    }

    /**
     * Set http client current host configuration.
     *
     * @param httpClient current Http client
     * @param url        target url
     * @throws DavMailException on error
     */
    public static void setClientHost(HttpClient httpClient, String url) throws DavMailException {
        try {
            HostConfiguration hostConfig = httpClient.getHostConfiguration();
            URI httpURI = new URI(url, true);
            hostConfig.setHost(httpURI);
        } catch (URIException e) {
            throw new DavMailException("LOG_INVALID_URL", url);
        }
    }

    protected static boolean isNoProxyFor(java.net.URI uri) {
        final String noProxyFor = Settings.getProperty("davmail.noProxyFor");
        if (noProxyFor != null) {
            final String urihost = uri.getHost().toLowerCase();
            final String[] domains = noProxyFor.toLowerCase().split(",\\s*");
            for (String domain : domains) {
                if (urihost.endsWith(domain)) {
                    return true; //break;
                }
            }
        }
        return false;
    }

    /**
     * Update http client configuration (proxy)
     *
     * @param httpClient current Http client
     * @param url        target url
     * @throws DavMailException on error
     */
    public static void configureClient(HttpClient httpClient, String url) throws DavMailException {
        setClientHost(httpClient, url);

        // force NTLM in direct EWS mode
        if (!needNTLM && url.toLowerCase().endsWith("/ews/exchange.asmx") && !Settings.getBooleanProperty("davmail.disableNTLM", false)) {
            needNTLM = true;
        }

        if (Settings.getBooleanProperty("davmail.enableKerberos", false)) {
            AuthPolicy.registerAuthScheme("Negotiate", SpNegoScheme.class);
            ArrayList<String> authPrefs = new ArrayList<String>();
            authPrefs.add("Negotiate");
            httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
        } else if (!needNTLM) {
            ArrayList<String> authPrefs = new ArrayList<String>();
            authPrefs.add(AuthPolicy.DIGEST);
            authPrefs.add(AuthPolicy.BASIC);
            // exclude NTLM authentication scheme
            httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
        }

        boolean enableProxy = Settings.getBooleanProperty("davmail.enableProxy");
        boolean useSystemProxies = Settings.getBooleanProperty("davmail.useSystemProxies", Boolean.FALSE);
        String proxyHost = null;
        int proxyPort = 0;
        String proxyUser = null;
        String proxyPassword = null;

        try {
            java.net.URI uri = new java.net.URI(url);
            if (isNoProxyFor(uri)) {
                LOGGER.debug("no proxy for " + uri.getHost());
            } else if (useSystemProxies) {
                // get proxy for url from system settings
                System.setProperty("java.net.useSystemProxies", "true");
                List<Proxy> proxyList = getProxyForURI(uri);
                if (!proxyList.isEmpty() && proxyList.get(0).address() != null) {
                    InetSocketAddress inetSocketAddress = (InetSocketAddress) proxyList.get(0).address();
                    proxyHost = inetSocketAddress.getHostName();
                    proxyPort = inetSocketAddress.getPort();

                    // we may still need authentication credentials
                    proxyUser = Settings.getProperty("davmail.proxyUser");
                    proxyPassword = Settings.getProperty("davmail.proxyPassword");
                }
            } else if (enableProxy) {
                proxyHost = Settings.getProperty("davmail.proxyHost");
                proxyPort = Settings.getIntProperty("davmail.proxyPort");
                proxyUser = Settings.getProperty("davmail.proxyUser");
                proxyPassword = Settings.getProperty("davmail.proxyPassword");
            }
        } catch (URISyntaxException e) {
            throw new DavMailException("LOG_INVALID_URL", url);
        }

        // configure proxy
        if (proxyHost != null && proxyHost.length() > 0) {
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
            if (proxyUser != null && proxyUser.length() > 0) {

                AuthScope authScope = new AuthScope(proxyHost, proxyPort, AuthScope.ANY_REALM);

                // detect ntlm authentication (windows domain name in user name)
                int backslashindex = proxyUser.indexOf('\\');
                if (backslashindex > 0) {
                    httpClient.getState().setProxyCredentials(authScope,
                            new NTCredentials(proxyUser.substring(backslashindex + 1),
                                    proxyPassword, WORKSTATION_NAME,
                                    proxyUser.substring(0, backslashindex)));
                } else {
                    httpClient.getState().setProxyCredentials(authScope,
                            new NTCredentials(proxyUser, proxyPassword, WORKSTATION_NAME, ""));
                }
            }
        }

    }

    /**
     * Retrieve Proxy Selector
     *
     * @param uri target uri
     * @return proxy selector
     */
    private static List<Proxy> getProxyForURI(java.net.URI uri) {
        LOGGER.debug("get Default proxy selector");
        ProxySelector proxySelector = ProxySelector.getDefault();
        LOGGER.debug("getProxyForURI(" + uri + ')');
        List<Proxy> proxies = proxySelector.select(uri);
        LOGGER.debug("got system proxies:" + proxies);
        return proxies;
    }


    /**
     * Get Http Status code for the given URL
     *
     * @param httpClient httpClient instance
     * @param url        url string
     * @return HttpStatus code
     */
    public static int getHttpStatus(HttpClient httpClient, String url) {
        int status = 0;
        HttpMethod testMethod = new GetMethod(url);
        testMethod.setDoAuthentication(false);
        try {
            status = httpClient.executeMethod(testMethod);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
            testMethod.releaseConnection();
        }
        return status;
    }

    /**
     * Check if status is a redirect (various 30x values).
     *
     * @param status Http status
     * @return true if status is a redirect
     */
    public static boolean isRedirect(int status) {
        return status == HttpStatus.SC_MOVED_PERMANENTLY
                || status == HttpStatus.SC_MOVED_TEMPORARILY
                || status == HttpStatus.SC_SEE_OTHER
                || status == HttpStatus.SC_TEMPORARY_REDIRECT;
    }

    /**
     * Execute given url, manually follow redirects.
     * Workaround for HttpClient bug (GET full URL over HTTPS and proxy)
     *
     * @param httpClient HttpClient instance
     * @param url        url string
     * @return executed method
     * @throws IOException on error
     */
    public static HttpMethod executeFollowRedirects(HttpClient httpClient, String url) throws IOException {
        HttpMethod method = new GetMethod(url);
        method.setFollowRedirects(false);
        return executeFollowRedirects(httpClient, method);
    }

    private static int checkNTLM(HttpClient httpClient, HttpMethod currentMethod) throws IOException {
        int status = currentMethod.getStatusCode();
        if ((status == HttpStatus.SC_UNAUTHORIZED || status == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED)
                && acceptsNTLMOnly(currentMethod) && !hasNTLMorNegotiate(httpClient)) {
            LOGGER.debug("Received " + status + " unauthorized at " + currentMethod.getURI() + ", retrying with NTLM");
            resetMethod(currentMethod);
            addNTLM(httpClient);
            status = httpClient.executeMethod(currentMethod);
        }
        return status;
    }

    /**
     * Checks if there is a Javascript redirect inside the page,
     * and returns it.
     * <p/>
     * A Javascript redirect is usually found on OTP pre-auth page,
     * when the pre-auth form is in a distinct page from the regular Exchange login one.
     *
     * @param method http method
     * @return the redirect URL if found, or null if no Javascript redirect has been found
     */
    private static String getJavascriptRedirectUrl(HttpMethod method) throws IOException {
        String responseBody = method.getResponseBodyAsString();
        String jsRedirectionUrl = null;
        if (responseBody.indexOf("javascript:go_url()") > 0) {
            // Create a pattern to match a javascript redirect url
            Pattern p = Pattern.compile("go_url\\(\\)[^{]+\\{[^l]+location.replace\\(\"(/[^\"]+)\"\\)");
            Matcher m = p.matcher(responseBody);
            if (m.find()) {
                // Javascript redirect found!
                jsRedirectionUrl = m.group(1);
            }
        }
        return jsRedirectionUrl;
    }


    private static String getLocationValue(HttpMethod method) throws URIException {
        String locationValue = null;
        Header location = method.getResponseHeader("Location");
        if (location != null && isRedirect(method.getStatusCode())) {
            locationValue = location.getValue();
            // Novell iChain workaround
            if (locationValue.indexOf('"') >= 0) {
                locationValue = URIUtil.encodePath(locationValue);
            }
            // workaround for invalid relative location
            if (locationValue.startsWith("./")) {
                locationValue = locationValue.substring(1);
            }
        }
        return locationValue;
    }

    /**
     * Execute method with httpClient, follow 30x redirects.
     *
     * @param httpClient Http client instance
     * @param method     Http method
     * @return last http method after redirects
     * @throws IOException on error
     */
    public static HttpMethod executeFollowRedirects(HttpClient httpClient, HttpMethod method) throws IOException {
        HttpMethod currentMethod = method;
        try {
            DavGatewayTray.debug(new BundleMessage("LOG_EXECUTE_FOLLOW_REDIRECTS", currentMethod.getURI()));
            httpClient.executeMethod(currentMethod);
            checkNTLM(httpClient, currentMethod);

            String locationValue = getLocationValue(currentMethod);
            // check javascript redirect (multiple authentication pages)
            if (locationValue == null) {
                locationValue = getJavascriptRedirectUrl(currentMethod);
            }

            int redirectCount = 0;
            while (redirectCount++ < 10
                    && locationValue != null) {
                currentMethod.releaseConnection();
                currentMethod = new GetMethod(locationValue);
                currentMethod.setFollowRedirects(false);
                DavGatewayTray.debug(new BundleMessage("LOG_EXECUTE_FOLLOW_REDIRECTS_COUNT", currentMethod.getURI(), redirectCount));
                httpClient.executeMethod(currentMethod);
                checkNTLM(httpClient, currentMethod);
                locationValue = getLocationValue(currentMethod);
            }
            if (locationValue != null) {
                currentMethod.releaseConnection();
                throw new HttpException("Maximum redirections reached");
            }
        } catch (IOException e) {
            currentMethod.releaseConnection();
            throw e;
        }
        // caller will need to release connection
        return currentMethod;
    }

    /**
     * Execute method with httpClient, do not follow 30x redirects.
     *
     * @param httpClient Http client instance
     * @param method     Http method
     * @return status
     * @throws IOException on error
     */
    public static int executeNoRedirect(HttpClient httpClient, HttpMethod method) throws IOException {
        int status;
        try {
            status = httpClient.executeMethod(method);
            // check NTLM
            if ((status == HttpStatus.SC_UNAUTHORIZED || status == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED)
                    && acceptsNTLMOnly(method) && !hasNTLMorNegotiate(httpClient)) {
                LOGGER.debug("Received " + status + " unauthorized at " + method.getURI() + ", retrying with NTLM");
                resetMethod(method);
                addNTLM(httpClient);
                status = httpClient.executeMethod(method);
            }
        } finally {
            method.releaseConnection();
        }
        // caller will need to release connection
        return status;
    }

    /**
     * Execute webdav search method.
     *
     * @param httpClient    http client instance
     * @param path          <i>encoded</i> searched folder path
     * @param searchRequest (SQL like) search request
     * @param maxCount      max item count
     * @return Responses enumeration
     * @throws IOException on error
     */
    public static MultiStatusResponse[] executeSearchMethod(HttpClient httpClient, String path, String searchRequest,
                                                            int maxCount) throws IOException {
        ExchangeSearchMethod searchMethod = new ExchangeSearchMethod(path, searchRequest);
        if (maxCount > 0) {
            searchMethod.addRequestHeader("Range", "rows=0-" + (maxCount - 1));
        }
        return executeMethod(httpClient, searchMethod);
    }

    /**
     * Execute webdav propfind method.
     *
     * @param httpClient http client instance
     * @param path       <i>encoded</i> searched folder path
     * @param depth      propfind request depth
     * @param properties propfind requested properties
     * @return Responses enumeration
     * @throws IOException on error
     */
    public static MultiStatusResponse[] executePropFindMethod(HttpClient httpClient, String path, int depth, DavPropertyNameSet properties) throws IOException {
        PropFindMethod propFindMethod = new PropFindMethod(path, properties, depth);
        return executeMethod(httpClient, propFindMethod);
    }

    /**
     * Execute a delete method on the given path with httpClient.
     *
     * @param httpClient Http client instance
     * @param path       Path to be deleted
     * @return http status
     * @throws IOException on error
     */
    public static int executeDeleteMethod(HttpClient httpClient, String path) throws IOException {
        DeleteMethod deleteMethod = new DeleteMethod(path);
        deleteMethod.setFollowRedirects(false);

        int status = executeHttpMethod(httpClient, deleteMethod);
        // do not throw error if already deleted
        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_NOT_FOUND) {
            throw DavGatewayHttpClientFacade.buildHttpException(deleteMethod);
        }
        return status;
    }

    /**
     * Execute webdav request.
     *
     * @param httpClient http client instance
     * @param method     webdav method
     * @return Responses enumeration
     * @throws IOException on error
     */
    public static MultiStatusResponse[] executeMethod(HttpClient httpClient, DavMethodBase method) throws IOException {
        MultiStatusResponse[] responses = null;
        try {
            int status = httpClient.executeMethod(method);

            // need to follow redirects (once) on public folders
            if (isRedirect(status)) {
                method.releaseConnection();
                URI targetUri = new URI(method.getResponseHeader("Location").getValue(), true);
                checkExpiredSession(targetUri.getQuery());
                method.setURI(targetUri);
                status = httpClient.executeMethod(method);
            }

            if (status != HttpStatus.SC_MULTI_STATUS) {
                throw buildHttpException(method);
            }
            responses = method.getResponseBodyAsMultiStatus().getResponses();

        } catch (DavException e) {
            throw new IOException(e.getMessage());
        } finally {
            method.releaseConnection();
        }
        return responses;
    }

    /**
     * Execute webdav request.
     *
     * @param httpClient http client instance
     * @param method     webdav method
     * @return Responses enumeration
     * @throws IOException on error
     */
    public static MultiStatusResponse[] executeMethod(HttpClient httpClient, ExchangeDavMethod method) throws IOException {
        MultiStatusResponse[] responses = null;
        try {
            int status = httpClient.executeMethod(method);

            // need to follow redirects (once) on public folders
            if (isRedirect(status)) {
                method.releaseConnection();
                URI targetUri = new URI(method.getResponseHeader("Location").getValue(), true);
                checkExpiredSession(targetUri.getQuery());
                method.setURI(targetUri);
                status = httpClient.executeMethod(method);
            }

            if (status != HttpStatus.SC_MULTI_STATUS) {
                throw buildHttpException(method);
            }
            responses = method.getResponses();

        } finally {
            method.releaseConnection();
        }
        return responses;
    }

    /**
     * Execute method with httpClient.
     *
     * @param httpClient Http client instance
     * @param method     Http method
     * @return Http status
     * @throws IOException on error
     */
    public static int executeHttpMethod(HttpClient httpClient, HttpMethod method) throws IOException {
        int status = 0;
        try {
            status = httpClient.executeMethod(method);
        } finally {
            method.releaseConnection();
        }
        return status;
    }

    /**
     * Test if NTLM auth scheme is enabled.
     *
     * @param httpClient HttpClient instance
     * @return true if NTLM is enabled
     */
    public static boolean hasNTLMorNegotiate(HttpClient httpClient) {
        Object authPrefs = httpClient.getParams().getParameter(AuthPolicy.AUTH_SCHEME_PRIORITY);
        return authPrefs == null || (authPrefs instanceof List<?> &&
                (((Collection) authPrefs).contains(AuthPolicy.NTLM) || ((Collection) authPrefs).contains("Negotiate")));
    }

    /**
     * Enable NTLM authentication on http client
     *
     * @param httpClient HttpClient instance
     */
    public static void addNTLM(HttpClient httpClient) {
        // disable preemptive authentication
        httpClient.getParams().setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, false);

        // register the jcifs based NTLMv2 implementation
        AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, NTLMv2Scheme.class);

        ArrayList<String> authPrefs = new ArrayList<String>();
        authPrefs.add(AuthPolicy.NTLM);
        authPrefs.add(AuthPolicy.DIGEST);
        authPrefs.add(AuthPolicy.BASIC);
        httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

        // make sure NTLM is always active
        needNTLM = true;

        // separate domain from username in credentials
        AuthScope authScope = new AuthScope(null, -1);
        NTCredentials credentials = (NTCredentials) httpClient.getState().getCredentials(authScope);
        setCredentials(httpClient, credentials.getUserName(), credentials.getPassword());
    }

    /**
     * Test method header for supported authentication mode,
     * return true if Basic authentication is not available
     *
     * @param getMethod http method
     * @return true if only NTLM is enabled
     */
    public static boolean acceptsNTLMOnly(HttpMethod getMethod) {
        Header authenticateHeader = null;
        if (getMethod.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            authenticateHeader = getMethod.getResponseHeader("WWW-Authenticate");
        } else if (getMethod.getStatusCode() == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
            authenticateHeader = getMethod.getResponseHeader("Proxy-Authenticate");
        }
        if (authenticateHeader == null) {
            return false;
        } else {
            boolean acceptBasic = false;
            boolean acceptNTLM = false;
            HeaderElement[] headerElements = authenticateHeader.getElements();
            for (HeaderElement headerElement : headerElements) {
                if ("NTLM".equalsIgnoreCase(headerElement.getName())) {
                    acceptNTLM = true;
                }
                if ("Basic realm".equalsIgnoreCase(headerElement.getName())) {
                    acceptBasic = true;
                }
            }
            return acceptNTLM && !acceptBasic;

        }
    }

    /**
     * Execute test method from checkConfig, with proxy credentials, but without Exchange credentials.
     *
     * @param httpClient Http client instance
     * @param method     Http method
     * @return Http status
     * @throws IOException on error
     */
    public static int executeTestMethod(HttpClient httpClient, GetMethod method) throws IOException {
        // do not follow redirects in expired sessions
        method.setFollowRedirects(false);
        int status = httpClient.executeMethod(method);
        if (status == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED
                && acceptsNTLMOnly(method) && !hasNTLMorNegotiate(httpClient)) {
            resetMethod(method);
            LOGGER.debug("Received " + status + " unauthorized at " + method.getURI() + ", retrying with NTLM");
            addNTLM(httpClient);
            status = httpClient.executeMethod(method);
        }

        return status;
    }

    /**
     * Execute Get method, do not follow redirects.
     *
     * @param httpClient      Http client instance
     * @param method          Http method
     * @param followRedirects Follow redirects flag
     * @throws IOException on error
     */
    public static void executeGetMethod(HttpClient httpClient, GetMethod method, boolean followRedirects) throws IOException {
        // do not follow redirects in expired sessions
        method.setFollowRedirects(followRedirects);
        int status = httpClient.executeMethod(method);
        if ((status == HttpStatus.SC_UNAUTHORIZED || status == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED)
                && acceptsNTLMOnly(method) && !hasNTLMorNegotiate(httpClient)) {
            resetMethod(method);
            LOGGER.debug("Received " + status + " unauthorized at " + method.getURI() + ", retrying with NTLM");
            addNTLM(httpClient);
            status = httpClient.executeMethod(method);
        }
        if (status != HttpStatus.SC_OK && (followRedirects || !isRedirect(status))) {
            LOGGER.warn("GET failed with status " + status + " at " + method.getURI());
            if (status != HttpStatus.SC_NOT_FOUND && status != HttpStatus.SC_FORBIDDEN) {
                LOGGER.warn(method.getResponseBodyAsString());
            }
            throw DavGatewayHttpClientFacade.buildHttpException(method);
        }
        // check for expired session
        if (followRedirects) {
            String queryString = method.getQueryString();
            checkExpiredSession(queryString);
        }
    }

    private static void resetMethod(HttpMethod method) {
        // reset method state
        method.releaseConnection();
        method.getHostAuthState().invalidate();
        method.getProxyAuthState().invalidate();
    }

    private static void checkExpiredSession(String queryString) throws DavMailAuthenticationException {
        if (queryString != null && (queryString.contains("reason=2") || queryString.contains("reason=0"))) {
            LOGGER.warn("Request failed, session expired");
            throw new DavMailAuthenticationException("EXCEPTION_SESSION_EXPIRED");
        }
    }

    /**
     * Build Http Exception from methode status
     *
     * @param method Http Method
     * @return Http Exception
     */
    public static HttpException buildHttpException(HttpMethod method) {
        int status = method.getStatusCode();
        StringBuilder message = new StringBuilder();
        message.append(status).append(' ').append(method.getStatusText());
        try {
            message.append(" at ").append(method.getURI().getURI());
            if (method instanceof CopyMethod || method instanceof MoveMethod) {
                message.append(" to ").append(method.getRequestHeader("Destination"));
            }
        } catch (URIException e) {
            message.append(method.getPath());
        }
        // 440 means forbidden on Exchange
        if (status == 440) {
            return new LoginTimeoutException(message.toString());
        } else if (status == HttpStatus.SC_FORBIDDEN) {
            return new HttpForbiddenException(message.toString());
        } else if (status == HttpStatus.SC_NOT_FOUND) {
            return new HttpNotFoundException(message.toString());
        } else if (status == HttpStatus.SC_PRECONDITION_FAILED) {
            return new HttpPreconditionFailedException(message.toString());
        } else if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            return new HttpServerErrorException(message.toString());
        } else {
            return new HttpException(message.toString());
        }
    }

    /**
     * Test if the method response is gzip encoded
     *
     * @param method http method
     * @return true if response is gzip encoded
     */
    public static boolean isGzipEncoded(HttpMethod method) {
        Header[] contentEncodingHeaders = method.getResponseHeaders("Content-Encoding");
        if (contentEncodingHeaders != null) {
            for (Header header : contentEncodingHeaders) {
                if ("gzip".equals(header.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Stop HttpConnectionManager.
     */
    public static void stop() {
        synchronized (LOCK) {
            if (httpConnectionManagerThread != null) {
                httpConnectionManagerThread.interrupt();
                httpConnectionManagerThread = null;
            }
            MultiThreadedHttpConnectionManager.shutdownAll();
        }
    }

    /**
     * Create and set connection pool.
     *
     * @param httpClient httpClient instance
     */
    public static void createMultiThreadedHttpConnectionManager(HttpClient httpClient) {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(Settings.getIntProperty("davmail.exchange.maxConnections",100));
        connectionManager.getParams().setConnectionTimeout(10000);
        connectionManager.getParams().setSoTimeout(120000);
        synchronized (LOCK) {
            httpConnectionManagerThread.addConnectionManager(connectionManager);
        }
        httpClient.setHttpConnectionManager(connectionManager);
    }

    /**
     * Create and start a new HttpConnectionManager, close idle connections every minute.
     */
    public static void start() {
        synchronized (LOCK) {
            if (httpConnectionManagerThread == null) {
                httpConnectionManagerThread = new IdleConnectionTimeoutThread();
                httpConnectionManagerThread.setName(IdleConnectionTimeoutThread.class.getSimpleName());
                httpConnectionManagerThread.setConnectionTimeout(ONE_MINUTE);
                httpConnectionManagerThread.setTimeoutInterval(ONE_MINUTE);
                httpConnectionManagerThread.start();
            }
        }
    }
}
