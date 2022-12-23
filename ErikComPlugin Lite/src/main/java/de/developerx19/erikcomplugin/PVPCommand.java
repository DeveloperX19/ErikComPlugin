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

public class PVPCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.PVP))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (args.length <= 1 && !(sender instanceof Player))
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command must be issued by a player");
            return false;
        }
        PlayerData data = null;
        if (sender instanceof Player)
        {
            data = PlayerData.get((Player)sender);
        }
        if (args.length > 2)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        if (data != null && System.currentTimeMillis() < data.pvpLastChange + InfoMap.pvpCmdCooldown && !sender.isOp())
        {
            long t = data.pvpLastChange + InfoMap.pvpCmdCooldown - System.currentTimeMillis();
            double cd = Math.round(t / 100.0) / 10.0;
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "This command is on cooldown. Please wait " + ChatColor.DARK_PURPLE + cd + ChatColor.RED + " seconds");
            return false;
        }
        if (args.length == 0 && data != null)
        {
            data.pvpOn(!data.pvpOn());
            data.setOpMode();
            data.pvpLastChange = System.currentTimeMillis();
            GroupTagManager.updateNameTagOf(data.player);
            GroupTagManager.updateNameTagsFor(data.player);
            VanishManager.updateVisibilityFor(data.player);
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "PVP toggled " + ChatColor.BLUE + (data.pvpOn() ? "ON" : "OFF"));
            return true;
        }
        Player target;
        String targetStrAddon = "";
        if (args.length == 1)
            target = (Player) sender;
        else
        {
            if (!sender.isOp())
            {
                sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
                return false;
            }
            OfflinePlayer t = ErikComPlugin.getPlayer(args[1]);
            if (t == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
                return false;
            }
            if (t.getPlayer() == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not online");
                return false;
            }
            target = t.getPlayer();
            targetStrAddon = ChatColor.GREEN + " for " + ChatColor.DARK_AQUA + target.getName();
        }
        data = PlayerData.get(target);
        if (args[0].equalsIgnoreCase("on"))
        {
            data.pvpOn(true);
            data.setOpMode();
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "PVP toggled " + ChatColor.BLUE + "ON" + targetStrAddon);
        }
        else if (args[0].equalsIgnoreCase("off"))
        {
            data.pvpOn(false);
            data.setOpMode();
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "PVP toggled " + ChatColor.BLUE + "OFF" + targetStrAddon);
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument : \"" + args[0] + "\"");
            return false;
        }
        data.pvpLastChange = System.currentTimeMillis();
        GroupTagManager.updateNameTagOf(data.player);
        GroupTagManager.updateNameTagsFor(data.player);
        VanishManager.updateVisibilityFor(data.player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.PVP)) return suggestions;
        if (args.length > 2 || (args.length > 1 && !sender.isOp())) return suggestions;
        if (args.length == 2)
        {
            for (Player p : ErikComPlugin.server.getOnlinePlayers())
                suggestions.add(p.getName().toLowerCase());
        }
        else
        {
            suggestions.add("on");
            suggestions.add("off");
        }
        return suggestions;
    }
}
