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

package plugily.projects.thebridge.events.spectator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.thebridge.Main;
import plugily.projects.thebridge.handlers.ChatManager;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 23.11.2020
 */
public class SpectatorSettingsMenu implements Listener {

  private final String inventoryName;
  private final String speedOptionName;
  private Inventory inv;
  private final Main plugin;

  public SpectatorSettingsMenu(Main plugin, String inventoryName, String speedOptionName) {
    this.inventoryName = inventoryName;
    this.speedOptionName = speedOptionName;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.inv = initInventory();
    this.plugin = plugin;
  }

  public void openSpectatorSettingsMenu(Player player) {
    player.openInventory(this.inv);
  }

  @EventHandler
  public void onSpectatorMenuClick(InventoryClickEvent e) {
    if (!e.getView().getTitle().equals(plugin.getChatManager().colorRawMessage(inventoryName))) {
      return;
    }
    if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
      return;
    }
    Player p = (Player) e.getWhoClicked();
    p.closeInventory();

    switch (e.getCurrentItem().getType()) {
      case LEATHER_BOOTS:
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFlySpeed(0.15f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        break;
      case CHAINMAIL_BOOTS:
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFlySpeed(0.2f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        break;
      case IRON_BOOTS:
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFlySpeed(0.25f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
        break;
      case GOLDEN_BOOTS:
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFlySpeed(0.3f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
        break;
      case DIAMOND_BOOTS:
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFlySpeed(0.35f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4, false, false));
        break;
      default:
        break;
    }
  }

  private Inventory initInventory() {
    Inventory inv = Bukkit.createInventory(null, 9 * 3, inventoryName);
    ChatManager chatManager = JavaPlugin.getPlugin(Main.class).getChatManager();
    inv.setItem(11, new ItemBuilder(Material.LEATHER_BOOTS)
      .name(chatManager.colorRawMessage(speedOptionName + " I")).build());
    inv.setItem(12, new ItemBuilder(Material.CHAINMAIL_BOOTS)
      .name(chatManager.colorRawMessage(speedOptionName + " II")).build());
    inv.setItem(13, new ItemBuilder(Material.IRON_BOOTS)
      .name(chatManager.colorRawMessage(speedOptionName + " III")).build());
    inv.setItem(14, new ItemBuilder(XMaterial.GOLDEN_BOOTS.parseItem())
      .name(chatManager.colorRawMessage(speedOptionName + " IV")).build());
    inv.setItem(15, new ItemBuilder(Material.DIAMOND_BOOTS)
      .name(chatManager.colorRawMessage(speedOptionName + " V")).build());
    return inv;
  }

}
