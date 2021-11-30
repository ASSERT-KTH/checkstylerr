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

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.piksel.sequoia.annotations.PublicEvolving;
import lombok.Getter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * An error occurred during the request.
 */
@PublicEvolving
@Getter
public class RequestExecutionException extends ClientException {

    private static final String X_CORRELATION_ID_HEADER = "x-correlation-id";

    private static final long serialVersionUID = 1L;

    private final transient Optional<HttpRequest> request;

    /**
     * Provides the HTTP status code from the response.
     */
    private int statusCode;

    private String correlationId;

    public RequestExecutionException(HttpRequest request, Exception e) {
        super("Unable to complete request [" + Optional.ofNullable(request).map(HttpRequest::getRequestMethod).orElse("")
                + " " + Optional.ofNullable(request).map(HttpRequest::getUrl).map(Objects::toString).orElse("")
                + "] - exception [" + e + "] - correlationId [" + getCorrelationId(e) + "]", e);
        this.request = Optional.ofNullable(request);
        if (e instanceof HttpResponseException) {
            initAttributes((HttpResponseException) e);
        }
    }

    private static String getCorrelationId(Exception e) {
        if (e instanceof HttpResponseException) {
            return getCorrelationId((HttpResponseException) e);
        }
        return null;
    }

    public RequestExecutionException(IOException requestBuildException) {
        super("Unable to complete request - exception [" + requestBuildException + "] - correlationId [" + getCorrelationId(requestBuildException)
                + "]", requestBuildException);
        this.request = Optional.empty();
        if (requestBuildException instanceof HttpResponseException) {
            initAttributes((HttpResponseException) requestBuildException);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    private void initAttributes(HttpResponseException requestBuildException) {
        this.statusCode = requestBuildException.getStatusCode();
        this.correlationId = getCorrelationId(requestBuildException);
    }

    private static String getCorrelationId(HttpResponseException e) {
        if (Objects.nonNull(e.getHeaders()) && e.getHeaders().containsKey(X_CORRELATION_ID_HEADER)) {
            return e.getHeaders().get(X_CORRELATION_ID_HEADER).toString();
        }
        return null;
    }

}
