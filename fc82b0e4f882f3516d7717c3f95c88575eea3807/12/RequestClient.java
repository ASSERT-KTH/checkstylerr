package com.piksel.sequoia.clientsdk.request;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.piksel.sequoia.annotations.PublicEvolving;
import com.piksel.sequoia.clientsdk.DefaultJsonElementResponse;
import com.piksel.sequoia.clientsdk.RequestExecutionException;
import com.piksel.sequoia.clientsdk.RequestFactory;
import com.piksel.sequoia.clientsdk.Response;
import com.piksel.sequoia.clientsdk.resource.Reference;
import com.piksel.sequoia.clientsdk.resource.Resource;
import com.piksel.sequoia.clientsdk.resource.ResourceCollection;
import com.piksel.sequoia.clientsdk.resource.json.JsonHttpContent;

import lombok.extern.slf4j.Slf4j;

/**
 * Central client shared between service endpoint clients for accessing data
 * services.
 */
@Slf4j
@PublicEvolving
public class RequestClient {

    private static final String APPLICATION_JSON = "application/json";
    private final RequestFactory requestFactory;
    private final HttpRequestInitializer requestInitializer;
    private static final JsonParser JSONPARSER = new JsonParser();
    private final Gson gson;

    @Inject
    public RequestClient(RequestFactory requestFactory,
            HttpRequestInitializer requestInitializer, Gson gson) {
        this.requestFactory = requestFactory;
        this.requestInitializer = requestInitializer;
        this.gson = gson;
    }

    protected RequestClient(RequestClient requestClient) {
        this.requestFactory = requestClient.requestFactory;
        this.requestInitializer = requestClient.requestInitializer;
        this.gson = requestClient.gson;
    }

    public Response<JsonElement> executeGetRequest(GenericUrl url) {
        log.debug("Performing client request to URL [{}] ", url.toString());
        HttpRequest request = createGetRequest(url);
        configureApplicationJsonHeaders(request);
        return executeRequest(request);
    }

    public Response<JsonElement> executeGetRequest(GenericUrl url,
            Map<? extends String, ?> headers) {
        log.debug("Performing client request to URL [{}] and Headers [{}]", url.toString(), headers);
        HttpRequest request = createGetRequest(url);
        request.getHeaders().putAll(headers);
        return executeRequest(request);
    }

    public <T> T executeGetRequest(GenericUrl url, Class<T> responseType) {
        log.debug(
                "Performing client request to URL [{}] with expected response type of [{}]",
                url.toString(), responseType);
        HttpRequest request = createHttpRequest(url);
        return executeRequest(responseType, request);
    }

    public Response<JsonElement> executeGetRequest(GenericUrl url, Reference reference) {
        url.appendRawPath("/" + reference.getOwner() + ":" + reference.getName());
        log.debug("Performing client request to URL [{}]", url.toString());
        HttpRequest request = createHttpRequest(url);
        return executeRequest(request);
    }

    public Response<JsonElement> executeGetRequest(GenericUrl url, Reference... references) {
        String refs = buildSeparatedReferences(references);
        url.appendRawPath("/" + refs);
        log.debug("Performing client request to URL [{}]", url.toString());
        HttpRequest request = createHttpRequest(url);
        return executeRequest(request);
    }

    public <T, K> Response<JsonElement> executePostRequest(GenericUrl url,
            K payload, Class<T> responseType) {
        log.debug(
                "Performing client request to URL [{}] with expected response type of [{}]",
                url.toString(), responseType);
        HttpRequest request = createPostRequest(url, payload);
        configureApplicationJsonHeaders(request);
        return executeRequest(request);
    }

    public <T, K> Response<JsonElement> executePostRequest(GenericUrl url,
            K payload, Class<T> responseType,
            Map<? extends String, ?> headers) {
        log.debug(
                "Performing client request to URL [{}] with expected response type of [{}]",
                url.toString(), responseType);
        HttpRequest request = createPostRequest(url, payload);
        request.getHeaders().putAll(headers);
        return executeRequest(request);
    }

    @SafeVarargs
    public final <T extends Resource> Response<JsonElement> executePostRequest(
            GenericUrl url, String resourceKey, T... content) {
        log.debug("Performing client request to URL [{}] ", url.toString());
        Map<String, ResourceCollection<T>> mapRequest = generateRequest(
                resourceKey, content);
        HttpRequest request = createPostRequest(url, mapRequest);
        configureApplicationJsonHeaders(request);
        return executeRequest(request);
    }

    private HttpRequest createHttpRequest(GenericUrl url) {
        HttpRequest request = createRequest(url);
        configureApplicationJsonHeaders(request);
        return request;
    }

    protected <T> T executeRequest(Class<T> responseType, HttpRequest request) {
        try {
            initializeRequest(request);
            T response = request.execute().parseAs(responseType);
            log.debug("Request client parsed response [{}]", response);
            return response;
        } catch (IOException requestExecutionException) {
            log.debug("Got response exception:", requestExecutionException);
            throw new RequestExecutionException(request,
                    requestExecutionException);
        }
    }

    protected void initializeRequest(HttpRequest request) throws IOException {
        requestInitializer.initialize(request);
    }

