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

package plugily.projects.murdermystery.events;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion;
import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion.Version;
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import pl.plajerlair.commonsbox.minecraft.compat.events.api.CBPlayerSwapHandItemsEvent;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XMaterial;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XSound;
import pl.plajerlair.commonsbox.minecraft.item.ItemUtils;
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaManager;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.ArenaUtils;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.handlers.hologram.HologramManager;
import plugily.projects.murdermystery.handlers.items.SpecialItemManager;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class Events implements Listener {

  private final Main plugin;

  public Events(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onItemSwap(CBPlayerSwapHandItemsEvent e) {
    if(ArenaRegistry.isInArena(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    if(ArenaRegistry.isInArena(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onSwordThrow(PlayerInteractEvent e) {
    Arena arena = ArenaRegistry.getArena(e.getPlayer());
    if(arena == null) {
      return;
    }
    if(!Role.isRole(Role.MURDERER, e.getPlayer())) {
      return;
    }
    if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.PHYSICAL) {
      return;
    }
    Player attacker = e.getPlayer();
    User attackerUser = plugin.getUserManager().getUser(attacker);
    if(VersionUtils.getItemInHand(attacker).getType() != plugin.getConfigPreferences().getMurdererSword().getType()) {
      return;
    }
    if(attackerUser.getCooldown("sword_shoot") > 0) {
      return;
    }
    attackerUser.setCooldown("sword_shoot", plugin.getConfig().getInt("Murderer-Sword-Fly-Cooldown", 5));
    if(ServerVersion.Version.isCurrentLower(Version.v1_9_R1)) {
      attackerUser.setCooldown("sword_attack", (plugin.getConfig().getInt("Murderer-Sword-Attack-Cooldown", 1)));
    } else {
      attacker.setCooldown(plugin.getConfigPreferences().getMurdererSword().getType(), 20 * (plugin.getConfig().getInt("Murderer-Sword-Attack-Cooldown", 1)));
    }
    createFlyingSword(arena, attacker, attackerUser);
    Utils.applyActionBarCooldown(attacker, plugin.getConfig().getInt("Murderer-Sword-Fly-Cooldown", 5));
  }

  private void createFlyingSword(Arena arena, Player attacker, User attackerUser) {
    Location loc = attacker.getLocation();
    Vector vec = loc.getDirection();
    vec.normalize().multiply(plugin.getConfig().getDouble("Murderer-Sword-Speed", 0.65));
    Location standStart = Utils.rotateAroundAxisY(new Vector(1.0D, 0.0D, 0.0D), loc.getYaw()).toLocation(attacker.getWorld()).add(loc);
    standStart.setYaw(loc.getYaw());
    ArmorStand stand = (ArmorStand) attacker.getWorld().spawnEntity(standStart, EntityType.ARMOR_STAND);
    stand.setVisible(false);
    if(Version.isCurrentHigher(Version.v1_8_R3)) {
      stand.setInvulnerable(true);
      stand.setSilent(true);
    }

    VersionUtils.setItemInHand(stand, plugin.getConfigPreferences().getMurdererSword());

    stand.setRightArmPose(new EulerAngle(Math.toRadians(350.0), Math.toRadians(loc.getPitch() * -1.0), Math.toRadians(90.0)));
    VersionUtils.setCollidable(stand, false);

    stand.setGravity(false);
    stand.setRemoveWhenFarAway(true);
    stand.setMarker(true);
    Location initialise = Utils.rotateAroundAxisY(new Vector(-0.8D, 1.45D, 0.0D), loc.getYaw()).toLocation(attacker.getWorld()).add(standStart).add(Utils.rotateAroundAxisY(Utils.rotateAroundAxisX(new Vector(0.0D, 0.0D, 1.0D), loc.getPitch()), loc.getYaw()));
    int maxRange = plugin.getConfig().getInt("Murderer-Sword-Fly-Range", 20);
    double maxHitRange = plugin.getConfig().getDouble("Murderer-Sword-Fly-Hit-Range", 0.5);
    new BukkitRunnable() {
      @Override
      public void run() {
        stand.teleport(standStart.add(vec));
        initialise.add(vec);
        initialise.getWorld().getNearbyEntities(initialise, maxHitRange, maxHitRange, maxHitRange).forEach(entity -> {
          if(entity instanceof Player) {
            Player victim = (Player) entity;
            if(ArenaRegistry.isInArena(victim) && !plugin.getUserManager().getUser(victim).isSpectator()) {
              if(!victim.equals(attacker)) {
                killBySword(arena, attackerUser, victim);
                this.cancel();
                stand.remove();
              }
            }
          }
        });
        if(loc.distance(initialise) > maxRange || initialise.getBlock().getType().isSolid()) {
          this.cancel();
          stand.remove();
        }
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  private void killBySword(Arena arena, User attackerUser, Player victim) {
    //check if victim is murderer
    if(Role.isRole(Role.MURDERER, victim)) {
      return;
    }
    XSound.ENTITY_PLAYER_DEATH.play(victim.getLocation(), 50, 1);
    victim.damage(100.0);
    VersionUtils.sendTitles(victim, plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Titles.Died", victim),
        plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Killed-You", victim), 5, 40, 5);
    attackerUser.addStat(StatsStorage.StatisticType.LOCAL_KILLS, 1);
    attackerUser.addStat(StatsStorage.StatisticType.KILLS, 1);
    ArenaUtils.addScore(attackerUser, ArenaUtils.ScoreAction.KILL_PLAYER, 0);
    if(Role.isRole(Role.ANY_DETECTIVE, victim) && arena.lastAliveDetective()) {
      if(Role.isRole(Role.FAKE_DETECTIVE, victim)) {
        arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
      }
      ArenaUtils.dropBowAndAnnounce(arena, victim);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCommandExecute(PlayerCommandPreprocessEvent event) {
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    if(arena == null) {
      return;
    }
    if(!plugin.getConfig().getBoolean("Block-Commands-In-Game", true)) {
      return;
    }
    String command = event.getMessage().substring(1);
    command = (command.indexOf(' ') >= 0 ? command.substring(0, command.indexOf(' ')) : command);
    for(String msg : plugin.getConfig().getStringList("Whitelisted-Commands")) {
      if(command.equalsIgnoreCase(msg)) {
        return;
      }
    }
    if(event.getPlayer().isOp() || event.getPlayer().hasPermission("murdermystery.admin") || event.getPlayer().hasPermission("murdermystery.command.bypass")) {
      return;
    }
    if(command.equalsIgnoreCase("mm") || command.equalsIgnoreCase("murdermystery")
        || event.getMessage().contains("murdermysteryadmin") || event.getMessage().contains("leave")
        || command.equalsIgnoreCase("stats") || command.equalsIgnoreCase("mma")) {
      return;
    }
    event.setCancelled(true);
    event.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Only-Command-Ingame-Is-Leave"));
  }

  @EventHandler
  public void onInGameInteract(PlayerInteractEvent event) {
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    if(arena == null || event.getClickedBlock() == null) {
      return;
    }
    if(event.getClickedBlock().getType() == XMaterial.PAINTING.parseMaterial() || event.getClickedBlock().getType() == XMaterial.FLOWER_POT.parseMaterial()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInGameBedEnter(PlayerBedEnterEvent event) {
    if(ArenaRegistry.isInArena(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onLeave(PlayerInteractEvent event) {
    if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
      return;
    }
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    ItemStack itemStack = VersionUtils.getItemInHand(event.getPlayer());
    if(arena == null || !ItemUtils.isItemStackNamed(itemStack)) {
      return;
    }
    String key = SpecialItemManager.getRelatedSpecialItem(itemStack);
    if(key == null) {
      return;
    }
    if(SpecialItemManager.getRelatedSpecialItem(itemStack).equalsIgnoreCase("Leave")) {
      event.setCancelled(true);
      if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
        plugin.getBungeeManager().connectToHub(event.getPlayer());
      } else {
        ArenaManager.leaveAttempt(event.getPlayer(), arena);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onFoodLevelChange(FoodLevelChangeEvent event) {
    if(event.getEntity().getType() == EntityType.PLAYER && ArenaRegistry.isInArena((Player) event.getEntity())) {
      event.setFoodLevel(20);
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  //highest priority to fully protect our game (i didn't set it because my test server was destroyed, n-no......)
  public void onBlockBreakEvent(BlockBreakEvent event) {
    if(ArenaRegistry.isInArena(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  //highest priority to fully protect our game (i didn't set it because my test server was destroyed, n-no......)
  public void onBuild(BlockPlaceEvent event) {
    if(ArenaRegistry.isInArena(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  //highest priority to fully protect our game (i didn't set it because my test server was destroyed, n-no......)
  public void onHangingBreakEvent(HangingBreakByEntityEvent event) {
    if(event.getEntity() instanceof ItemFrame || event.getEntity() instanceof Painting) {
      if(event.getRemover() instanceof Player && ArenaRegistry.isInArena((Player) event.getRemover())) {
        event.setCancelled(true);
        return;
      }
      if(!(event.getRemover() instanceof Arrow)) {
        return;
      }
      Arrow arrow = (Arrow) event.getRemover();
      if(arrow.getShooter() instanceof Player && ArenaRegistry.isInArena((Player) arrow.getShooter())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onArmorStandDestroy(EntityDamageByEntityEvent e) {
    if(!(e.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity livingEntity = (LivingEntity) e.getEntity();
    if(livingEntity.getType() != EntityType.ARMOR_STAND) {
      return;
    }
    if((e.getDamager() instanceof Arrow && ((Arrow) e.getDamager()).getShooter() instanceof Player && ArenaRegistry.isInArena((Player) ((Arrow) e.getDamager()).getShooter()))
        || (e.getDamager() instanceof Player && ArenaRegistry.isInArena((Player) e.getDamager()))) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onHopperBreak(BlockBreakEvent event) {
    HologramManager.getArmorStands().removeIf(armorStand -> {
        boolean isSameType = armorStand.getLocation().getBlock().getType() == event.getBlock().getType();
        if (isSameType) {
          armorStand.remove();
          armorStand.setCustomNameVisible(false);
        }

        return isSameType;
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteractWithArmorStand(PlayerArmorStandManipulateEvent event) {
    if(ArenaRegistry.isInArena(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onCraft(PlayerInteractEvent event) {
    if(!ArenaRegistry.isInArena(event.getPlayer())) {
      return;
    }
    if(event.getPlayer().getTargetBlock(null, 7).getType() == XMaterial.CRAFTING_TABLE.parseMaterial()) {
      event.setCancelled(true);
    }
  }

}
