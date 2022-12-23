package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignItemCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.SIGNITEM))
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
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "You are not holding an item in your main hand");
            return false;
        }
        if (args.length == 0)
        {
            item.lore(Collections.singletonList(
                    Component.text("Signiert von ").color(TextColor.color(0xff00ff))
                            .append(Component.text(player.getName()).color(TextColor.color(0xAA00AA)))));
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Item signed succesfully");
        }
        else if (args[0].equalsIgnoreCase("enchant"))
        {
            if (item.getEnchantments().isEmpty())
            {
                item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
                item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Item enchanted succesfully");
        }
        else if (args[0].equalsIgnoreCase("remove"))
        {
            item.lore(null);
            if (item.hasItemFlag(ItemFlag.HIDE_ENCHANTS))
            {
                item.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
                item.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Item de-signed succesfully");
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid argument");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.SIGNITEM)) return suggestions;
        suggestions.add("remove");
        suggestions.add("enchant");
        return suggestions;
    }
}
