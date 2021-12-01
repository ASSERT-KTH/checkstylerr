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

package plugily.projects.thebridge.kits;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.thebridge.Main;
import plugily.projects.thebridge.api.events.player.TBPlayerChooseKitEvent;
import plugily.projects.thebridge.arena.Arena;
import plugily.projects.thebridge.arena.ArenaRegistry;
import plugily.projects.thebridge.handlers.items.SpecialItem;
import plugily.projects.thebridge.handlers.items.SpecialItemManager;
import plugily.projects.thebridge.kits.basekits.Kit;
import plugily.projects.thebridge.user.User;
import plugily.projects.thebridge.utils.Utils;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 12.07.2019
 */
public class KitMenuHandler implements Listener {

  private final Main plugin;
  private final String unlockedString;
  private final String lockedString;
  private final SpecialItem kitItem;

  public KitMenuHandler(Main plugin) {
    this.plugin = plugin;
    this.kitItem = plugin.getSpecialItemManager().getSpecialItem(SpecialItemManager.SpecialItems.KIT_SELECTOR.getName());
    unlockedString = plugin.getChatManager().colorMessage("Kits.Kit-Menu.Lores.Unlocked");
    lockedString = plugin.getChatManager().colorMessage("Kits.Kit-Menu.Lores.Locked");
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public void createMenu(Player player) {
    Gui gui = new Gui(plugin, Utils.serializeInt(KitRegistry.getKits().size()) / 9, plugin.getChatManager().colorMessage("Kits.Kit-Menu.Title"));
    StaticPane pane = new StaticPane(9, gui.getRows());
    gui.addPane(pane);
    int x = 0;
    int y = 0;
    for(Kit kit : KitRegistry.getKits()) {
      ItemStack itemStack = kit.getItemStack();
      if(kit.isUnlockedByPlayer(player)) {
        itemStack = new ItemBuilder(itemStack).lore(unlockedString).build();
      } else {
        itemStack = new ItemBuilder(itemStack).lore(lockedString).build();
      }

      pane.addItem(new GuiItem(itemStack, e -> {
        e.setCancelled(true);
        if(!(e.getWhoClicked() instanceof Player) || !(e.isLeftClick() || e.isRightClick())) {
          return;
        }
        Arena arena = ArenaRegistry.getArena(player);
        TBPlayerChooseKitEvent event = new TBPlayerChooseKitEvent(player, KitRegistry.getKit(e.getCurrentItem()), arena);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) {
          return;
        }
        if(!kit.isUnlockedByPlayer(player)) {
          player.sendMessage(plugin.getChatManager().colorMessage("Kits.Not-Unlocked-Message").replace("%KIT%", kit.getName()));
          return;
        }
        User user = plugin.getUserManager().getUser(player);
        user.setKit(kit);
        player.sendMessage(plugin.getChatManager().colorMessage("Kits.Choose-Message").replace("%KIT%", kit.getName()));
      }), x, y);
      x++;
      if(x == 9) {
        x = 0;
        y++;
      }
    }
    gui.show(player);
  }

  @EventHandler
  public void onKitMenuItemClick(PlayerInteractEvent e) {
    if(!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
      return;
    }
    ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
    if(!stack.equals(kitItem.getItemStack())) {
      return;
    }
    e.setCancelled(true);
    createMenu(e.getPlayer());
  }

}
