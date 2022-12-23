package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SubperkCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.SUBPERK))
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
        if (args.length < 1)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }

        if (args[0].equalsIgnoreCase("unbreaking_4"))
        {
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
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 4);
            return true;
        }
        else if (args[0].equalsIgnoreCase("fortune_4"))
        {
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
            item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 4);
            return true;
        }
        else if (args[0].equalsIgnoreCase("looting_4"))
        {
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
            item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 4);
            return true;
        }
        else if (args[0].equalsIgnoreCase("efficiency_6"))
        {
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
            item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 6);
            return true;
        }
        else if (args[0].equalsIgnoreCase("punch_3"))
        {
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
            item.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 3);
            return true;
        }
        else if (args[0].equalsIgnoreCase("respiration_4"))
        {
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
            item.addUnsafeEnchantment(Enchantment.OXYGEN, 4);
            return true;
        }
        else if (args[0].equalsIgnoreCase("soul_speed_5"))
        {
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
            item.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 5);
            return true;
        }
        else if (args[0].equalsIgnoreCase("bow"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR)
                item = new ItemStack(Material.BOW, 1);
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
            player.getInventory().setItemInMainHand(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("legs"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR)
                item = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
            item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            item.addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 3);
            item.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 3);
            player.getInventory().setItemInMainHand(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("leather"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack[] items = new ItemStack[4];
            items[0] = new ItemStack(Material.LEATHER_BOOTS, 1);
            items[1] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
            items[2] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
            items[3] = new ItemStack(Material.LEATHER_HELMET, 1);
            for (var item : items)
                item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
            player.getInventory().addItem(items);
            return true;
        }
        else if (args[0].equalsIgnoreCase("sword"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR)
                item = new ItemStack(Material.DIAMOND_SWORD, 1);
            item.addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
            item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
            player.getInventory().setItemInMainHand(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("crossbow"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR)
                item = new ItemStack(Material.CROSSBOW, 1);
            item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 5);
            player.getInventory().setItemInMainHand(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("loyalty_5"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR)
                item = new ItemStack(Material.TRIDENT, 1);
            item.addUnsafeEnchantment(Enchantment.LOYALTY, 5);
            player.getInventory().setItemInMainHand(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("riptide_5"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR)
                item = new ItemStack(Material.TRIDENT, 1);
            item.addUnsafeEnchantment(Enchantment.RIPTIDE, 5);
            player.getInventory().setItemInMainHand(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("turtle"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = new ItemStack(Material.TURTLE_HELMET, 1);
            item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5);
            player.getInventory().addItem(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("stick"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = new ItemStack(Material.STICK, 1);
            item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
            player.getInventory().addItem(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("deepslate"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = new ItemStack(Material.REINFORCED_DEEPSLATE, 16);
            player.getInventory().addItem(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("end_portal"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = new ItemStack(Material.END_PORTAL_FRAME, 3);
            player.getInventory().addItem(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("explosive_c4"))
        {
            if (args.length > 1)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            ItemStack item = CustomItemGenerator.explosive_c4(16);
            player.getInventory().addItem(item);
            return true;
        }
        else if (args[0].equalsIgnoreCase("player_head"))
        {
            if (args.length != 2)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
                return false;
            }
            String player_name = args[1];
            OfflinePlayer head_player = ErikComPlugin.getPlayer(player_name);
            if (head_player == null)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
                return false;
            }
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            skullMeta.setOwningPlayer(head_player);
            skullMeta.displayName(Component.text(player_name).color(TextColor.color(0x22DDFF)));
            item.setItemMeta(skullMeta);
            player.getInventory().addItem(item);
            return true;
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid subperk");
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.SUBPERK)) return suggestions;
        if (args.length <= 1)
        {
            suggestions.add("unbreaking_4");
            suggestions.add("fortune_4");
            suggestions.add("looting_4");
            suggestions.add("efficiency_6");
            suggestions.add("punch_3");
            suggestions.add("respiration_4");
            suggestions.add("soul_speed_5");

            suggestions.add("bow");
            suggestions.add("legs");
            suggestions.add("sword");
            suggestions.add("crossbow");
            suggestions.add("loyalty_5");
            suggestions.add("riptide_5");
            suggestions.add("leather");
            suggestions.add("turtle");
            suggestions.add("stick");
            suggestions.add("deepslate");
            suggestions.add("end_portal");
            suggestions.add("explosive_c4");
            suggestions.add("player_head");
        }
        return suggestions;
    }
}
