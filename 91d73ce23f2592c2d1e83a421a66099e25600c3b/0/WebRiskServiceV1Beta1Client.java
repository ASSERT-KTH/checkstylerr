/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.webrisk.v1beta1;

import com.google.api.core.BetaApi;
import com.google.api.gax.core.BackgroundResource;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.webrisk.v1beta1.stub.WebRiskServiceV1Beta1Stub;
import com.google.cloud.webrisk.v1beta1.stub.WebRiskServiceV1Beta1StubSettings;
import com.google.protobuf.ByteString;
import com.google.webrisk.v1beta1.ComputeThreatListDiffRequest;
import com.google.webrisk.v1beta1.ComputeThreatListDiffResponse;
import com.google.webrisk.v1beta1.SearchHashesRequest;
import com.google.webrisk.v1beta1.SearchHashesResponse;
import com.google.webrisk.v1beta1.SearchUrisRequest;
import com.google.webrisk.v1beta1.SearchUrisResponse;
import com.google.webrisk.v1beta1.ThreatType;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Generated;

// AUTO-GENERATED DOCUMENTATION AND SERVICE
/**
 * Service Description: Web Risk v1beta1 API defines an interface to detect malicious URLs on your
 * website and in client applications.
 *
 * <p>This class provides the ability to make remote calls to the backing service through method
 * calls that map to API methods. Sample code to get started:
 *
 * <pre>
 * <code>
 * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
 *   ThreatType threatType = ThreatType.THREAT_TYPE_UNSPECIFIED;
 *   ByteString versionToken = ByteString.copyFromUtf8("");
 *   ComputeThreatListDiffRequest.Constraints constraints = ComputeThreatListDiffRequest.Constraints.newBuilder().build();
 *   ComputeThreatListDiffResponse response = webRiskServiceV1Beta1Client.computeThreatListDiff(threatType, versionToken, constraints);
 * }
 * </code>
 * </pre>
 *
 * <p>Note: close() needs to be called on the webRiskServiceV1Beta1Client object to clean up
 * resources such as threads. In the example above, try-with-resources is used, which automatically
 * calls close().
 *
 * <p>The surface of this class includes several types of Java methods for each of the API's
 * methods:
 *
 * <ol>
 *   <li>A "flattened" method. With this type of method, the fields of the request type have been
 *       converted into function parameters. It may be the case that not all fields are available as
 *       parameters, and not every API method will have a flattened method entry point.
 *   <li>A "request object" method. This type of method only takes one parameter, a request object,
 *       which must be constructed before the call. Not every API method will have a request object
 *       method.
 *   <li>A "callable" method. This type of method takes no parameters and returns an immutable API
 *       callable object, which can be used to initiate calls to the service.
 * </ol>
 *
 * <p>See the individual methods for example code.
 *
 * <p>Many parameters require resource names to be formatted in a particular way. To assist with
 * these names, this class includes a format method for each type of name, and additionally a parse
 * method to extract the individual identifiers contained within names that are returned.
 *
 * <p>This class can be customized by passing in a custom instance of WebRiskServiceV1Beta1Settings
 * to create(). For example:
 *
 * <p>To customize credentials:
 *
 * <pre>
 * <code>
 * WebRiskServiceV1Beta1Settings webRiskServiceV1Beta1Settings =
 *     WebRiskServiceV1Beta1Settings.newBuilder()
 *         .setCredentialsProvider(FixedCredentialsProvider.create(myCredentials))
 *         .build();
 * WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client =
 *     WebRiskServiceV1Beta1Client.create(webRiskServiceV1Beta1Settings);
 * </code>
 * </pre>
 *
 * To customize the endpoint:
 *
 * <pre>
 * <code>
 * WebRiskServiceV1Beta1Settings webRiskServiceV1Beta1Settings =
 *     WebRiskServiceV1Beta1Settings.newBuilder().setEndpoint(myEndpoint).build();
 * WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client =
 *     WebRiskServiceV1Beta1Client.create(webRiskServiceV1Beta1Settings);
 * </code>
 * </pre>
 */
