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

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.clientsdk.configuration.ClientConfiguration;
import com.piksel.sequoia.clientsdk.configuration.ClientConfigurationHostRegistry;
import com.piksel.sequoia.clientsdk.configuration.DefaultClientConfiguration;
import com.piksel.sequoia.clientsdk.configuration.UserAgentConfigurer;
import com.piksel.sequoia.clientsdk.recovery.RecoveryStrategy;
import com.piksel.sequoia.clientsdk.recovery.RequestRecoveryStrategyProvider;
import com.piksel.sequoia.clientsdk.registry.DefaultRegistryClient;
import com.piksel.sequoia.clientsdk.registry.RegistryClient;
import com.piksel.sequoia.clientsdk.registry.RegistryClientConfiguration;
import com.piksel.sequoia.clientsdk.request.RequestClient;
import com.piksel.sequoia.clientsdk.token.ClientGrantCredentialUnsuccessfulResponseHandler;
import com.piksel.sequoia.clientsdk.token.DataServicesClientGrantUnsuccessfulResponseHandler;
import com.piksel.sequoia.clientsdk.token.DataServicesCredentialProvider;
import com.piksel.sequoia.clientsdk.token.HttpClientAccessCredentialProvider;

/**
 * Defines the standard bindings to wire the various components of the client
 * infrastructure. Each binding can be customised by a sub-class.
 */
@Internal
public class DataServicesClientModule extends AbstractModule {

    private ClientConfiguration configuration;
    
    public DataServicesClientModule(ClientConfiguration config) {
        this.configuration = config;
    }

    @Override
    protected void configure() {
        bind(ClientConfiguration.class).toInstance(configuration);
        bind(DataServicesRequestConfigurator.class)
                .to(providesDataServiceRequestConfigurator())
                .asEagerSingleton();
        bind(RequestFactory.class).to(providesRequestFactory())
                .asEagerSingleton();
        bind(DataServicesCredentialProvider.class)
                .to(HttpClientAccessCredentialProvider.class)
                .asEagerSingleton();
        bind(HttpRequestInitializer.class).to(providesRequestInitializer())
                .asEagerSingleton();
        bind(RegistryClient.class).to(providesRegistryClient())
                .asEagerSingleton();
        bind(DataServicesCredentialProvider.class)
                .to(HttpClientAccessCredentialProvider.class)
                .asEagerSingleton();
        bind(RequestClient.class)
            .to(configuration.getRequestClientClass())
            .asEagerSingleton();
    }

    private Class<? extends RegistryClient> providesRegistryClient() {
        return DefaultRegistryClient.class;
    }

    protected Class<? extends HttpRequestInitializer> providesRequestInitializer() {
        return DataServicesRequestInitializer.class;
    }

    protected Class<? extends RequestFactory> providesRequestFactory() {
        return DataServicesRequestFactory.class;
    }

    protected Class<? extends DataServicesRequestConfigurator> providesDataServiceRequestConfigurator() {
        return DefaultDataServicesRequestConfigurator.class;
    }
    
    @Provides
    public UserAgentStringSupplier providesUserAgentStringSupplier() {
        UserAgentConfigurer configurer = configuration.getUserAgentConfigurer();
        return new UserAgentStringSupplier(configurer);
    }

    @Provides
    public RegistryClientConfiguration providesRegistryClientConfigurator() {
        return RegistryClientConfiguration.builder()
                .registryServiceOwner(configuration.getRegistryServiceOwner())
                .build();
    }

    @Provides
    public PreconfiguredHostRegistry providesHostRegistry() {
        return createHostRegistry();
    }

    protected PreconfiguredHostRegistry createHostRegistry() {
        return new ClientConfigurationHostRegistry(configuration);
    }

    @Provides
    public MessageConfiguration providesMessageConfiguration() {
        return createMessageConfiguration();
    }

    private MessageConfiguration createMessageConfiguration() {
        return configuration.getMessageConfiguration();
    }

    @Provides
    public JsonObjectParser providesJsonObjectParser() {
        return createJsonParser();
    }

    protected JsonObjectParser createJsonParser() {
        return new JsonObjectParser(GsonFactory.getDefaultInstance());
    }

    @Provides
    public ClientGrantCredentialUnsuccessfulResponseHandler provideUnsuccessfulResponseHandler() {
        return new DataServicesClientGrantUnsuccessfulResponseHandler(provideRequestRecoveryStrategy().getRecoveryStrategy().getBackOff());
    }

    @Provides
    public Gson provideGson() {
        return DefaultClientConfiguration.createGson(
                configuration.getGsonBuilder(),
                configuration.getTypeAdapters());
    }


    @Provides
    @SuppressWarnings("deprecation")
    public RequestRecoveryStrategyProvider provideRequestRecoveryStrategy() {
    
        return configuration.getRecoveryStrategy() != null ? 
                getRecoveryStrategyFromConfiguration() : () -> RecoveryStrategy.builder().backOff(configuration.getBackOffStrategy()).build();
     
    }

    @SuppressWarnings("deprecation")
    private RequestRecoveryStrategyProvider getRecoveryStrategyFromConfiguration() {
        if(configuration.getRecoveryStrategy().getBackOff() == null){
            // When backOff is null, it's using BackOffStrategy.
            return () -> RecoveryStrategy.builder().backOff(configuration.getBackOffStrategy()).
                    numberOfRetries(configuration.getRecoveryStrategy().getNumberOfRetries()).build();
        }
        return () -> configuration.getRecoveryStrategy();
    }

}
