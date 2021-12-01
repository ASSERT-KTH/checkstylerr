/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.Sets;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.HasPermissions;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.LoginStatusListener;
import org.roda.wui.common.client.tools.CachedAsynRequest;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 * 
 * 
 */
public class UserLogin {

  private static final ClientLogger logger = new ClientLogger(UserLogin.class.getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static UserLoginServiceAsync userLoginService;
  private static UserLogin instance = null;
  private static Map<String, String> rodaProperties;
  private static boolean initialized = false;
  private static boolean initializing = false;
  private static Queue<AsyncCallback<Map<String, String>>> initQueue = new LinkedList<>();

  private static void init(final AsyncCallback<Map<String, String>> callback) {
    initQueue.add(callback);

    if (!initialized && !initializing) {
      initializing = true;

      userLoginService = UserLoginService.Util.getInstance();
      userLoginService.getRodaProperties(new AsyncCallback<Map<String, String>>() {

        @Override
        public void onFailure(Throwable caught) {
          logger.fatal("Error getting role mapping", caught);
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Map<String, String> properties) {
          rodaProperties = properties;
          initialized = true;
          treatInitQueue();
        }

      });
    } else if (initialized) {
      treatInitQueue();
    }
  }

  private static void treatInitQueue() {
    while (!initQueue.isEmpty()) {
      initQueue.poll().onSuccess(rodaProperties);
    }
  }

  static {
    init(new AsyncCallback<Map<String, String>>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.error("Error initializing", caught);
      }

      @Override
      public void onSuccess(Map<String, String> properties) {
        // nothing to do
      }

    });
  }

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static UserLogin getInstance() {
    if (instance == null) {
      instance = new UserLogin();
    }
    return instance;
  }

  private final List<LoginStatusListener> listeners;

  private UserLogin() {
    listeners = new Vector<>();
  }

  private final CachedAsynRequest<User> getUserRequest = new CachedAsynRequest<User>() {

    @Override
    public void getFromServer(AsyncCallback<User> callback) {
      userLoginService.getAuthenticatedUser(callback);
    }
  };

  /**
   * Get current authenticated user. User is cached and only refreshed when login
   * or logout actions are called.
   * 
   * @param callback
   *          call back handler that receives error if failed or AuthOfficeUser if
   *          success.
   */
  public void getAuthenticatedUser(final AsyncCallback<User> callback) {
    getAuthenticatedUser(callback, false);
  }

  public void getAuthenticatedUser(final AsyncCallback<User> callback, boolean ensureIsFresh) {
    if (ensureIsFresh) {
      getUserRequest.clearCache();
    }
    getUserRequest.request(callback);
  }

  /**
   * Login into RODA Core
   */
  public void login() {
    String hash = Window.Location.getHash();
    if (hash.length() > 0) {
      hash = hash.substring(1);
      hash = URL.encodeQueryString(hash);
    }

    StringBuilder url = new StringBuilder();
    url.append(GWT.getHostPageBaseURL());
    url.append("login");
    url.append(Window.Location.getQueryString());
    url.append(Window.Location.getQueryString().isEmpty() ? "?" : "&");
    url.append("path=").append(Window.Location.getPath());
    url.append("&hash=").append(hash);

    Window.open(url.toString(), "_self", "");
  }

  public void login(String username, String password, final AsyncCallback<User> callback) {
    userLoginService.login(username, password, new AsyncCallback<User>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(User newUser) {
        getUserRequest.setCached(newUser);
        onLoginStatusChanged(newUser);
        callback.onSuccess(newUser);
      }
    });
  }

  public void logout() {
    String hash = Window.Location.getHash();
    if (hash.length() > 0) {
      hash = hash.substring(1);
      hash = UriUtils.encode(hash);
    }

    StringBuilder url = new StringBuilder();
    url.append(GWT.getHostPageBaseURL());
    url.append("logout");
    url.append(Window.Location.getQueryString());
    url.append(Window.Location.getQueryString().isEmpty() ? "?" : "&");
    url.append("path=").append(Window.Location.getPath());
    url.append("&hash=").append(hash);

    Window.open(url.toString(), "_self", "");
    getUserRequest.clearCache();
  }

  /**
   * Add a login status listener
   * 
   * @param listener
   */
  public void addLoginStatusListener(LoginStatusListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove a login status listener
   * 
   * @param listener
   */
  public void removeLoginStatusListener(LoginStatusListener listener) {
    listeners.remove(listener);
  }

  public void onLoginStatusChanged(User newUser) {
    for (LoginStatusListener listener : listeners) {
      listener.onLoginStatusChanged(newUser);
    }
  }

  /**
   * Check if the changing of the permissions of the RODA member affects current
   * authenticated user and alert login status listeners
   * 
   * @param member
   *          the member which had his permissions changed
   */
  public void permissionsChanged(final RODAMember member) {
    getAuthenticatedUser(new AsyncCallback<User>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(User user) {
        User authUser = user;
        if (member instanceof User && member.getName().equals(authUser.getName())) {
          onLoginStatusChanged(authUser);
        }

        if (member instanceof Group && Arrays.asList(authUser.getGroups()).contains(member.getName())) {
          onLoginStatusChanged(authUser);
        }
      }

    });
  }

  /**
   * Get RODA properties
   * 
   * @param callback
   */
  public static void getRodaProperties(final AsyncCallback<Map<String, String>> callback) {
    init(new AsyncCallback<Map<String, String>>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(Map<String, String> properties) {
        callback.onSuccess(properties);

      }
    });
  }

  /**
   * Get a RODA property
   * 
   * @param key
   * @param callback
   */
  public static void getRodaProperty(final String key, final AsyncCallback<String> callback) {
    init(new AsyncCallback<Map<String, String>>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(Map<String, String> properties) {
        callback.onSuccess(properties.get(key));
      }

    });
  }

  /**
   * Check if current user has permission to access a history resolver
   * 
   * @param res
   * @param callback
   */
  public void checkRole(final HistoryResolver res, final AsyncCallback<Boolean> callback) {
    String historyKey = StringUtils.join(res.getHistoryPath(), HistoryUtils.HISTORY_PERMISSION_SEP);
    final String propertyName = "ui.menu." + historyKey + ".role";
    UserLogin.getRodaProperty(propertyName, new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(final String role) {
        checkRole(role, callback);
      }
    });
  }

  public void checkRole(final String role, final AsyncCallback<Boolean> callback) {
    if (StringUtils.isBlank(role)) {
      callback.onSuccess(Boolean.TRUE);
    } else {
      getAuthenticatedUser(new AsyncCallback<User>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(User authUser) {
          callback.onSuccess(authUser.hasRole(role));
        }
      });
    }
  }

  /**
   * Check if current authenticated user can access some history resolvers
   * 
   * @param res
   * @param exclusive
   * @param callback
   */
  public void checkRoles(HistoryResolver[] res, final boolean exclusive, final AsyncCallback<Boolean> callback) {
    final Boolean[] results = new Boolean[res.length];

    for (int i = 0; i < res.length; i++) {
      final int index = i;
      checkRole(res[i], new AsyncCallback<Boolean>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Boolean rolePermitted) {
          results[index] = rolePermitted;
          boolean lastOne = true;
          for (int i = 0; i < results.length; i++) {
            if (results[i] == null) {
              lastOne = false;
            }
          }

          if (lastOne) {
            if (exclusive) {
              boolean ret = true;
              for (int i = 0; i < results.length; i++) {
                ret = results[i].booleanValue() ? ret : false;
              }
              callback.onSuccess(ret);
            } else {
              boolean ret = false;
              for (int i = 0; i < results.length; i++) {
                ret = results[i].booleanValue() ? true : ret;
              }
              callback.onSuccess(ret);
            }
          }
        }

      });
    }
  }

  public void showSuggestLoginDialog() {
    Dialogs.showConfirmDialog(messages.loginDialogTitle(), messages.casForwardWarning(), messages.loginDialogCancel(),
      messages.loginDialogLogin(), new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            UserLogin.getInstance().login();
          } else {
            HistoryUtils.newHistory(Welcome.RESOLVER);
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          HistoryUtils.newHistory(Welcome.RESOLVER);
        }
      });
  }

  public void updateLoggedUser(User u) {
    getUserRequest.setCached(u);
  }

  public Optional<User> getCachedUser() {
    return getUserRequest.getCached();
  }
}
