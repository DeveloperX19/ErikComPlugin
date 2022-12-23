package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VanishCommand implements CommandExecutor, TabCompleter, Listener
{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.VANISH))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (!(sender instanceof Player))
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command must be issued by a player");
            return false;
        }
        Player player = (Player) sender;
        PlayerData data = PlayerData.get(player);
        if (args.length == 0)
        {
            data.vanished = !data.vanished;
            String action;
            if (data.vanished)
                action = "left";
            else
                action = "joined";
            Component messageString = Component.text(player.getName() + " " + action + " the server").color(TextColor.color(0xFFFF55));
            ErikComPlugin.server.sendMessage(messageString);
            VanishManager.updateVisibilityOf(player);
            GroupTagManager.updateNameTagOf(player);
            return true;
        }
        else if (args.length == 1)
        {
            if (args[0].equalsIgnoreCase("silent_step"))
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Silent step is currently " + ChatColor.BLUE + (data.silent_step() ? "ENABLED" : "DISABLED"));
            else if (args[0].equalsIgnoreCase("silent_join"))
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Silent join is currently " + ChatColor.BLUE + (data.silent_join() ? "ENABLED" : "DISABLED"));
            else
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[0] + "\"");
                return false;
            }
            return true;
        }
        else if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("silent_step"))
            {
                if (args[1].equalsIgnoreCase("on"))
                {
                    data.silent_step(true);
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Silent step is now " + ChatColor.BLUE + "ENABLED");
                }
                else if (args[1].equalsIgnoreCase("off"))
                {
                    data.silent_step(false);
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Silent step is now " + ChatColor.BLUE + "DISABLED");
                }
                else
                {
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[1] + "\"");
                    return false;
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("silent_join"))
            {
                if (args[1].equalsIgnoreCase("on"))
                {
                    data.silent_join(true);
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Silent join is now " + ChatColor.BLUE + "ENABLED");
                }
                else if (args[1].equalsIgnoreCase("off"))
                {
                    data.silent_join(false);
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Silent join is now " + ChatColor.BLUE + "DISABLED");
                }
                else
                {
                    sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[1] + "\"");
                    return false;
                }
                return true;
            }
            else
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[0] + "\"");
                return false;
            }
        }
        else
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.VANISH)) return suggestions;
        if (args.length <= 1)
        {
            suggestions.add("silent_step");
            suggestions.add("silent_join");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("silent_step") || args[0].equalsIgnoreCase("silent_join")))
        {
            suggestions.add("on");
            suggestions.add("off");
        }

        return suggestions;
    }
}