@Generated("by gapic-generator")
@BetaApi
public class WebRiskServiceV1Beta1Client implements BackgroundResource {
  private final WebRiskServiceV1Beta1Settings settings;
  private final WebRiskServiceV1Beta1Stub stub;

  /** Constructs an instance of WebRiskServiceV1Beta1Client with default settings. */
  public static final WebRiskServiceV1Beta1Client create() throws IOException {
    return create(WebRiskServiceV1Beta1Settings.newBuilder().build());
  }

  /**
   * Constructs an instance of WebRiskServiceV1Beta1Client, using the given settings. The channels
   * are created based on the settings passed in, or defaults for any settings that are not set.
   */
  public static final WebRiskServiceV1Beta1Client create(WebRiskServiceV1Beta1Settings settings)
      throws IOException {
    return new WebRiskServiceV1Beta1Client(settings);
  }

  /**
   * Constructs an instance of WebRiskServiceV1Beta1Client, using the given stub for making calls.
   * This is for advanced usage - prefer to use WebRiskServiceV1Beta1Settings}.
   */
  @BetaApi("A restructuring of stub classes is planned, so this may break in the future")
  public static final WebRiskServiceV1Beta1Client create(WebRiskServiceV1Beta1Stub stub) {
    return new WebRiskServiceV1Beta1Client(stub);
  }

  /**
   * Constructs an instance of WebRiskServiceV1Beta1Client, using the given settings. This is
   * protected so that it is easy to make a subclass, but otherwise, the static factory methods
   * should be preferred.
   */
  protected WebRiskServiceV1Beta1Client(WebRiskServiceV1Beta1Settings settings) throws IOException {
    this.settings = settings;
    this.stub = ((WebRiskServiceV1Beta1StubSettings) settings.getStubSettings()).createStub();
  }

  @BetaApi("A restructuring of stub classes is planned, so this may break in the future")
  protected WebRiskServiceV1Beta1Client(WebRiskServiceV1Beta1Stub stub) {
    this.settings = null;
    this.stub = stub;
  }

  public final WebRiskServiceV1Beta1Settings getSettings() {
    return settings;
  }

