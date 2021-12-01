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
package com.google.cloud.dialogflow.v2beta1.stub;

import static com.google.cloud.dialogflow.v2beta1.KnowledgeBasesClient.ListKnowledgeBasesPagedResponse;

import com.google.api.core.BetaApi;
import com.google.api.gax.core.BackgroundResource;
import com.google.api.gax.core.BackgroundResourceAggregation;
import com.google.api.gax.grpc.GrpcCallSettings;
import com.google.api.gax.grpc.GrpcStubCallableFactory;
import com.google.api.gax.rpc.ClientContext;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.dialogflow.v2beta1.CreateKnowledgeBaseRequest;
import com.google.cloud.dialogflow.v2beta1.DeleteKnowledgeBaseRequest;
import com.google.cloud.dialogflow.v2beta1.GetKnowledgeBaseRequest;
import com.google.cloud.dialogflow.v2beta1.KnowledgeBase;
import com.google.cloud.dialogflow.v2beta1.ListKnowledgeBasesRequest;
import com.google.cloud.dialogflow.v2beta1.ListKnowledgeBasesResponse;
import com.google.protobuf.Empty;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Generated;

// AUTO-GENERATED DOCUMENTATION AND CLASS
/**
 * gRPC stub implementation for Dialogflow API.
 *
 * <p>This class is for advanced usage and reflects the underlying API directly.
 */
@Generated("by gapic-generator")
@BetaApi("A restructuring of stub classes is planned, so this may break in the future")
public class GrpcKnowledgeBasesStub extends KnowledgeBasesStub {

  private static final MethodDescriptor<ListKnowledgeBasesRequest, ListKnowledgeBasesResponse>
      listKnowledgeBasesMethodDescriptor =
          MethodDescriptor.<ListKnowledgeBasesRequest, ListKnowledgeBasesResponse>newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(
                  "google.cloud.dialogflow.v2beta1.KnowledgeBases/ListKnowledgeBases")
              .setRequestMarshaller(
                  ProtoUtils.marshaller(ListKnowledgeBasesRequest.getDefaultInstance()))
              .setResponseMarshaller(
                  ProtoUtils.marshaller(ListKnowledgeBasesResponse.getDefaultInstance()))
              .build();
  private static final MethodDescriptor<GetKnowledgeBaseRequest, KnowledgeBase>
      getKnowledgeBaseMethodDescriptor =
          MethodDescriptor.<GetKnowledgeBaseRequest, KnowledgeBase>newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName("google.cloud.dialogflow.v2beta1.KnowledgeBases/GetKnowledgeBase")
              .setRequestMarshaller(
                  ProtoUtils.marshaller(GetKnowledgeBaseRequest.getDefaultInstance()))
              .setResponseMarshaller(ProtoUtils.marshaller(KnowledgeBase.getDefaultInstance()))
              .build();
  private static final MethodDescriptor<CreateKnowledgeBaseRequest, KnowledgeBase>
      createKnowledgeBaseMethodDescriptor =
          MethodDescriptor.<CreateKnowledgeBaseRequest, KnowledgeBase>newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(
                  "google.cloud.dialogflow.v2beta1.KnowledgeBases/CreateKnowledgeBase")
              .setRequestMarshaller(
                  ProtoUtils.marshaller(CreateKnowledgeBaseRequest.getDefaultInstance()))
              .setResponseMarshaller(ProtoUtils.marshaller(KnowledgeBase.getDefaultInstance()))
              .build();
  private static final MethodDescriptor<DeleteKnowledgeBaseRequest, Empty>
      deleteKnowledgeBaseMethodDescriptor =
          MethodDescriptor.<DeleteKnowledgeBaseRequest, Empty>newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(
                  "google.cloud.dialogflow.v2beta1.KnowledgeBases/DeleteKnowledgeBase")
              .setRequestMarshaller(
                  ProtoUtils.marshaller(DeleteKnowledgeBaseRequest.getDefaultInstance()))
              .setResponseMarshaller(ProtoUtils.marshaller(Empty.getDefaultInstance()))
              .build();

  private final BackgroundResource backgroundResources;

  private final UnaryCallable<ListKnowledgeBasesRequest, ListKnowledgeBasesResponse>
      listKnowledgeBasesCallable;
  private final UnaryCallable<ListKnowledgeBasesRequest, ListKnowledgeBasesPagedResponse>
      listKnowledgeBasesPagedCallable;
  private final UnaryCallable<GetKnowledgeBaseRequest, KnowledgeBase> getKnowledgeBaseCallable;
  private final UnaryCallable<CreateKnowledgeBaseRequest, KnowledgeBase>
      createKnowledgeBaseCallable;
  private final UnaryCallable<DeleteKnowledgeBaseRequest, Empty> deleteKnowledgeBaseCallable;

  private final GrpcStubCallableFactory callableFactory;

  public static final GrpcKnowledgeBasesStub create(KnowledgeBasesStubSettings settings)
      throws IOException {
    return new GrpcKnowledgeBasesStub(settings, ClientContext.create(settings));
  }

  public static final GrpcKnowledgeBasesStub create(ClientContext clientContext)
      throws IOException {
    return new GrpcKnowledgeBasesStub(
        KnowledgeBasesStubSettings.newBuilder().build(), clientContext);
  }

