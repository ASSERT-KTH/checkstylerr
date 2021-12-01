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

package plugily.projects.thebridge.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.thebridge.Main;
import plugily.projects.thebridge.arena.Arena;
import plugily.projects.thebridge.arena.options.ArenaOption;
import plugily.projects.thebridge.handlers.setup.SetupInventory;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 08.06.2019
 */
public class PlayerAmountComponents implements SetupComponent {

  private SetupInventory setupInventory;

  @Override
  public void prepare(SetupInventory setupInventory) {
    this.setupInventory = setupInventory;
  }

  @Override
  public void injectComponents(StaticPane pane) {
    Arena arena = setupInventory.getArena();
    if(arena == null) {
      return;
    }
    FileConfiguration config = setupInventory.getConfig();
    Main plugin = setupInventory.getPlugin();
    pane.addItem(new GuiItem(new ItemBuilder(Material.COAL).amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("minimumplayers"))
      .name(plugin.getChatManager().colorRawMessage("&e&lSet Minimum Players Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "(how many players are needed")
      .lore(ChatColor.DARK_GRAY + "for game to start lobby countdown)")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".minimumplayers"))
      .build(), e -> {
      ItemStack itemStack = e.getInventory().getItem(e.getSlot());
      if(itemStack == null || e.getCurrentItem() == null) {
        return;
      }
      if(itemStack.getAmount() <= 1) {
        e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 1!"));
        itemStack.setAmount(1);
      }
      if(e.getClick().isRightClick()) {
        itemStack.setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if(e.getClick().isLeftClick()) {
        itemStack.setAmount(e.getCurrentItem().getAmount() - 1);
      }
      config.set("instances." + arena.getId() + ".minimumplayers", e.getCurrentItem().getAmount());
      arena.setMinimumPlayers(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 5, 0);

    pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE)
      .amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("maximumsize"))
      .name(plugin.getChatManager().colorRawMessage("&e&lSet Maximum Players Per Base Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "(how many players one base can hold)")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".maximumsize"))
      .build(), e -> {
      ItemStack itemStack = e.getInventory().getItem(e.getSlot());
      if(itemStack == null || e.getCurrentItem() == null) {
        return;
      }
      if(e.getClick().isRightClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if(e.getClick().isLeftClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if(itemStack.getAmount() < 1) {
        e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 1!"));
        itemStack.setAmount(1);
      }
      config.set("instances." + arena.getId() + ".maximumsize", e.getCurrentItem().getAmount());
      arena.setOptionValue(ArenaOption.SIZE, e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 6, 0);
  }

}
