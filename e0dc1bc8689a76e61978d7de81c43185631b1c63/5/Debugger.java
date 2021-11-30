/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2020  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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

package plugily.projects.murdermystery.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.handlers.ChatManager;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Plajer
 * <p>
 * Created at 06.04.2019
 */
public class Debugger {

  private static final HashSet<String> listenedPerformance = new HashSet<>();
  private static boolean enabled = false;
  private static boolean deep = false;
  private static final Logger logger = Logger.getLogger("Murder Mystery");
  private static final Main plugin = JavaPlugin.getPlugin(Main.class);

  public static void setEnabled(boolean enabled) {
    Debugger.enabled = enabled;
  }

  public static void deepDebug(boolean deep) {
    Debugger.deep = deep;
  }

  public static void monitorPerformance(String task) {
    listenedPerformance.add(task);
  }

  public static void sendConsoleMsg(String msg) {
    if (msg.contains("#") && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_16_R1)) {
      msg = Utils.matchColorRegex(msg);
    }

    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
  }

  public static void debug(String msg) {
    debug(Level.INFO, msg);
  }

  /**
   * Prints debug message with selected log level.
   * Messages of level INFO or TASK won't be posted if
   * debugger is enabled, warnings and errors will be.
   *
   * @param level level of debugged message
   * @param msg   debugged message
   */
  public static void debug(Level level, String msg) {
    if (!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
      return;
    }
    logger.log(level, "[MMDBG] " + msg);
  }

  public static void debug(String msg, Object... params) {
    debug(Level.INFO, msg, params);
  }

  /**
   * Prints debug message with selected log level and replaces parameters.
   * Messages of level INFO or TASK won't be posted if
   * debugger is enabled, warnings and errors will be.
   *
   * @param level level of debugged message
   * @param msg   debugged message
   */
  public static void debug(Level level, String msg, Object... params) {
    if (!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
      return;
    }
    logger.log(level, "[MMDBG] " + msg, params);
  }

  /**
   * Prints performance debug message with selected log level and replaces parameters.
   *
   * @param msg debugged message
   */
  public static void performance(String monitorName, String msg, Object... params) {
    if (!deep || !listenedPerformance.contains(monitorName)) {
      return;
    }
    logger.log(Level.INFO, "[MMDBG] " + msg, params);
  }

}