    protected Response<JsonElement> executeRequest(HttpRequest request) {
        try {
            initializeRequest(request);
            logRequest(request);
            HttpResponse httpResponse = request.execute();
            String jsonString = httpResponse.parseAsString();
            JsonElement jsonElement = JSONPARSER.parse(jsonString);
            log.debug("Request client parsed response [{}]", jsonElement);
            return DefaultJsonElementResponse.builder()
                    .payload(Optional.ofNullable(jsonElement))
                    .statusCode(httpResponse.getStatusCode())
                    .successStatusCode(httpResponse.isSuccessStatusCode())
                    .build();
        } catch (IOException requestExecutionException) {
            return manageException(request, requestExecutionException);
        }
    }

    protected HttpRequest createRequest(GenericUrl url) {
        try {
            return requestFactory.getRequestFactory().buildGetRequest(url);
        } catch (IOException requestBuildException) {
            throw new RequestExecutionException(requestBuildException);
        }
    }


    public final <T extends Resource> Response<JsonElement> executePutRequest(
            GenericUrl url, String resourceKey, T content,
            Reference reference) {
        log.debug("Performing client request to URL [{}] ", url.toString());
        url.appendRawPath("/" + reference.toString());
        Map<String, ResourceCollection<T>> mapRequest = generateRequest(resourceKey, content);
        HttpRequest request = createPutRequest(url, mapRequest);
        configureApplicationJsonHeaders(request);
        addIfMatch(request, content.getVersion());
        return executeRequest(request);
    }

    private void addIfMatch(HttpRequest request, String version) {
        request.getHeaders().setIfMatch("\"" + version + "\"");
        log.debug("Headers: " + request.getHeaders().toString());
    }

    @SafeVarargs
    public final Response<JsonElement> executeDeleteRequest(GenericUrl url,
            Reference... references) {
        String refs = buildSeparatedReferences(references);
        HttpRequest request = createDeleteRequest(url, refs);
        return executeRequest(request);
    }

    @SafeVarargs
    private final String buildSeparatedReferences(Reference... references) {
        return Arrays.stream(references).map(l -> l.toString())
                .collect(Collectors.joining(","));
    }

    private HttpRequest createDeleteRequest(GenericUrl url, String refs) {
        url.appendRawPath("/" + refs);
        try {
            return requestFactory.getRequestFactory().buildDeleteRequest(url);
        } catch (IOException requestBuildException) {
            throw new RequestExecutionException(requestBuildException);
        }
    }

    @SafeVarargs
    private final <T extends Resource> Map<String, ResourceCollection<T>> generateRequest(
            String resourceKey, T... content) {
        ResourceCollection<T> resourceCollection = new ResourceCollection<>();
        resourceCollection.addAll(Arrays.asList(content));
        Map<String, ResourceCollection<T>> mapRequest = new HashMap<>(
                resourceCollection.size());
        mapRequest.put(resourceKey, resourceCollection);
        return mapRequest;
    }

    private void configureApplicationJsonHeaders(HttpRequest request) {
        request.getHeaders().setContentType(APPLICATION_JSON);
        request.getHeaders().setAccept(APPLICATION_JSON);
    }

    private <T> HttpRequest createPostRequest(GenericUrl url,
            Map<String, T> content) {
        try {
            return requestFactory.getRequestFactory().buildPostRequest(url,
                    new JsonHttpContent(gson, content));

        } catch (IOException requestBuildException) {
            throw new RequestExecutionException(requestBuildException);
        }
    }

    private <T> HttpRequest createPostRequest(GenericUrl url, T content) {
        try {
            return requestFactory.getRequestFactory().buildPostRequest(url,
                    new JsonHttpContent(gson, content));

        } catch (IOException requestBuildException) {
            throw new RequestExecutionException(requestBuildException);
        }
    }

    private <T> HttpRequest createPutRequest(GenericUrl url,
            Map<String, T> content) {
        try {
            return requestFactory.getRequestFactory().buildPutRequest(url,
                    new JsonHttpContent(gson, content));

        } catch (IOException requestBuildException) {
            throw new RequestExecutionException(requestBuildException);
        }
    }

    private HttpRequest createGetRequest(GenericUrl url) {
        try {
            return requestFactory.getRequestFactory().buildGetRequest(url);
        } catch (IOException requestBuildException) {
            throw new RequestExecutionException(requestBuildException);
        }
    }

    private Response<JsonElement> manageException(HttpRequest request,
            IOException requestExecutionException) {
        if (requestExecutionException instanceof HttpResponseException) {
            if (((HttpResponseException) requestExecutionException)
                    .getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
                return DefaultJsonElementResponse.builder()
                        .payload(Optional.empty())
                        .statusCode(
                                ((HttpResponseException) requestExecutionException)
                                        .getStatusCode())
                        .successStatusCode(false).build();
            }
        }
        log.debug("Got response exception [{}]", requestExecutionException);
        throw new RequestExecutionException(request, requestExecutionException);
    }

    private void logRequest(HttpRequest request) throws IOException {
        if (request.getContent() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            request.getContent().writeTo(baos);
            log.debug("Request client json request [{}]", baos.toString(UTF_8.name()));
        }
    }

}
