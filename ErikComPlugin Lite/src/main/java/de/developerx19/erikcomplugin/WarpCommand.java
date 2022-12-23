package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter
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
        if (args.length != 1 && args.length != 3 && args.length != 4)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        if (args.length == 1)
        {
            Player target = ErikComPlugin.server.getPlayer(args[0]);
            if (target == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not online");
                return false;
            }
            player.teleport(target.getLocation());
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Teleported to " + ChatColor.BLUE + target.getName());
        }
        else
        {
            int x;
            int y;
            int z;
            World w = player.getWorld();
            try
            {
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
                if (args.length == 4)
                {
                    World.Environment e;
                    if (args[3].equalsIgnoreCase("overworld"))
                        e = World.Environment.NORMAL;
                    else if (args[3].equalsIgnoreCase("nether"))
                        e = World.Environment.NETHER;
                    else if (args[3].equalsIgnoreCase("end"))
                        e = World.Environment.THE_END;
                    else
                        throw new Exception();
                    for (World world : ErikComPlugin.server.getWorlds()) {
                        if (world.getEnvironment() == e)
                        {
                            w = world;
                            break;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid location");
                return false;
            }
            Location target = new Location(w, x, y, z);
            player.teleport(target);
            String posStr = x + " | " + y + " | " + z + ((args.length == 4) ? (" [" + args[3].toUpperCase() + "]") : "");
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Teleported to " + ChatColor.BLUE + posStr);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.TELEPORT)) return suggestions;
        if (args.length == 1)
        {
            for (Player p : ErikComPlugin.server.getOnlinePlayers())
                suggestions.add(p.getName().toLowerCase());
        }
        if (args.length == 4)
        {
            suggestions.add("overworld");
            suggestions.add("nether");
            suggestions.add("end");
        }
        return suggestions;
    }
}
