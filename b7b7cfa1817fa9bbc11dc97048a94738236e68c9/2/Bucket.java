/**
 * Copyright (C) 2014 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.client.java;

import com.couchbase.client.core.BackpressureException;
import com.couchbase.client.core.ClusterFacade;
import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.core.RequestCancelledException;
import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.kv.MutationToken;
import com.couchbase.client.core.message.kv.subdoc.multi.Lookup;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.BinaryDocument;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.LegacyDocument;
import com.couchbase.client.java.document.StringDocument;
import com.couchbase.client.java.document.subdoc.DocumentFragment;
import com.couchbase.client.java.document.subdoc.ExtendDirection;
import com.couchbase.client.java.document.subdoc.LookupResult;
import com.couchbase.client.java.document.subdoc.LookupSpec;
import com.couchbase.client.java.document.subdoc.MultiLookupResult;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.error.CASMismatchException;
import com.couchbase.client.java.error.CouchbaseOutOfMemoryException;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.DurabilityException;
import com.couchbase.client.java.error.RequestTooBigException;
import com.couchbase.client.java.error.TemporaryFailureException;
import com.couchbase.client.java.error.TemporaryLockFailureException;
import com.couchbase.client.java.error.TranscodingException;
import com.couchbase.client.java.error.ViewDoesNotExistException;
import com.couchbase.client.java.error.subdoc.CannotInsertValueException;
import com.couchbase.client.java.error.subdoc.DeltaTooBigException;
import com.couchbase.client.java.error.subdoc.DocumentNotJsonException;
import com.couchbase.client.java.error.subdoc.NumberTooBigException;
import com.couchbase.client.java.error.subdoc.PathExistsException;
import com.couchbase.client.java.error.subdoc.PathInvalidException;
import com.couchbase.client.java.error.subdoc.PathMismatchException;
import com.couchbase.client.java.error.subdoc.PathNotFoundException;
import com.couchbase.client.java.error.subdoc.PathTooDeepException;
import com.couchbase.client.java.error.subdoc.SubDocumentException;
import com.couchbase.client.java.error.subdoc.ZeroDeltaException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.repository.Repository;
import com.couchbase.client.java.search.SearchQueryResult;
import com.couchbase.client.java.search.query.SearchQuery;
import com.couchbase.client.java.transcoder.Transcoder;
import com.couchbase.client.java.view.SpatialViewQuery;
import com.couchbase.client.java.view.SpatialViewResult;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import rx.Observable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Defines operations that can be executed synchronously against a Couchbase Server bucket.
 *
 * Note that only a subset of the provided operations are available for "memcached" type buckets. Also, some other
 * operations are only available against specific versions of Couchbase Server.
 *
 * Default timeouts are always applied and can be configured through the {@link CouchbaseEnvironment}. Overloads
 * are also available to change them on a per-call basis.
 *
 * @author Michael Nitschinger
 * @since 2.0
 */
@InterfaceStability.Committed
@InterfaceAudience.Public
public interface Bucket {

    /**
     * Provides access to the underlying asynchronous bucket interface.
     *
     * @return the asynchronous bucket.
     */
    AsyncBucket async();

    /**
     * Returns the underlying "core-io" library through its {@link ClusterFacade}.
     *
     * Handle with care, with great power comes great responsibility. All additional checks which are normally performed
     * by this library are skipped.
     *
     * @return the underlying {@link ClusterFacade} from the "core-io" package.
     */
    ClusterFacade core();

    /**
     * The {@link CouchbaseEnvironment} used.
     *
     * @return the CouchbaseEnvironment.
     */
    CouchbaseEnvironment environment();

    /**
     * The name of the {@link Bucket}.
     *
     * @return the name of the bucket.
     */
    String name();

    /**
     * Retrieves a {@link JsonDocument} by its unique ID with the default key/value timeout.
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, null is returned.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the unique ID of the document.
     * @return the found {@link JsonDocument} or null if not found.
     */
    JsonDocument get(String id);

