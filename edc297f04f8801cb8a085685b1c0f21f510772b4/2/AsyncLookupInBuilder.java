/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.java.subdoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.couchbase.client.core.ClusterFacade;
import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.core.logging.CouchbaseLogger;
import com.couchbase.client.core.logging.CouchbaseLoggerFactory;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.kv.subdoc.multi.Lookup;
import com.couchbase.client.core.message.kv.subdoc.multi.MultiLookupResponse;
import com.couchbase.client.core.message.kv.subdoc.multi.MultiResult;
import com.couchbase.client.core.message.kv.subdoc.multi.SubMultiLookupRequest;
import com.couchbase.client.core.message.kv.subdoc.simple.SimpleSubdocResponse;
import com.couchbase.client.core.message.kv.subdoc.simple.SubExistRequest;
import com.couchbase.client.core.message.kv.subdoc.simple.SubGetRequest;
import com.couchbase.client.deps.io.netty.util.internal.StringUtil;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.TranscodingException;
import com.couchbase.client.java.error.subdoc.DocumentNotJsonException;
import com.couchbase.client.java.error.subdoc.SubDocumentException;
import com.couchbase.client.java.transcoder.TranscoderUtils;
import com.couchbase.client.java.transcoder.subdoc.FragmentTranscoder;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import static com.couchbase.client.java.util.OnSubscribeDeferAndWatch.deferAndWatch;

/**
 * A builder for subdocument lookups. In order to perform the final set of operations, use the
 * {@link #execute()} method. Operations are performed asynchronously (see {@link LookupInBuilder} for a synchronous
 * version).
 *
 * Instances of this builder should be obtained through {@link AsyncBucket#lookupIn(String)} rather than directly
 * constructed.
 *
 * @author Simon Baslé
 * @since 2.2
 */
@InterfaceStability.Committed
@InterfaceAudience.Public
public class AsyncLookupInBuilder {

    private static final CouchbaseLogger LOGGER = CouchbaseLoggerFactory.getInstance(AsyncLookupInBuilder.class);

    private final ClusterFacade core;
    private final CouchbaseEnvironment environment;
    private final String bucketName;
    private final FragmentTranscoder subdocumentTranscoder;

    private final String docId;

    private final List<LookupSpec> specs;
    private boolean includeRaw = false;

    /**
     * Instances of this builder should be obtained through {@link AsyncBucket#lookupIn(String)} rather than directly
     * constructed.
    */
    @InterfaceAudience.Private
    public AsyncLookupInBuilder(ClusterFacade core, String bucketName, CouchbaseEnvironment environment,
            FragmentTranscoder transcoder, String docId) {
        if (docId == null || docId.isEmpty()) {
            throw new IllegalArgumentException("The document ID must not be null or empty.");
        }
        if (docId.getBytes().length > 250) {
            throw new IllegalArgumentException("The document ID must not be larger than 250 bytes");
        }

        this.core = core;
        this.bucketName = bucketName;
        this.environment = environment;
        this.subdocumentTranscoder = transcoder;
        this.docId = docId;
        this.specs = new ArrayList<LookupSpec>();
    }

    /**
     * Perform several {@link Lookup lookup} operations inside a single existing {@link JsonDocument JSON document}.
     * The list of path to look for inside the JSON is constructed through builder methods {@link #get(String...)} and
     * {@link #exists(String...)}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document.
     *
     * If multiple operations are specified, each spec will receive an answer, overall contained in a
     * {@link DocumentFragment}, meaning that if sub-document level error conditions happen (like the path is malformed
     * or doesn't exist), the whole operation still succeeds.
     *
     * If a single operation is specified, then any error other that a path not found will cause the Observable to
     * fail with the corresponding {@link SubDocumentException}. Otherwise a {@link DocumentFragment} is returned.
     *
     * Calling {@link DocumentFragment#content(String)} or one of its variants on a failed spec/path will throw the
     * corresponding {@link SubDocumentException}. For successful gets, it will return the value (or null in the case
     * of a path not found, and only in this case). For exists, it will return true (or false for a path not found).
     *
     * To check for any error without throwing an exception, use {@link DocumentFragment#status(String)}
     * (or its index-based variant).
     *
     * To check that a given path (or index) is valid for calling {@link DocumentFragment#content(String) content()} on
     * it without raising an Exception, use {@link DocumentFragment#exists(String)}.
     *
     * One special fatal error can also happen, when the value couldn't be decoded from JSON. In that case,
     * the ResponseStatus for the path is {@link ResponseStatus#FAILURE} and the content(path) will throw a
     * {@link TranscodingException}.
     *
     * This Observable most notable error conditions are:
     *
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - No lookup was defined through the builder API: {@link IllegalArgumentException}
     *
     * Other document-level error conditions are similar to those encountered during a document-level {@link AsyncBucket#get(String)}.
     *
     * @return an {@link Observable} of a single {@link DocumentFragment} representing the whole list of results (1 for
     *        each spec), unless a document-level error happened (in which case an exception is propagated).
     */
    public Observable<DocumentFragment<Lookup>> execute() {
        if (specs.isEmpty()) {
            throw new IllegalArgumentException("Execution of a subdoc lookup requires at least one operation");
        } else if (specs.size() == 1) {
            //single path optimization
            return doSingleLookup(specs.get(0));
        } else {
            return doMultiLookup();
        }
    }

