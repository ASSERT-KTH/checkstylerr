/*
 * TheBridge - Defend your base and try to wipe out the others
 * Copyright (C)  2021  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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
 *
 */

package plugily.projects.thebridge.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tigerpanzer_02, 2Wild4You
 * <p>
 * Created at 31.10.2020
 */
public class Debugger {

  private static final HashSet<String> listenedPerformance = new HashSet<>();
  private static boolean enabled = false;
  private static boolean deep = false;
  private static final Logger logger = Logger.getLogger("thebridge");

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
    if(msg.contains("#") && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_16_R1)) {
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
    if(!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
      return;
    }
    logger.log(level, "[TBDBG] " + msg);
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
    if(!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
      return;
    }
    logger.log(level, "[TBDBG] " + msg, params);
  }

  /**
   * Prints performance debug message with selected log level and replaces parameters.
   *
   * @param msg debugged message
   */
  public static void performance(String monitorName, String msg, Object... params) {
    if(!deep || !listenedPerformance.contains(monitorName)) {
      return;
    }
    logger.log(Level.INFO, "[TBDBG] " + msg, params);
  }

}
