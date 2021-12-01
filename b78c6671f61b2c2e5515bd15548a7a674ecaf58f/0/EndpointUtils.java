/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.foundation.common.net;

import static com.google.common.collect.ImmutableMap.of;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

/**
 * <pre>
 *   http://xxx  -> rest://xxx
 *   https://xxx -> rest://xxx?sslEnabled=true
 *
 *   h2c://xxx   -> rest://xxx?protocol=http2
 *   h2://xxx    -> rest://xxx?sslEnabled=true&protocol=http2
 *
 *   xxx         -> rest://xxx?protocol=http2
 *   other://xxx -> other://xxx
 * </pre>
 **/
public final class EndpointUtils {
  private static final String HTTP = "http";

  private static final String HTTPS = "https";

  private static final String H2C = "h2c";

  private static final String H2 = "h2";

  private static final String HTTP2 = "http2";

  private static final int DEFAULT_HTTP_PORT = 80;

  private static final int DEFAULT_HTTPS_PORT = 443;

  private static class SchemeMeta {
    String protocol;

    boolean ssl;

    int defaultPort;

    public SchemeMeta(String protocol, boolean ssl, int defaultPort) {
      this.protocol = protocol;
      this.ssl = ssl;
      this.defaultPort = defaultPort;
    }
  }

  private static final Map<String, SchemeMeta> SCHEME_META_MAP = of(
      HTTP, new SchemeMeta(null, false, DEFAULT_HTTP_PORT),
      HTTPS, new SchemeMeta(null, true, DEFAULT_HTTPS_PORT),
      H2C, new SchemeMeta(HTTP2, false, DEFAULT_HTTP_PORT),
      H2, new SchemeMeta(HTTP2, true, DEFAULT_HTTPS_PORT)
  );

  private EndpointUtils() {
  }

  public static String formatFromUri(String inputUri) {
    try {
      return doFormatFromUri(inputUri);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("failed to convert uri to endpoint.", e);
    }
  }

  private static String doFormatFromUri(String inputUri) throws URISyntaxException {
    URIBuilder builder = new URIBuilder(inputUri);
    if (builder.getScheme() == null) {
      builder.setScheme(H2C);
      builder.setHost(builder.getPath());
      builder.setPath(null);
    }

    SchemeMeta schemeMeta = SCHEME_META_MAP.get(builder.getScheme());
    if (schemeMeta == null) {
      return inputUri;
    }

    return format(builder, schemeMeta);
  }

  private static String format(URIBuilder builder, SchemeMeta schemeMeta) throws URISyntaxException {
    if (schemeMeta.ssl) {
      builder.addParameter("sslEnabled", "true");
    }
    if (schemeMeta.protocol != null) {
      builder.addParameter("protocol", schemeMeta.protocol);
    }
    if (builder.getPort() == -1) {
      builder.setPort(schemeMeta.defaultPort);
    }

    return builder.setScheme("rest").build().toString();
  }
}