    /**
     * Set to true, includes the raw byte value for each GET in the results, in addition to the deserialized content.
     */
    public AsyncLookupInBuilder includeRaw(boolean includeRaw) {
        this.includeRaw = includeRaw;
        return this;
    }

    /**
     * @return true if this builder is configured to include raw byte values for each GET result.
     */
    public boolean isIncludeRaw() {
        return this.includeRaw;
    }

    /**
     * Get a value inside the JSON document.
     *
     * @param paths the path inside the document where to get the value from.
     * @return this builder for chaining.
     */
    public AsyncLookupInBuilder get(String... paths) {
        if (paths == null || paths.length == 0) {
            throw new IllegalArgumentException("Path is mandatory for subdoc get");
        }
        for (String path : paths) {
            if (StringUtil.isNullOrEmpty(path)) {
                throw new IllegalArgumentException("Path is mandatory for subdoc get");
            }
            this.specs.add(new LookupSpec(Lookup.GET, path));
        }
        return this;
    }


    /**
     * Get a value inside the JSON document.
     *
     * @param path the path inside the document where to get the value from.
     * @param optionsBuilder {@link SubdocOptionsBuilder}
     * @return this builder for chaining.
     */
    public AsyncLookupInBuilder get(String path, SubdocOptionsBuilder optionsBuilder) {
        if (path == null) {
            throw new IllegalArgumentException("Path is mandatory for subdoc get");
        }
        if (optionsBuilder.createParents()) {
            throw new IllegalArgumentException("Options createParents are not supported for lookup");
        }
        this.specs.add(new LookupSpec(Lookup.GET, path, optionsBuilder));
        return this;
    }


    /**
     * Get a value inside the JSON document.
     *
     * @param paths the path inside the document where to get the value from.
     * @param optionsBuilder {@link SubdocOptionsBuilder}
     * @return this builder for chaining.
     */
    public AsyncLookupInBuilder get(Iterable<String> paths, SubdocOptionsBuilder optionsBuilder) {
        if (paths == null) {
            throw new IllegalArgumentException("Path is mandatory for subdoc get");
        }
        if (optionsBuilder.createParents()) {
            throw new IllegalArgumentException("Options createParents are not supported for lookup");
        }
        for (String path : paths) {
            if (StringUtil.isNullOrEmpty(path)) {
                throw new IllegalArgumentException("Path is mandatory for subdoc get");
            }
            this.specs.add(new LookupSpec(Lookup.GET, path, optionsBuilder));
        }
        return this;
    }

    /**
     * Check if a value exists inside the document (if it does not, attempting to get the
     * {@link DocumentFragment#content(int)} will raise an error).
     * This doesn't transmit the value on the wire if it exists, saving the corresponding byte overhead.
     *
     * @param paths the path inside the document to check for existence.
     * @return this builder for chaining.
     */
    public AsyncLookupInBuilder exists(String... paths) {
        if (paths == null || paths.length == 0) {
            throw new IllegalArgumentException("Path is mandatory for subdoc exists");
        }
        for (String path : paths) {
            if (StringUtil.isNullOrEmpty(path)) {
                throw new IllegalArgumentException("Path is mandatory for subdoc exists");
            }
            this.specs.add(new LookupSpec(Lookup.EXIST, path));
        }
        return this;
    }

    /**
     * Check if a value exists inside the document (if it does not, attempting to get the
     * {@link DocumentFragment#content(int)} will raise an error).
     * This doesn't transmit the value on the wire if it exists, saving the corresponding byte overhead.
     *
     * @param path the path inside the document to check for existence.
     * @param optionsBuilder {@link SubdocOptionsBuilder}
     * @return this builder for chaining.
     */
    public AsyncLookupInBuilder exists(String path, SubdocOptionsBuilder optionsBuilder) {
        if (path == null) {
            throw new IllegalArgumentException("Path is mandatory for subdoc exists");
        }
        if (optionsBuilder.createParents()) {
            throw new IllegalArgumentException("Options createParents are not supported for lookup");
        }
        this.specs.add(new LookupSpec(Lookup.EXIST, path, optionsBuilder));
        return this;
    }

