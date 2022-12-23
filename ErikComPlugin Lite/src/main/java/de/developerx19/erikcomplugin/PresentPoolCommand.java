package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PresentPoolCommand implements CommandExecutor, TabCompleter
{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.OPERATOR))
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
        PlayerData data = PlayerData.get(player);
        if (args.length > 2)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        if (args.length == 0)
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Present Pool Mode is currently " + ChatColor.DARK_AQUA + (data.renderAllPresents ? "ON" : "OFF"));
        else
        {
            var key = new NamespacedKey(ErikComPlugin.plugin, "present_pool_tool");
            if (args[0].equalsIgnoreCase("on"))
            {
                data.renderAllPresents = true;
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Present Pool Mode is now " + ChatColor.DARK_AQUA + "ON");
                var inventory = player.getInventory();

                var tool = new ItemStack(Material.EMERALD_BLOCK, 1);
                tool.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
                tool.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                var tool_meta = tool.getItemMeta();
                tool_meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
                tool_meta.displayName(Component.text("Einfach").color(TextColor.color(0x00AAFF)));
                tool.setItemMeta(tool_meta);
                inventory.addItem(tool);

                tool = new ItemStack(Material.GOLD_BLOCK, 1);
                tool.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
                tool.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                tool_meta = tool.getItemMeta();
                tool_meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
                tool_meta.displayName(Component.text("Normal").color(TextColor.color(0x00AAFF)));
                tool.setItemMeta(tool_meta);
                inventory.addItem(tool);

                tool = new ItemStack(Material.REDSTONE_BLOCK, 1);
                tool.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
                tool.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                tool_meta = tool.getItemMeta();
                tool_meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
                tool_meta.displayName(Component.text("Schwierig").color(TextColor.color(0x00AAFF)));
                tool.setItemMeta(tool_meta);
                inventory.addItem(tool);

                tool = new ItemStack(Material.COAL_BLOCK, 1);
                tool.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
                tool.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                tool_meta = tool.getItemMeta();
                tool_meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
                tool_meta.displayName(Component.text("Entfernen").color(TextColor.color(0x00AAFF)));
                tool.setItemMeta(tool_meta);
                inventory.addItem(tool);

            }
            else if (args[0].equalsIgnoreCase("off"))
            {
                data.renderAllPresents = false;
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.GREEN + "Present Pool Mode is now " + ChatColor.DARK_AQUA + "OFF");
                var inventory = player.getInventory();
                for (var item : inventory.getContents())
                {
                    if (item == null) continue;
                    var meta = item.getItemMeta();
                    if (meta.getPersistentDataContainer().has(key))
                        item.setAmount(0);
                }
            }
            EventXmas22Manager.updatePlayer(player);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.OPERATOR)) return suggestions;
        if (args.length == 1)
        {
            suggestions.add("on");
            suggestions.add("off");
        }
        return suggestions;
    }
}
