package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class EventXmas22Calender
{
    public static ItemStack get_reward(int collectedCount)
    {
        ItemStack box_item = new ItemStack(Material.MAGENTA_SHULKER_BOX, 1);

        // Event läuft zur Zeit der Veröffentlichung dieses Codes noch, daher ist der Inhalt noch nicht bekannt.

        return box_item;
    }
}