    /**
     * Retrieves a {@link JsonDocument} by its unique ID with a custom timeout.
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, null is returned.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the unique ID of the document.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link JsonDocument} or null if not found.
     */
    JsonDocument get(String id, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves any type of {@link Document} with the default key/value timeout.
     *
     * The document ID is taken out of the {@link Document} provided, as well as the target type to return. Note that
     * not the same document is returned, but rather a new one of the same type with the freshly loaded properties.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the source document from which the ID is taken and the type is inferred.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D get(D document);

    /**
     * Retrieves any type of {@link Document} with a custom timeout.
     *
     * The document ID is taken out of the {@link Document} provided, as well as the target type to return. Note that
     * not the same document is returned, but rather a new one of the same type with the freshly loaded properties.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the source document from which the ID is taken and the type is inferred.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D get(D document, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves any type of {@link Document} by its ID with the default key/value timeout.
     *
     * The document ID is taken out of the {@link Document} provided, as well as the target type to return. Note that
     * not the same document is returned, but rather a new one of the same type with the freshly loaded properties.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param target the target document type to use.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D get(String id, Class<D> target);

    /**
     * Retrieves any type of {@link Document} by its ID with a custom timeout.
     *
     * The document ID is taken out of the {@link Document} provided, as well as the target type to return. Note that
     * not the same document is returned, but rather a new one of the same type with the freshly loaded properties.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D get(String id, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Check whether a document with the given ID does exist in the bucket.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @return true if it exists, false otherwise.
     */
    boolean exists(String id);

    /**
     * Check whether a document with the given ID does exist in the bucket.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return true if it exists, false otherwise.
     */
    boolean exists(String id, long timeout, TimeUnit timeUnit);

    /**
     * Check whether a document with the given ID does exist in the bucket.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document where the ID is extracted from.
     * @return true if it exists, false otherwise.
     */
    <D extends Document<?>> boolean exists(D document);

    /**
     * Check whether a document with the given ID does exist in the bucket.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document where the ID is extracted from.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return true if it exists, false otherwise.
     */
    <D extends Document<?>> boolean exists(D document, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with the
     * default timeout.
     *
     * Depending on the {@link ReplicaMode} selected, there can be none to four {@link JsonDocument} be returned
     * from the {@link Observable}. If {@link ReplicaMode#FIRST}, {@link ReplicaMode#SECOND} or
     * {@link ReplicaMode#THIRD} are selected zero or one documents are returned, if {@link ReplicaMode#ALL} is used,
     * all configured replicas plus the master node may return a document.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id id the unique ID of the document.
     * @param type the {@link ReplicaMode} to select.
     * @return a List containing zero to N {@link JsonDocument}s.
     */
    List<JsonDocument> getFromReplica(String id, ReplicaMode type);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with the
     * default timeout.
     *
     * This method has the {@link ReplicaMode#ALL} preselected. If you are only interested in the first
     * (or just some) values, you can iterate and then break out of the iterator loop. Documents
     * are pushed into the iterator as they arrive, which distinguishes this method from the {@link List}
     * equivalents which wait until all responses arrive.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id the unique ID of the document.
     * @return the Iterator containing Documents as they arrive.
     */
    Iterator<JsonDocument> getFromReplica(String id);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with a
     * custom timeout.
     *
     * Depending on the {@link ReplicaMode} selected, there can be none to four {@link JsonDocument} be returned
     * from the {@link Observable}. If {@link ReplicaMode#FIRST}, {@link ReplicaMode#SECOND} or
     * {@link ReplicaMode#THIRD} are selected zero or one documents are returned, if {@link ReplicaMode#ALL} is used,
     * all configured replicas plus the master node may return a document.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id id the unique ID of the document.
     * @param type the {@link ReplicaMode} to select.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a List containing zero to N {@link JsonDocument}s.
     */
    List<JsonDocument> getFromReplica(String id, ReplicaMode type, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with a
     * custom timeout.
     *
     * This method has the {@link ReplicaMode#ALL} preselected. If you are only interested in the first
     * (or just some) values, you can iterate and then break out of the iterator loop. Documents
     * are pushed into the iterator as they arrive, which distinguishes this method from the {@link List}
     * equivalents which wait until all responses arrive.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id the unique ID of the document.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the Iterator containing Documents as they arrive.
     */
    Iterator<JsonDocument> getFromReplica(String id, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with the
     * default timeout.
     *
     * Depending on the {@link ReplicaMode} selected, there can be none to four {@link JsonDocument} be returned
     * from the {@link Observable}. If {@link ReplicaMode#FIRST}, {@link ReplicaMode#SECOND} or
     * {@link ReplicaMode#THIRD} are selected zero or one documents are returned, if {@link ReplicaMode#ALL} is used,
     * all configured replicas plus the master node may return a document.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param document the document to extract the ID from.
     * @param type the {@link ReplicaMode} to select.
     * @return a List containing zero to N {@link JsonDocument}s.
     */
    <D extends Document<?>> List<D> getFromReplica(D document, ReplicaMode type);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with the
     * default timeout.
     *
     * This method has the {@link ReplicaMode#ALL} preselected. If you are only interested in the first
     * (or just some) values, you can iterate and then break out of the iterator loop. Documents
     * are pushed into the iterator as they arrive, which distinguishes this method from the {@link List}
     * equivalents which wait until all responses arrive.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param document the document to extract the ID from.
     * @return the Iterator containing Documents as they arrive.
     */
    <D extends Document<?>> Iterator<D> getFromReplica(D document);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with a
     * custom timeout.
     *
     * Depending on the {@link ReplicaMode} selected, there can be none to four {@link JsonDocument} be returned
     * from the {@link Observable}. If {@link ReplicaMode#FIRST}, {@link ReplicaMode#SECOND} or
     * {@link ReplicaMode#THIRD} are selected zero or one documents are returned, if {@link ReplicaMode#ALL} is used,
     * all configured replicas plus the master node may return a document.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param document the document to extract the ID from.
     * @param type the {@link ReplicaMode} to select.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a List containing zero to N {@link JsonDocument}s.
     */
    <D extends Document<?>> List<D> getFromReplica(D document, ReplicaMode type, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with a
     * custom timeout.
     *
     * This method has the {@link ReplicaMode#ALL} preselected. If you are only interested in the first
     * (or just some) values, you can iterate and then break out of the iterator loop. Documents
     * are pushed into the iterator as they arrive, which distinguishes this method from the {@link List}
     * equivalents which wait until all responses arrive.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param document the document to extract the ID from.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the Iterator containing Documents as they arrive.
     */
    <D extends Document<?>> Iterator<D> getFromReplica(D document, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with the
     * default timeout.
     *
     * Depending on the {@link ReplicaMode} selected, there can be none to four {@link JsonDocument} be returned
     * from the {@link Observable}. If {@link ReplicaMode#FIRST}, {@link ReplicaMode#SECOND} or
     * {@link ReplicaMode#THIRD} are selected zero or one documents are returned, if {@link ReplicaMode#ALL} is used,
     * all configured replicas plus the master node may return a document.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id the id of the document.
     * @param type the {@link ReplicaMode} to select.
     * @return a List containing zero to N {@link JsonDocument}s.
     */
    <D extends Document<?>> List<D> getFromReplica(String id, ReplicaMode type, Class<D> target);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with the
     * default timeout.
     *
     * This method has the {@link ReplicaMode#ALL} preselected. If you are only interested in the first
     * (or just some) values, you can iterate and then break out of the iterator loop. Documents
     * are pushed into the iterator as they arrive, which distinguishes this method from the {@link List}
     * equivalents which wait until all responses arrive.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id the unique ID of the document.
     * @param target the target document type to use.
     * @return the Iterator containing Documents as they arrive.
     */
    <D extends Document<?>> Iterator<D> getFromReplica(String id, Class<D> target);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with a
     * custom timeout.
     *
     * Depending on the {@link ReplicaMode} selected, there can be none to four {@link JsonDocument} be returned
     * from the {@link Observable}. If {@link ReplicaMode#FIRST}, {@link ReplicaMode#SECOND} or
     * {@link ReplicaMode#THIRD} are selected zero or one documents are returned, if {@link ReplicaMode#ALL} is used,
     * all configured replicas plus the master node may return a document.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id the id of the document.
     * @param type the {@link ReplicaMode} to select.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a List containing zero to N {@link JsonDocument}s.
     */
    <D extends Document<?>> List<D> getFromReplica(String id, ReplicaMode type, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Retrieves one or more, possibly stale, representations of a {@link JsonDocument} by its unique ID with a
     * custom timeout.
     *
     * This method has the {@link ReplicaMode#ALL} preselected. If you are only interested in the first
     * (or just some) values, you can iterate and then break out of the iterator loop. Documents
     * are pushed into the iterator as they arrive, which distinguishes this method from the {@link List}
     * equivalents which wait until all responses arrive.
     *
     * If the document has not been replicated yet or if the replica or master are not available (because a node has
     * been failed over), no response is expected from these nodes.
     *
     * **Since data is replicated asynchronously, all data returned from this method must be considered stale. If the
     * appropriate {@link ReplicateTo} constraints are set on write and the operation returns successfully, then the
     * data can be considered as non-stale.**
     *
     * Note that the returning {@link JsonDocument} responses can come in any order.
     *
     * Because this method is considered to be a "last resort" call against the database if a regular get didn't
     * succeed, most errors are swallowed (but logged) and the Observable will return all successful responses.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException}
     *   wrapped in a {@link RuntimeException}
     *
     * @param id the unique ID of the document.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the Iterator containing Documents as they arrive.
     */
    <D extends Document<?>> Iterator<D> getFromReplica(String id, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Retrieve and lock a {@link JsonDocument} by its unique ID with the default key/value timeout.
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String)}, but in addition it (write) locks the document for the given
     * lock time interval. Note that this lock time is hard capped to 30 seconds, even if provided with a higher
     * value and is not configurable. The document will unlock afterwards automatically.
     *
     * Detecting an already locked document is done by checking for {@link TemporaryLockFailureException}. Note that
     * this exception can also be raised in other conditions, always when the error is transient and retrying may help.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A transient error occurred, most probably the key was already locked: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param lockTime the time to write lock the document (max. 30 seconds).
     * @return the found {@link JsonDocument} or null.
     */
    JsonDocument getAndLock(String id, int lockTime);

    /**
     * Retrieve and lock a {@link JsonDocument} by its unique ID with a custom timeout.
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String)}, but in addition it (write) locks the document for the given
     * lock time interval. Note that this lock time is hard capped to 30 seconds, even if provided with a higher
     * value and is not configurable. The document will unlock afterwards automatically.
     *
     * Detecting an already locked document is done by checking for {@link TemporaryLockFailureException}. Note that
     * this exception can also be raised in other conditions, always when the error is transient and retrying may help.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A transient error occurred, most probably the key was already locked: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param lockTime the time to write lock the document (max. 30 seconds).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link JsonDocument} or null.
     */
    JsonDocument getAndLock(String id, int lockTime, long timeout, TimeUnit timeUnit);

    /**
     * Retrieve and lock a {@link Document} by its unique ID with the default key/value timeout.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(Document)}, but in addition it (write) locks the document for the given
     * lock time interval. Note that this lock time is hard capped to 30 seconds, even if provided with a higher
     * value and is not configurable. The document will unlock afterwards automatically.
     *
     * Detecting an already locked document is done by checking for {@link TemporaryLockFailureException}. Note that
     * this exception can also be raised in other conditions, always when the error is transient and retrying may help.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A transient error occurred, most probably the key was already locked: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the source document from which the ID is taken and the type is inferred.
     * @param lockTime the time to write lock the document (max. 30 seconds).
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndLock(D document, int lockTime);

    /**
     * Retrieve and lock a {@link Document} by its unique ID with a custom timeout.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(Document)}, but in addition it (write) locks the document for the given
     * lock time interval. Note that this lock time is hard capped to 30 seconds, even if provided with a higher
     * value and is not configurable. The document will unlock afterwards automatically.
     *
     * Detecting an already locked document is done by checking for {@link TemporaryLockFailureException}. Note that
     * this exception can also be raised in other conditions, always when the error is transient and retrying may help.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A transient error occurred, most probably the key was already locked: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the source document from which the ID is taken and the type is inferred.
     * @param lockTime the time to write lock the document (max. 30 seconds).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndLock(D document, int lockTime, long timeout, TimeUnit timeUnit);

    /**
     * Retrieve and lock a {@link Document} by its unique ID with the default key/value timeout.
     *
     * This method differs from {@link #getAndLock(String, int)} in that if a specific {@link Document} type is passed
     * in, the appropriate {@link Transcoder} will be selected (and not JSON conversion).
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String)}, but in addition it (write) locks the document for the given
     * lock time interval. Note that this lock time is hard capped to 30 seconds, even if provided with a higher
     * value and is not configurable. The document will unlock afterwards automatically.
     *
     * Detecting an already locked document is done by checking for {@link TemporaryLockFailureException}. Note that
     * this exception can also be raised in other conditions, always when the error is transient and retrying may help.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A transient error occurred, most probably the key was already locked: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param lockTime the time to write lock the document (max. 30 seconds).
     * @param target the target document type to use.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndLock(String id, int lockTime, Class<D> target);

    /**
     * Retrieve and lock a {@link Document} by its unique ID with the a custom timeout.
     *
     * This method differs from {@link #getAndLock(String, int)} in that if a specific {@link Document} type is passed
     * in, the appropriate {@link Transcoder} will be selected (and not JSON conversion).
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String)}, but in addition it (write) locks the document for the given
     * lock time interval. Note that this lock time is hard capped to 30 seconds, even if provided with a higher
     * value and is not configurable. The document will unlock afterwards automatically.
     *
     * Detecting an already locked document is done by checking for {@link TemporaryLockFailureException}. Note that
     * this exception can also be raised in other conditions, always when the error is transient and retrying may help.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A transient error occurred, most probably the key was already locked: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param lockTime the time to write lock the document (max. 30 seconds).
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndLock(String id, int lockTime, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Retrieve and touch a {@link JsonDocument} by its unique ID with the default key/value timeout.
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String)}, but in addition it touches the document, which will reset
     * its configured expiration time to the value provided.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param expiry the new expiration time for the document.
     * @return the found {@link JsonDocument} or null.
     */
    JsonDocument getAndTouch(String id, int expiry);

    /**
     * Retrieve and touch a {@link JsonDocument} by its unique ID with the a custom timeout.
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String)}, but in addition it touches the document, which will reset
     * its configured expiration time to the value provided.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param expiry the new expiration time for the document.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link JsonDocument} or null.
     */
    JsonDocument getAndTouch(String id, int expiry, long timeout, TimeUnit timeUnit);

    /**
     * Retrieve and touch a {@link Document} by its unique ID with the default key/value timeout.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(Document)}, but in addition it touches the document, which will reset
     * its configured expiration time set on the given document itself.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the source document from which the ID and expiry is taken and the type is inferred.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndTouch(D document);

    /**
     * Retrieve and touch a {@link Document} by its unique ID with a custom timeout.
     *
     * If the document is found, a {@link Document} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(Document)}, but in addition it touches the document, which will reset
     * its configured expiration time set on the given document itself.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the source document from which the ID and expiry is taken and the type is inferred.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndTouch(D document, long timeout, TimeUnit timeUnit);

    /**
     * Retrieve and touch a {@link Document} by its unique ID with the default key/value timeout.
     *
     * This method differs from {@link #getAndTouch(String, int)} in that if a specific {@link Document} type is passed
     * in, the appropriate {@link Transcoder} will be selected (and not JSON conversion).
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String, Class)}, but in addition it touches the document, which will
     * reset its configured expiration time to the value provided.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param expiry the new expiration time for the document.
     * @param target the target document type to use.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndTouch(String id, int expiry, Class<D> target);

    /**
     * Retrieve and touch a {@link Document} by its unique ID with a custom timeout.
     *
     * This method differs from {@link #getAndTouch(String, int)} in that if a specific {@link Document} type is passed
     * in, the appropriate {@link Transcoder} will be selected (and not JSON conversion).
     *
     * If the document is found, a {@link JsonDocument} is returned. If the document is not found, the
     * {@link Observable} completes without an item emitted.
     *
     * This method works similar to {@link #get(String, Class)}, but in addition it touches the document, which will
     * reset its configured expiration time to the value provided.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id id the unique ID of the document.
     * @param expiry the new expiration time for the document.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the found {@link Document} or null.
     */
    <D extends Document<?>> D getAndTouch(String id, int expiry, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Insert a {@link Document} if it does not exist already with the default key/value timeout.
     *
     * If the given {@link Document} (identified by its unique ID) already exists, the observable errors with a
     * {@link DocumentAlreadyExistsException}. If the operation should also override the existing {@link Document},
     * {@link #upsert(Document)} should be used instead. It will always either return a document or fail with an error.
     *
     * The returned {@link Document} contains original properties, but has the refreshed CAS value set.
     *
     * This operation will return successfully if the {@link Document} has been acknowledged in the managed cache layer
     * on the master server node. If increased data durability is a concern,
     * {@link #insert(Document, PersistTo, ReplicateTo)} should be used instead.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the {@link Document} to insert.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document);

    /**
     * Insert a {@link Document} if it does not exist already with a custom timeout.
     *
     * If the given {@link Document} (identified by its unique ID) already exists, the observable errors with a
     * {@link DocumentAlreadyExistsException}. If the operation should also override the existing {@link Document},
     * {@link #upsert(Document)} should be used instead. It will always either return a document or fail with an error.
     *
     * The returned {@link Document} contains original properties, but has the refreshed CAS value set.
     *
     * This operation will return successfully if the {@link Document} has been acknowledged in the managed cache layer
     * on the master server node. If increased data durability is a concern,
     * {@link #insert(Document, PersistTo, ReplicateTo)} should be used instead.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the {@link Document} to insert.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document, long timeout, TimeUnit timeUnit);

    /**
     * Insert a {@link Document} if it does not exist already and watch for durability constraints with the default
     * key/value timeout.
     *
     * This method works exactly like {@link #insert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original insert failed because the document is already stored: {@link DocumentAlreadyExistsException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original insert has already happened, so the actual
     * insert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to insert.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Insert a {@link Document} if it does not exist already and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #insert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original insert failed because the document is already stored: {@link DocumentAlreadyExistsException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original insert has already happened, so the actual
     * insert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to insert.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Insert a {@link Document} if it does not exist already and watch for durability constraints with the default
     * key/value timeout.
     *
     * This method works exactly like {@link #insert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original insert failed because the document is already stored: {@link DocumentAlreadyExistsException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original insert has already happened, so the actual
     * insert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to insert.
     * @param persistTo the persistence constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document, PersistTo persistTo);

    /**
     * Insert a {@link Document} if it does not exist already and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #insert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original insert failed because the document is already stored: {@link DocumentAlreadyExistsException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original insert has already happened, so the actual
     * insert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to insert.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Insert a {@link Document} if it does not exist already and watch for durability constraints with the default
     * key/value timeout.
     *
     * This method works exactly like {@link #insert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original insert failed because the document is already stored: {@link DocumentAlreadyExistsException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original insert has already happened, so the actual
     * insert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to insert.
     * @param replicateTo the replication constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document, ReplicateTo replicateTo);

    /**
     * Insert a {@link Document} if it does not exist already and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #insert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original insert failed because the document is already stored: {@link DocumentAlreadyExistsException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original insert has already happened, so the actual
     * insert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to insert.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D insert(D document, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Insert or overwrite a {@link Document} with the default key/value timeout.
     *
     * If the given {@link Document} (identified by its unique ID) already exists, it will be overridden by the current
     * one. The returned {@link Document} contains original properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document)} method needs to be used.
     *
     * This operation will return successfully if the {@link Document} has been acknowledged in the managed cache layer
     * on the master server node. If increased data durability is a concern,
     * {@link #upsert(Document, PersistTo, ReplicateTo)} should be used instead.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the {@link Document} to upsert.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document);

    /**
     * Insert or overwrite a {@link Document} with a custom timeout.
     *
     * If the given {@link Document} (identified by its unique ID) already exists, it will be overridden by the current
     * one. The returned {@link Document} contains original properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document, long, TimeUnit)} method needs to be used.
     *
     * This operation will return successfully if the {@link Document} has been acknowledged in the managed cache layer
     * on the master server node. If increased data durability is a concern,
     * {@link #upsert(Document, PersistTo, ReplicateTo)} should be used instead.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the {@link Document} to upsert.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document, long timeout, TimeUnit timeUnit);

    /**
     * Insert or overwrite a {@link Document} and watch for durability constraints with the default key/value timeout.
     *
     * This method works exactly like {@link #upsert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document, PersistTo, ReplicateTo)} method needs to be used.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original upsert has already happened, so the actual
     * upsert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to upsert.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Insert or overwrite a {@link Document} and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #upsert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document, PersistTo, ReplicateTo, long, TimeUnit)} method needs to be used.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original upsert has already happened, so the actual
     * upsert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to upsert.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Insert or overwrite a {@link Document} and watch for durability constraints with the default key/value timeout.
     *
     * This method works exactly like {@link #upsert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document, PersistTo)} method needs to be used.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original upsert has already happened, so the actual
     * upsert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to upsert.
     * @param persistTo the persistence constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document, PersistTo persistTo);

    /**
     * Insert or overwrite a {@link Document} and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #upsert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document, PersistTo, long, TimeUnit)} method needs to be used.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original upsert has already happened, so the actual
     * upsert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to upsert.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Insert or overwrite a {@link Document} and watch for durability constraints with the default key/value timeout.
     *
     * This method works exactly like {@link #upsert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document, ReplicateTo)} method needs to be used.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original upsert has already happened, so the actual
     * upsert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to upsert.
     * @param replicateTo the replication constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document, ReplicateTo replicateTo);

    /**
     * Insert or overwrite a {@link Document} and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #upsert(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * Please note that this method will not use the {@link Document#cas()} for optimistic concurrency checks. If
     * this behavior is needed, the {@link #replace(Document, ReplicateTo, long, TimeUnit)} method needs to be used.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original upsert has already happened, so the actual
     * upsert and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to upsert.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D upsert(D document, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Replace a {@link Document} if it does already exist with the default key/value timeout.
     *
     * If the given {@link Document} (identified by its unique ID) does not exist already, the method errors with a
     * {@link DocumentDoesNotExistException}. If the operation should also insert the {@link Document},
     * {@link #upsert(Document)} should be used instead. It will always either return a document or fail with an error.
     *
     * The returned {@link Document} contains original properties, but has the refreshed CAS value set.
     *
     * This operation will return successfully if the {@link Document} has been acknowledged in the managed cache layer
     * on the master server node. If increased data durability is a concern,
     * {@link #replace(Document, PersistTo, ReplicateTo)} should be used instead.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the {@link Document} to replace.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document);

    /**
     * Replace a {@link Document} if it does already exist with a custom timeout.
     *
     * If the given {@link Document} (identified by its unique ID) does not exist already, the method errors with a
     * {@link DocumentDoesNotExistException}. If the operation should also insert the {@link Document},
     * {@link #upsert(Document)} should be used instead. It will always either return a document or fail with an error.
     *
     * The returned {@link Document} contains original properties, but has the refreshed CAS value set.
     *
     * This operation will return successfully if the {@link Document} has been acknowledged in the managed cache layer
     * on the master server node. If increased data durability is a concern,
     * {@link #replace(Document, PersistTo, ReplicateTo)} should be used instead.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the {@link Document} to replace.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document, long timeout, TimeUnit timeUnit);

    /**
     * Replace a {@link Document} if it does exist and watch for durability constraints with the default key/value
     * timeout.
     *
     * This method works exactly like {@link #replace(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original replace has already happened, so the actual
     * replace and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to replace.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Replace a {@link Document} if it does exist and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #replace(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original replace has already happened, so the actual
     * replace and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to replace.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Replace a {@link Document} if it does exist and watch for durability constraints with the default key/value
     * timeout.
     *
     * This method works exactly like {@link #replace(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original replace has already happened, so the actual
     * replace and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to replace.
     * @param persistTo the persistence constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document, PersistTo persistTo);

    /**
     * Replace a {@link Document} if it does exist and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #replace(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original replace has already happened, so the actual
     * replace and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to replace.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Replace a {@link Document} if it does exist and watch for durability constraints with the default key/value
     * timeout.
     *
     * This method works exactly like {@link #replace(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original replace has already happened, so the actual
     * replace and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to replace.
     * @param replicateTo the replication constraint to watch.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document, ReplicateTo replicateTo);

    /**
     * Replace a {@link Document} if it does exist and watch for durability constraints with a custom timeout.
     *
     * This method works exactly like {@link #replace(Document)}, but afterwards watches the server states if the given
     * durability constraints are met. If this is the case, a new document is returned which contains the original
     * properties, but has the refreshed CAS value set.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The original replace failed because the document does not exist: {@link DocumentDoesNotExistException}
     * - The request content is too big: {@link RequestTooBigException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original replace has already happened, so the actual
     * replace and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the {@link Document} to replace.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the new {@link Document}.
     */
    <D extends Document<?>> D replace(D document, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server with the default key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document);

    /**
     * Removes a {@link Document} from the Server with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server and apply a durability requirement with the default key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Removes a {@link Document} from the Server and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server and apply a durability requirement with the default key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @param persistTo the persistence constraint to watch.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document, PersistTo persistTo);

    /**
     * Removes a {@link Document} from the Server and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server and apply a durability requirement with the default key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @param replicateTo the replication constraint to watch.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document, ReplicateTo replicateTo);

    /**
     * Removes a {@link Document} from the Server and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - A CAS value was set on the {@link Document} and it did not match with the server: {@link CASMismatchException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to remove, with the ID extracted.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(D document, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server identified by its ID with the default key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id);

    /**
     * Removes a {@link Document} from the Server identified by its ID with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with the default
     * key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with the default
     * key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id, PersistTo persistTo);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with the default
     * key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param replicateTo the replication constraint to watch.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id, ReplicateTo replicateTo);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    JsonDocument remove(String id, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server identified by its ID with the default key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param target the target document type to use.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, Class<D> target);

    /**
     * Removes a {@link Document} from the Server identified by its ID with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with the default
     * key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param target the target document type to use.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, PersistTo persistTo, ReplicateTo replicateTo, Class<D> target);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, PersistTo persistTo, ReplicateTo replicateTo, Class<D> target,
        long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with the default
     * key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @param target the target document type to use.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, PersistTo persistTo, Class<D> target);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param persistTo the persistence constraint to watch.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, PersistTo persistTo, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with the default
     * key/value timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param replicateTo the replication constraint to watch.
     * @param target the target document type to use.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, ReplicateTo replicateTo, Class<D> target);

    /**
     * Removes a {@link Document} from the Server by its ID and apply a durability requirement with a custom timeout.
     *
     * The {@link Document} returned just has the document ID and its CAS value set, since the value and all other
     * associated properties have been removed from the server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The document to remove does not exist: {@link DocumentDoesNotExistException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to remove.
     * @param replicateTo the replication constraint to watch.
     * @param target the target document type to use.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return the document containing the ID.
     */
    <D extends Document<?>> D remove(String id, ReplicateTo replicateTo, Class<D> target, long timeout, TimeUnit timeUnit);

    /**
     * Queries a Couchbase Server {@link View} with the {@link CouchbaseEnvironment#viewTimeout() default view timeout}.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - If the design document or view is not found: {@link ViewDoesNotExistException}
     *
     * @param query the query to perform.
     * @return a result containing all the found rows and additional information.
     */
    ViewResult query(ViewQuery query);

    /**
     * Queries a Couchbase Server Spatial {@link View} with the {@link CouchbaseEnvironment#viewTimeout() default view timeout}.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - If the design document or view is not found: {@link ViewDoesNotExistException}
     *
     * @param query the query to perform.
     * @return a result containing all the found rows and additional information.
     */
    SpatialViewResult query(SpatialViewQuery query);

    /**
     * Queries a Couchbase Server {@link View} with a custom timeout.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - If the design document or view is not found: {@link ViewDoesNotExistException}
     *
     * @param query the query to perform.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a result containing all the found rows and additional information.
     */
    ViewResult query(ViewQuery query, long timeout, TimeUnit timeUnit);

    /**
     * Queries a Couchbase Server Spatial {@link View} with a custom timeout.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - If the design document or view is not found: {@link ViewDoesNotExistException}
     *
     * @param query the query to perform.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a result containing all the found rows and additional information.
     */
    SpatialViewResult query(SpatialViewQuery query, long timeout, TimeUnit timeUnit);

    /**
     * Experimental: Queries a N1QL secondary index with the {@link CouchbaseEnvironment#queryTimeout() default query timeout}.
     * Said timeout includes the time it takes to retrieve all of the rows and errors from server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     *
     * @param statement the statement in a DSL form (start with a static select() import)
     * @return a result containing all found rows and additional information.
     */
    N1qlQueryResult query(Statement statement);

    /**
     * Experimental: Queries a N1QL secondary index with a custom timeout. Said timeout includes the time it
     * takes to retrieve all of the rows and errors from server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     *
     * @param statement the statement in a DSL form (start with a static select() import)
     * @param timeout the custom full timeout, including the time to retrieve all rows, errors, etc...
     * @param timeUnit the unit for the timeout.
     * @return a result containing all found rows and additional information.
     */
    N1qlQueryResult query(Statement statement, long timeout, TimeUnit timeUnit);

    /**
     * Experimental: Queries a N1QL secondary index with the {@link CouchbaseEnvironment#queryTimeout() default query timeout}.
     * Said timeout includes the time it takes to retrieve all of the rows and errors from server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     *
     * @param query the full {@link N1qlQuery}, including statement and any other additional parameter.
     * @return a result containing all found rows and additional information.
     */
    N1qlQueryResult query(N1qlQuery query);

    /**
     * Experimental: Queries a N1QL secondary index with a custom timeout. Said timeout includes the time it
     * takes to retrieve all of the rows and errors from server.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     *
     * @param query the full {@link N1qlQuery}, including statement and any other additional parameter.
     * @param timeout the custom full timeout, including the time to retrieve all rows, errors, etc...
     * @param timeUnit the unit for the timeout.
     * @return a result containing all found rows and additional information.
     */
    N1qlQueryResult query(N1qlQuery query, long timeout, TimeUnit timeUnit);

    /**
     * Experimental: Queries a Full-Text Index
     *
     * This method throws under the following conditions:
     *
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     *
     * @param query the query builder.
     * @return a query result containing the matches and additional information.
     */
    @InterfaceStability.Experimental
    SearchQueryResult query(SearchQuery query);

    /**
     * Experimental: Queries a Full-Text Index
     *
     * This method throws under the following conditions:
     *
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     *
     * @param query the query builder.
     * @param timeout the custom full timeout, including the time to retrieve all rows, errors, etc...
     * @param timeUnit the unit for the timeout.
     * @return a query result containing the matches and additional information.
     */
    @InterfaceStability.Experimental
    SearchQueryResult query(SearchQuery query, long timeout, TimeUnit timeUnit);

    /**
     * Unlocks a write-locked {@link Document} with the default key/value timeout.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The document does not exist: {@link DocumentDoesNotExistException}
     * - A transient error occurred, most probably the CAS value was not correct: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to unlock.
     * @param cas the CAS value which is mandatory to unlock it.
     * @return a Boolean indicating if the unlock was successful or not.
     */
    Boolean unlock(String id, long cas);

    /**
     * Unlocks a write-locked {@link Document} with a custom timeout.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The document does not exist: {@link DocumentDoesNotExistException}
     * - A transient error occurred, most probably the CAS value was not correct: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document to unlock.
     * @param cas the CAS value which is mandatory to unlock it.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a Boolean indicating if the unlock was successful or not.
     */
    Boolean unlock(String id, long cas, long timeout, TimeUnit timeUnit);

    /**
     * Unlocks a write-locked {@link Document} with the default key/value timeout.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The document does not exist: {@link DocumentDoesNotExistException}
     * - A transient error occurred, most probably the CAS value was not correct: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document where ID and CAS are extracted from.
     * @return a Boolean indicating if the unlock was successful or not.
     */
    <D extends Document<?>> Boolean unlock(D document);

    /**
     * Unlocks a write-locked {@link Document} with a custom timeout.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The document does not exist: {@link DocumentDoesNotExistException}
     * - A transient error occurred, most probably the CAS value was not correct: {@link TemporaryLockFailureException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document where ID and CAS are extracted from.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a Boolean indicating if the unlock was successful or not.
     */
    <D extends Document<?>> Boolean unlock(D document, long timeout, TimeUnit timeUnit);

    /**
     * Renews the expiration time of a {@link Document} with the default key/value timeout.
     *
     * Compared to {@link #getAndTouch(Document)}, this method does not actually fetch the document from the server,
     * but it just resets its expiration time to the given value.
     *
     * This method throws under the following conditions:
     *
     * - The document doesn't exist: {@link DocumentDoesNotExistException}
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param expiry the new expiration time. 0 means no expiry.
     * @return a Boolean indicating if the touch had been successful or not.
     */
    Boolean touch(String id, int expiry);

    /**
     * Renews the expiration time of a {@link Document} with a custom timeout.
     *
     * Compared to {@link #getAndTouch(Document)}, this method does not actually fetch the document from the server,
     * but it just resets its expiration time to the given value.
     *
     * This method throws under the following conditions:
     *
     * - The document doesn't exist: {@link DocumentDoesNotExistException}
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param expiry the new expiration time. 0 means no expiry.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a Boolean indicating if the touch had been successful or not.
     */
    Boolean touch(String id, int expiry, long timeout, TimeUnit timeUnit);

    /**
     * Renews the expiration time of a {@link Document} with the default key/value timeout.
     *
     * Compared to {@link #getAndTouch(Document)}, this method does not actually fetch the document from the server,
     * but it just resets its expiration time to the given value.
     *
     * This method throws under the following conditions:
     *
     * - The document doesn't exist: {@link DocumentDoesNotExistException}
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to extract the ID and expiry from.
     * @return a Boolean indicating if the touch had been successful or not.
     */
    <D extends Document<?>> Boolean touch(D document);

    /**
     * Renews the expiration time of a {@link Document} with a custom timeout.
     *
     * Compared to {@link #getAndTouch(Document)}, this method does not actually fetch the document from the server,
     * but it just resets its expiration time to the given value.
     *
     * This method throws under the following conditions:
     *
     * - The document doesn't exist: {@link DocumentDoesNotExistException}
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document to extract the ID and expiry from.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a Boolean indicating if the touch had been successful or not.
     */
    <D extends Document<?>> Boolean touch(D document, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with the default kvTimeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with the default kvTimeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param persistTo the persistence constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, PersistTo persistTo);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with the default kvTimeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param replicateTo the replication constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, ReplicateTo replicateTo);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with the default kvTimeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with a custom timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with a custom timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with a custom timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value or throw an exception if it does not
     * exist yet with a custom timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - If the document does not exist: {@link DocumentDoesNotExistException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the default
     * key/value timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the default
     * key/value timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param persistTo the persistence constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, PersistTo persistTo);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the default
     * key/value timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param replicateTo the replication constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, ReplicateTo replicateTo);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the default
     * key/value timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * It is not allowed that the delta value will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the
     * default key/value timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the
     * default key/value timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @param persistTo the persistence constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry, PersistTo persistTo);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the
     * default key/value timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @param replicateTo the replication constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry, ReplicateTo replicateTo);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with the
     * default key/value timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a counter with the given value and a initial value if it does not exist with a custom
     * timeout.
     *
     * This method allows to set an expiration time for the document as well. It is not allowed that the delta value
     * will bring the actual value below zero.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original increment/decrement has already happened, so the actual
     * increment/decrement and the watching for durability constraints are two separate tasks internally.**
     *
     * @param id the id of the document.
     * @param delta the increment or decrement amount.
     * @param initial the initial value.
     * @param expiry the new expiration time of the counter.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link Document} containing the resulting value.
     */
    JsonLongDocument counter(String id, long delta, long initial, int expiry, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Append a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document);

    /**
     * Append a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original append has already happened, so the actual
     * append and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document, PersistTo persistTo);

    /**
     * Append a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original append has already happened, so the actual
     * append and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @param replicateTo the replication constraint to watch.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document, ReplicateTo replicateTo);

    /**
     * Append a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original append has already happened, so the actual
     * append and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Append a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document, long timeout, TimeUnit timeUnit);

    /**
     * Append a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original append has already happened, so the actual
     * append and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Append a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original append has already happened, so the actual
     * append and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Append a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the appended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original append has already happened, so the actual
     * append and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is appended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D append(D document, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Prepend a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document);

    /**
     * Prepend a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original prepend has already happened, so the actual
     * prepend and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document, PersistTo persistTo);

    /**
     * Prepend a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original prepend has already happened, so the actual
     * prepend and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @param replicateTo the replication constraint to watch.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document, ReplicateTo replicateTo);

    /**
     * Prepend a {@link Document}s content to an existing one with the default key/value timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original prepend has already happened, so the actual
     * prepend and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Prepend a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document, long timeout, TimeUnit timeUnit);

    /**
     * Prepend a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original prepend has already happened, so the actual
     * prepend and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document, PersistTo persistTo, long timeout, TimeUnit timeUnit);

    /**
     * Prepend a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original prepend has already happened, so the actual
     * prepend and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Prepend a {@link Document}s content to an existing one with a custom timeout.
     *
     * The {@link Document} returned explicitly has the {@link Document#content()} set to null, because the server
     * does not return the prepended result, so at this point the client does not know how the {@link Document} now
     * looks like. A separate {@link Bucket#get(Document)} call needs to be issued in order to get the full
     * current content.
     *
     * If the {@link Document} does not exist, it needs to be created upfront. Note that {@link JsonDocument}s in all
     * forms are not supported, it is advised that the following ones are used:
     *
     * - {@link LegacyDocument}
     * - {@link StringDocument}
     * - {@link BinaryDocument}
     *
     * Note that this method does not support expiration on the {@link Document}. If set, it will be ignored.
     *
     * This method throws under the following conditions:
     *
     * - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     * - The producer outpaces the SDK: {@link BackpressureException}
     * - The operation had to be cancelled while on the wire or the retry strategy cancelled it instead of
     *   retrying: {@link RequestCancelledException}
     * - The request content is too big: {@link RequestTooBigException}
     * - If the document does not exist: {@link DocumentDoesNotExistException}
     * - The server is currently not able to process the request, retrying may help: {@link TemporaryFailureException}
     * - The server is out of memory: {@link CouchbaseOutOfMemoryException}
     * - The durability constraint could not be fulfilled because of a temporary or persistent problem:
     *   {@link DurabilityException}.
     * - Unexpected errors are caught and contained in a generic {@link CouchbaseException}.
     *
     * A {@link DurabilityException} typically happens if the given amount of replicas needed to fulfill the durability
     * constraint cannot be met because either the bucket does not have enough replicas configured or they are not
     * available in a failover event. As an example, if one replica is configured and {@link ReplicateTo#TWO} is used,
     * the observable is errored with a  {@link DurabilityException}. The same can happen if one replica is configured,
     * but one node has been failed over and not yet rebalanced (hence, on a subset of the partitions there is no
     * replica available). **It is important to understand that the original prepend has already happened, so the actual
     * prepend and the watching for durability constraints are two separate tasks internally.**
     *
     * @param document the document, identified by its id, from which the content is prepended to the existing one.
     * @param persistTo the persistence constraint to watch.
     * @param replicateTo the replication constraint to watch.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a document which mirrors the one supplied as an argument.
     */
    <D extends Document<?>> D prepend(D document, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /*---------------------------*
     * START OF SUB-DOCUMENT API *
     *---------------------------*/
    /**
     * Retrieve a fragment of an existing {@link JsonDocument JSON document}, as denoted by the given path, with
     * the default timeout.
     * The {@link Observable} will be empty if the path could not be found inside the document.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #get(Document)})
     * and some generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param id the JSON document's id to look into.
     * @param path the path to look for inside the JSON content.
     * @param fragmentType the type of the fragment value to be retrieved (so it can be directly casted).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the requested value, or null if the path could not be found.
     */
    <T> DocumentFragment<T> getIn(String id, String path, Class<T> fragmentType);

    /**
     * Retrieve a fragment of an existing {@link JsonDocument JSON document}, as denoted by the given path, with
     * a custom timeout.
     * The {@link Observable} will be empty if the path could not be found inside the document.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #get(Document)})
     * and some generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param id the JSON document's id to look into.
     * @param path the path to look for inside the JSON content.
     * @param fragmentType the type of the fragment value to be retrieved (so it can be directly casted).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the requested value, or null if the path could not be found.
     */
    <T> DocumentFragment<T> getIn(String id, String path, Class<T> fragmentType, long timeout, TimeUnit timeUnit);

    /**
     * Check for the existence of a path inside of a {@link JsonDocument JSON document}, with the default timeout.
     * For example "sub.some" will check that there is a JSON object called "sub" at the root of the document and
     * that it contains a "some" entry (which could be a scalar, array or another sub-object).
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #get(Document)})
     * and some generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param id the JSON document's id to look into.
     * @param path the path to look for inside the JSON content.
     * @return <code>true</code> if the path could be found, <code>false</code> otherwise.
     */
    boolean existsIn(String id, String path);

    /**
     * Check for the existence of a path inside of a {@link JsonDocument JSON document}, with a custom timeout.
     * For example "sub.some" will check that there is a JSON object called "sub" at the root of the document and
     * that it contains a "some" entry (which could be a scalar, array or another sub-object).
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #get(Document)})
     * and some generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param id the JSON document's id to look into.
     * @param path the path to look for inside the JSON content.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return <code>true</code> if the path could be found, <code>false</code> otherwise.
     */
    boolean existsIn(String id, String path, long timeout, TimeUnit timeUnit);

    /**
     * Upsert a fragment of JSON into an existing {@link JsonDocument JSON document}, with the default timeout.
     * The document id and path inside the JSON where this mutation should happen are given by the {@link DocumentFragment}.
     *
     * If the last element of the path doesn't exist, it will be created. Otherwise, the current value at
     * this location in the JSON is replaced by the one in the DocumentFragment.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The path doesn't exist entirely and <code>createParents</code> is set to false: {@link PathNotFoundException}.
     *  - The path contains a node that is used as a wrong type (eg. "some.sub" where "some" is actually an array): {@link PathMismatchException}
     *  - The path ends at an array index (eg. "some.array[1]"): {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * Contrary to document-level {@link #upsert(Document)}, the document's CAS is actually taken into account if
     * provided in the fragment parameter, and the internal mutation will be rejected if the enclosing document was
     * externally modified.
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to mutate and containing the new value to apply.
     * @param createParents true
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with
     * updated cas metadata.
     */
    <T> DocumentFragment<T> upsertIn(DocumentFragment<T> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Upsert a fragment of JSON into an existing {@link JsonDocument JSON document}, with a custom timeout.
     * The document id and path inside the JSON where this mutation should happen are given by the {@link DocumentFragment}.
     *
     * If the last element of the path doesn't exist, it will be created. Otherwise, the current value at
     * this location in the JSON is replaced by the one in the DocumentFragment.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The path doesn't exist entirely and <code>createParents</code> is set to false: {@link PathNotFoundException}.
     *  - The path contains a node that is used as a wrong type (eg. "some.sub" where "some" is actually an array): {@link PathMismatchException}
     *  - The path ends at an array index (eg. "some.array[1]"): {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * Contrary to document-level {@link #upsert(Document)}, the document's CAS is actually taken into account if
     * provided in the fragment parameter, and the internal mutation will be rejected if the enclosing document was
     * externally modified.
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to mutate and containing the new value to apply.
     * @param createParents true
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with
     * updated cas metadata.
     */
    <T> DocumentFragment<T> upsertIn(DocumentFragment<T> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Insert a fragment of JSON into an existing {@link JsonDocument JSON document}, on the condition that no value
     * already exist at this location, with a custom timeout. The document id and path inside the JSON where this
     * insertion should happen are given by the {@link DocumentFragment}.
     *
     * Unless <code>createParents</code> is set to true, only the last element of the path is created.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The path doesn't exist entirely and <code>createParents</code> is set to false: {@link PathNotFoundException}.
     *  - The path contains a node that is used as a wrong type (eg. "some.sub" where "some" is actually an array): {@link PathMismatchException}
     *  - The path ends at an array index (eg. "some.array[1]"): {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to mutate and containing the new value to insert.
     * @param createParents true
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> insertIn(DocumentFragment<T> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Insert a fragment of JSON into an existing {@link JsonDocument JSON document}, on the condition that no value
     * already exist at this location, with a custom timeout. The document id and path inside the JSON where this
     * insertion should happen are given by the {@link DocumentFragment}.
     *
     * Unless <code>createParents</code> is set to true, only the last element of the path is created.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The path doesn't exist entirely and <code>createParents</code> is set to false: {@link PathNotFoundException}.
     *  - The path contains a node that is used as a wrong type (eg. "some.sub" where "some" is actually an array): {@link PathMismatchException}
     *  - The path ends at an array index (eg. "some.array[1]"): {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to mutate and containing the new value to insert.
     * @param createParents true
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> insertIn(DocumentFragment<T> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Replace a fragment of JSON with another one inside an existing {@link JsonDocument JSON document}, with the
     * default timeout. There should already be a value at this location. The document id and path inside the JSON where
     * this mutation should happen are given by the {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path contains a node that is used as a wrong type (eg. "some.sub" where "some" is actually an array): {@link PathMismatchException}
     *  - The path ends at an array index (eg. "some.array[1]"): {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to mutate and containing the new value to apply.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> replaceIn(DocumentFragment<T> fragment, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Replace a fragment of JSON with another one inside an existing {@link JsonDocument JSON document}, with a custom
     * timeout. There should already be a value at this location. The document id and path inside the JSON where this
     * mutation should happen are given by the {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path contains a node that is used as a wrong type (eg. "some.sub" where "some" is actually an array): {@link PathMismatchException}
     *  - The path ends at an array index (eg. "some.array[1]"): {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to mutate and containing the new value to apply.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> replaceIn(DocumentFragment<T> fragment, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Extend an array inside a {@link JsonDocument JSON document}, either pushing the new value at the
     * {@link ExtendDirection#FRONT front} or at the {@link ExtendDirection#BACK back}, with the default timeout.
     * The document id and path inside the JSON where this mutation should happen are given by the {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path isn't an array element (including if the path points to an array index): {@link PathMismatchException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the array to mutate and containing the new value to add to the array.
     * @param direction the position in the array where to insert (start/front of array or end/back of array).
     * @param createParents true if intermediate missing nodes in the path should also be created (effectively creating a new array).
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> extendIn(DocumentFragment<T> fragment, ExtendDirection direction, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Extend an array inside a {@link JsonDocument JSON document}, either pushing the new value at the
     * {@link ExtendDirection#FRONT front} or at the {@link ExtendDirection#BACK back}, with a custom timeout.
     * The document id and path inside the JSON where this mutation should happen are given by the {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path isn't an array element (including if the path points to an array index): {@link PathMismatchException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the array to mutate and containing the new value to add to the array.
     * @param direction the position in the array where to insert (start/front of array or end/back of array).
     * @param createParents true if intermediate missing nodes in the path should also be created (effectively creating a new array).
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> extendIn(DocumentFragment<T> fragment, ExtendDirection direction, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Insert a value in an existing array inside a {@link JsonDocument JSON document}, at a specific index as denoted
     * by the path (eg. "some.array[2]"), with the default timeout. Existing values at the left of the index are shifted forward.
     * The document id and path inside the JSON where this mutation should happen are given by the
     * {@link DocumentFragment}. Index can be in the range 0-arraySize (inclusive).
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path denotes an existing array but with an index too large: {@link PathNotFoundException}
     *  - The path doesn't denote an array index: {@link PathInvalidException}
     *  - The array index is negative: {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the array position at which to insert and containing
     *                 the new value to add to the array.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> arrayInsertIn(DocumentFragment<T> fragment, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Insert a value in an existing array inside a {@link JsonDocument JSON document}, at a specific index as denoted
     * by the path (eg. "some.array[2]"), with a custom timeout. Existing values at the left of the index are shifted forward.
     * The document id and path inside the JSON where this mutation should happen are given by the
     * {@link DocumentFragment}. Index can be in the range 0-arraySize (inclusive).
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path denotes an existing array but with an index too large: {@link PathNotFoundException}
     *  - The path doesn't denote an array index: {@link PathInvalidException}
     *  - The array index is negative: {@link PathInvalidException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the array position at which to insert and containing
     *                 the new value to add to the array.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> arrayInsertIn(DocumentFragment<T> fragment, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Adds a value in an existing array inside a {@link JsonDocument JSON document}, with the default timeout.
     * This is provided said value isn't already present in the array (as checked using string comparison).
     * This is restricted to primitive values and arrays containing only primitives (no dictionary nor sub-array).
     *
     * The document id and path inside the JSON where this mutation should happen are given by the
     * {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path doesn't denote an array: {@link PathMismatchException}
     *  - The array doesn't contain only primitive types: {@link PathMismatchException}
     *  - The value isn't primitive: {@link CannotInsertValueException}
     *  - The value is already in the array: {@link PathExistsException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the array in which to check and insert, containing
     *                 the new value to add to the array.
     * @param createParents set to true to create intermediary missing nodes and the array if it doesn't exist.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> addUniqueIn(DocumentFragment<T> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Adds a value in an existing array inside a {@link JsonDocument JSON document}, with a custom timeout.
     * This is provided said value isn't already present in the array (as checked using string comparison).
     * This is restricted to primitive values and arrays containing only primitives (no dictionary nor sub-array).
     *
     * The document id and path inside the JSON where this mutation should happen are given by the
     * {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - The path doesn't denote an array: {@link PathMismatchException}
     *  - The array doesn't contain only primitive types: {@link PathMismatchException}
     *  - The value isn't primitive: {@link CannotInsertValueException}
     *  - The value is already in the array: {@link PathExistsException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the array in which to check and insert, containing
     *                 the new value to add to the array.
     * @param createParents set to true to create intermediary missing nodes and the array if it doesn't exist.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the mutated value, with updated cas metadata.
     */
    <T> DocumentFragment<T> addUniqueIn(DocumentFragment<T> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Removes an entry from an existing {@link JsonDocument JSON document}, with the default timeout.
     * This can be a scalar, whole dictionary or array or specific dictionary entry or array index,
     * as denoted by the path. The last element in an array can also be removed by using the -1 index
     * (eg. "some.array[-1]").
     *
     * The document id and path inside the JSON where this removal should happen are given by the
     * {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - An empty path was provided: {@link IllegalArgumentException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to remove (fragment value is ignored).
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the removed value, with updated cas metadata but no content.
     */
    <T> DocumentFragment<T> removeIn(DocumentFragment<T> fragment, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Removes an entry from an existing {@link JsonDocument JSON document}, with a custom timeout.
     * This can be a scalar, whole dictionary or array or specific dictionary entry or array index,
     * as denoted by the path. The last element in an array can also be removed by using the -1 index
     * (eg. "some.array[-1]").
     *
     * The document id and path inside the JSON where this removal should happen are given by the
     * {@link DocumentFragment}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - There is no value at this location in the JSON: {@link PathNotFoundException}
     *  - An empty path was provided: {@link IllegalArgumentException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the path to remove (fragment value is ignored).
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param <T> the type of the fragment value.
     * @return the {@link DocumentFragment} corresponding to the removed value, with updated cas metadata but no content.
     */
    <T> DocumentFragment<T> removeIn(DocumentFragment<T> fragment, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Increment or decrement a numerical value inside an existing {@link JsonDocument JSON document} with the default
     * timeout.
     * The document id and path inside the JSON where this should happen, and the delta to apply, are given by the
     * {@link DocumentFragment}.
     *
     * If the last element of the path doesn't exist, it is created and initialized with the delta.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The provided delta is zero: {@link ZeroDeltaException}
     *  - The delta would make the current value grow beyond {@link Long#MAX_VALUE}
     *    or below/at {@link Long#MIN_VALUE}: {@link DeltaTooBigException}
     *  - The current value is already over bounds (see above) due to external modification: {@link NumberTooBigException}
     *  - The path points to a value that isn't numeric: {@link PathMismatchException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the numerical value to increment/decrement
     *                 (fragment is the Long delta to apply) ignored).
     * @param createParents true if intermediary missing nodes in the path should be created.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @return a {@link DocumentFragment} corresponding to the modified value (contains the new value of the "counter",
     * with updated cas metadata).
     */
    DocumentFragment<Long> counterIn(DocumentFragment<Long> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo);

    /**
     * Increment or decrement a numerical value inside an existing {@link JsonDocument JSON document} with a custom
     * timeout.
     * The document id and path inside the JSON where this should happen, and the delta to apply, are given by the
     * {@link DocumentFragment}.
     *
     * If the last element of the path doesn't exist, it is created and initialized with the delta.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document. The returned {@link DocumentFragment} wraps the value and additional
     * metadata like a reminder of the document's id, the path, enclosing document's cas (or {@link MutationToken}).
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The provided cas doesn't match the one of the enclosing document: {@link CASMismatchException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The provided delta is zero: {@link ZeroDeltaException}
     *  - The delta would make the current value grow beyond {@link Long#MAX_VALUE}
     *    or below/at {@link Long#MIN_VALUE}: {@link DeltaTooBigException}
     *  - The current value is already over bounds (see above) due to external modification: {@link NumberTooBigException}
     *  - The path points to a value that isn't numeric: {@link PathMismatchException}
     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
     *
     * Other error conditions include document-level error conditions (similar to those of {@link #replace(Document)})
     * and generic sub-document-level error conditions (all of which extend {@link SubDocumentException},
     * like {@link PathTooDeepException}).
     *
     * @param fragment a {@link DocumentFragment} pointing to the numerical value to increment/decrement
     *                 (fragment is the Long delta to apply) ignored).
     * @param createParents true if intermediary missing nodes in the path should be created.
     * @param persistTo the persistence constraint to watch (or NONE if not required).
     * @param replicateTo the replication constraint to watch (or NONE if not required).
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return a {@link DocumentFragment} corresponding to the modified value (contains the new value of the "counter",
     * with updated cas metadata).
     */
    DocumentFragment<Long> counterIn(DocumentFragment<Long> fragment, boolean createParents, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit);

    /**
     * Perform several {@link Lookup lookup} operations inside a single existing {@link JsonDocument JSON document}
     * with the default timeout.
     * The list of path to look for inside the JSON is represented through {@link LookupSpec LookupSpecs}.
     *
     * Each spec will receive an answer in the form of a {@link LookupResult}, meaning that if sub-document level
     * error conditions happen (like the path is malformed or doesn't exist), the error condition is still represented
     * as a LookupResult and the whole operation still succeeds.
     *
     * {@link MultiLookupResult} aggregates all these results and allows to check for such a partial failure (see
     * {@link MultiLookupResult#hasFailure()}, {@link MultiLookupResult#isTotalFailure()}, ...).
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document.
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The lookupSpecs vararg is null: {@link NullPointerException}
     *  - The lookupSpecs vararg is omitted/empty: {@link IllegalArgumentException}
     *
     * Each individual {@link LookupResult} can have errors denoted by a "SUBDOC_" error status (eg.
     * {@link ResponseStatus#SUBDOC_PATH_MISMATCH} or {@link ResponseStatus#SUBDOC_PATH_NOT_FOUND}),
     * in which case the value is null. For {@link Lookup#EXIST}, SUBDOC_PATH_NOT_FOUND is considered a "success" where
     * {@link LookupResult#exists()} returns false and {@link LookupResult#value()} returns false as well.
     *
     * If one prefers to deal with a {@link SubDocumentException} instead of the ResponseStatus, one can use
     * {@link LookupResult#valueOrThrow()}.
     *
     * One special fatal error can also happen that is represented as a LookupResult, when the value couldn't be decoded
     * from JSON. In that case, the ResponseStatus is {@link ResponseStatus#FAILURE} and the value() is a
     * {@link TranscodingException}.
     *
     * Other document-level error conditions are similar to those encountered during a document-level {@link #get(Document)}.
     *
     * @param id the id of the JSON document to look into.
     * @param lookupSpecs the list of lookup operations to perform (use
     *        {@link LookupSpec#get(String) LookupSpec's static methods})
     * @return a {@link MultiLookupResult} representing the whole list of results (1 for each spec),
     * unless a document-level error happened (in which case an exception is propagated).
     */
    MultiLookupResult lookupIn(String id, LookupSpec... lookupSpecs);

    /**
     * Perform several {@link Lookup lookup} operations inside a single existing {@link JsonDocument JSON document}
     * with a custom timeout.
     * The list of path to look for inside the JSON is represented through {@link LookupSpec LookupSpecs}.
     *
     * Each spec will receive an answer in the form of a {@link LookupResult}, meaning that if sub-document level
     * error conditions happen (like the path is malformed or doesn't exist), the error condition is still represented
     * as a LookupResult and the whole operation still succeeds.
     *
     * {@link MultiLookupResult} aggregates all these results and allows to check for such a partial failure (see
     * {@link MultiLookupResult#hasFailure()}, {@link MultiLookupResult#isTotalFailure()}, ...).
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document.
     *
     * This method throws under the following notable error conditions:
     *
     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The lookupSpecs vararg is null: {@link NullPointerException}
     *  - The lookupSpecs vararg is omitted/empty: {@link IllegalArgumentException}
     *
     * Each individual {@link LookupResult} can have errors denoted by a "SUBDOC_" error status (eg.
     * {@link ResponseStatus#SUBDOC_PATH_MISMATCH} or {@link ResponseStatus#SUBDOC_PATH_NOT_FOUND}),
     * in which case the value is null. For {@link Lookup#EXIST}, SUBDOC_PATH_NOT_FOUND is considered a "success" where
     * {@link LookupResult#exists()} returns false and {@link LookupResult#value()} returns false as well.
     *
     * If one prefers to deal with a {@link SubDocumentException} instead of the ResponseStatus, one can use
     * {@link LookupResult#valueOrThrow()}.
     *
     * One special fatal error can also happen that is represented as a LookupResult, when the value couldn't be decoded
     * from JSON. In that case, the ResponseStatus is {@link ResponseStatus#FAILURE} and the value() is a
     * {@link TranscodingException}.
     *
     * Other document-level error conditions are similar to those encountered during a document-level {@link #get(Document)}.
     *
     * @param id the id of the JSON document to look into.
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @param lookupSpecs the list of lookup operations to perform (use
     *        {@link LookupSpec#get(String) LookupSpec's static methods})
     * @return a {@link MultiLookupResult} representing the whole list of results (1 for each spec),
     * unless a document-level error happened (in which case an exception is propagated).
     */
    MultiLookupResult lookupIn(String id, long timeout, TimeUnit timeUnit, LookupSpec... lookupSpecs);

    //TODO reintroduce mutateIn once the protocol has been stabilized
//    /**
//     * Perform several {@link Mutation mutation} operations inside a single existing {@link JsonDocument JSON document}
//     * with the default timeout.
//     * The list of mutations and paths to mutate in the JSON is represented through {@link MutationSpec MutationSpecs}.
//     *
//     * Multi-mutations are applied as a whole, atomically at the document level. That means that if one of the mutations
//     * fails, none of the mutations are applied. Otherwise, all mutations can be considered successful and the whole
//     * operation will receive a {@link MultiMutationResult} with the updated cas (and optionally {@link MutationToken}).
//     *
//     * The subdocument API has the benefit of only transmitting the fragment of the document you want to mutate
//     * on the wire, instead of the whole document.
//     *
//     * This method throws under the following notable error conditions:
//     *
//     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
//     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
//     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
//     *  - The provided CAS doesn't match with the one of the enclosing document: {@link CASMismatchException}
//     *  - The mutationSpecs vararg is null: {@link NullPointerException}
//     *  - The mutationSpecs vararg is omitted/empty: {@link IllegalArgumentException}
//     *  - A mutationSpec couldn't be encoded and the whole operation was cancelled: {@link TranscodingException}
//     *  - The multi-mutation failed: {@link MultiMutationException}
//     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
//
//     * When receiving a {@link MultiMutationException}, one can inspect the exception to find the zero-based index and
//     * error {@link ResponseStatus status code} of the first failing {@link MutationSpec}. Subsequent mutations may have
//     * also failed had they been attempted, but a single spec failing causes the whole operation to be cancelled.
//     *
//     * Other top-level error conditions are similar to those encountered during a document-level {@link #replace(Document)}.
//     *
//     * @param doc a {@link JsonDocument} to mutate. Only the {@link JsonDocument#id() id}, {@link JsonDocument#cas() cas}
//     *            and {@link JsonDocument#mutationToken()} are used.
//     * @param persistTo the persistence constraint to watch (or NONE if not required).
//     * @param replicateTo the replication constraint to watch (or NONE if not required).
//     * @param mutationSpecs the list of {@link MutationSpec} to apply to the target document.
//     * @return a {@link MultiMutationResult} (if successful) containing updated cas metadata.
//     */
//    MultiMutationResult mutateIn(JsonDocument doc, PersistTo persistTo, ReplicateTo replicateTo, MutationSpec... mutationSpecs);
//
//    /**
//     * Perform several {@link Mutation mutation} operations inside a single existing {@link JsonDocument JSON document}
//     * with a custom timeout.
//     * The list of mutations and paths to mutate in the JSON is represented through {@link MutationSpec MutationSpecs}.
//     *
//     * Multi-mutations are applied as a whole, atomically at the document level. That means that if one of the mutations
//     * fails, none of the mutations are applied. Otherwise, all mutations can be considered successful and the whole
//     * operation will receive a {@link MultiMutationResult} with the updated cas (and optionally {@link MutationToken}).
//     *
//     * The subdocument API has the benefit of only transmitting the fragment of the document you want to mutate
//     * on the wire, instead of the whole document.
//     *
//     * This method throws under the following notable error conditions:
//     *
//     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
//     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
//     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
//     *  - The provided CAS doesn't match with the one of the enclosing document: {@link CASMismatchException}
//     *  - The mutationSpecs vararg is null: {@link NullPointerException}
//     *  - The mutationSpecs vararg is omitted/empty: {@link IllegalArgumentException}
//     *  - A mutationSpec couldn't be encoded and the whole operation was cancelled: {@link TranscodingException}
//     *  - The multi-mutation failed: {@link MultiMutationException}
//     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
//
//     * When receiving a {@link MultiMutationException}, one can inspect the exception to find the zero-based index and
//     * error {@link ResponseStatus status code} of the first failing {@link MutationSpec}. Subsequent mutations may have
//     * also failed had they been attempted, but a single spec failing causes the whole operation to be cancelled.
//     *
//     * Other top-level error conditions are similar to those encountered during a document-level {@link #replace(Document)}.
//     *
//     * @param doc a {@link JsonDocument} to mutate. Only the {@link JsonDocument#id() id}, {@link JsonDocument#cas() cas}
//     *            and {@link JsonDocument#mutationToken()} are used.
//     * @param persistTo the persistence constraint to watch (or NONE if not required).
//     * @param replicateTo the replication constraint to watch (or NONE if not required).
//     * @param timeout the custom timeout.
//     * @param timeUnit the unit for the timeout.
//     * @param mutationSpecs the list of {@link MutationSpec} to apply to the target document.
//     * @return a {@link MultiMutationResult} (if successful) containing updated cas metadata.
//     */
//    MultiMutationResult mutateIn(JsonDocument doc, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit, MutationSpec... mutationSpecs);
//
//    /**
//     * Perform several {@link Mutation mutation} operations inside a single existing {@link JsonDocument JSON document},
//     * with the default timeout.
//     * The list of mutations and paths to mutate in the JSON is represented through {@link MutationSpec MutationSpecs}.
//     *
//     * Multi-mutations are applied as a whole, atomically at the document level. That means that if one of the mutations
//     * fails, none of the mutations are applied. Otherwise, all mutations can be considered successful and the whole
//     * operation will receive a {@link MultiMutationResult} with the updated cas (and optionally {@link MutationToken}).
//     *
//     * The subdocument API has the benefit of only transmitting the fragment of the document you want to mutate
//     * on the wire, instead of the whole document.
//     *
//     * This method throws under the following notable error conditions:
//     *
//     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
//     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
//     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
//     *  - The mutationSpecs vararg is null: {@link NullPointerException}
//     *  - The mutationSpecs vararg is omitted/empty: {@link IllegalArgumentException}
//     *  - A mutationSpec couldn't be encoded and the whole operation was cancelled: {@link TranscodingException}
//     *  - The multi-mutation failed: {@link MultiMutationException}
//     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
//     *
//     * When receiving a {@link MultiMutationException}, one can inspect the exception to find the zero-based index and
//     * error {@link ResponseStatus status code} of the first failing {@link MutationSpec}. Subsequent mutations may have
//     * also failed had they been attempted, but a single spec failing causes the whole operation to be cancelled.
//     *
//     * Other top-level error conditions are similar to those encountered during a document-level {@link #replace(Document)}.
//     *
//     * @param docId the id of {@link JsonDocument} to mutate. Use the
//     *              {@link #mutateIn(JsonDocument, PersistTo, ReplicateTo, MutationSpec[]) JsonDocument-based variant}
//     *              if you want to use optimistic locking via cas, or modify the document's expiry.
//     * @param persistTo the persistence constraint to watch (or NONE if not required).
//     * @param replicateTo the replication constraint to watch (or NONE if not required).
//     * @param mutationSpecs the list of {@link MutationSpec} to apply to the target document.
//     * @return a {@link MultiMutationResult} (if successful) containing updated cas metadata.
//     */
//    MultiMutationResult mutateIn(String docId, PersistTo persistTo, ReplicateTo replicateTo, MutationSpec... mutationSpecs);
//
//    /**
//     * Perform several {@link Mutation mutation} operations inside a single existing {@link JsonDocument JSON document},
//     * with a custom timeout.
//     * The list of mutations and paths to mutate in the JSON is represented through {@link MutationSpec MutationSpecs}.
//     *
//     * Multi-mutations are applied as a whole, atomically at the document level. That means that if one of the mutations
//     * fails, none of the mutations are applied. Otherwise, all mutations can be considered successful and the whole
//     * operation will receive a {@link MultiMutationResult} with the updated cas (and optionally {@link MutationToken}).
//     *
//     * The subdocument API has the benefit of only transmitting the fragment of the document you want to mutate
//     * on the wire, instead of the whole document.
//     *
//     * This method throws under the following notable error conditions:
//     *
//     *  - The operation takes longer than the specified timeout: {@link TimeoutException} wrapped in a {@link RuntimeException}
//     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
//     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
//     *  - The mutationSpecs vararg is null: {@link NullPointerException}
//     *  - The mutationSpecs vararg is omitted/empty: {@link IllegalArgumentException}
//     *  - A mutationSpec couldn't be encoded and the whole operation was cancelled: {@link TranscodingException}
//     *  - The multi-mutation failed: {@link MultiMutationException}
//     *  - The durability constraint could not be fulfilled because of a temporary or persistent problem: {@link DurabilityException}
//     *
//     * When receiving a {@link MultiMutationException}, one can inspect the exception to find the zero-based index and
//     * error {@link ResponseStatus status code} of the first failing {@link MutationSpec}. Subsequent mutations may have
//     * also failed had they been attempted, but a single spec failing causes the whole operation to be cancelled.
//     *
//     * Other top-level error conditions are similar to those encountered during a document-level {@link #replace(Document)}.
//     *
//     * @param docId the id of {@link JsonDocument} to mutate. Use the
//     *              {@link #mutateIn(JsonDocument, PersistTo, ReplicateTo, MutationSpec[]) JsonDocument-based variant}
//     *              if you want to use optimistic locking via cas, or modify the document's expiry.
//     * @param persistTo the persistence constraint to watch (or NONE if not required).
//     * @param replicateTo the replication constraint to watch (or NONE if not required).
//     * @param timeout the custom timeout.
//     * @param timeUnit the unit for the timeout.
//     * @param mutationSpecs the list of {@link MutationSpec} to apply to the target document.
//     * @return a {@link MultiMutationResult} (if successful) containing updated cas metadata.
//     */
//    MultiMutationResult mutateIn(String docId, PersistTo persistTo, ReplicateTo replicateTo, long timeout, TimeUnit timeUnit, MutationSpec... mutationSpecs);
    /*-------------------------*
     * END OF SUB-DOCUMENT API *
     *-------------------------*/


    /**
     * Invalidates and clears the internal query cache.
     *
     * This method can be used to explicitly clear the internal N1QL query cache. This cache will
     * be filled with non-adhoc query statements (query plans) to speed up those subsequent executions.
     *
     * Triggering this method will wipe out the complete cache, which will not cause an interruption but
     * rather all queries need to be re-prepared internally. This method is likely to be deprecated in
     * the future once the server side query engine distributes its state throughout the cluster.
     *
     * This method will not throw under any conditions.
     *
     * @return the number of entries in the cache before it was cleared out.
     */
    int invalidateQueryCache();

    /**
     * Provides access to the {@link BucketManager} for administrative access.
     *
     * The manager lets you perform operations such as flushing a bucket or creating and managing design documents.
     *
     * @return the bucket manager for administrative operations.
     */
    BucketManager bucketManager();

    /**
     * The {@link Repository} provides access to full object document mapping (ODM) capabilities.
     *
     * It allows you to work with POJO entities only and use annotations to customize the behaviour and mapping
     * characteristics.
     *
     * @return the repository for ODM capabilities.
     */
    @InterfaceAudience.Public
    @InterfaceStability.Experimental
    Repository repository();

    /**
     * Closes this bucket with the default disconnect timeout.
     *
     * @return true if the bucket was successfully closed.
     */
    Boolean close();

    /**
     * Closes this bucket with a custom timeout.
     *
     * @param timeout the custom timeout.
     * @param timeUnit the unit for the timeout.
     * @return true if the bucket was successfully closed.
     */
    Boolean close(long timeout, TimeUnit timeUnit);

    /**
     * Returns true if this bucket is already closed, false if it is still open.
     *
     * @return true if closed, false otherwise.
     */
    boolean isClosed();

}
