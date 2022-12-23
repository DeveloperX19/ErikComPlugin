package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HealCommand implements CommandExecutor, TabCompleter
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
        if (args.length == 0 && !(sender instanceof Player))
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command must be issued by a player");
            return false;
        }
        if (args.length > 1)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        Player target;
        if (args.length == 0)
            target = (Player)sender;
        else
        {
            OfflinePlayer t = ErikComPlugin.getPlayer(args[0]);
            if (t == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
                return false;
            }
            if (t.getPlayer() != null)
                target = t.getPlayer();
            else
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not online");
                return false;
            }
        }
        AttributeInstance maxHP = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHP == null)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Internal error trying to fetch player max Health");
            return false;
        }
        target.setHealth(maxHP.getValue());
        target.setFoodLevel(20);
        target.setFireTicks(0);
        target.setFreezeTicks(0);
        for (var e : InfoMap.potion_effect_bad_all)
            target.removePotionEffect(e);
        sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "healed " + ChatColor.BLUE + target.getName());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.CREATIVE)) return suggestions;
        if (args.length > 1) return suggestions;
        for (Player p : ErikComPlugin.server.getOnlinePlayers())
            suggestions.add(p.getName().toLowerCase());
        return suggestions;
    }
}
