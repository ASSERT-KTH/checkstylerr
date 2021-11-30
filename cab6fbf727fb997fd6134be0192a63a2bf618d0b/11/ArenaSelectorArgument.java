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

package plugily.projects.villagedefense.commands.arguments.game;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajerlair.commonsbox.minecraft.compat.xseries.XMaterial;
import pl.plajerlair.commonsbox.minecraft.misc.stuff.ComplementAccessor;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaManager;
import plugily.projects.villagedefense.arena.ArenaRegistry;
import plugily.projects.villagedefense.commands.arguments.ArgumentsRegistry;
import plugily.projects.villagedefense.commands.arguments.data.CommandArgument;
import plugily.projects.villagedefense.commands.arguments.data.LabelData;
import plugily.projects.villagedefense.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.villagedefense.handlers.ChatManager;
import plugily.projects.villagedefense.handlers.language.LanguageManager;
import plugily.projects.villagedefense.handlers.language.Messages;
import plugily.projects.villagedefense.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * @author 2Wild4You
 * <p>
 * Created at 09.08.2020
 */
public class ArenaSelectorArgument implements Listener {

  private final ChatManager chatManager;
  private final Map<Integer, Arena> arenas = new HashMap<>();

  public ArenaSelectorArgument(ArgumentsRegistry registry, ChatManager chatManager) {
    this.chatManager = chatManager;
    registry.getPlugin().getServer().getPluginManager().registerEvents(this, registry.getPlugin());
    registry.mapArgument("villagedefense", new LabeledCommandArgument("arenas", "villagedefense.arenas", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/vd arenas", "/vd arenas", "&7Select an arena\n&6Permission: &7villagedefense.arenas")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if(ArenaRegistry.getArenas().size() == 0) {
          player.sendMessage(chatManager.colorMessage(Messages.COMMANDS_ADMIN_LIST_NO_ARENAS));
          return;
        }
        int slot = 0;
        arenas.clear();
        Inventory inventory = ComplementAccessor.getComplement().createInventory(player, Utils.serializeInt(ArenaRegistry.getArenas().size()), chatManager.colorMessage(Messages.ARENA_SELECTOR_INV_TITLE));
        for(Arena arena : ArenaRegistry.getArenas()) {
          arenas.put(slot, arena);
          ItemStack itemStack = XMaterial.matchXMaterial(registry.getPlugin().getConfig().getString("Arena-Selector.State-Item." + arena.getArenaState().getFormattedName(), "YELLOW_CONCRETE").toUpperCase()).orElse(XMaterial.YELLOW_WOOL).parseItem();

          if(itemStack == null)
            return;

          ItemMeta itemMeta = itemStack.getItemMeta();
          if(itemMeta == null) {
            return;
          }
          ComplementAccessor.getComplement().setDisplayName(itemMeta, formatItem(LanguageManager.getLanguageMessage("Arena-Selector.Item.Name"), arena));

          java.util.List<String> lore = new ArrayList<>();
          for(String string : LanguageManager.getLanguageList(Messages.ARENA_SELECTOR_ITEM_LORE.getAccessor())) {
            lore.add(formatItem(string, arena));
          }

          ComplementAccessor.getComplement().setLore(itemMeta, lore);
          itemStack.setItemMeta(itemMeta);
          inventory.addItem(itemStack);
          slot++;
        }
        player.openInventory(inventory);
      }
    });

  }

  private String formatItem(String string, Arena arena) {
    String formatted = string;
    formatted = StringUtils.replace(formatted, "%mapname%", arena.getMapName());
    int maxPlayers = arena.getMaximumPlayers();
    if(arena.getPlayers().size() >= maxPlayers) {
      formatted = StringUtils.replace(formatted, "%state%", chatManager.colorMessage(Messages.SIGNS_GAME_STATES_FULL_GAME));
    } else {
      formatted = StringUtils.replace(formatted, "%state%", arena.getArenaState().getPlaceholder());
    }
    formatted = StringUtils.replace(formatted, "%playersize%", Integer.toString(arena.getPlayers().size()));
    formatted = StringUtils.replace(formatted, "%maxplayers%", Integer.toString(maxPlayers));
    formatted = chatManager.colorRawMessage(formatted);
    return formatted;
  }

  @EventHandler
  public void onArenaSelectorMenuClick(InventoryClickEvent e) {
    if(!ComplementAccessor.getComplement().getTitle(e.getView()).equals(chatManager.colorMessage(Messages.ARENA_SELECTOR_INV_TITLE))) {
      return;
    }
    if(e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
      return;
    }
    Player player = (Player) e.getWhoClicked();
    player.closeInventory();

    Arena arena = arenas.get(e.getRawSlot());
    if(arena != null) {
      ArenaManager.joinAttempt(player, arena);
    } else {
      player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage(Messages.COMMANDS_NO_ARENA_LIKE_THAT));
    }
  }

}
