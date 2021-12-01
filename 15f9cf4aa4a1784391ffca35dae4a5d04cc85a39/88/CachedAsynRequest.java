/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.shared.client.common;

import java.util.LinkedList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class CachedAsynRequest<T> {

  private T cached = null;
  private boolean loadingFromServer = false;
  private final LinkedList<AsyncCallback<T>> waitQueue = new LinkedList<AsyncCallback<T>>();

  public abstract void getFromServer(AsyncCallback<T> callback);

  public void clearCache() {
    this.cached = null;
  }

  public void setCached(T cached) {
    this.cached = cached;
  }

  public void request(AsyncCallback<T> callback) {
    if (cached != null) {
      callback.onSuccess(cached);
    } else {
      waitQueue.add(callback);
      ensureIsLoadingFromServer();
    }
  }

  private void ensureIsLoadingFromServer() {
    if (!loadingFromServer) {
      loadingFromServer = true;
      getFromServer(new AsyncCallback<T>() {

        @Override
        public void onFailure(Throwable caught) {
          while (!waitQueue.isEmpty()) {
            waitQueue.pop().onFailure(caught);
          }
          loadingFromServer = false;
        }

        @Override
        public void onSuccess(T result) {
          cached = result;
          while (!waitQueue.isEmpty()) {
            waitQueue.pop().onSuccess(result);
          }
          loadingFromServer = false;
        }
      });

    }
  }
}
