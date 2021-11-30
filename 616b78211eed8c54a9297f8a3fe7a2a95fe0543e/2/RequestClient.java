package com.piksel.sequoia.clientsdk.request;

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

import com.google.api.client.http.GenericUrl;
import com.google.gson.JsonElement;
import com.piksel.sequoia.clientsdk.Response;
import com.piksel.sequoia.clientsdk.resource.Reference;
import com.piksel.sequoia.clientsdk.resource.Resource;
import java.util.Map;

public interface RequestClient {

    /**
     *
     * Executes a GET request for the given url
     *
     * @param url is the url to do the request
     * @return Json with the response
     */
    Response<JsonElement> executeGetRequest(GenericUrl url);

    /**
     *
     * Executes a GET request for the given url and adding the given headers
     *
     * @param url is the url to do the request
     * @param headers are the headers needed to do the request
     * @return Json with the response
     */
    Response<JsonElement> executeGetRequest(GenericUrl url, Map<? extends String, ?> headers);

    /**
     * Executes a GET request and return a Serialized T type object
     *
     * @param url is the url to do the request
     * @param responseType the class type of the response object
     * @param <T> The generic type
     * @return A response serialized to the T type object.
     */
    <T> T executeGetRequest(GenericUrl url, Class<T> responseType);

    /**
     *
     * Executes a GET request
     *
     * @param url is the url to do the request
     * @param reference
     * @return
     */
    Response<JsonElement> executeGetRequest(GenericUrl url, Reference reference);

    /**
     *
     * Executes a GET request
     *
     * @param url is the url to do the request
     * @param references
     * @return
     */
    Response<JsonElement> executeGetRequest(GenericUrl url, Reference... references);

    /**
     * Executes a POST request for the given url using a payload and returning a serialized object of type T
     *
     * @param url is the url to do the request
     * @param payload the payload for the POST request
     * @param responseType tye class type of the response object
     * @param <T> the generic type for the response object
     * @param <K> the generic type for Payload
     * @return A response serialized to the T type object.
     */
    <T, K> Response<JsonElement> executePostRequest(GenericUrl url, K payload,
        Class<T> responseType);

    /**
     *
     * Executes a POST request for the given url, using a payload and adding the needed headers. It returns a serialized object of type T
     *
     * @param url is the url to do the request
     * @param payload the payload for the POST request
     * @param responseType tye class type of the response object
     * @param headers the needed headers for the requets
     * @param <T> the generic type for the response object
     * @param <K> the generic type for Payload
     * @return A response serialized to the T type object.
     */
    <T, K> Response<JsonElement> executePostRequest(GenericUrl url, K payload,
        Class<T> responseType, Map<? extends String, ?> headers);

    /**
     *
     * Executes a POST request
     *
     * @param url is the url to do the request
     * @param resourceKey
     * @param content
     * @param <T>
     * @return
     */
    <T extends Resource> Response<JsonElement> executePostRequest(GenericUrl url,
        String resourceKey, T... content);

    /**
     *
     * Executes a PUT request
     *
     * @param url is the url to do the request
     * @param resourceKey
     * @param content
     * @param reference
     * @param <T>
     * @return
     */
    <T extends Resource> Response<JsonElement> executePutRequest(GenericUrl url, String resourceKey,
        T content, Reference reference);

    /**
     *
     * Executesa DELETE reuqest
     *
     * @param url is the url to do the request
     * @param references
     * @return
     */
    Response<JsonElement> executeDeleteRequest(GenericUrl url, Reference... references);
}
