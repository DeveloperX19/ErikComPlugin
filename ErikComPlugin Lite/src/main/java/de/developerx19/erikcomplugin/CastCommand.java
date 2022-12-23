package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class CastCommand implements CommandExecutor, TabCompleter
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
        if (args.length < 1 || args.length > 2)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        Enchantment ench = Enchantment.getByKey(NamespacedKey.fromString(args[0]));
        if (ench == null)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "\"" + args[0] + "\" : Enchantment not found");
            return false;
        }
        int level;
        try
        {
            level = (args.length == 1) ? 1 : Integer.parseInt(args[1]);
        }
        catch (Exception e)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "\"" + args[1] + "\" is not a valid number");
            return false;
        }
        if (item.getType() == Material.AIR)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "You are not holding an item in your main hand");
            return false;
        }
        if (level > 0)
        {
            item.addUnsafeEnchantment(ench, level);
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Item enchanted succesfully");
        }
        else
        {
            item.removeEnchantment(ench);
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Item disenchanted succesfully");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.CREATIVE)) return suggestions;
        if (args.length > 1) return suggestions;
        Enchantment[] ench_list = Enchantment.values();
        for (var e : ench_list)
            suggestions.add(e.getKey().getKey());
        return suggestions;
    }
}