    /**
     * Check if a value exists inside the document (if it does not, attempting to get the
     * {@link DocumentFragment#content(int)} will raise an error).
     * This doesn't transmit the value on the wire if it exists, saving the corresponding byte overhead.
     *
     * @param paths the path inside the document to check for existence.
     * @param optionsBuilder {@link SubdocOptionsBuilder}
     * @return this builder for chaining.
     */
    public AsyncLookupInBuilder exists(Iterable<String> paths, SubdocOptionsBuilder optionsBuilder) {
        if (paths == null) {
            throw new IllegalArgumentException("Path is mandatory for subdoc exists");
        }
        if (optionsBuilder.createParents()) {
            throw new IllegalArgumentException("Options createParents are not supported for lookup");
        }
        for (String path : paths) {
            if (StringUtil.isNullOrEmpty(path)) {
                throw new IllegalArgumentException("Path is mandatory for subdoc exists");
            }
            this.specs.add(new LookupSpec(Lookup.EXIST, path, optionsBuilder));
        }
        return this;
    }

    protected Observable<DocumentFragment<Lookup>> doSingleLookup(LookupSpec spec) {
        if (spec.lookup() == Lookup.GET) {
            return getIn(docId, spec, Object.class);
        } else if (spec.lookup() == Lookup.EXIST) {
            return existsIn(docId, spec);
        }
        return Observable.error(new UnsupportedOperationException("Lookup type " + spec.lookup() + " unknown"));
    }

    private final Func1<MultiResult<Lookup>, SubdocOperationResult<Lookup>> multiCoreResultToLookupResult
        = new Func1<MultiResult<Lookup>, SubdocOperationResult<Lookup>>() {
        @Override
        public SubdocOperationResult<Lookup> call(MultiResult<Lookup> lookupResult) {
            String path = lookupResult.path();
            Lookup operation = lookupResult.operation();
            ResponseStatus status = lookupResult.status();
            boolean isExist = operation == Lookup.EXIST;
            boolean isSuccess = status.isSuccess();
            boolean isNotFound = status == ResponseStatus.SUBDOC_PATH_NOT_FOUND;

            try {
                if (isExist && isSuccess) {
                    return SubdocOperationResult.createResult(path, operation, status, true);
                } else if (isExist && isNotFound) {
                    return SubdocOperationResult.createResult(path, operation, status, false);
                } else if (!isExist && isSuccess) {
                    try  {
                        byte[] raw = null;
                        if (isIncludeRaw()) {
                            //make a copy of the bytes
                            TranscoderUtils.ByteBufToArray rawData = TranscoderUtils.byteBufToByteArray(lookupResult.value());
                            raw = Arrays.copyOfRange(rawData.byteArray, rawData.offset, rawData.offset + rawData.length);
                        }
                        //generic, so will transform dictionaries into JsonObject and arrays into JsonArray
                        Object content = subdocumentTranscoder.decode(lookupResult.value(), Object.class);
                        return SubdocOperationResult.createResult(path, operation, status, content, raw);
                    } catch (TranscodingException e) {
                        LOGGER.error("Couldn't decode multi-lookup " + operation + " for " + docId + "/" + path, e);
                        return SubdocOperationResult.createFatal(path, operation, e);
                    }
                } else if (!isExist && isNotFound) {
                    return SubdocOperationResult.createResult(path, operation, status, null);
                } else {
                    return SubdocOperationResult
                            .createError(path, operation, status, SubdocHelper.commonSubdocErrors(status, docId, path));
                }
            } finally {
                if (lookupResult.value() != null) {
                    lookupResult.value().release();
                }
            }
        }
    };

    protected Observable<DocumentFragment<Lookup>> doMultiLookup() {
        if (specs.isEmpty()) {
            throw new IllegalArgumentException("At least one Lookup Command is necessary for lookupIn");
        }
        final LookupSpec[] lookupSpecs = specs.toArray(new LookupSpec[specs.size()]);

        return deferAndWatch(new Func0<Observable<MultiLookupResponse>>() {
            @Override
            public Observable<MultiLookupResponse> call() {
                return core.send(new SubMultiLookupRequest(docId, bucketName, lookupSpecs));
            }
        }).filter(new Func1<MultiLookupResponse, Boolean>() {
            @Override
            public Boolean call(MultiLookupResponse response) {
                if (response.status().isSuccess() || response.status() == ResponseStatus.SUBDOC_MULTI_PATH_FAILURE) {
                    return true;
                }

                if (response.content() != null && response.content().refCnt() > 0) {
                    response.content().release();
                }

                throw SubdocHelper.commonSubdocErrors(response.status(), docId, "MULTI-LOOKUP");
            }
        }).flatMap(new Func1<MultiLookupResponse, Observable<DocumentFragment<Lookup>>>() {
            @Override
            public Observable<DocumentFragment<Lookup>> call(final MultiLookupResponse mlr) {
                return Observable
                    .from(mlr.responses()).map(multiCoreResultToLookupResult)
                    .toList()
                    .map(new Func1<List<SubdocOperationResult<Lookup>>, DocumentFragment<Lookup>>() {
                        @Override
                        public DocumentFragment<Lookup> call(List<SubdocOperationResult<Lookup>> lookupResults) {
                            return new DocumentFragment<Lookup>(docId, mlr.cas(), null, lookupResults);
                        }
                    });
            }
        });
    }


