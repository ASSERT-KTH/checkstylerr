/*
 *
 * BuildBattle - Ultimate building competition minigame
 * Copyright (C) 2021 Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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

package plugily.projects.buildbattle.utils;

import org.bukkit.Bukkit;
import plugily.projects.buildbattle.Main;
import plugily.projects.buildbattle.utils.services.exception.ReportedException;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Plajer
 * <p>
 * Created at 23.03.2019
 */
public class ExceptionLogHandler extends Handler {

  //these classes if found in stacktraces won't be reported
  //to the Error Service
  private final String[] blacklistedClasses = {"plugily.projects.buildbattle.user.data.MysqlManager", "plugily.projects.buildbattle.plajerlair.commonsbox.database.MysqlDatabase"};
  private final Main plugin;

  public ExceptionLogHandler(Main plugin) {
    this.plugin = plugin;
    Bukkit.getLogger().addHandler(this);
  }

  @Override
  public void close() {
    //unused
  }

  @Override
  public void flush() {
    //unused
  }

  @Override
  public void publish(LogRecord record) {
    Throwable throwable = record.getThrown();
    if(!(throwable instanceof Exception) || !throwable.getClass().getSimpleName().contains("Exception") || throwable.getStackTrace().length == 0) {
      return;
    }
    if(throwable.getCause() != null && (throwable.getCause().getStackTrace().length == 0 ||
        throwable.getCause().getStackTrace()[0] == null || !throwable.getCause().getStackTrace()[0].getClassName().contains("plugily.projects.buildbattle"))) {
      return;
    }
    if(!throwable.getStackTrace()[0].getClassName().contains("plugily.projects.buildbattle") || containsBlacklistedClass(throwable)) {
      return;
    }
    new ReportedException(plugin, (Exception) throwable);
    record.setThrown(null);
    record.setMessage("[BuildBattle] We have found a bug in the code. Contact us at our official discord server (Invite link: https://discordapp.com/invite/UXzUdTP) with the following error given above!");
  }

  private boolean containsBlacklistedClass(Throwable throwable) {
    for(StackTraceElement element : throwable.getStackTrace()) {
      for(String blacklist : blacklistedClasses) {
        if(element.getClassName().contains(blacklist)) {
          return true;
        }
      }
    }
    return false;
  }

}
