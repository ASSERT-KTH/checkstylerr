/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.enrichment.adapters.geo;

import ch.hsr.geohash.WGS84Point;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum GeoLiteDatabase {
  INSTANCE;

  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String GEO_HDFS_FILE = "geo.hdfs.file";
  public static final String GEO_HDFS_FILE_DEFAULT = "/apps/metron/geo/default/GeoLite2-City.mmdb.gz";

  private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private static final Lock readLock = lock.readLock();
  private static final Lock writeLock = lock.writeLock();
  private static InetAddressValidator ipvalidator = new InetAddressValidator();
  private static volatile String hdfsLoc = GEO_HDFS_FILE_DEFAULT;
  private static DatabaseReader reader = null;

  public enum GeoProps {
    LOC_ID("locID"),
    COUNTRY("country"),
    CITY("city"),
    POSTAL_CODE("postalCode"),
    DMA_CODE("dmaCode"),
    LATITUDE("latitude"),
    LONGITUDE("longitude"),
    LOCATION_POINT("location_point"),
    ;
    Function<Map<String, String>, String> getter;
    String simpleName;

    GeoProps(String simpleName) {
      this(simpleName, m -> m.get(simpleName));
    }

    GeoProps(String simpleName,
             Function<Map<String, String>, String> getter
    ) {
      this.simpleName = simpleName;
      this.getter = getter;
    }
    public String getSimpleName() {
      return simpleName;
    }

    public String get(Map<String, String> map) {
      return getter.apply(map);
    }

    public void set(Map<String, String> map, String val) {
      map.put(simpleName, val);
    }
  }

  public synchronized void updateIfNecessary(Map<String, Object> globalConfig) {
    // Reload database if necessary (file changes on HDFS)
    LOG.trace("[Metron] Determining if GeoIpDatabase update required");
    String hdfsFile = GEO_HDFS_FILE_DEFAULT;
    if (globalConfig != null) {
      hdfsFile = (String) globalConfig.getOrDefault(GEO_HDFS_FILE, GEO_HDFS_FILE_DEFAULT);
    }

    // Always update if we don't have a DatabaseReader
    if (reader == null || !hdfsLoc.equals(hdfsFile)) {
      // Update
      hdfsLoc = hdfsFile;
      update(hdfsFile);
    } else {
      LOG.trace("[Metron] Update to GeoIpDatabase unnecessary");
    }
  }

  @SuppressWarnings("unchecked")
  public void update(String hdfsFile) {
    // If nothing is set (or it's been unset, use the defaults)
    if (hdfsFile == null || hdfsFile.isEmpty()) {
      LOG.debug("[Metron] Using default for {}: {}", GEO_HDFS_FILE, GEO_HDFS_FILE_DEFAULT);
      hdfsFile = GEO_HDFS_FILE_DEFAULT;
    }

    FileSystem fs;
    try {
      fs = FileSystem.get(new Configuration());
    } catch (IOException e) {
      LOG.error("[Metron] Unable to retrieve get HDFS FileSystem");
      throw new IllegalStateException("[Metron] Unable to get HDFS FileSystem");
    }

    try (GZIPInputStream gis = new GZIPInputStream(fs.open(new Path(hdfsFile)))) {
      writeLock.lock();
      LOG.info("[Metron] Update to GeoIP data started with {}", hdfsFile);
      // InputStream based DatabaseReaders are always in memory.
      DatabaseReader newReader = new DatabaseReader.Builder(gis).withCache(new CHMCache()).build();
      DatabaseReader oldReader = reader;
      reader = newReader;
      // If we've never set a reader, don't close the old one
      if (oldReader != null) {
        oldReader.close();
      }
      LOG.info("[Metron] Finished update to GeoIP data started with {}", hdfsFile);
    } catch (IOException e) {
      LOG.error("[Metron] Unable to open new database file {}", hdfsFile, e);
      throw new IllegalStateException("[Metron] Unable to update MaxMind database");
    } finally {
      // Don't unlock if the try failed
      if (lock.isWriteLocked()) {
        writeLock.unlock();
      }
    }
  }

  // Optional.empty means that we don't have any geo location in database.
  // Optional exists, but empty means local IP (valid, but no info will be in the DB)
  @SuppressWarnings("unchecked")
  public Optional<HashMap<String, String>> get(String ip) {
    // Call get every single time, returns current version. Updates behind the scenes.
    LOG.trace("[Metron] Called GeoIpDatabase.get({})", ip);
    InetAddress addr = null;
    try {
      addr = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      LOG.warn("[Metron] No result found for IP {}", ip, e);
      return Optional.empty();
    }
    if (isIneligibleAddress(ip, addr)) {
      LOG.debug("[Metron] IP ineligible for GeoLite2 lookup {}", ip);
      return Optional.empty();
    }

    try {
      readLock.lock();
      addr = InetAddress.getByName(ip);
      CityResponse cityResponse = reader.city(addr);
      HashMap<String, String> geoInfo = new HashMap<>();

      Country country = cityResponse.getCountry();
      City city = cityResponse.getCity();
      Postal postal = cityResponse.getPostal();
      Location location = cityResponse.getLocation();

      GeoProps.LOC_ID.set(geoInfo, convertNullToEmptyString(city.getGeoNameId()));
      GeoProps.COUNTRY.set(geoInfo, convertNullToEmptyString(country.getIsoCode()));
      GeoProps.CITY.set(geoInfo, convertNullToEmptyString(city.getName()));
      GeoProps.POSTAL_CODE.set(geoInfo, convertNullToEmptyString(postal.getCode()));
      GeoProps.DMA_CODE.set(geoInfo, convertNullToEmptyString(location.getMetroCode()));

      Double latitudeRaw = location.getLatitude();
      String latitude = convertNullToEmptyString(latitudeRaw);
      GeoProps.LATITUDE.set(geoInfo, latitude);

      Double longitudeRaw = location.getLongitude();
      String longitude = convertNullToEmptyString(longitudeRaw);
      GeoProps.LONGITUDE.set(geoInfo, longitude);

      if (latitudeRaw == null || longitudeRaw == null) {
        GeoProps.LOCATION_POINT.set(geoInfo, "");
      } else {
        GeoProps.LOCATION_POINT.set(geoInfo, latitude + "," + longitude);
      }

      return Optional.of(geoInfo);
    } catch (UnknownHostException | AddressNotFoundException e) {
      LOG.debug("[Metron] No result found for IP {}", ip);
    } catch (GeoIp2Exception | IOException e) {
      LOG.warn("[Metron] GeoLite2 DB encountered an error", e);
    } finally {
      readLock.unlock();
    }
    return Optional.empty();
  }

  public Optional<WGS84Point> toPoint(Map<String, String> geoInfo) {
    String latitude = GeoProps.LATITUDE.get(geoInfo);
    String longitude = GeoProps.LONGITUDE.get(geoInfo);
    if(latitude == null || longitude == null) {
      return Optional.empty();
    }

    try {
      double latD = Double.parseDouble(latitude.toString());
      double longD = Double.parseDouble(longitude.toString());
      return Optional.of(new WGS84Point(latD, longD));
    } catch (NumberFormatException nfe) {
      LOG.warn(String.format("Invalid lat/long: %s/%s: %s", latitude, longitude, nfe.getMessage()), nfe);
      return Optional.empty();
    }
  }

  protected String convertNullToEmptyString(Object raw) {
    return raw == null ? "" : String.valueOf(raw);
  }

  private boolean isIneligibleAddress(String ipStr, InetAddress addr) {
    return addr.isAnyLocalAddress() || addr.isLoopbackAddress()
            || addr.isSiteLocalAddress() || addr.isMulticastAddress()
            || !ipvalidator.isValidInet4Address(ipStr);
  }
}
