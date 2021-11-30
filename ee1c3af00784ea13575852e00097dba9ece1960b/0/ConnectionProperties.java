package com.rockset.jdbc;

import static com.rockset.jdbc.AbstractConnectionProperty.checkedPredicate;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

final class ConnectionProperties {
  public static final ConnectionProperty<String> USER = new User();
  public static final ConnectionProperty<String> PASSWORD = new Password();
  public static final ConnectionProperty<HostAndPort> SOCKS_PROXY = new SocksProxy();
  public static final ConnectionProperty<HostAndPort> HTTP_PROXY = new HttpProxy();
  public static final ConnectionProperty<Boolean> SSL = new Ssl();
  public static final ConnectionProperty<String> SSL_TRUST_STORE_PATH =
      new SslTrustStorePath();
  public static final ConnectionProperty<String> SSL_TRUST_STORE_PASSWORD =
      new SslTrustStorePassword();

  private static final Set<ConnectionProperty<?>> ALL_PROPERTIES =
      ImmutableSet.<ConnectionProperty<?>>builder()
      .add(USER)
      .add(PASSWORD)
      .add(SOCKS_PROXY)
      .add(HTTP_PROXY)
      .add(SSL)
      .add(SSL_TRUST_STORE_PATH)
      .add(SSL_TRUST_STORE_PASSWORD)
      .build();

  private static final Map<String, ConnectionProperty<?>> KEY_LOOKUP =
      unmodifiableMap(ALL_PROPERTIES.stream()
      .collect(toMap(ConnectionProperty::getKey, identity())));

  private static final Map<String, String> DEFAULTS;

  static {
    ImmutableMap.Builder<String, String> defaults = ImmutableMap.builder();
    for (ConnectionProperty<?> property : ALL_PROPERTIES) {
      property.getDefault().ifPresent(value -> defaults.put(property.getKey(), value));
    }
    DEFAULTS = defaults.build();
  }

  private ConnectionProperties() {}

  public static ConnectionProperty<?> forKey(String propertiesKey) {
    return KEY_LOOKUP.get(propertiesKey);
  }

  public static Set<ConnectionProperty<?>> allProperties() {
    return ALL_PROPERTIES;
  }

  public static Map<String, String> getDefaults() {
    return DEFAULTS;
  }

  private static class User extends AbstractConnectionProperty<String> {
    public User() {
      super("user", REQUIRED, ALLOWED, NON_EMPTY_STRING_CONVERTER);
    }
  }

  private static class Password extends AbstractConnectionProperty<String> {
    public Password() {
      super("password", NOT_REQUIRED, ALLOWED, STRING_CONVERTER);
    }
  }

  private static class SocksProxy extends AbstractConnectionProperty<HostAndPort> {
    private static final Predicate<Properties> NO_HTTP_PROXY =
        checkedPredicate(properties -> !HTTP_PROXY.getValue(properties).isPresent());

    public SocksProxy() {
      super("socksProxy", NOT_REQUIRED, NO_HTTP_PROXY, HostAndPort::fromString);
    }
  }

  private static class HttpProxy extends AbstractConnectionProperty<HostAndPort> {

    private static final Predicate<Properties> NO_SOCKS_PROXY =
        checkedPredicate(properties -> !SOCKS_PROXY.getValue(properties).isPresent());

    public HttpProxy() {
      super("httpProxy", NOT_REQUIRED, NO_SOCKS_PROXY, HostAndPort::fromString);
    }
  }

  private static class Ssl extends AbstractConnectionProperty<Boolean> {
    public Ssl() {
      super("SSL", Optional.of("false"), NOT_REQUIRED, ALLOWED, BOOLEAN_CONVERTER);
    }
  }

  private static class SslTrustStorePath extends AbstractConnectionProperty<String> {
    private static final Predicate<Properties> IF_SSL_ENABLED =
        checkedPredicate(properties -> SSL.getValue(properties).orElse(false));

    public SslTrustStorePath() {
      super("SSLTrustStorePath", NOT_REQUIRED, IF_SSL_ENABLED, STRING_CONVERTER);
    }
  }

  private static class SslTrustStorePassword extends AbstractConnectionProperty<String> {
    private static final Predicate<Properties> IF_TRUST_STORE =
        checkedPredicate(properties -> SSL_TRUST_STORE_PATH.getValue(properties).isPresent());

    public SslTrustStorePassword() {
      super("SSLTrustStorePassword", NOT_REQUIRED, IF_TRUST_STORE, STRING_CONVERTER);
    }
  }
}
