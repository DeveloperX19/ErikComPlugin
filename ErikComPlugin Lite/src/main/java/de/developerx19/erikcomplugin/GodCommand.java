package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GodCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.CREATIVE))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (!(sender instanceof Player))
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command must be issued by a player");
            return false;
        }
        Player player = (Player)sender;
        if (args.length > 0)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        GameMode gm = player.getGameMode();
        player.setGameMode((gm == GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE);
        gm = player.getGameMode();
        if (gm == GameMode.CREATIVE)
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "switched to " + ChatColor.BLUE + gm.toString() + ChatColor.GREEN + " mode");
        else
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "switched to " + ChatColor.DARK_AQUA + gm.toString() + ChatColor.GREEN + " mode");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        return new ArrayList<>();
    }
}
