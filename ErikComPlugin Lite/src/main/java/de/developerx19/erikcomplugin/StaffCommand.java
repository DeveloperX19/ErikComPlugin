package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StaffCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.STAFF))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (args.length == 2)
        {
            boolean newState;
            if (args[0].equalsIgnoreCase("add"))
                newState = true;
            else if (args[0].equalsIgnoreCase("remove"))
                newState = false;
            else
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[0] + "\"");
                return false;
            }
            OfflinePlayer player = ErikComPlugin.getPlayer(args[1]);
            if (player == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
                return false;
            }
            if (player.getPlayer() != null)
                PlayerData.get(player.getPlayer()).staff_member(newState);
            else
            {
                long id = Database.getPlayerID(player.getUniqueId());
                Database.setDataBoolean(id, Database.PlayerField.STAFF_MEMBER, newState);
            }
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Player " + player.getName() +
                    (newState ? " added to staff-team" : " removed from staff-team"));
            return true;
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.STAFF)) return suggestions;
        if (args.length <= 1)
        {
            suggestions.add("add");
            suggestions.add("remove");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("add"))
        {
            for (Player player : ErikComPlugin.server.getOnlinePlayers())
                suggestions.add(player.getName().toLowerCase());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove"))
            suggestions.addAll(Database.getStaffMembers());
        return suggestions;
    }
}
