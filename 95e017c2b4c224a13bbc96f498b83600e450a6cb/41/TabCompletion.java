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

package plugily.projects.villagedefense.commands.completion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaRegistry;
import plugily.projects.villagedefense.commands.arguments.ArgumentsRegistry;
import plugily.projects.villagedefense.commands.arguments.data.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 11.05.2018
 */
public class TabCompletion implements TabCompleter {

    private final List<CompletableArgument> registeredCompletions = new ArrayList<>();
    private final ArgumentsRegistry registry;

    public TabCompletion(ArgumentsRegistry registry) {
        this.registry = registry;
    }

    public void registerCompletion(CompletableArgument completion) {
        registeredCompletions.add(completion);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completionList = new ArrayList<>(), cmds = new ArrayList<>();
        String partOfCommand = null;

        if (cmd.getName().equalsIgnoreCase("villagedefenseadmin")) {
          if (args.length == 1) {
            cmds.addAll(registry.getMappedArguments().get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName)
                .collect(Collectors.toList()));
            partOfCommand = args[0];
          } else if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("tp"))) {
            cmds.addAll(ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList()));
            partOfCommand = args[1];
          }
        }

        if (cmd.getName().equalsIgnoreCase("villagedefense")) {
          if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            cmds.addAll(ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList()));
            partOfCommand = args[1];
          } else if (args.length == 1) {
            cmds.addAll(registry.getMappedArguments().get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName)
                .collect(Collectors.toList()));
            partOfCommand = args[0];
          }
        }

        if (cmds.isEmpty() || partOfCommand == null) {
          for (CompletableArgument completion : registeredCompletions) {
            if (!cmd.getName().equalsIgnoreCase(completion.getMainCommand()) || !completion.getArgument().equalsIgnoreCase(args[0])) {
              continue;
            }
            return completion.getCompletions();
          }

          return null;
        }

        StringUtil.copyPartialMatches(partOfCommand, cmds, completionList);
        Collections.sort(completionList);
        return completionList;
  }
}
