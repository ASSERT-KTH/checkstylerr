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
package org.apache.servicecomb.loadbalance;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.RetryHandler;

import io.vertx.core.VertxException;

@Component
public class DefaultRetryExtensionsFactory implements ExtensionsFactory {
  private static final Collection<String> ACCEPT_KEYS = Lists.newArrayList(
      Configuration.RETRY_HANDLER);

  private static final String RETRY_DEFAULT = "default";

  private static final Collection<String> ACCEPT_VALUES = Lists.newArrayList(
      RETRY_DEFAULT);

  private static final Map<Class<? extends Throwable>, List<String>> strictRetriable =
      ImmutableMap.<Class<? extends Throwable>, List<String>>builder()
          .put(ConnectException.class, Arrays.asList())
          .put(SocketTimeoutException.class, Arrays.asList())
          /*
           * deal with some special exceptions caused by the server side close the connection
           */
          .put(IOException.class, Arrays.asList(new String[] {"Connection reset by peer"}))
          .put(VertxException.class, Arrays.asList(new String[] {"Connection was closed"}))
          .put(NoRouteToHostException.class, Arrays.asList())
          .build();

  @Override
  public boolean isSupport(String key, String value) {
    return ACCEPT_KEYS.contains(key) && ACCEPT_VALUES.contains(value);
  }

  public RetryHandler createRetryHandler(String retryName, String microservice) {
    return new DefaultLoadBalancerRetryHandler(
        Configuration.INSTANCE.getRetrySameServer(microservice),
        Configuration.INSTANCE.getRetryNextServer(microservice), true) {

      @Override
      public boolean isRetriableException(Throwable e, boolean sameServer) {
        boolean retryable = isPresentAsCause(e);
        if (!retryable) {
          if (e instanceof InvocationException) {
            if (((InvocationException) e).getStatusCode() == 503) {
              return true;
            }
          }
        }
        return retryable;
      }

      public boolean isPresentAsCause(Throwable throwableToSearchIn) {
        int infiniteLoopPreventionCounter = 10;
        while (throwableToSearchIn != null && infiniteLoopPreventionCounter > 0) {
          infiniteLoopPreventionCounter--;
          for (Entry<Class<? extends Throwable>, List<String>> c : strictRetriable.entrySet()) {
            Class<? extends Throwable> key = c.getKey();
            if (key.isAssignableFrom(throwableToSearchIn.getClass())) {
              if (c.getValue() == null || c.getValue().isEmpty()) {
                return true;
              } else {
                String msg = throwableToSearchIn.getMessage();
                for (String val : c.getValue()) {
                  if (val.equals(msg)) {
                    return true;
                  }
                }
              }
            }
          }
          throwableToSearchIn = throwableToSearchIn.getCause();
        }
        return false;
      }
    };
  }
}