    private <T> Observable<DocumentFragment<Lookup>> getIn(final String id, final LookupSpec spec, final Class<T> fragmentType) {
        return deferAndWatch(new Func0<Observable<SimpleSubdocResponse>>() {
            @Override
            public Observable<SimpleSubdocResponse> call() {
                SubGetRequest request = new SubGetRequest(id, spec.path(), bucketName);
                request.xattr(spec.xattr());
                return core.send(request);
            }
        }).map(new Func1<SimpleSubdocResponse, DocumentFragment<Lookup>>() {
            @Override
            public DocumentFragment<Lookup> call(SimpleSubdocResponse response) {
                if (response.status().isSuccess()) {
                    try {
                        byte[] raw = null;
                        if (isIncludeRaw()) {
                            TranscoderUtils.ByteBufToArray rawData = TranscoderUtils.byteBufToByteArray(response.content());
                            raw = Arrays.copyOfRange(rawData.byteArray, rawData.offset, rawData.offset + rawData.length);
                        }
                        T content = subdocumentTranscoder.decodeWithMessage(response.content(), fragmentType,
                                "Couldn't decode subget fragment for " + id + "/" + spec.path());
                        SubdocOperationResult<Lookup> single = SubdocOperationResult
                                .createResult(spec.path(), Lookup.GET, response.status(), content, raw);
                        return new DocumentFragment<Lookup>(id, response.cas(), response.mutationToken(),
                                Collections.singletonList(single));
                    } finally {
                        if (response.content() != null) {
                            response.content().release();
                        }
                    }
                } else {
                    if (response.content() != null && response.content().refCnt() > 0) {
                        response.content().release();
                    }

                    if (response.status() == ResponseStatus.SUBDOC_PATH_NOT_FOUND) {
                        SubdocOperationResult<Lookup> single = SubdocOperationResult
                                .createResult(spec.path(), Lookup.GET, response.status(), null);
                        return new DocumentFragment<Lookup>(id, response.cas(), response.mutationToken(), Collections.singletonList(single));
                    } else {
                        throw SubdocHelper.commonSubdocErrors(response.status(), id, spec.path());
                    }
                }
            }
        });
    }

    private Observable<DocumentFragment<Lookup>> existsIn(final String id, final LookupSpec spec) {
        return deferAndWatch(new Func0<Observable<SimpleSubdocResponse>>() {
            @Override
            public Observable<SimpleSubdocResponse> call() {
                SubExistRequest request = new SubExistRequest(id, spec.path(), bucketName);
                request.xattr(spec.xattr());
                return core.send(request);
            }
        }).map(new Func1<SimpleSubdocResponse, DocumentFragment<Lookup>>() {
            @Override
            public DocumentFragment<Lookup> call(SimpleSubdocResponse response) {
                if (response.content() != null && response.content().refCnt() > 0) {
                    response.content().release();
                }

                if (response.status().isSuccess()) {
                    SubdocOperationResult<Lookup> result = SubdocOperationResult
                            .createResult(spec.path(), Lookup.EXIST, response.status(), true);
                    return new DocumentFragment<Lookup>(docId, response.cas(), response.mutationToken(), Collections.singletonList(result));
                } else if (response.status() == ResponseStatus.SUBDOC_PATH_NOT_FOUND) {
                    SubdocOperationResult<Lookup> result = SubdocOperationResult
                            .createResult(spec.path(), Lookup.EXIST, response.status(), false);
                    return new DocumentFragment<Lookup>(docId, response.cas(), response.mutationToken(), Collections.singletonList(result));
                }

                throw SubdocHelper.commonSubdocErrors(response.status(), id, spec.path());
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("lookupIn(").append(docId).append(")[");
        int pos = sb.length();
        for (LookupSpec spec : specs) {
            sb.append(", ").append(spec);
        }
        sb.delete(pos, pos+2);
        sb.append(']');
        return sb.toString();
    }
}
