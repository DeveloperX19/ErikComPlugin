package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CountdownCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.COUNTDOWN))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (args.length < 2)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        long delay;
        try
        {
            delay = Long.parseLong(args[0]);
            if (delay <= 1)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "\"" + args[0] + "\" is not a valid number");
            return false;
        }
        String event_name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        EventManager.start_global_countdown(event_name, delay * 1000, 7, true);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.COUNTDOWN)) return suggestions;
        if (args.length == 1)
            suggestions.add("<seconds>");
        return suggestions;
    }
}