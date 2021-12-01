/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
/**
 *
 */
package com.databasepreservation.common.client.widgets;

import com.databasepreservation.common.client.tools.HistoryManager;
import org.roda.core.data.exceptions.GenericException;

import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.tools.RestUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Luís Faria
 *
 */
public class HTMLWidgetWrapper extends HTML {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  public HTMLWidgetWrapper(String resourceId) {
    this(resourceId, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(Void result) {
        // do nothing
      }
    });
  }

  public HTMLWidgetWrapper(String resourceId, final AsyncCallback<Void> callback) {
    String id = resourceId;
    if (id.endsWith(".html")) {
      id = id.substring(0, id.length() - 5);
    }

    String locale = LocaleInfo.getCurrentLocale().getLocaleName();

    String localizedResourceId = id + "_" + locale + ".html";
    String defaultResourceId = id + ".html";

    RequestBuilder request = new RequestBuilder(RequestBuilder.GET,
      RestUtils.createThemeResourceUri(localizedResourceId, defaultResourceId, false).asString());

    try {
      request.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            String html = response.getText();
            HTMLWidgetWrapper.this.setHTML(html);
            callback.onSuccess(null);
          } else if (response.getStatusCode() == 404) {
            //HistoryUtils.newHistory(Theme.RESOLVER, "Error404.html");
          } else {
            logger.error("Error sending request");
            callback.onFailure(new GenericException("Error sending request"));
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          logger.error("Error sending request", exception);
          callback.onFailure(exception);
        }
      });
    } catch (RequestException exception) {
      logger.error("Error sending request", exception);
    }
  }
}
