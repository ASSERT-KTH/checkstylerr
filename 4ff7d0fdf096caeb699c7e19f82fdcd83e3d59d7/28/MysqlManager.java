/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery.user.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.commonsbox.database.MysqlDatabase;

/**
 * @author Plajer
 * <p>
 * Created at 03.10.2018
 */
public class MysqlManager implements UserDatabase {

  private Main plugin;
  private MysqlDatabase database;

  public MysqlManager(Main plugin) {
    this.plugin = plugin;
    database = plugin.getMysqlDatabase();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try (Connection connection = database.getConnection()) {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `playerstats` (\n"
          + "  `UUID` text NOT NULL,\n"
          + "  `name` text NOT NULL,\n"
          + "  `kills` int(11) NOT NULL DEFAULT '0',\n"
          + "  `deaths` int(11) NOT NULL DEFAULT '0',\n"
          + "  `highestscore` int(11) NOT NULL DEFAULT '0',\n"
          + "  `gamesplayed` int(11) NOT NULL DEFAULT '0',\n"
          + "  `wins` int(11) NOT NULL DEFAULT '0',\n"
          + "  `loses` int(11) NOT NULL DEFAULT '0',\n"
          + "  `contribmurderer` int(11) NOT NULL DEFAULT '1',\n"
          + "  `contribdetective` int(11) NOT NULL DEFAULT '1'\n"
          + ");");
      } catch (SQLException e) {
        e.printStackTrace();
        MessageUtils.errorOccurred();
        Bukkit.getConsoleSender().sendMessage("Cannot save contents to MySQL database!");
        Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
      }
    });
  }

  @Override
  public void saveStatistic(User user, StatsStorage.StatisticType stat) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE playerstats SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';"));
  }

  @Override
  public void loadStatistic(User user, StatsStorage.StatisticType stat) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try (Connection connection = database.getConnection()) {
        Statement statement = connection.createStatement();
        //insert into the database
        if (!statement.executeQuery("SELECT UUID from playerstats WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "'").next()) {
          insertPlayer(user.getPlayer());
        }

        ResultSet set = statement.executeQuery("SELECT " + stat.getName() + " FROM playerstats WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "'");
        if (!set.next()) {
          user.setStat(stat, 0);
          return;
        }
        user.setStat(stat, set.getInt(1));
      } catch (SQLException e) {
        e.printStackTrace();
        user.setStat(stat, 0);
      }
    });
  }

  private void insertPlayer(Player player) {
    database.executeUpdate("INSERT INTO playerstats (UUID,name) VALUES ('" + player.getUniqueId().toString() + "','" + player.getName() + "')");
  }

  public MysqlDatabase getDatabase() {
    return database;
  }
}