  @BetaApi("A restructuring of stub classes is planned, so this may break in the future")
  public WebRiskServiceV1Beta1Stub getStub() {
    return stub;
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * Gets the most recent threat list diffs.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   ThreatType threatType = ThreatType.THREAT_TYPE_UNSPECIFIED;
   *   ByteString versionToken = ByteString.copyFromUtf8("");
   *   ComputeThreatListDiffRequest.Constraints constraints = ComputeThreatListDiffRequest.Constraints.newBuilder().build();
   *   ComputeThreatListDiffResponse response = webRiskServiceV1Beta1Client.computeThreatListDiff(threatType, versionToken, constraints);
   * }
   * </code></pre>
   *
   * @param threatType Required. The ThreatList to update.
   * @param versionToken The current version token of the client for the requested list (the client
   *     version that was received from the last successful diff).
   * @param constraints The constraints associated with this request.
   * @throws com.google.api.gax.rpc.ApiException if the remote call fails
   */
  public final ComputeThreatListDiffResponse computeThreatListDiff(
      ThreatType threatType,
      ByteString versionToken,
      ComputeThreatListDiffRequest.Constraints constraints) {

    ComputeThreatListDiffRequest request =
        ComputeThreatListDiffRequest.newBuilder()
            .setThreatType(threatType)
            .setVersionToken(versionToken)
            .setConstraints(constraints)
            .build();
    return computeThreatListDiff(request);
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * Gets the most recent threat list diffs.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   ThreatType threatType = ThreatType.THREAT_TYPE_UNSPECIFIED;
   *   ComputeThreatListDiffRequest.Constraints constraints = ComputeThreatListDiffRequest.Constraints.newBuilder().build();
   *   ComputeThreatListDiffRequest request = ComputeThreatListDiffRequest.newBuilder()
   *     .setThreatType(threatType)
   *     .setConstraints(constraints)
   *     .build();
   *   ComputeThreatListDiffResponse response = webRiskServiceV1Beta1Client.computeThreatListDiff(request);
   * }
   * </code></pre>
   *
   * @param request The request object containing all of the parameters for the API call.
   * @throws com.google.api.gax.rpc.ApiException if the remote call fails
   */
  public final ComputeThreatListDiffResponse computeThreatListDiff(
      ComputeThreatListDiffRequest request) {
    return computeThreatListDiffCallable().call(request);
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * Gets the most recent threat list diffs.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   ThreatType threatType = ThreatType.THREAT_TYPE_UNSPECIFIED;
   *   ComputeThreatListDiffRequest.Constraints constraints = ComputeThreatListDiffRequest.Constraints.newBuilder().build();
   *   ComputeThreatListDiffRequest request = ComputeThreatListDiffRequest.newBuilder()
   *     .setThreatType(threatType)
   *     .setConstraints(constraints)
   *     .build();
   *   ApiFuture&lt;ComputeThreatListDiffResponse&gt; future = webRiskServiceV1Beta1Client.computeThreatListDiffCallable().futureCall(request);
   *   // Do something
   *   ComputeThreatListDiffResponse response = future.get();
   * }
   * </code></pre>
   */
  public final UnaryCallable<ComputeThreatListDiffRequest, ComputeThreatListDiffResponse>
      computeThreatListDiffCallable() {
    return stub.computeThreatListDiffCallable();
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * This method is used to check whether a URI is on a given threatList.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   String uri = "";
   *   List&lt;ThreatType&gt; threatTypes = new ArrayList&lt;&gt;();
   *   SearchUrisResponse response = webRiskServiceV1Beta1Client.searchUris(uri, threatTypes);
   * }
   * </code></pre>
   *
   * @param uri The URI to be checked for matches.
   * @param threatTypes Required. The ThreatLists to search in.
   * @throws com.google.api.gax.rpc.ApiException if the remote call fails
   */
  public final SearchUrisResponse searchUris(String uri, List<ThreatType> threatTypes) {

    SearchUrisRequest request =
        SearchUrisRequest.newBuilder().setUri(uri).addAllThreatTypes(threatTypes).build();
    return searchUris(request);
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * This method is used to check whether a URI is on a given threatList.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   String uri = "";
   *   List&lt;ThreatType&gt; threatTypes = new ArrayList&lt;&gt;();
   *   SearchUrisRequest request = SearchUrisRequest.newBuilder()
   *     .setUri(uri)
   *     .addAllThreatTypes(threatTypes)
   *     .build();
   *   SearchUrisResponse response = webRiskServiceV1Beta1Client.searchUris(request);
   * }
   * </code></pre>
   *
   * @param request The request object containing all of the parameters for the API call.
   * @throws com.google.api.gax.rpc.ApiException if the remote call fails
   */
  public final SearchUrisResponse searchUris(SearchUrisRequest request) {
    return searchUrisCallable().call(request);
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * This method is used to check whether a URI is on a given threatList.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   String uri = "";
   *   List&lt;ThreatType&gt; threatTypes = new ArrayList&lt;&gt;();
   *   SearchUrisRequest request = SearchUrisRequest.newBuilder()
   *     .setUri(uri)
   *     .addAllThreatTypes(threatTypes)
   *     .build();
   *   ApiFuture&lt;SearchUrisResponse&gt; future = webRiskServiceV1Beta1Client.searchUrisCallable().futureCall(request);
   *   // Do something
   *   SearchUrisResponse response = future.get();
   * }
   * </code></pre>
   */
  public final UnaryCallable<SearchUrisRequest, SearchUrisResponse> searchUrisCallable() {
    return stub.searchUrisCallable();
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * Gets the full hashes that match the requested hash prefix. This is used after a hash prefix is
   * looked up in a threatList and there is a match. The client side threatList only holds partial
   * hashes so the client must query this method to determine if there is a full hash match of a
   * threat.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   ByteString hashPrefix = ByteString.copyFromUtf8("");
   *   List&lt;ThreatType&gt; threatTypes = new ArrayList&lt;&gt;();
   *   SearchHashesResponse response = webRiskServiceV1Beta1Client.searchHashes(hashPrefix, threatTypes);
   * }
   * </code></pre>
   *
   * @param hashPrefix A hash prefix, consisting of the most significant 4-32 bytes of a SHA256
   *     hash. For JSON requests, this field is base64-encoded.
   * @param threatTypes Required. The ThreatLists to search in.
   * @throws com.google.api.gax.rpc.ApiException if the remote call fails
   */
  public final SearchHashesResponse searchHashes(
      ByteString hashPrefix, List<ThreatType> threatTypes) {

    SearchHashesRequest request =
        SearchHashesRequest.newBuilder()
            .setHashPrefix(hashPrefix)
            .addAllThreatTypes(threatTypes)
            .build();
    return searchHashes(request);
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * Gets the full hashes that match the requested hash prefix. This is used after a hash prefix is
   * looked up in a threatList and there is a match. The client side threatList only holds partial
   * hashes so the client must query this method to determine if there is a full hash match of a
   * threat.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   List&lt;ThreatType&gt; threatTypes = new ArrayList&lt;&gt;();
   *   SearchHashesRequest request = SearchHashesRequest.newBuilder()
   *     .addAllThreatTypes(threatTypes)
   *     .build();
   *   SearchHashesResponse response = webRiskServiceV1Beta1Client.searchHashes(request);
   * }
   * </code></pre>
   *
   * @param request The request object containing all of the parameters for the API call.
   * @throws com.google.api.gax.rpc.ApiException if the remote call fails
   */
  public final SearchHashesResponse searchHashes(SearchHashesRequest request) {
    return searchHashesCallable().call(request);
  }

