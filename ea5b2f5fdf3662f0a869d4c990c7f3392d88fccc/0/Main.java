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

package plugily.projects.thebridge;

import me.tigerhix.lib.scoreboard.ScoreboardLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajerlair.commonsbox.database.MysqlDatabase;
import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion;
import pl.plajerlair.commonsbox.minecraft.compat.events.EventsInitializer;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.misc.MiscUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;
import plugily.projects.thebridge.api.StatsStorage;
import plugily.projects.thebridge.arena.Arena;
import plugily.projects.thebridge.arena.ArenaEvents;
import plugily.projects.thebridge.arena.ArenaRegistry;
import plugily.projects.thebridge.arena.base.BaseMenuHandler;
import plugily.projects.thebridge.commands.arguments.ArgumentsRegistry;
import plugily.projects.thebridge.events.ChatEvents;
import plugily.projects.thebridge.events.Events;
import plugily.projects.thebridge.events.JoinEvent;
import plugily.projects.thebridge.events.LobbyEvent;
import plugily.projects.thebridge.events.QuitEvent;
import plugily.projects.thebridge.events.spectator.SpectatorEvents;
import plugily.projects.thebridge.events.spectator.SpectatorItemEvents;
import plugily.projects.thebridge.handlers.BungeeManager;
import plugily.projects.thebridge.handlers.ChatManager;
import plugily.projects.thebridge.handlers.PermissionsManager;
import plugily.projects.thebridge.handlers.PlaceholderManager;
import plugily.projects.thebridge.handlers.hologram.HologramManager;
import plugily.projects.thebridge.handlers.items.SpecialItemManager;
import plugily.projects.thebridge.handlers.language.LanguageManager;
import plugily.projects.thebridge.handlers.party.PartyHandler;
import plugily.projects.thebridge.handlers.party.PartySupportInitializer;
import plugily.projects.thebridge.handlers.rewards.RewardsFactory;
import plugily.projects.thebridge.handlers.setup.SetupInventory;
import plugily.projects.thebridge.handlers.sign.SignManager;
import plugily.projects.thebridge.kits.KitMenuHandler;
import plugily.projects.thebridge.kits.KitRegistry;
import plugily.projects.thebridge.kits.basekits.Kit;
import plugily.projects.thebridge.user.User;
import plugily.projects.thebridge.user.UserManager;
import plugily.projects.thebridge.user.data.MysqlManager;
import plugily.projects.thebridge.utils.CuboidSelector;
import plugily.projects.thebridge.utils.Debugger;
import plugily.projects.thebridge.utils.ExceptionLogHandler;
import plugily.projects.thebridge.utils.MessageUtils;
import plugily.projects.thebridge.utils.UpdateChecker;
import plugily.projects.thebridge.utils.Utils;
import plugily.projects.thebridge.utils.services.ServiceRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * @author Tigerpanzer_02, 2Wild4You
 * <p>
 * Created at 31.10.2020
 */
public class Main extends JavaPlugin {


  private ExceptionLogHandler exceptionLogHandler;
  private boolean forceDisable = false;
  private BungeeManager bungeeManager;
  private RewardsFactory rewardsHandler;
  private MysqlDatabase database;
  private SignManager signManager;
  private PartyHandler partyHandler;
  private ConfigPreferences configPreferences;
  private KitMenuHandler kitMenuHandler;
  private BaseMenuHandler baseMenuHandler;
  private ArgumentsRegistry argumentsRegistry;
  private UserManager userManager;
  private ChatManager chatManager;
  private SpecialItemManager specialItemManager;
  private CuboidSelector cuboidSelector;

  @Override
  public void onEnable() {
    if(!validateIfPluginShouldStart()) {
      return;
    }

    long start = System.currentTimeMillis();
    setupFiles();
    saveDefaultConfig();
    ServiceRegistry.registerService(this);
    exceptionLogHandler = new ExceptionLogHandler(this);
    LanguageManager.init(this);

    Debugger.setEnabled(getDescription().getVersion().contains("debug") || getConfig().getBoolean("Debug"));

    Debugger.debug("[System] Initialization start");
    if(getConfig().getBoolean("Developer-Mode")) {
      Debugger.deepDebug(true);
      Debugger.debug(Level.FINE, "Deep debug enabled");
      for(String listenable : new ArrayList<>(getConfig().getStringList("Performance-Listenable"))) {
        Debugger.monitorPerformance(listenable);
      }
    }

    configPreferences = new ConfigPreferences(this);
    initializeClasses();
    checkUpdate();
    Debugger.debug("[System] Initialization finished took {0}ms", System.currentTimeMillis() - start);

    Debugger.debug("Plugin loaded! Hooking into soft-dependencies in a while!");
  }

