package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GroupTagCommand implements CommandExecutor, TabCompleter, Listener
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.TAGS))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (args.length < 1)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        if (args[0].equalsIgnoreCase("create"))
        {
            if (args.length < 4 || args.length > 5)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            String groupID = args[1];
            if (GroupTagManager.hasGroup(groupID))
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "A group with the name '" + groupID + "' already exists");
                return false;
            }
            int sortIndex;
            try
            {
                sortIndex = Integer.parseInt(args[2]);
                if (sortIndex < 0) throw new Exception();
            }
            catch (Exception ignored)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "'" + args[2] + "' is not a valid number");
                return false;
            }
            if (GroupTagManager.GroupTag.getColor(args[3]) == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "color '" + args[3] + "' was not recognised");
                return false;
            }
            String display = "";
            if (args.length == 5)
                display = args[4].replace('_', ' ');
            GroupTagManager.createGroup(groupID, display, args[3], sortIndex);
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Group " + ChatColor.BLUE + args[1] + ChatColor.GREEN + " created");
        }
        else if (args[0].equalsIgnoreCase("modify"))
        {
            if (args.length != 4)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            if (!GroupTagManager.hasGroup(args[1]))
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Group '" + args[1] + "' does not exist");
                return false;
            }
            if (args[2].equalsIgnoreCase("priority"))
            {
                int sortIndex;
                try
                {
                    sortIndex = Integer.parseInt(args[3]);
                    if (sortIndex < 0) throw new Exception();
                }
                catch (Exception ignored)
                {
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "'" + args[3] + "' is not a valid number");
                    return false;
                }
                GroupTagManager.updateGroup(args[1], null, null, sortIndex);
            }
            else if (args[2].equalsIgnoreCase("color"))
            {
                if (GroupTagManager.GroupTag.getColor(args[3]) == null)
                {
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "color '" + args[3] + "' was not recognised");
                    return false;
                }
                GroupTagManager.updateGroup(args[1], null, args[3], null);
            }
            else if (args[2].equalsIgnoreCase("name"))
                GroupTagManager.updateGroup(args[1], args[3].replace('_', ' '), null, null);
            else
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid option : \"" + args[2] + "\"");
                return false;
            }
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Group " + ChatColor.BLUE + args[1] + ChatColor.GREEN + " updated");
        }
        else if (args[0].equalsIgnoreCase("delete"))
        {
            if (args.length != 2)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            if (!GroupTagManager.hasGroup(args[1]))
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Group '" + args[1] + "' does not exist");
                return false;
            }
            GroupTagManager.deleteGroup(args[1]);
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Group " + ChatColor.BLUE + args[1] + ChatColor.GREEN + " deleted");
        }
        else if (args[0].equalsIgnoreCase("add"))
        {
            if (args.length != 3)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            if (!GroupTagManager.hasGroup(args[1]))
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Group '" + args[1] + "' does not exist");
                return false;
            }
            OfflinePlayer player = ErikComPlugin.getPlayer(args[2]);
            if (player == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
                return false;
            }

            if (player.getPlayer() != null)
                GroupTagManager.addMember(args[1], player.getPlayer());

            Database.addGroupMember(args[1], player.getUniqueId());
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Added " + ChatColor.BLUE + player.getName() + ChatColor.GREEN + " to group " + ChatColor.BLUE + args[1]);
        }
        else if (args[0].equalsIgnoreCase("remove"))
        {
            if (args.length != 3)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            if (!GroupTagManager.hasGroup(args[1]))
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Group '" + args[1] + "' does not exist");
                return false;
            }
            OfflinePlayer player = ErikComPlugin.getPlayer(args[2]);
            if (player == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
                return false;
            }

            if (player.getPlayer() != null)
                GroupTagManager.removeMember(args[1], player.getPlayer());

            Database.removeGroupMember(args[1], player.getUniqueId());
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Removed " + ChatColor.BLUE + player.getName() + ChatColor.GREEN + " from group " + ChatColor.BLUE + args[1]);
        }
        else if (args[0].equalsIgnoreCase("info"))
        {
            if (args.length != 2)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            if (!GroupTagManager.hasGroup(args[1]))
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Group '" + args[1] + "' does not exist");
                return false;
            }
            GroupTagManager.GroupTag group =  GroupTagManager.getGroup(args[1]);
            sender.sendMessage(InfoMap.cmd_fb +
                    ChatColor.GREEN + "identifier: " + ChatColor.BLUE + group.name +
                    ChatColor.GREEN + " - priority: " + ChatColor.BLUE + group.sortIndex +
                    ChatColor.GREEN + " - color: " + ChatColor.BLUE + group.color.toString().toLowerCase() +
                    ChatColor.GREEN + " - display-name: " + ChatColor.BLUE + group.tag);
            StringBuilder members = new StringBuilder();
            members.append("[");
            for (String m : Database.getGroupMembers(group))
                members.append(ChatColor.BLUE).append(m).append(ChatColor.GREEN).append(", ");
            if (members.length() >= 2)
                members.delete(members.length() - 2, members.length());
            members.append("]");
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "members: " + members);
        }
        else if (args[0].equalsIgnoreCase("list"))
        {
            if (args.length != 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            for (var g : GroupTagManager.getGroups())
            {
                GroupTagManager.GroupTag group =  GroupTagManager.getGroup(g);
                sender.sendMessage(InfoMap.cmd_fb +
                        ChatColor.GREEN + "identifier: " + ChatColor.BLUE + group.name +
                        ChatColor.GREEN + " - priority: " + ChatColor.BLUE + group.sortIndex +
                        ChatColor.GREEN + " - color: " + ChatColor.BLUE + group.color.toString().toLowerCase() +
                        ChatColor.GREEN + " - display-name: " + ChatColor.BLUE + group.tag);
            }
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[0] + "\"");
            return false;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.TAGS)) return suggestions;
        if (args.length <= 1)
        {
            suggestions.add("create");
            suggestions.add("modify");
            suggestions.add("delete");
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("info");
            suggestions.add("list");
            return suggestions;
        }
        if (args[0].equalsIgnoreCase("create"))
        {
            if (args.length == 2)
                suggestions.add("<group_name>");
            if (args.length == 3)
                suggestions.add("<group_sort_priority>");
            if (args.length == 4)
                for (String color : NamedTextColor.NAMES.keys())
                    suggestions.add(color.toLowerCase());
            if (args.length == 5)
                suggestions.add("<group_displayed_name>");
        }
        if (args[0].equalsIgnoreCase("modify"))
        {
            if (args.length == 2)
                suggestions.addAll(GroupTagManager.getGroups());
            if (args.length == 3)
            {
                suggestions.add("priority");
                suggestions.add("color");
                suggestions.add("name");
            }
            if (args.length == 4)
            {
                if (args[2].equalsIgnoreCase("priority"))
                    suggestions.add("<group_sort_priority>");
                if (args[2].equalsIgnoreCase("color"))
                    for (String color : NamedTextColor.NAMES.keys())
                        suggestions.add(color.toLowerCase());
                if (args[2].equalsIgnoreCase("name"))
                    suggestions.add("<group_displayed_name>");
            }
        }
        if (args[0].equalsIgnoreCase("delete"))
        {
            if (args.length == 2)
                suggestions.addAll(GroupTagManager.getGroups());
        }
        if (args[0].equalsIgnoreCase("add"))
        {
            if (args.length == 2)
                suggestions.addAll(GroupTagManager.getGroups());
            if (args.length == 3)
                for (Player player : ErikComPlugin.server.getOnlinePlayers())
                    suggestions.add(player.getName().toLowerCase());
        }
        if (args[0].equalsIgnoreCase("remove"))
        {
            if (args.length == 2)
                suggestions.addAll(GroupTagManager.getGroups());
            if (args.length == 3)
                for (Player player : ErikComPlugin.server.getOnlinePlayers())
                    suggestions.add(player.getName().toLowerCase());
        }
        if (args[0].equalsIgnoreCase("info"))
        {
            if (args.length == 2)
                suggestions.addAll(GroupTagManager.getGroups());
        }
        return suggestions;
    }
}
