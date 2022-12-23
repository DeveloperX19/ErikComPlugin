package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StreamermodeCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.LIVE))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (!(sender instanceof Player))
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command must be issued by a player");
            return false;
        }
        PlayerData data = PlayerData.get((Player)sender);
        if (args.length == 0)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Streamer-mode is currently " + ChatColor.BLUE + (data.streamer_mode() ? "ON" : "OFF"));
            data.setOpMode();
            return true;
        }
        else if (args.length == 1)
        {
            if (args[0].equalsIgnoreCase("on"))
            {
                data.streamer_mode(true);
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Streamer-mode is now " + ChatColor.BLUE + "ON");
            }
            else if (args[0].equalsIgnoreCase("off"))
            {
                data.streamer_mode(false);
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Streamer-mode is now " + ChatColor.BLUE + "OFF");
            }
            else
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[0] + "\"");
                return false;
            }
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        data.setOpMode();
        GroupTagManager.updateNameTagOf(data.player);
        GroupTagManager.updateNameTagsFor(data.player);
        VanishManager.updateVisibilityFor(data.player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.LIVE)) return suggestions;
        if (args.length <= 1)
        {
            suggestions.add("on");
            suggestions.add("off");
        }
        return suggestions;
    }
}
