/*
 * Copyright 2018 Google LLC
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
package com.google.cloud.bigtable.data.v2.stub.mutaterows;

import com.google.api.core.ApiFunction;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.core.InternalApi;
import com.google.api.gax.rpc.ApiCallContext;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.bigtable.v2.MutateRowsRequest;
import com.google.bigtable.v2.MutateRowsResponse;
import com.google.cloud.bigtable.data.v2.internal.RequestContext;
import com.google.cloud.bigtable.data.v2.models.RowMutation;

/**
 * Simple wrapper for BulkMutations to wrap the request and response protobufs.
 *
 * <p>This class is considered an internal implementation detail and not meant to be used by
 * applications.
 */
@InternalApi
public class MutateRowsUserFacingCallable extends UnaryCallable<RowMutation, Void> {
  private final UnaryCallable<MutateRowsRequest, MutateRowsResponse> inner;
  private final RequestContext requestContext;

  public MutateRowsUserFacingCallable(
      UnaryCallable<MutateRowsRequest, MutateRowsResponse> inner, RequestContext requestContext) {

    this.inner = inner;
    this.requestContext = requestContext;
  }

  @Override
  public ApiFuture<Void> futureCall(RowMutation request, ApiCallContext context) {
    ApiFuture<MutateRowsResponse> rawResponse =
        inner.futureCall(request.toBulkProto(requestContext), context);

    return ApiFutures.transform(
        rawResponse,
        new ApiFunction<MutateRowsResponse, Void>() {
          @Override
          public Void apply(MutateRowsResponse mutateRowsResponse) {
            return null;
          }
        });
  }
}