  public static final GrpcKnowledgeBasesStub create(
      ClientContext clientContext, GrpcStubCallableFactory callableFactory) throws IOException {
    return new GrpcKnowledgeBasesStub(
        KnowledgeBasesStubSettings.newBuilder().build(), clientContext, callableFactory);
  }

  /**
   * Constructs an instance of GrpcKnowledgeBasesStub, using the given settings. This is protected
   * so that it is easy to make a subclass, but otherwise, the static factory methods should be
   * preferred.
   */
  protected GrpcKnowledgeBasesStub(KnowledgeBasesStubSettings settings, ClientContext clientContext)
      throws IOException {
    this(settings, clientContext, new GrpcKnowledgeBasesCallableFactory());
  }

  /**
   * Constructs an instance of GrpcKnowledgeBasesStub, using the given settings. This is protected
   * so that it is easy to make a subclass, but otherwise, the static factory methods should be
   * preferred.
   */
  protected GrpcKnowledgeBasesStub(
      KnowledgeBasesStubSettings settings,
      ClientContext clientContext,
      GrpcStubCallableFactory callableFactory)
      throws IOException {
    this.callableFactory = callableFactory;

    GrpcCallSettings<ListKnowledgeBasesRequest, ListKnowledgeBasesResponse>
        listKnowledgeBasesTransportSettings =
            GrpcCallSettings.<ListKnowledgeBasesRequest, ListKnowledgeBasesResponse>newBuilder()
                .setMethodDescriptor(listKnowledgeBasesMethodDescriptor)
                .build();
    GrpcCallSettings<GetKnowledgeBaseRequest, KnowledgeBase> getKnowledgeBaseTransportSettings =
        GrpcCallSettings.<GetKnowledgeBaseRequest, KnowledgeBase>newBuilder()
            .setMethodDescriptor(getKnowledgeBaseMethodDescriptor)
            .build();
    GrpcCallSettings<CreateKnowledgeBaseRequest, KnowledgeBase>
        createKnowledgeBaseTransportSettings =
            GrpcCallSettings.<CreateKnowledgeBaseRequest, KnowledgeBase>newBuilder()
                .setMethodDescriptor(createKnowledgeBaseMethodDescriptor)
                .build();
    GrpcCallSettings<DeleteKnowledgeBaseRequest, Empty> deleteKnowledgeBaseTransportSettings =
        GrpcCallSettings.<DeleteKnowledgeBaseRequest, Empty>newBuilder()
            .setMethodDescriptor(deleteKnowledgeBaseMethodDescriptor)
            .build();

    this.listKnowledgeBasesCallable =
        callableFactory.createUnaryCallable(
            listKnowledgeBasesTransportSettings,
            settings.listKnowledgeBasesSettings(),
            clientContext);
    this.listKnowledgeBasesPagedCallable =
        callableFactory.createPagedCallable(
            listKnowledgeBasesTransportSettings,
            settings.listKnowledgeBasesSettings(),
            clientContext);
    this.getKnowledgeBaseCallable =
        callableFactory.createUnaryCallable(
            getKnowledgeBaseTransportSettings, settings.getKnowledgeBaseSettings(), clientContext);
    this.createKnowledgeBaseCallable =
        callableFactory.createUnaryCallable(
            createKnowledgeBaseTransportSettings,
            settings.createKnowledgeBaseSettings(),
            clientContext);
    this.deleteKnowledgeBaseCallable =
        callableFactory.createUnaryCallable(
            deleteKnowledgeBaseTransportSettings,
            settings.deleteKnowledgeBaseSettings(),
            clientContext);

    backgroundResources = new BackgroundResourceAggregation(clientContext.getBackgroundResources());
  }

  public UnaryCallable<ListKnowledgeBasesRequest, ListKnowledgeBasesPagedResponse>
      listKnowledgeBasesPagedCallable() {
    return listKnowledgeBasesPagedCallable;
  }

  public UnaryCallable<ListKnowledgeBasesRequest, ListKnowledgeBasesResponse>
      listKnowledgeBasesCallable() {
    return listKnowledgeBasesCallable;
  }

  public UnaryCallable<GetKnowledgeBaseRequest, KnowledgeBase> getKnowledgeBaseCallable() {
    return getKnowledgeBaseCallable;
  }

  public UnaryCallable<CreateKnowledgeBaseRequest, KnowledgeBase> createKnowledgeBaseCallable() {
    return createKnowledgeBaseCallable;
  }

  public UnaryCallable<DeleteKnowledgeBaseRequest, Empty> deleteKnowledgeBaseCallable() {
    return deleteKnowledgeBaseCallable;
  }

  @Override
  public final void close() {
    shutdown();
  }

  @Override
  public void shutdown() {
    backgroundResources.shutdown();
  }

  @Override
  public boolean isShutdown() {
    return backgroundResources.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return backgroundResources.isTerminated();
  }

  @Override
  public void shutdownNow() {
    backgroundResources.shutdownNow();
  }

  @Override
  public boolean awaitTermination(long duration, TimeUnit unit) throws InterruptedException {
    return backgroundResources.awaitTermination(duration, unit);
  }
}
