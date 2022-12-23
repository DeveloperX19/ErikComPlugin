package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BackCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.TELEPORT))
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
        if (args.length > 1)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        PlayerData data = PlayerData.get(player);
        if (args.length == 0)
        {
            if (data.backPosition() == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "You do not have a saved position for /" + label);
                return false;
            }
            Location dest = data.backPosition();
            dest.setPitch(player.getLocation().getPitch());
            dest.setYaw(player.getLocation().getYaw());
            player.teleport(dest);
            String posStr = data.backPosition().getBlockX() + " | " + data.backPosition().getBlockY() + " | " + data.backPosition().getBlockZ() + " [" + data.backPosition().getWorld().getEnvironment().name() + "]";
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Teleported to " + ChatColor.BLUE + posStr);
        }
        else if (args[0].equalsIgnoreCase("here"))
        {
            data.backPosition(player.getLocation());
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Position saved");
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument");
            return false;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.TELEPORT)) return suggestions;
        if (args.length > 1) return suggestions;
        suggestions.add("here");
        return suggestions;
    }
}
