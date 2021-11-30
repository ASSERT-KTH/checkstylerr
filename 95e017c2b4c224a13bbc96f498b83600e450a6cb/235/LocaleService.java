/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (C) 2021  Plugily Projects - maintained by 2Wild4You, Tigerpanzer_02 and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.villagedefense.utils.services.locale;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.villagedefense.utils.services.ServiceRegistry;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Localization service used for fetching latest locales for minigames
 */
public class LocaleService {

  private JavaPlugin plugin;
  private FileConfiguration localeData;

  public LocaleService(JavaPlugin plugin) {
    if (ServiceRegistry.getRegisteredService() == null || !ServiceRegistry.getRegisteredService().equals(plugin)) {
      throw new IllegalArgumentException("LocaleService cannot be used without registering service via ServiceRegistry first!");
    }
    if (!ServiceRegistry.isServiceEnabled()) {
      return;
    }
    this.plugin = plugin;
    try (Scanner scanner = new Scanner(requestLocaleFetch(null), "UTF-8").useDelimiter("\\A")) {
      String data = scanner.hasNext() ? scanner.next() : "";
      File file = new File(plugin.getDataFolder().getPath() + "/locales/locale_data.yml");
      if (!file.exists()) {
        new File(plugin.getDataFolder().getPath() + "/locales").mkdir();
        if (!file.createNewFile()) {
          plugin.getLogger().log(Level.WARNING, "Couldn't create locales folder! We must disable locales support.");
          return;
        }
      }
      Files.write(file.toPath(), data.getBytes());
      this.localeData = ConfigUtils.getConfig(plugin, "/locales/locale_data");
      plugin.getLogger().log(Level.INFO, "Fetched latest localization file from repository.");
    } catch (IOException ignored) {
      //ignore exceptions
      plugin.getLogger().log(Level.WARNING, "Couldn't access locale fetcher service or there is other problem! You should notify author!");
    }
  }

  private static String toReadable(String version) {
    String[] split = Pattern.compile(".", Pattern.LITERAL).split(version.replace("v", ""));
    StringBuilder versionBuilder = new StringBuilder();
    for (String s : split) {
      versionBuilder.append(String.format("%4s", s));
    }
    version = versionBuilder.toString();
    return version;
  }

  private InputStream requestLocaleFetch(Locale locale) {
    try {
      URL url = new URL("https://api.plugily.xyz/locale/v2/fetch.php");
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("User-Agent", "PLLocale/1.0");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Accept-Charset", "UTF-8");
      conn.setDoOutput(true);

      OutputStream os = conn.getOutputStream();
      if (locale == null) {
        os.write(("pass=localeservice&type=" + plugin.getName()).getBytes(StandardCharsets.UTF_8));
      } else {
        os.write(("pass=localeservice&type=" + plugin.getName() + "&locale=" + locale.getPrefix()).getBytes(StandardCharsets.UTF_8));
      }
      os.flush();
      os.close();
      return conn.getInputStream();
    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Could not fetch locale from plugily.xyz api! Cause: {0} ({1})", new Object[]{e.getCause(), e.getMessage()});
      return new InputStream() {
        @Override
        public int read() {
          return -1;
        }
      };
    }
  }

  /**
   * Sends a demand request to download latest locale from Plugily-Projects/locale_storage repository
   * Whole repository can be seen here https://github.com/Plugily-Projects/locale_storage
   *
   * @param locale locale to download
   * @return SUCCESS for downloaded locale, FAIL for service fault, LATEST when locale is latest as one in repository
   */
  public DownloadStatus demandLocaleDownload(Locale locale) {
    //service fault
    if (localeData == null) {
      return DownloadStatus.FAIL;
    }
    File localeFile = new File(plugin.getDataFolder() + "/locales/" + locale.getPrefix() + ".properties");
    if (!localeFile.exists() || !isExact(locale, localeFile)) {
      return writeFile(locale);
    }
    return DownloadStatus.LATEST;
  }

  private DownloadStatus writeFile(Locale locale) {
    try (Scanner scanner = new Scanner(requestLocaleFetch(locale), "UTF-8").useDelimiter("\\A")) {
      String data = scanner.hasNext() ? scanner.next() : "";
      try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(plugin.getDataFolder().getPath() + "/locales/" + locale.getPrefix() + ".properties")), StandardCharsets.UTF_8)) {
        writer.write(data);
      }
      return DownloadStatus.SUCCESS;
    } catch (IOException ignored) {
      plugin.getLogger().log(Level.WARNING, "Demanded locale " + locale.getPrefix() + " cannot be downloaded! You should notify author!");
      return DownloadStatus.FAIL;
    }
  }

  /**
   * Checks if plugin version allows to update locale
   *
   * @return true if locale can be updated for this version else cannot
   */
  public boolean isValidVersion() {
    //service fault
    if (localeData == null) {
      return false;
    }
    return !checkHigher(plugin.getDescription().getVersion(), localeData.getString("locales.valid-version", plugin.getDescription().getVersion()));
  }

  private boolean isExact(Locale locale, File file) {
    try (Scanner scanner = new Scanner(requestLocaleFetch(locale), "UTF-8").useDelimiter("\\A");
         Scanner localScanner = new Scanner(file, "UTF-8").useDelimiter("\\A")) {
      String onlineData = scanner.hasNext() ? scanner.next() : "";
      String localData = localScanner.hasNext() ? localScanner.next() : "";

      return onlineData.equals(localData);
    } catch (IOException ignored) {
      return false;
    }
  }

  private boolean checkHigher(String currentVersion, String newVersion) {
    String current = toReadable(currentVersion);
    String newVer = toReadable(newVersion);
    return current.compareTo(newVer) < 0;
  }

  /**
   * Download status enum for locale download demands
   */
  public enum DownloadStatus {
    SUCCESS, FAIL, LATEST
  }

}