  // AUTO-GENERATED DOCUMENTATION AND METHOD
  /**
   * Gets the full hashes that match the requested hash prefix. This is used after a hash prefix is
   * looked up in a threatList and there is a match. The client side threatList only holds partial
   * hashes so the client must query this method to determine if there is a full hash match of a
   * threat.
   *
   * <p>Sample code:
   *
   * <pre><code>
   * try (WebRiskServiceV1Beta1Client webRiskServiceV1Beta1Client = WebRiskServiceV1Beta1Client.create()) {
   *   List&lt;ThreatType&gt; threatTypes = new ArrayList&lt;&gt;();
   *   SearchHashesRequest request = SearchHashesRequest.newBuilder()
   *     .addAllThreatTypes(threatTypes)
   *     .build();
   *   ApiFuture&lt;SearchHashesResponse&gt; future = webRiskServiceV1Beta1Client.searchHashesCallable().futureCall(request);
   *   // Do something
   *   SearchHashesResponse response = future.get();
   * }
   * </code></pre>
   */
  public final UnaryCallable<SearchHashesRequest, SearchHashesResponse> searchHashesCallable() {
    return stub.searchHashesCallable();
  }

  @Override
  public final void close() {
    stub.close();
  }

  @Override
  public void shutdown() {
    stub.shutdown();
  }

  @Override
  public boolean isShutdown() {
    return stub.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return stub.isTerminated();
  }

  @Override
  public void shutdownNow() {
    stub.shutdownNow();
  }

  @Override
  public boolean awaitTermination(long duration, TimeUnit unit) throws InterruptedException {
    return stub.awaitTermination(duration, unit);
  }
}
