package com.piksel.sequoia.clientsdk.configuration;

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