  private boolean validateIfPluginShouldStart() {
    if(ServerVersion.Version.isCurrentLower(ServerVersion.Version.v1_8_R1)) {
      MessageUtils.thisVersionIsNotSupported();
      Debugger.sendConsoleMsg("&cYour server version is not supported by The Bridge!");
      Debugger.sendConsoleMsg("&cSadly, we must shut off. Maybe you consider changing your server version?");
      forceDisable = true;
      getServer().getPluginManager().disablePlugin(this);
      return false;
    }
    try {
      Class.forName("org.spigotmc.SpigotConfig");
    } catch(Exception e) {
      MessageUtils.thisVersionIsNotSupported();
      Debugger.sendConsoleMsg("&cYour server software is not supported by The Bridge!");
      Debugger.sendConsoleMsg("&cWe support only Spigot and Spigot forks only! Shutting off...");
      forceDisable = true;
      getServer().getPluginManager().disablePlugin(this);
      return false;
    }
    return true;
  }

  @Override
  public void onDisable() {
    if(forceDisable) {
      return;
    }
    Debugger.debug("System disable initialized");
    long start = System.currentTimeMillis();

    Bukkit.getLogger().removeHandler(exceptionLogHandler);
    saveAllUserStatistics();
    if(configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      getMysqlDatabase().shutdownConnPool();
    }
    for(ArmorStand armorStand : HologramManager.getArmorStands()) {
      armorStand.remove();
      armorStand.setCustomNameVisible(false);
    }
    HologramManager.getArmorStands().clear();
    for(Arena arena : ArenaRegistry.getArenas()) {
      arena.getScoreboardManager().stopAllScoreboards();
      for(Player player : arena.getPlayers()) {
        arena.doBarAction(Arena.BarAction.REMOVE, player);
        arena.teleportToEndLocation(player);
        player.setFlySpeed(0.1f);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
        player.setWalkSpeed(0.2f);
        player.setGameMode(GameMode.SURVIVAL);
        if(configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
          InventorySerializer.loadInventory(this, player);
        }
      }
      arena.teleportAllToEndLocation();
      arena.cleanUpArena();
    }
    Debugger.debug("System disable finished took {0}ms", System.currentTimeMillis() - start);
  }

  private void initializeClasses() {
    chatManager = new ChatManager(this);
    ScoreboardLib.setPluginInstance(this);
    if(getConfig().getBoolean("BungeeActivated")) {
      bungeeManager = new BungeeManager(this);
    }
    if(configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      FileConfiguration config = ConfigUtils.getConfig(this, "mysql");
      database = new MysqlDatabase(config.getString("user"), config.getString("password"), config.getString("address"));
    }
    argumentsRegistry = new ArgumentsRegistry(this);
    userManager = new UserManager(this);
    Utils.init(this);
    PermissionsManager.init();
    new ArenaEvents(this);
    new SpectatorEvents(this);
    new QuitEvent(this);
    new JoinEvent(this);
    new ChatEvents(this);
    registerSoftDependenciesAndServices();
    User.cooldownHandlerTask();
    signManager = new SignManager(this);
    ArenaRegistry.registerArenas();
    signManager.loadSigns();
    signManager.updateSigns();
    new Events(this);
    new LobbyEvent(this);
    new SpectatorItemEvents(this);
    rewardsHandler = new RewardsFactory(this);
    specialItemManager = new SpecialItemManager(this);
    specialItemManager.registerItems();
    Kit.init(this);
    KitRegistry.init(this);
    SetupInventory.init(this);
    baseMenuHandler = new BaseMenuHandler(this);
    kitMenuHandler = new KitMenuHandler(this);
    partyHandler = new PartySupportInitializer().initialize(this);
    cuboidSelector = new CuboidSelector(this);

    new EventsInitializer().initialize(this);
    MiscUtils.sendStartUpMessage(this, "TheBridge", getDescription(),true, true);
  }

