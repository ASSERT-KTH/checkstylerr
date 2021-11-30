package com.piksel.sequoia.clientsdk;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;

import javax.inject.Inject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseInterceptor;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.ObjectParser;
import com.piksel.sequoia.clientsdk.configuration.ClientConfiguration;
import com.piksel.sequoia.clientsdk.token.DataServicesCredentialProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures a {@link HttpRequest} with {@link Credential} allowing transparent token management to take place. Also
 * configures the JSON parser to be used with the request and response bodies.
 */
@Slf4j
class DefaultDataServicesRequestConfigurator implements DataServicesRequestConfigurator {

    private final JsonObjectParser jsonObjectParser;
    private final DataServicesCredentialProvider dataServicesCredentialProvider;
    private final ClientConfiguration clientConfig;
    private final UserAgentStringSupplier userAgentStringSupplier;

    @Inject
    public DefaultDataServicesRequestConfigurator(
            JsonObjectParser jsonObjectParser,
            DataServicesCredentialProvider dataServicesCredentialProvider,
            ClientConfiguration clientConfiguration,
            UserAgentStringSupplier userAgentStringSupplier) {
        this.jsonObjectParser = jsonObjectParser;
        this.dataServicesCredentialProvider = dataServicesCredentialProvider;
        this.clientConfig = clientConfiguration;
        this.userAgentStringSupplier = userAgentStringSupplier;
    }

    @Override
    public void configure(HttpRequest request) {
        withDataServicesCredentialProvider(request,
                dataServicesCredentialProvider);
        withParser(request, jsonObjectParser);
        withConfiguration(request, clientConfig);
    }

    private void withConfiguration(HttpRequest request,
            ClientConfiguration clientConfig) {
        request.setConnectTimeout(clientConfig.getConnectTimeOut());
        request.setReadTimeout(clientConfig.getReadTimeOut());
        request.getHeaders().setUserAgent(userAgentStringSupplier.get());
        request.setNumberOfRetries(clientConfig.getRecoveryStrategy().getNumberOfRetries());
        request.setResponseInterceptor(responseInterceptor(clientConfig.getHttpResponseInterceptorName()));
    }

    private HttpResponseInterceptor responseInterceptor(String className) {
        if (isNotEmpty(className)) {
            try {
                Object instance = Class.forName(className).newInstance();
                if (instance instanceof HttpResponseInterceptor) {
                    return (HttpResponseInterceptor) instance;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                log.error("The class [{}] couldn't be loaded", className);
                throw new RequestConfigurationException(e);
            }

            log.error("The class [{}] couldn't be loaded", className);
        }
        return null;
    }


    private void withDataServicesCredentialProvider(HttpRequest request,
            DataServicesCredentialProvider dataServicesCredentialProvider) {
        if (needsCredentials() && validCredentials(dataServicesCredentialProvider)) {
            try {
                dataServicesCredentialProvider.getCredential()
                        .initialize(request);
            } catch (IOException e) {
                throw aConfigurationException(request,
                        dataServicesCredentialProvider, e);
            }
        }
    }

    private boolean needsCredentials() {
        return clientConfig != null &&
                clientConfig.getIdentityComponentCredentials() != null;
    }

    private boolean validCredentials(DataServicesCredentialProvider dataServicesCredentialProvider) {
        return dataServicesCredentialProvider != null &&
                dataServicesCredentialProvider.getCredential() != null;
    }

    private void withParser(HttpRequest request, ObjectParser parser) {
        if (parser != null) {
            request.setParser(parser);
        }
    }

    private RequestConfigurationException aConfigurationException(
            HttpRequest request, Object configurer, Exception exception) {
        return new RequestConfigurationException(request, configurer,
                exception);
    }
}
