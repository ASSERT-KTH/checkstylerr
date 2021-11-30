package org.openmrs.module.sync2.client;

import org.openmrs.module.fhir.api.client.Client;
import org.openmrs.module.fhir.api.client.FHIRClient;
import org.openmrs.module.sync2.client.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openmrs.module.sync2.SyncConstants.FHIR_CLIENT;
import static org.openmrs.module.sync2.SyncConstants.REST_CLIENT;

public class ClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);

    public Client createClient(final String clientType) {
        switch (clientType) {
            case REST_CLIENT:
                return new RestClient();
            case FHIR_CLIENT:
                return new FHIRClient();
            default:
                LOGGER.warn(String.format("Unrecognized clientType: %s", clientType));
                return null;
        }
    }
}
