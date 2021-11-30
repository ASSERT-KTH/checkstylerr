package org.radargun.http.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.radargun.traits.CacheInformation;

/**
 * Providing cache info over resteasy-client.
 * @author Anna Manukyan
 */
public class RESTEasyCacheInfo implements CacheInformation {
   private static final String CONTENT_TYPE = "text/plain";
   private RESTEasyCacheService service;

   public RESTEasyCacheInfo(RESTEasyCacheService service) {
      this.service = service;
   }

   @Override
   public String getDefaultCacheName() {
      return service.cacheName;
   }

   @Override
   public Collection<String> getCacheNames() {
      return Arrays.asList(service.cacheName);
   }

   @Override
   public Cache getCache(String cacheName) {
      return new Cache(service.getHttpClient());
   }

   protected class Cache implements CacheInformation.Cache {
      protected ResteasyClient httpClient;

      public Cache(ResteasyClient httpClient) {
         this.httpClient = httpClient;
      }

      @Override
      public long getOwnedSize() {
         return -1;
      }

      @Override
      public long getLocallyStoredSize() {
         return -1;
      }

      @Override
      public long getMemoryStoredSize() {
         return -1;
      }

      @Override
      public long getTotalSize() {
         long size = 0;
         if (service.isRunning()) {
            String target = service.buildUrl(null);
            Response response = null;
            String responseStr = null;
            try {
               Invocation get = service.getHttpClient().target(target).request().accept(CONTENT_TYPE).buildGet();
               response = get.invoke();

               responseStr = response.readEntity(String.class);
               size = Long.parseLong(responseStr);    //if the call was done to nodejs app, then the long size is returned

            } catch (NumberFormatException ex) {
               Matcher matcher = Pattern.compile("\"size\":(\\d+)").matcher(responseStr);
               if (matcher.find()) {
                  size = Long.parseLong(matcher.group().split(":")[1]);
               }
            } catch (Exception e) {
               throw new RuntimeException("RESTEasyCacheOperations::size request threw exception: " + target, e);
            } finally {
               if (response != null) {
                  response.close();
               }
            }
         }
         return size;
      }

      @Override
      public Map<?, Long> getStructuredSize() {
         return new HashMap<>();
      }

      @Override
      public int getNumReplicas() {
         return -1;
      }

      @Override
      public int getEntryOverhead() {
         return -1;
      }
   }
}

