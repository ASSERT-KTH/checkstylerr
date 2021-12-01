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

package plugily.projects.thebridge.commands.arguments.admin.arena;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;
import plugily.projects.thebridge.ConfigPreferences;
import plugily.projects.thebridge.arena.Arena;
import plugily.projects.thebridge.arena.ArenaManager;
import plugily.projects.thebridge.arena.ArenaRegistry;
import plugily.projects.thebridge.commands.arguments.ArgumentsRegistry;
import plugily.projects.thebridge.commands.arguments.data.CommandArgument;
import plugily.projects.thebridge.commands.arguments.data.LabelData;
import plugily.projects.thebridge.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.thebridge.handlers.ChatManager;
import plugily.projects.thebridge.handlers.language.LanguageManager;
import plugily.projects.thebridge.utils.Debugger;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 18.05.2019
 */
public class ReloadArgument {

  private final Set<CommandSender> confirmations = new HashSet<>();

  public ReloadArgument(ArgumentsRegistry registry, ChatManager chatManager) {
    registry.mapArgument("thebridgeadmin", new LabeledCommandArgument("reload", "thebridge.admin.reload", CommandArgument.ExecutorType.BOTH,
      new LabelData("/tba reload", "/tba reload", "&7Reload all game arenas and configurations\n&7&lArenas will be stopped!\n&6Permission: &7thebridge.admin.reload")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (!confirmations.contains(sender)) {
          confirmations.add(sender);
          Bukkit.getScheduler().runTaskLater(registry.getPlugin(), () -> confirmations.remove(sender), 20 * 10);
          sender.sendMessage(chatManager.getPrefix() + chatManager.colorRawMessage("&cAre you sure you want to do this action? Type the command again &6within 10 seconds &cto confirm!"));
          return;
        }
        confirmations.remove(sender);
        Debugger.debug(Level.INFO, "Initiated plugin reload by {0}", sender.getName());
        long start = System.currentTimeMillis();

        registry.getPlugin().reloadConfig();
        LanguageManager.reloadConfig();

        for (Arena arena : ArenaRegistry.getArenas()) {
          Debugger.debug(Level.INFO, "[Reloader] Stopping {0} instance.");
          long stopTime = System.currentTimeMillis();
          for (Player player : arena.getPlayers()) {
            arena.doBarAction(Arena.BarAction.REMOVE, player);
            arena.teleportToEndLocation(player);
            if (registry.getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
              InventorySerializer.loadInventory(registry.getPlugin(), player);
            } else {
              player.getInventory().clear();
              player.getInventory().setArmorContents(null);
              player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
              player.setWalkSpeed(0.2f);
            }
          }
          ArenaManager.stopGame(true, arena);
          Debugger.debug(Level.INFO, "[Reloader] Instance {0} stopped took {1}ms", arena.getId(), System.currentTimeMillis() - stopTime);
        }
        ArenaRegistry.registerArenas();
        sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.Admin-Commands.Success-Reload"));
        Debugger.debug(Level.INFO, "[Reloader] Finished reloading took {0}ms", System.currentTimeMillis() - start);
      }
    });
  }

}
