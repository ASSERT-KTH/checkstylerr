package com.piksel.sequoia.clientsdk.configuration;

import java.net.MalformedURLException;
import java.net.URL;

import com.piksel.sequoia.annotations.PublicEvolving;

import lombok.NonNull;
import lombok.Value;

/**
 * General host configuration consisting of a service, stage and domain.
 */
@Value
@PublicEvolving
public class HostConfiguration {

    /**
     * The service URL for the host.
     */
    @NonNull
    String url;

    public URL getBaseUrl() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}