  private void registerSoftDependenciesAndServices() {
    Debugger.debug("Hooking into soft dependencies");
    long start = System.currentTimeMillis();

    startPluginMetrics();
    if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      Debugger.debug("Hooking into PlaceholderAPI");
      new PlaceholderManager().register();
    }
    Debugger.debug("Hooked into soft dependencies took {0}ms", System.currentTimeMillis() - start);
  }

  private void startPluginMetrics() {
    Metrics metrics = new Metrics(this);
    if(!metrics.isEnabled())
      return;

    metrics.addCustomChart(new Metrics.SimplePie("database_enabled", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED))));
    metrics.addCustomChart(new Metrics.SimplePie("bungeecord_hooked", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.BUNGEE_ENABLED))));
    metrics.addCustomChart(new Metrics.SimplePie("locale_used", () -> LanguageManager.getPluginLocale().getPrefix()));
    metrics.addCustomChart(new Metrics.SimplePie("update_notifier", () -> {
      if(getConfig().getBoolean("Update-Notifier.Enabled", true)) {
        return getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true) ? "Enabled with beta notifier" : "Enabled";
      }
      return getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true) ? "Beta notifier only" : "Disabled";
    }));
  }

  private void checkUpdate() {
    if(!getConfig().getBoolean("Update-Notifier.Enabled", true)) {
      return;
    }
    UpdateChecker.init(this, 87320).requestUpdateCheck().whenComplete((result, exception) -> {
      if(!result.requiresUpdate()) {
        return;
      }
      if(result.getNewestVersion().contains("b")) {
        if(getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true)) {
          Debugger.sendConsoleMsg("&c[TheBridge] Your software is ready for update! However it's a BETA VERSION. Proceed with caution.");
          Debugger.sendConsoleMsg("&c[TheBridge] Current version %old%, latest version %new%".replace("%old%", getDescription().getVersion()).replace("%new%",
            result.getNewestVersion()));
        }
        return;
      }
      MessageUtils.updateIsHere();
      Debugger.sendConsoleMsg("&aYour TheBridge plugin is outdated! Download it to keep with latest changes and fixes.");
      Debugger.sendConsoleMsg("&aDisable this option in config.yml if you wish.");
      Debugger.sendConsoleMsg("&eCurrent version: &c" + getDescription().getVersion() + "&e Latest version: &a" + result.getNewestVersion());
    });
  }

  private void setupFiles() {
    for(String fileName : Arrays.asList("arenas", "bungee", "rewards", "stats", "special_items", "mysql", "kits")) {
      File file = new File(getDataFolder() + File.separator + fileName + ".yml");
      if(!file.exists()) {
        saveResource(fileName + ".yml", false);
      }
    }
  }

  public RewardsFactory getRewardsHandler() {
    return rewardsHandler;
  }

  public BungeeManager getBungeeManager() {
    return bungeeManager;
  }

  public PartyHandler getPartyHandler() {
    return partyHandler;
  }

  public ChatManager getChatManager() {
    return chatManager;
  }

  public ConfigPreferences getConfigPreferences() {
    return configPreferences;
  }

  public MysqlDatabase getMysqlDatabase() {
    return database;
  }

  public SignManager getSignManager() {
    return signManager;
  }

  public SpecialItemManager getSpecialItemManager() {
    return specialItemManager;
  }

  public ArgumentsRegistry getArgumentsRegistry() {
    return argumentsRegistry;
  }

  public UserManager getUserManager() {
    return userManager;
  }

  public KitMenuHandler getKitMenuHandler() {
    return kitMenuHandler;
  }

  public BaseMenuHandler getBaseMenuHandler() {
    return baseMenuHandler;
  }

  public CuboidSelector getCuboidSelector() {
    return cuboidSelector;
  }

  private void saveAllUserStatistics() {
    for(Player player : getServer().getOnlinePlayers()) {
      User user = userManager.getUser(player);
      if(userManager.getDatabase() instanceof MysqlManager) {
        StringBuilder update = new StringBuilder(" SET ");
        for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
          if(!stat.isPersistent()) continue;
          if(update.toString().equalsIgnoreCase(" SET ")) {
            update.append(stat.getName()).append('=').append(user.getStat(stat));
          }
          update.append(", ").append(stat.getName()).append('=').append(user.getStat(stat));
        }
        String finalUpdate = update.toString();
        //copy of userManager#saveStatistic but without async database call that's not allowed in onDisable method.
        ((MysqlManager) userManager.getDatabase()).getDatabase().executeUpdate("UPDATE " + ((MysqlManager) getUserManager().getDatabase()).getTableName()
          + finalUpdate + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
        continue;
      }
      for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
        userManager.getDatabase().saveStatistic(user, stat);
      }
    }
  }
}
